# RN-002 - Disponibilidade das necessidades

## Identificação

| Campo       | Valor              |
|-------------|--------------------|
| **ID**      | RN-002             |
| **Nome**    | Disponibilidade das necessidades |
| **Versão**  | 1.1                |
| **Status**  | Ativa |

## Descrição

Somente necessidades ativas de instituições aprovadas podem receber propostas.

## Contexto

Consulta de necessidades, cadastro de itens e envio de propostas.

## Regra

- A lista de necessidades disponíveis exige status ativo, instituição aprovada e saldo restante maior que zero.
- O saldo restante é a quantidade solicitada menos a soma das quantidades alocadas em propostas que não estejam recusadas ou canceladas.
- A abertura da página de proposta e o cadastro de itens exigem necessidade ativa e instituição aprovada.
- O envio de proposta revalida a disponibilidade e os saldos.

## Exceções

O endpoint de cadastro de itens e a abertura da página de proposta não exigem, por si só, saldo restante positivo. Na interface, os botões de cadastro e de envio da proposta são desabilitados quando o saldo da necessidade é zero. O saldo é obrigatório para efetivar a proposta.

## Histórico de Alterações

| Versão | Data       | Autor  | Descrição          |
|--------|------------|--------|--------------------|
| 1.0    | 2026-10-09 | Hiel Saraiva | Criação do documento |
| 1.1    | 2026-10-09 | Hiel Saraiva | Distinção entre validação do endpoint e disponibilidade na interface |
