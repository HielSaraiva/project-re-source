# RN-021 - Gerenciamento de necessidades

## Identificação

| Campo | Valor |
|---|---|
| **ID** | RN-021 |
| **Nome** | Gerenciamento de necessidades |
| **Versão** | 1.2 |
| **Status** | Ativa |

## Regra

- Uma necessidade pertence à instituição associada à conta autenticada.
- Somente a instituição proprietária pode alterar a necessidade.
- O cadastro exige categoria, nome do item, quantidade inteira positiva e prioridade válida.
- Somente necessidades ativas podem ser editadas.
- A quantidade solicitada deve ser maior ou igual ao total alocado em propostas existentes; propostas concluídas continuam contando para preservar o histórico de atendimento.
- A categoria de uma necessidade com qualquer proposta associada não pode ser alterada, mesmo que todas as propostas estejam canceladas ou recusadas, para manter a compatibilidade do histórico.
- Alterar categoria ou nome em uma necessidade não pode modificar itens ou outras necessidades que compartilhem o tipo de item anterior.
- Ao editar a quantidade solicitada, a necessidade passa para atendida se o total de propostas concluídas (recebidas pela ONG) for igual ou superior à nova quantidade. Unidades apenas reservadas em propostas ainda não concluídas não contam como recebidas.
- A prioridade deve ser uma das opções baixa, média ou alta.
- A consulta de necessidades apresenta separadamente o total solicitado, o total reservado em propostas ativas, o total recebido em propostas concluídas e o saldo disponível para novas propostas.
- A quantidade mínima para edição corresponde ao total recebido mais o total reservado em propostas ainda não concluídas. A interface deve explicar os componentes desse limite e a API deve recalculá-lo sob bloqueio transacional antes de salvar.
- Se a redução da quantidade deixar o total recebido igual ou superior ao novo pedido, a ONG deve confirmar explicitamente a mudança antes de a necessidade ser marcada como atendida.
- Se uma proposta alterar o limite enquanto a ONG edita a necessidade, a operação é rejeitada com o limite atualizado; os demais dados preenchidos permanecem disponíveis para nova tentativa.
- A exclusão exige confirmação explícita, exibindo o identificador e a descrição da necessidade.
- A necessidade é removida permanentemente do banco de dados somente quando não há propostas associadas, em qualquer status.
- A exclusão de necessidade com propostas associadas é bloqueada para preservar o histórico de doações.
- Necessidades podem ser filtradas por texto, categoria, prioridade e status, e a paginação deve refletir os filtros ativos.
- Uma solicitação não autenticada para “Minhas necessidades” deve retornar à rota originalmente solicitada após o login da ONG.

## Histórico de Alterações

| Versão | Data | Autor | Descrição |
|---|---|---|---|
| 1.0 | 2026-10-18 | João Marcelo Pimenta | Criação da regra de gerenciamento de necessidades |
| 1.1 | 2026-10-25 | João Marcelo Pimenta | Precisão do atendimento, categoria com propostas históricas e quantidade inteira |
| 1.2 | 2026-10-25 | João Marcelo Pimenta | Acompanhamento de quantidades, confirmação de atendimento, conflitos concorrentes e consulta paginada |
