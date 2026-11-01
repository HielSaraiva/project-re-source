# RN-009 - Cancelamento pelo doador

## Identificação

| Campo       | Valor              |
|-------------|--------------------|
| **ID**      | RN-009             |
| **Nome**    | Cancelamento pelo doador |
| **Versão**  | 1.1                |
| **Status**  | Ativa |

## Descrição

O doador pode cancelar a proposta antes de informar entrega ou postagem.

## Contexto

Cancelamento manual de propostas.

## Regra

- O cancelamento exige confirmação expressa verdadeira e deve ser solicitado pelo proprietário.
- A proposta deve estar ativa e não pode estar em trânsito nem aguardando confirmação da ONG.
- O sistema registra data e motivo do cancelamento.
- A entrega vinculada, quando existente, também recebe status cancelado.
- A reserva é liberada e o status consolidado da doação é recalculado.

## Exceções

Se o prazo já terminou e a confirmação e as permissões são válidas, o sistema registra cancelamento automático por expiração em lugar do cancelamento solicitado pelo doador. O motivo do cancelamento manual é definido pelo sistema; não existe campo para o doador informar um motivo.

## Histórico de Alterações

| Versão | Data       | Autor  | Descrição          |
|--------|------------|--------|--------------------|
| 1.0    | 2026-10-09 | Hiel Saraiva | Criação do documento |
| 1.1    | 2026-10-09 | Hiel Saraiva | Precisão das validações anteriores ao cancelamento por prazo |
