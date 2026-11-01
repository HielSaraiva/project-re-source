# RN-013 - Confirmação de recebimento e atendimento da necessidade

## Identificação

| Campo       | Valor              |
|-------------|--------------------|
| **ID**      | RN-013             |
| **Nome**    | Confirmação de recebimento e atendimento da necessidade |
| **Versão**  | 1.0                |
| **Status**  | Ativa |

## Descrição

Somente a ONG destinatária conclui a proposta por confirmação de recebimento.

## Contexto

Conferência e conclusão das doações.

## Regra

- O recebimento exige confirmação expressa verdadeira.
- Para Correios, proposta e entrega devem estar em trânsito e a modalidade deve ser transportadora.
- Para entrega presencial, a proposta deve aguardar confirmação da ONG e a entrega deve aguardar confirmação, com modalidade presencial.
- A confirmação altera a proposta para concluída e a entrega para entregue, registrando o instante e a instituição responsável.
- Quando ainda não existe data de entrega, ela é preenchida com o instante da confirmação.
- A necessidade passa a atendida quando a soma das quantidades de propostas concluídas atinge ou supera a quantidade solicitada.

## Exceções

Não há confirmação parcial implementada. A confirmação abrange todos os itens e a quantidade alocada na proposta.

## Histórico de Alterações

| Versão | Data       | Autor  | Descrição          |
|--------|------------|--------|--------------------|
| 1.0    | 2026-10-09 | Hiel Saraiva | Criação do documento |
