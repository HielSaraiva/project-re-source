# UC-011 - Cancelar proposta de doação

## Identificação

| Campo          | Valor                        |
|----------------|------------------------------|
| **ID**         | UC-011                       |
| **Nome**       | Cancelar proposta de doação |
| **Ator(es)**   | Doador |
| **Versão**     | 1.1                          |
| **Status**     | Ativo |

## Descrição

Encerrar uma proposta antes de informar entrega ou postagem.

## Pré-condições

- O doador está autenticado, ativo e é proprietário da proposta.
- A proposta está ativa, dentro do prazo e ainda não foi enviada nem está aguardando confirmação da ONG.

## Fluxo Principal

1. O doador acessa a proposta e solicita cancelamento.
2. O doador confirma expressamente o cancelamento.
3. O sistema registra a proposta como cancelada, com data e motivo.
4. O sistema cancela a entrega vinculada, quando existente, libera a reserva e registra o evento.

## Fluxos Alternativos

### FA-01: Prazo encerrado

1. Após validar a confirmação e as permissões da solicitação de cancelamento, o sistema identifica que o prazo ativo terminou.
2. O sistema registra o cancelamento automático por prazo, com a etapa expirada.
3. O sistema informa o encerramento do prazo.

## Fluxos de Exceção

### FE-01: Cancelamento indisponível

1. A confirmação não é verdadeira, a proposta está encerrada, foi enviada ou aguarda confirmação de recebimento.
2. O sistema rejeita a solicitação.
3. O cancelamento solicitado não é realizado.

## Pós-condições

- A proposta está cancelada e a entrega vinculada também, quando existente.
- Os saldos e o status consolidado da doação refletem a liberação da reserva.

## Histórico de Alterações

| Versão | Data       | Autor  | Descrição            |
|--------|------------|--------|----------------------|
| 1.0    | 2026-10-09 | Hiel Saraiva | Criação do documento |
| 1.1    | 2026-10-09 | Hiel Saraiva | Precisão das validações anteriores ao cancelamento por prazo |
