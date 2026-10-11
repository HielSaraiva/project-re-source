# RN-020 - Destinatários e leitura das notificações

## Identificação

| Campo | Valor |
|-------|-------|
| **ID** | RN-020 |
| **Nome** | Destinatários e leitura das notificações |
| **Versão** | 1.1 |
| **Status** | Ativa |

## Descrição

Notificar os participantes sobre eventos relevantes de suas propostas sem expor dados de outras contas.

## Contexto

Atualizações do fluxo de doação, consulta no header e envio de e-mail.

## Regra

- A notificação pertence ao doador da proposta ou à ONG destinatária, conforme o evento.
- A distribuição implementada segue esta tabela:

| Evento | Doador | ONG destinatária |
| --- | --- | --- |
| Proposta enviada | Comprovante por e-mail, sem aviso adicional no sino | Nova proposta para análise |
| Proposta aceita | Aceite e orientação para escolher a entrega | — |
| Proposta recusada | Recusa e acesso ao motivo nos detalhes | — |
| Modalidade escolhida | — | Modalidade e próximos passos |
| Entrega presencial informada | — | Orientação para conferir e confirmar |
| Postagem informada | — | Postagem e acesso ao rastreamento |
| Recebimento confirmado | Conclusão da doação | Aviso de necessidade atendida, quando a soma concluída atende a quantidade solicitada no momento do processamento |
| Cancelamento manual ou por prazo | Cancelamento e acesso ao motivo | Cancelamento e acesso ao motivo |
| Alteração genérica de status, se emitida | Atualização da proposta | Atualização da proposta |

- A verificação de necessidade atendida ocorre por evento de recebimento; não existe unicidade global de aviso por necessidade.
- Um mesmo evento processado novamente não cria outra notificação para o mesmo destinatário. Eventos distintos da mesma proposta continuam gerando avisos próprios.
- A notificação registra título, mensagem, protocolo, data do evento e destino para consultar a proposta.
- Somente seu destinatário pode consultar ou marcar a notificação como lida. Abrir a proposta continua sujeito às permissões de RN-001.
- Abrir o painel de notificações não marca todos os avisos como lidos. A leitura individual ocorre ao selecionar o aviso; a leitura coletiva exige a ação correspondente.
- O envio de e-mail e a leitura no site são independentes: receber o e-mail não marca o aviso como lido, e ler o aviso não cancela o envio pendente.
- O canal do site permanece disponível quando o envio de e-mail está desabilitado ou falha. O e-mail depende de SMTP e notificações por e-mail habilitados e da liberação do evento pelo processamento assíncrono.

## Exceções

Não há preferências individuais de canais, notificações administrativas de aprovação ou alertas preventivos antes do prazo implementados. Cancelamentos por prazo já ocorrido estão cobertos pela tabela.

A unicidade dos registros não garante entrega SMTP exatamente uma vez: uma interrupção entre envio e confirmação pode resultar em reenvio. O processamento e as tentativas estão descritos em [arquitetura](../arquitetura/arquitetura.md).

## Histórico de Alterações

| Versão | Data | Autor | Descrição |
|--------|------|-------|-----------|
| 1.0 | 2026-10-10 | Hiel Saraiva | Centralização dos eventos, destinatários e estados de leitura |
| 1.1 | 2026-10-10 | Hiel Saraiva | Comprovante exclusivo por e-mail ao doador que envia a proposta |
