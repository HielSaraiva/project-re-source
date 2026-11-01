# RF-026 — Gerenciar necessidades da instituição

| Campo | Valor |
|---|---|
| ID | RF-026 |
| Versão | 2.0 |
| Prioridade | Alta |
| Ator | ONG autenticada e aprovada |

A ONG consulta, filtra e pagina suas necessidades, acompanha quantidades, cadastra novos pedidos e administra necessidades ativas conforme RN-021.

## Entradas

- Categoria, nome do item (até 150 caracteres), quantidade inteira de 1 a 2.147.483.647 e prioridade baixa/média/alta.
- Versão atual do registro para editar ou excluir.

## Comportamento

- Mostrar ativas por padrão e permitir consultar concluídas, usando a legenda **Concluída**.
- Exibir quantidades solicitadas, reservadas, recebidas e disponíveis; concluídas destacam o recebido.
- Liberar edição e exclusão somente sem propostas ou com todas canceladas/recusadas.
- Desabilitar ambas as ações e explicar o motivo se houver qualquer proposta em andamento ou concluída. Oferecer acesso aos protocolos vinculados.
- Impedir a mesma operação no backend, inclusive quando uma proposta surgir enquanto o formulário estiver aberto.
- Exigir confirmação de exclusão com nome e identificador. Preservar histórico por exclusão lógica quando houver propostas canceladas/recusadas.
- Rejeitar edição com versão antiga; preservar os campos e oferecer recarga explícita dos dados.
- Não permitir que uma resposta atrasada de outro registro altere o formulário atual.
- Exibir erros com orientação de recuperação, carregamento e confirmação de sucesso; impedir envio repetido.
- Preservar filtros/página após operações e retornar à página solicitada depois do login.
- Buscar o nome apenas por envio explícito, sem interromper a digitação; seleções de filtros podem atualizar a lista imediatamente.

## Referências

RN-001, RN-002, RN-004, RN-005, RN-013, RN-021 e UC-023.
