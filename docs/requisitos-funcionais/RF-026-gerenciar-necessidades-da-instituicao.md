# RF-026 - Gerenciar necessidades da instituição

## Identificação

| Campo | Valor |
|---|---|
| **ID** | RF-026 |
| **Nome** | Gerenciar necessidades da instituição |
| **Prioridade** | Alta |
| **Versão** | 1.0 |
| **Status** | Implementado |

## Descrição

O sistema deve permitir que uma ONG aprovada consulte suas necessidades e prioridades, cadastre novas necessidades e edite as necessidades ativas de sua própria instituição.

## Atores

- ONG autenticada e aprovada

## Entradas e Saídas

### Entradas

- Categoria do item.
- Nome/descrição curta do item, com até 150 caracteres.
- Quantidade solicitada, número inteiro positivo.
- Prioridade: baixa, média ou alta.

### Saídas

- Lista das necessidades pertencentes à instituição autenticada, com item, quantidade, prioridade e status.
- Confirmação de cadastro ou atualização.
- Mensagem de validação quando os dados forem inválidos ou a quantidade solicitada ficar abaixo do total já reservado.

## Regras

- A instituição é identificada pela sessão autenticada; não se aceita um identificador de instituição fornecido pelo cliente.
- Uma instituição não pode consultar ou editar necessidades de outra instituição.
- Somente necessidades ativas podem ser editadas.
- A quantidade solicitada não pode ser reduzida abaixo das quantidades já alocadas em propostas existentes, incluindo doações concluídas.
- A categoria não pode ser alterada enquanto houver propostas associadas à necessidade.
- A edição da categoria ou do nome associa a necessidade a um tipo de item correspondente, sem alterar os dados de outras necessidades ou doações existentes.
- A exclusão de necessidades não faz parte deste requisito.

## Histórico de Alterações

| Versão | Data | Autor | Descrição |
|---|---|---|---|
| 1.0 | 2026-10-18 | João Marcelo Pimenta | Criação do requisito para consulta, cadastro e edição das necessidades da ONG |
