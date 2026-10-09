# RF-003 - Consultar necessidades disponíveis

## Identificação

| Campo          | Valor                                   |
|----------------|-----------------------------------------|
| **ID**         | RF-003                                  |
| **Nome**       | Consultar necessidades disponíveis |
| **Prioridade** | Alta |
| **Versão**     | 1.1                                     |
| **Status**     | Implementado |

## Descrição

O sistema deve permitir ao doador pesquisar e filtrar necessidades ativas de ONGs aprovadas com quantidade restante positiva, apresentando resultados paginados.

## Atores

- Doador

## Entradas e Saídas

### Entradas (Dados necessários)
- Busca por tipo de item, nome da instituição ou descrição (texto de até 200 caracteres, opcional).
- Categoria (categoria de item, opcional).
- Prioridade (prioridade da necessidade, opcional).
- Estado (identificador inteiro positivo no parâmetro stateId da URL, opcional; sem seletor na tela).
- Página (inteiro a partir de 1, opcional; padrão 1).
- Tamanho da página (inteiro de 1 a 100 no parâmetro size da URL, opcional; padrão 6; sem seletor na tela).

### Saídas (Resultados esperados)
- Cartões com instituição, item, descrição, categoria, prioridade, quantidade restante disponível e localização, quando cadastrada. A quantidade originalmente solicitada consta nos dados internos da consulta, mas não é exibida separadamente nos cartões.
- Resultados ordenados por prioridade alta, média e baixa, com data de criação decrescente dentro de cada prioridade.
- Página atual, tamanho, total de registros e total de páginas.

## Histórico de Alterações

| Versão | Data       | Autor  | Descrição            |
|--------|------------|--------|----------------------|
| 1.0    | 2026-10-09 | Hiel Saraiva | Criação do documento |
| 1.1    | 2026-10-09 | Hiel Saraiva | Correção das entradas por URL e dos dados exibidos nos cartões |
