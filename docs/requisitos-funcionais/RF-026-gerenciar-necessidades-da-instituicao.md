# RF-026 - Gerenciar necessidades da instituição

## Identificação

| Campo | Valor |
|---|---|
| **ID** | RF-026 |
| **Nome** | Gerenciar necessidades da instituição |
| **Prioridade** | Alta |
| **Versão** | 1.1 |
| **Status** | Implementado |

## Descrição

O sistema deve permitir que uma ONG aprovada consulte suas necessidades e prioridades, cadastre novas necessidades, edite as necessidades ativas e exclua permanentemente necessidades de sua própria instituição que não tenham propostas associadas.

## Atores

- ONG autenticada e aprovada

## Entradas e Saídas

### Entradas

- Categoria do item.
- Nome/descrição curta do item, com até 150 caracteres.
- Quantidade solicitada, número inteiro positivo; valores fracionários são rejeitados pela API.
- Prioridade: baixa, média ou alta.

### Saídas

- Lista das necessidades pertencentes à instituição autenticada, com item, quantidade, prioridade e status.
- Confirmação de cadastro ou atualização.
- Mensagem de validação quando os dados forem inválidos ou a quantidade solicitada ficar abaixo do total já reservado.
- Confirmação explícita antes da exclusão, identificando o número e a descrição da necessidade.

## Regras

- A instituição é identificada pela sessão autenticada; não se aceita um identificador de instituição fornecido pelo cliente.
- Uma instituição não pode consultar ou editar necessidades de outra instituição.
- Necessidades atendidas não aparecem na lista “Minhas necessidades”; permanecem acessíveis na gestão de doações.
- Somente necessidades ativas podem ser editadas.
- A quantidade solicitada não pode ser reduzida abaixo das quantidades já alocadas em propostas existentes, incluindo doações concluídas.
- Se a nova quantidade for igual ou inferior ao total já recebido em propostas concluídas, a necessidade passa para atendida. Quantidades apenas reservadas em propostas pendentes não contam como recebimento.
- A categoria não pode ser alterada enquanto houver qualquer proposta associada à necessidade, mesmo que recusada ou cancelada.
- A edição da categoria ou do nome associa a necessidade a um tipo de item correspondente, sem alterar os dados de outras necessidades ou doações existentes.
- A exclusão remove permanentemente o registro da necessidade do banco de dados.
- A necessidade não pode ser excluída se houver qualquer proposta associada, para preservar o histórico e os registros de doação.

## Histórico de Alterações

| Versão | Data | Autor | Descrição |
|---|---|---|---|
| 1.0 | 2026-10-18 | João Marcelo Pimenta | Criação do requisito para consulta, cadastro e edição das necessidades da ONG |
| 1.1 | 2026-10-25 | João Marcelo Pimenta | Regras de atendimento após edição, categoria com propostas históricas e rejeição de quantidades fracionárias |
