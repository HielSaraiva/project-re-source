# UC-007 - Consultar propostas da ONG

## Identificação

| Campo          | Valor                        |
|----------------|------------------------------|
| **ID**         | UC-007                       |
| **Nome**       | Consultar propostas da ONG |
| **Ator(es)**   | ONG |
| **Versão**     | 1.1                          |
| **Status**     | Ativo |

## Descrição

Pesquisar as propostas destinadas às necessidades da instituição.

## Pré-condições

- A ONG está autenticada e aprovada.

## Fluxo Principal

1. A ONG acessa a lista de doações.
2. O sistema apresenta somente propostas destinadas à instituição autenticada.
3. A ONG informa busca, status, ordenação e página.
4. O sistema apresenta os resultados paginados e a quantidade de propostas aguardando aceite.

## Fluxos Alternativos

### FA-01: Ordenação por prazo

1. A ONG seleciona a ordenação por prazo em lugar da ordem de criação mais recente.
2. O sistema apresenta as propostas por prazo crescente, com as que não possuem prazo ao final e desempate por criação e identificador decrescentes.

## Fluxos de Exceção

### FE-01: Filtros inválidos

1. A busca excede 200 caracteres, a página é menor que 1 ou o tamanho está fora de 1 a 100.
2. O sistema rejeita a consulta inválida.
3. A consulta não é concluída.

## Pós-condições

- A ONG consultou suas propostas sem alterar seus estados.

## Histórico de Alterações

| Versão | Data       | Autor  | Descrição            |
|--------|------------|--------|----------------------|
| 1.0    | 2026-10-09 | Hiel Saraiva | Criação do documento |
| 1.1    | 2026-10-09 | Hiel Saraiva | Precisão da ordenação por prazo |
