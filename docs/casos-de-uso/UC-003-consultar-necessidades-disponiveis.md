# UC-003 - Consultar necessidades disponíveis

## Identificação

| Campo          | Valor                        |
|----------------|------------------------------|
| **ID**         | UC-003                       |
| **Nome**       | Consultar necessidades disponíveis |
| **Ator(es)**   | Doador |
| **Versão**     | 1.1                          |
| **Status**     | Ativo |

## Descrição

Localizar necessidades que ainda podem receber propostas de doação.

## Pré-condições

- O doador está autenticado e ativo.

## Fluxo Principal

1. O doador acessa a lista de necessidades.
2. O sistema lista necessidades ativas de ONGs aprovadas com quantidade restante positiva.
3. O doador informa busca, categoria ou prioridade e escolhe a página pelos controles de paginação.
4. O sistema apresenta os resultados filtrados, ordenados por prioridade alta, média e baixa, com data de criação decrescente dentro de cada prioridade.

## Fluxos Alternativos

### FA-01: Nenhuma necessidade encontrada

1. Os filtros não correspondem a necessidades disponíveis.
2. O sistema apresenta a lista vazia.
3. O doador pode alterar os filtros e repetir a consulta.

### FA-02: Filtro por estado via parâmetro

1. O doador acessa a lista informando o identificador do estado no parâmetro stateId da URL.
2. O sistema restringe os resultados a instituições com endereço nesse estado e preserva o parâmetro nos links de paginação.
3. A tela não apresenta um seletor de estado.

## Fluxos de Exceção

### FE-01: Filtros inválidos

1. A busca excede 200 caracteres, o estado não é positivo, a página é menor que 1 ou o tamanho está fora de 1 a 100.
2. O sistema rejeita a consulta inválida.
3. A consulta não é concluída.

## Pós-condições

- As necessidades disponíveis foram consultadas sem alteração dos registros.

## Histórico de Alterações

| Versão | Data       | Autor  | Descrição            |
|--------|------------|--------|----------------------|
| 1.0    | 2026-10-09 | Hiel Saraiva | Criação do documento |
| 1.1    | 2026-10-09 | Hiel Saraiva | Correção dos filtros disponíveis na tela e da ordenação |
