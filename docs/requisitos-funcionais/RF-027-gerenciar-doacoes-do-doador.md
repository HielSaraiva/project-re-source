# RF-027 — Gerenciar doações do doador (RF-PoC01)

| Campo | Valor |
|---|---|
| Ator | Doador autenticado e ativo |
| Prioridade | Alta |
| Versão | 1.0 |

O doador visualiza, cadastra, edita e exclui seu inventário conforme RN-022, acompanha saldos e consulta todas as propostas vinculadas.

## Comportamento

- Cards com identificador, categoria, data, status consolidado, quantidades cadastrada/reservada/recebida/disponível e ações identificadas por texto.
- Busca por nome/descrição, número, ONG ou protocolo; filtros por categoria, status da doação e inventário/concluídas/excluídas; paginação estável que preserva os filtros.
- Modais seguem os componentes compartilhados do MVP: cabeçalho contextual, ícones semânticos, identificador, feedback de carregamento, erros anunciados, foco e ações consistentes.
- Cadastro seguro contra reenvios, com descrição opcional e validação inteira sem truncamento de quantidades fracionárias.
- Edição com categoria somente para leitura, validação da quantidade recebida e controle da versão de todo o agregado.
- Exclusão confirmada com nome, número e quantidade; preservação do histórico quando houver apenas propostas canceladas/recusadas.
- Consulta paginada e filtrável de todas as propostas, incluindo encerradas, com ONG, quantidade, data, estado e acesso ao protocolo correto.
- Excluídas com histórico continuam consultáveis em visão separada e não representam saldo disponível.
- Operações pendentes não podem ser reenviadas pelo botão nem encerradas silenciosamente; falhas preservam campos e oferecem recuperação.
- Retorno após login, página e filtros mantidos após operações; página fora do intervalo ajustada à última página válida.

## Interfaces

`GET /donor/donations?query=&category=&status=&view=inventory&page=1` retorna a página. Os estados do filtro principal não incluem Aceita, Recusada ou Cancelada; são estados de proposta ou legados. `view=completed` mostra somente concluídas; `view=archived` mostra exclusões lógicas. Essas duas visões ignoram o filtro de estado do inventário; Concluída também sai das opções desse filtro, pois tem visão própria. A listagem padrão exclui concluídas e excluídas logicamente.

Operações JSON, todas com autenticação do doador e CSRF nas gravações:

- `POST /api/donor/inventory`: `category`, `title`, `quantity`, `condition`, `description` opcional, `submissionKey` UUID; retorna 201 com `id`, reutilizado em retries idênticos.
- `GET /api/donor/inventory/{id}`: dados atuais, versão, saldos e permissões de ações. Registros excluídos não abrem edição.
- `PUT /api/donor/inventory/{id}`: `title`, `quantity`, `condition`, `description`, `version`; sucesso 204.
- `DELETE /api/donor/inventory/{id}?version=...`: sucesso 204; exclusão física/lógica conforme RN-022.
- `GET /api/donor/inventory/{id}/proposals?page=1&status=...`: lista paginada de protocolos, inclusive após exclusão lógica, restrita ao proprietário. Recusadas/Canceladas são opções deste filtro. Estados legados de proposta são agrupados com a etapa correspondente.

Erros usam ProblemDetail: 400 para validação, 404 para registro indisponível/alheio, 409 para versão/estado/chave de tentativa incompatíveis, além de 401/403 conforme autenticação/perfil. Erros de campo incluem `errors` quando disponíveis. Nenhum valor rejeitado é exposto nessa propriedade.

Os antigos POSTs de formulário em `/donor/donations`, `/{id}/edit` e `/{id}/delete` são substituídos pelos endpoints JSON; o cadastro usado no envio de proposta em `/api/donor/donations` permanece independente e inalterado.
