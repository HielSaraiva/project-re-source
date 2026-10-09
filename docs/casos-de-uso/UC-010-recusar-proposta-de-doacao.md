# UC-010 - Recusar proposta de doação

## Identificação

| Campo          | Valor                        |
|----------------|------------------------------|
| **ID**         | UC-010                       |
| **Nome**       | Recusar proposta de doação |
| **Ator(es)**   | ONG |
| **Versão**     | 1.1                          |
| **Status**     | Ativo |

## Descrição

Recusar uma proposta com indicação do motivo.

## Pré-condições

- A ONG está autenticada, aprovada e é destinatária da proposta.
- A proposta aguarda aceite e está dentro do prazo.

## Fluxo Principal

1. A ONG consulta a proposta.
2. A ONG informa um motivo não vazio de até 1.000 caracteres e solicita a recusa.
3. O sistema registra a proposta como recusada, com data e motivo.
4. O sistema registra o evento, libera a quantidade reservada e atualiza o status consolidado da doação.

## Fluxos Alternativos

### FA-01: Prazo encerrado

1. Após validar o motivo e as permissões da solicitação de recusa, o sistema identifica que o prazo de aceite terminou.
2. O sistema cancela automaticamente a proposta e libera a reserva.
3. O sistema informa o encerramento do prazo.

## Fluxos de Exceção

### FE-01: Motivo inválido ou estado incompatível

1. O motivo está vazio, excede 1.000 caracteres ou a proposta não aguarda aceite.
2. O sistema rejeita a recusa.
3. A recusa não é registrada.

## Pós-condições

- A proposta está recusada e mantém seu motivo no histórico.
- A quantidade da proposta deixa de consumir os saldos.

## Histórico de Alterações

| Versão | Data       | Autor  | Descrição            |
|--------|------------|--------|----------------------|
| 1.0    | 2026-10-09 | Hiel Saraiva | Criação do documento |
| 1.1    | 2026-10-09 | Hiel Saraiva | Precisão das validações anteriores ao cancelamento por prazo |
