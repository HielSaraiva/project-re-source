# Notificações do ReSource

As atualizações do fluxo de doação geram avisos persistidos para a pessoa que precisa acompanhar ou agir. O mesmo aviso pode ser enviado por e-mail com a identidade visual do site.

| Atualização | Quem recebe |
| --- | --- |
| Nova proposta | ONG |
| Proposta aceita ou recusada | Doador |
| Modalidade escolhida | ONG |
| Entrega presencial informada ou postagem pelos Correios | ONG |
| Recebimento confirmado | Doador |
| Quantidade da necessidade atendida pelos recebimentos | ONG |
| Cancelamento pelo doador ou encerramento do prazo | Doador e ONG |
| Evento genérico de atualização de status | Doador e ONG |

Os avisos começam a ser gerados com esta implementação. O histórico antigo já consumido não é convertido em uma campanha de e-mails. Eventos ainda pendentes na outbox são processados normalmente. Aprovação administrativa e publicação/edição de necessidades ainda não possuem fluxos próprios de notificação; o escopo cobre as operações de doação existentes.

## Persistência e RabbitMQ

`MatchEventRepository.append` cria a entrada de `match_event_outbox` e chama `NotificationService.record` dentro da mesma transação da operação. Portanto, rollback desfaz também o aviso, e indisponibilidade do RabbitMQ não impede a atualização nem a notificação no site.

O publicador existente usa mensagens persistentes, confirmações do broker e outbox com lease e novas tentativas. `MatchEventConsumer` mantém o registro de processamento existente, materializa eventuais avisos ausentes e libera o envio de e-mail (`email_ready`). As restrições únicas por evento e destinatário tornam a materialização idempotente, preservando leitura e estado de entrega nas reentregas.

`NotificationMailDispatcher` consulta a entrega liberada pelo RabbitMQ a cada cinco segundos. Reivindica até cinco registros atomicamente com `FOR UPDATE SKIP LOCKED`, lease de cinco minutos e token de posse. O envio SMTP acontece fora de uma transação longa. Falhas recebem intervalo exponencial, de 30 segundos até uma hora, por até oito tentativas. A última falha registra `notification_email_failed`; sucessos registram `notification_email_sent`. Os logs incluem apenas ID, tentativa e classe da falha, sem destinatários, corpo ou credenciais.

Depois de corrigir uma configuração SMTP, registros que esgotaram as oito tentativas podem ser retomados por uma operação administrativa no banco:

```sql
UPDATE user_notifications
SET email_attempts = 0, email_next_attempt_at = now(),
    email_claim = NULL, email_claim_until = NULL
WHERE email_ready AND email_sent_at IS NULL AND email_attempts >= 8
  AND (email_claim_until IS NULL OR email_claim_until <= now());
```

SMTP não participa da transação do banco: uma interrupção depois da aceitação pelo servidor e antes de gravar `email_sent_at` pode causar repetição na próxima tentativa. O processamento evita duplicações por reentrega normal de eventos, mas não promete envio SMTP exatamente uma vez. O aviso persistido continua disponível independentemente de falhas de e-mail.

## Header e autorização

`GET /api/notifications?page=0` retorna até 20 avisos, quantidade de não lidos e indicação de próxima página. O servidor identifica o dono usando `CurrentActor`, sem aceitar IDs de destinatários do cliente. Contas precisam ser doador ativo ou ONG aprovada. Avisos são sempre filtrados pelo dono; IDs de outros usuários recebem 404 nas operações de leitura.

`POST /api/notifications/{id}/read` marca um aviso como lido. `POST /api/notifications/read-all` marca os avisos da própria conta. Ambas as operações exigem CSRF. Marcar como lido não cancela o envio de e-mail. A resposta da consulta usa `Cache-Control: no-store`.

O sino possui contador real (sem indicador fixo), estado vazio, paginação, erro com nova tentativa e links para as páginas protegidas de cada doação. Ao abrir um aviso, a leitura é salva antes de navegar. O menu fecha ao clicar fora, mover o foco para fora ou pressionar Escape; Escape devolve o foco ao sino. A animação respeita redução de movimento. O layout se adapta a celular. A consulta ocorre ao carregar a página, abrir o menu, voltar à aba e a cada minuto enquanto a aba está visível e o menu fechado.

## E-mail e configuração

`BrandedMailService` compartilha remetente, renderização, MIME HTML/texto e logo PNG incorporado entre recuperação de senha e notificações. Os templates reutilizam `email/brand.html`. Textos dinâmicos são escapados pelo Thymeleaf; links são construídos com `APP_BASE_URL` e caminhos internos definidos pelo servidor.

Não há novas credenciais SMTP. `MAIL_ENABLED=true` habilita o envio pelo provedor já configurado. `NOTIFICATION_EMAIL_ENABLED=true` é o padrão; definir `false` suspende somente os e-mails de notificações, mantendo recuperação de senha e avisos do site. Os e-mails pendentes são retomados quando a opção é reativada. Em produção, `APP_BASE_URL` precisa ser a URL pública HTTPS, e RabbitMQ/PostgreSQL precisam de armazenamento persistente.

## Verificação

A validação usou banco e RabbitMQ isolados, capturador SMTP local e Chrome, sem adicionar testes automatizados ao repositório. Foram verificados: operação real de aceite/cancelamento/recebimento; necessidade atendida; isolamento por destinatário e CSRF; reentrega de eventos; falha SMTP e nova tentativa; indisponibilidade/retorno do RabbitMQ; paginação; HTML/texto/logo CID; regressão da recuperação de senha; menu de doador/ONG e estado vazio em 1366, 390 e 320 pixels.

Referências: [confiabilidade e reentregas do RabbitMQ](https://www.rabbitmq.com/docs/reliability) e [confirmações de publicação no Spring AMQP](https://docs.spring.io/spring-amqp/reference/amqp/connections.html).
