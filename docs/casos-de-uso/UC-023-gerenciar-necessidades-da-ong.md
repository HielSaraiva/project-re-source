# UC-023 — Gerenciar necessidades da ONG

| Campo | Valor |
|---|---|
| ID | UC-023 |
| Versão | 2.0 |
| Ator | ONG autenticada e aprovada |

## Fluxo principal

1. A ONG abre Minhas necessidades e consulta suas necessidades ativas, quantidades e prioridades.
2. Pode filtrar, buscar por nome, paginar ou consultar as concluídas.
3. Escolhe Nova necessidade ou Editar em uma necessidade ativa liberada.
4. O formulário apresenta categoria, nome, quantidade e prioridade. Na edição, carrega dados atuais e a versão do registro.
5. A ONG confirma. O backend valida propriedade, aprovação, versão e estados das propostas sob bloqueio transacional.
6. Salva os dados, mantém os filtros/página e apresenta confirmação.

## Exclusão

1. A ONG escolhe Excluir em uma necessidade ativa liberada.
2. O modal identifica nome e número, informa a consequência e permite Manter necessidade ou Excluir necessidade.
3. O backend revalida os mesmos bloqueios e a versão.
4. Sem propostas, remove o registro. Com todas canceladas/recusadas, exclui logicamente, preservando protocolos e histórico.
5. A necessidade sai das listas e deixa de receber propostas; a interface confirma a operação.

## Alternativas e exceções

- **Sem registros:** orientar o primeiro cadastro; com filtros sem resultado, oferecer ajuste/limpeza.
- **Propostas em andamento ou concluídas:** edição e exclusão desabilitadas, justificativa visível e acesso às propostas. A API também bloqueia.
- **Somente canceladas/recusadas:** ambas as ações liberadas, inclusive com mistura desses estados.
- **Conflito durante edição:** preservar campos, explicar o estado atual e oferecer acesso às propostas ou recarga explícita dos dados. Não sugerir ajuste de quantidade se o registro já está bloqueado.
- **Outra sessão editou:** impedir sobrescrita da versão antiga e pedir revisão dos dados atuais.
- **Necessidade removida:** informar indisponibilidade e oferecer atualização da lista.
- **Dados inválidos:** rejeitar sem persistir e orientar os campos a conferir.
- **Outra ONG:** rejeitar sem expor os dados.
- **Login necessário:** voltar à rota e aos filtros originais após autenticar.
- **Troca rápida de registro:** cancelar/ignorar respostas da edição anterior; elas nunca atualizam o modal atual.

## Pós-condições

Necessidade cadastrada, atualizada ou removida da gestão conforme RN-021, com preservação de reservas, recebimentos, itens compartilhados e histórico.
