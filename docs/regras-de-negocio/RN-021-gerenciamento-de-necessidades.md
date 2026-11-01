# RN-021 — Gerenciamento de necessidades

| Campo | Valor |
|---|---|
| ID | RN-021 |
| Versão | 2.0 |
| Status | Ativa |

- Somente a ONG proprietária, autenticada e aprovada, pode administrar a necessidade.
- Categoria, nome de até 150 caracteres, quantidade inteira positiva e prioridade são obrigatórios.
- Uma necessidade ativa pode ser editada ou excluída quando não tem propostas, ou quando **todas** as propostas estão canceladas ou recusadas. Uma combinação desses dois estados também libera as operações.
- Qualquer proposta em outro estado, inclusive concluída, bloqueia **edição e exclusão**. O bloqueio é aplicado no backend sob o mesmo bloqueio transacional utilizado no matching, além de ser explicado na interface.
- Uma necessidade concluída ou cancelada não pode ser editada ou excluída pela gestão.
- Propostas ativas reservam quantidades; concluídas compõem a quantidade recebida. Canceladas e recusadas não consomem saldo. Somente a confirmação de recebimento conclui o atendimento, conforme RN-013.
- A página apresenta pedido, reserva, recebimento e saldo separadamente. A legenda de `fulfilled` é **Concluída**.
- Editar nome ou categoria associa o tipo de item correspondente sem modificar outras necessidades ou itens. A identidade do catálogo por categoria/nome ignora diferenças de maiúsculas/minúsculas e espaços nas extremidades, inclusive em cadastros simultâneos.
- A exclusão exige confirmação com identificador e nome. Sem propostas, remove-se a necessidade; com propostas exclusivamente canceladas/recusadas, faz-se exclusão lógica. Ela deixa de aparecer na gestão e na descoberta de necessidades, mas protocolos, histórico, entregas e eventos vinculados são preservados.
- Cada edição/exclusão deve apresentar a versão lida. Um formulário antigo não pode sobrescrever alterações de outra sessão. Quando ocorre conflito, os campos são preservados até a ONG decidir recarregar os dados.
- Se novas propostas bloquearem a operação enquanto o formulário estiver aberto, o sistema impede a gravação, explica o bloqueio e oferece acesso às propostas e à lista atualizada.
- A gestão é paginada e filtrada por texto, categoria, prioridade e status. O padrão mostra ativas; concluídas continuam acessíveis pelo filtro correspondente.
- Cadastro, edição e exclusão mostram feedback e preservam os filtros e a página atual, ajustando para a última página válida se necessário.
- Após login, retornar à rota originalmente solicitada.

## Histórico

| Versão | Data | Descrição |
|---|---|---|
| 1.0–1.2 | 2026-10 | Cadastro, consulta, quantidades e gestão inicial |
| 2.0 | 2026-10-31 | Edição/exclusão por estado das propostas, preservação do histórico, versão de edição e catálogo concorrente |
