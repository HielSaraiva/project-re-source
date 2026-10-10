# RN-002 - Disponibilidade das necessidades

## Identificação

| Campo       | Valor              |
|-------------|--------------------|
| **ID**      | RN-002             |
| **Nome**    | Disponibilidade das necessidades |
| **Versão**  | 1.2                |
| **Status**  | Ativa |

## Descrição

Somente necessidades ativas de instituições aprovadas podem receber propostas.

## Contexto

Consulta de necessidades, cadastro de itens e envio de propostas.

## Regra

- A lista de necessidades disponíveis exige status ativo, instituição aprovada e saldo restante maior que zero.
- O saldo restante é calculado conforme [RN-005](RN-005-reserva-e-limites-de-quantidade.md).
- A abertura da página de proposta e o cadastro de itens exigem necessidade ativa e instituição aprovada.
- O envio de proposta revalida a disponibilidade e os saldos.

## Exceções

O cadastro de itens e a abertura da página de proposta não exigem, por si só, saldo restante positivo. O saldo é obrigatório para efetivar a proposta. O tratamento dessa situação na interface está em RF-004 e RF-005.

## Histórico de Alterações

| Versão | Data       | Autor  | Descrição          |
|--------|------------|--------|--------------------|
| 1.0    | 2026-10-09 | Hiel Saraiva | Criação do documento |
| 1.1    | 2026-10-09 | Hiel Saraiva | Distinção entre validação do endpoint e disponibilidade na interface |
| 1.2 | 2026-10-10 | Hiel Saraiva | Referência única para saldos e separação do comportamento de interface |
