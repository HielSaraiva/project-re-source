# RN-022 — Gerenciamento do inventário do doador

| Campo | Valor |
|---|---|
| ID | RN-022 |
| Status | Ativa |
| Versão | 1.0 |

- Somente o doador proprietário, autenticado e ativo, pode consultar e administrar seu inventário.
- Cadastrar inventário não reserva quantidade: a reserva ocorre no envio de proposta, conforme RN-005.
- O cadastro independente exige categoria, nome com até 120 caracteres, quantidade inteira positiva e condição; descrição de até 1.000 caracteres é opcional.
- Cada tentativa de cadastro tem uma chave única por doador. Reenvios com a mesma chave e dados retornam a mesma doação; a mesma chave com dados diferentes é rejeitada. Excluir a doação não permite recriá-la repetindo a tentativa antiga.
- A edição exige ausência de propostas ativas e doação não concluída. A categoria é mantida. A quantidade não pode ficar abaixo do que já foi recebido.
- A versão enviada deve ser a versão atual do agregado. Toda edição de item, descrição, condição ou quantidade incrementa essa versão sob bloqueio da doação. Formulário antigo é rejeitado sem sobrescrever dados novos.
- Se a quantidade for reduzida ao total recebido, a consolidação pode concluir a doação. A interface deve explicar essa consequência antes de salvar.
- A exclusão é permitida sem propostas ou quando todas estiverem canceladas ou recusadas, inclusive uma combinação desses estados. Qualquer proposta em andamento ou concluída bloqueia a exclusão.
- Sem propostas, a doação pode ser removida fisicamente. Com propostas exclusivamente canceladas/recusadas, a exclusão é lógica: inventário, item, pacote, protocolos, entregas, notificações e histórico são preservados.
- Uma doação excluída logicamente não aparece no inventário disponível nem pode receber novas propostas. Em Minhas doações, Exibir → Excluídas (histórico) permite consultar seus protocolos, sem editar, excluir novamente ou oferecer saldo.
- O status principal é consolidado conforme RN-014. Cancelamento/recusa de propostas não significa que o inventário inteiro tenha sido cancelado: havendo saldo e nenhuma proposta ativa, ele volta a Registrada.
- O filtro de doação oferece os estados efetivos do fluxo: Registrada, Aguardando aceite/envio/entrega, Enviada, Aguardando confirmação da ONG. Concluídas ficam na visão Exibir → Concluídas, fora do inventário principal. Canceladas e Recusadas são filtros das propostas vinculadas.
- Toda proposta vinculada deve ser consultável por protocolo, sem substituir o status consolidado da doação por um estado individual.
- Campos preenchidos permanecem disponíveis em falhas, validações e conflitos. Recarregar dados de outra versão deve ser uma decisão explícita do usuário.
