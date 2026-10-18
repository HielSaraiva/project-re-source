# RN-021 - Gerenciamento de necessidades

## Identificação

| Campo | Valor |
|---|---|
| **ID** | RN-021 |
| **Nome** | Gerenciamento de necessidades |
| **Versão** | 1.0 |
| **Status** | Ativa |

## Regra

- Uma necessidade pertence à instituição associada à conta autenticada.
- Somente a instituição proprietária pode alterar a necessidade.
- O cadastro exige categoria, nome do item, quantidade inteira positiva e prioridade válida.
- Somente necessidades ativas podem ser editadas.
- A quantidade solicitada deve ser maior ou igual ao total alocado em propostas existentes; propostas concluídas continuam contando para preservar o histórico de atendimento.
- A categoria de uma necessidade com propostas associadas não pode ser alterada, para manter a compatibilidade das propostas registradas.
- Alterar categoria ou nome em uma necessidade não pode modificar itens ou outras necessidades que compartilhem o tipo de item anterior.
- A prioridade deve ser uma das opções baixa, média ou alta.
- A exclusão exige confirmação explícita, exibindo o identificador e a descrição da necessidade.
- A necessidade é removida permanentemente do banco de dados somente quando não há propostas associadas, em qualquer status.
- A exclusão de necessidade com propostas associadas é bloqueada para preservar o histórico de doações.

## Histórico de Alterações

| Versão | Data | Autor | Descrição |
|---|---|---|---|
| 1.0 | 2026-10-18 | João Marcelo Pimenta | Criação da regra de gerenciamento de necessidades |
