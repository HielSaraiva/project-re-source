# RF-004 - Cadastrar itens para doação

## Identificação

| Campo          | Valor                                   |
|----------------|-----------------------------------------|
| **ID**         | RF-004                                  |
| **Nome**       | Cadastrar itens para doação |
| **Prioridade** | Alta |
| **Versão**     | 1.1                                     |
| **Status**     | Implementado |

## Descrição

O sistema deve permitir ao doador cadastrar itens para doação a partir de uma necessidade ativa de uma ONG aprovada, criando o item, seu pacote e a doação no inventário.

## Atores

- Doador

## Entradas e Saídas

### Entradas (Dados necessários)
- Necessidade (identificador inteiro positivo, obrigatório).
- Título do item (texto não vazio de até 120 caracteres, obrigatório).
- Quantidade (inteiro positivo, obrigatório).
- Estado de conservação (estado do item, obrigatório).
- Descrição (texto de até 1.000 caracteres, opcional).

### Saídas (Resultados esperados)
- Doação cadastrada com identificador, título, categoria, estado de conservação, descrição, quantidade total, saldo disponível e status.
- Tipo de item obtido da necessidade selecionada.
- Nova doação adicionada ao seletor de inventário na tela de proposta após o cadastro. Na interface, a abertura do cadastro exige saldo positivo da necessidade; o endpoint valida apenas sua situação ativa e a aprovação da ONG.
- Registro de criação no histórico da doação.
- Mensagem de rejeição para dados inválidos ou necessidade indisponível.

## Histórico de Alterações

| Versão | Data       | Autor  | Descrição            |
|--------|------------|--------|----------------------|
| 1.0    | 2026-10-09 | Hiel Saraiva | Criação do documento |
| 1.1    | 2026-10-09 | Hiel Saraiva | Precisão do resultado e da disponibilidade do cadastro na interface |
