# RN-005 - Reserva e limites de quantidade

## Identificação

| Campo       | Valor              |
|-------------|--------------------|
| **ID**      | RN-005             |
| **Nome**    | Reserva e limites de quantidade |
| **Versão**  | 1.1                |
| **Status**  | Ativa |

## Descrição

A quantidade proposta deve respeitar os saldos da doação e da necessidade.

## Contexto

Alocação de itens entre propostas.

## Regra

- A quantidade proposta deve ser inteira e positiva.
- O saldo da doação é a quantidade cadastrada menos as quantidades alocadas em propostas que não estejam recusadas ou canceladas.
- O saldo da necessidade é a quantidade solicitada menos as quantidades alocadas em propostas que não estejam recusadas ou canceladas.
- A quantidade proposta não pode exceder nenhum dos dois saldos.
- Propostas recusadas ou canceladas liberam a quantidade reservada.
- Propostas concluídas continuam consumindo os saldos.
- As quantidades reservadas ou concluídas não podem ser alocadas novamente em outra proposta, inclusive quando propostas são enviadas simultaneamente.

## Exceções

Não há exceções implementadas.

## Histórico de Alterações

| Versão | Data       | Autor  | Descrição          |
|--------|------------|--------|--------------------|
| 1.0    | 2026-10-09 | Hiel Saraiva | Criação do documento |
| 1.1    | 2026-10-09 | Hiel Saraiva | Separação entre política de negócio e comportamento funcional |
