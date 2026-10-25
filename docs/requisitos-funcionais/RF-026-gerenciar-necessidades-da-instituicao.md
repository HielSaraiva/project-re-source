# RF-026 - Gerenciar necessidades da instituição

## Identificação

| Campo | Valor |
|---|---|
| **ID** | RF-026 |
| **Nome** | Gerenciar necessidades da instituição |
| **Prioridade** | Alta |
| **Versão** | 1.2 |
| **Status** | Implementado |

## Descrição

O sistema deve permitir que uma ONG aprovada consulte, filtre e pagine as necessidades de sua instituição, acompanhe as quantidades solicitadas, reservadas, recebidas e disponíveis, cadastre e edite necessidades e exclua permanentemente aquelas que não tenham propostas associadas.

## Atores

- ONG autenticada e aprovada

## Entradas e Saídas

### Entradas

- Categoria do item.
- Nome/descrição curta do item, com até 150 caracteres.
- Quantidade solicitada, número inteiro positivo; valores fracionários são rejeitados pela API.
- Prioridade: baixa, média ou alta.

### Saídas

- Lista paginada das necessidades pertencentes à instituição autenticada, com filtros por texto, categoria, prioridade e status; por padrão, exibe necessidades ativas e permite consultar as concluídas.
- Para cada necessidade, quantidade solicitada, reservada, recebida e disponível para novas propostas.
- Confirmação de cadastro ou atualização.
- Mensagem de validação quando os dados forem inválidos ou a quantidade solicitada ficar abaixo da soma recebida e reservada.
- Confirmação explícita antes da exclusão, identificando a descrição da necessidade.

## Regras

- A instituição é identificada pela sessão autenticada; não se aceita um identificador de instituição fornecido pelo cliente.
- Uma instituição não pode consultar ou editar necessidades de outra instituição.
- Necessidades atendidas não aparecem na lista “Minhas necessidades”; permanecem acessíveis na gestão de doações.
- Somente necessidades ativas podem ser editadas.
- A quantidade solicitada não pode ser reduzida abaixo das quantidades já alocadas em propostas existentes, incluindo doações concluídas.
- O sistema deve informar a quantidade mínima permitida e detalhar quanto já foi recebido e quanto permanece reservado.
- Se novas propostas forem registradas durante a edição e elevarem a quantidade mínima, a atualização deve ser rejeitada com o limite atual, preservando os demais valores preenchidos no formulário.
- Se a nova quantidade for igual ou inferior ao total já recebido em propostas concluídas, a necessidade passa para atendida. Quantidades apenas reservadas em propostas pendentes não contam como recebimento.
- Antes dessa alteração de estado, o sistema deve solicitar confirmação e, após salvar, informar claramente que a necessidade foi atendida.
- A categoria não pode ser alterada enquanto houver qualquer proposta associada à necessidade, mesmo que recusada ou cancelada.
- A edição da categoria ou do nome associa a necessidade a um tipo de item correspondente, sem alterar os dados de outras necessidades ou doações existentes.
- A exclusão remove permanentemente o registro da necessidade do banco de dados.
- A necessidade não pode ser excluída se houver qualquer proposta associada, para preservar o histórico e os registros de doação.
- Após cadastro, edição ou exclusão concluídos, o sistema apresenta uma confirmação visual do resultado.
- Após autenticação, a ONG deve retornar à rota “Minhas necessidades” quando essa foi a página solicitada antes do login.

## Histórico de Alterações

| Versão | Data | Autor | Descrição |
|---|---|---|---|
| 1.0 | 2026-10-18 | João Marcelo Pimenta | Criação do requisito para consulta, cadastro e edição das necessidades da ONG |
| 1.1 | 2026-10-25 | João Marcelo Pimenta | Regras de atendimento após edição, categoria com propostas históricas e rejeição de quantidades fracionárias |
| 1.2 | 2026-10-25 | João Marcelo Pimenta | Quantidades detalhadas, limites dinâmicos, feedback, filtros, paginação e retorno após login |
