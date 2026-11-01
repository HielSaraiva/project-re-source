# RF-022 - Notificar atualizações da doação

## Identificação

| Campo | Valor |
|-------|-------|
| **ID** | RF-022 |
| **Nome** | Notificar atualizações da doação |
| **Prioridade** | Alta |
| **Versão** | 1.0 |
| **Status** | Implementado |

## Descrição

O sistema deve registrar avisos das atualizações relevantes do fluxo de doação para os participantes definidos em RN-020, disponibilizando-os no site e enviando e-mails quando o canal estiver habilitado. Uma falha do canal de e-mail não deve desfazer a operação de doação confirmada.

## Atores

- Doador.
- ONG.
- Sistema, responsável pelo processamento dos eventos.

## Entradas e Saídas

### Entradas

- Evento confirmado da proposta, seus participantes, protocolo e data.
- Configuração do canal de e-mail.

### Saídas

- Avisos privados de cada destinatário com título, mensagem e acesso aos detalhes.
- E-mail com logo e identidade visual ReSource, identificação do destinatário, atualização, protocolo e link para a proposta.
- Avisos preservados no site mesmo quando o envio de e-mail falhar.
- Processamento de eventos repetidos sem duplicar registros por evento e destinatário.

A matriz de eventos e destinatários é mantida somente em [RN-020](../regras-de-negocio/RN-020-destinatarios-e-leitura-das-notificacoes.md). O mecanismo RabbitMQ e as tentativas de envio pertencem à [arquitetura](../arquitetura/arquitetura.md).

## Histórico de Alterações

| Versão | Data | Autor | Descrição |
|--------|------|-------|-----------|
| 1.0 | 2026-10-10 | Hiel Saraiva | Avisos por evento e canal de e-mail compartilhado |
