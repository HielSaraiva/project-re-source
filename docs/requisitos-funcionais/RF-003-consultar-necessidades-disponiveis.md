# RF-003 - Consultar necessidades disponíveis

## Identificação

| Campo          | Valor                                   |
|----------------|-----------------------------------------|
| **ID**         | RF-003                                  |
| **Nome**       | Consultar necessidades disponíveis |
| **Prioridade** | Alta |
| **Versão**     | 1.2                                     |
| **Status**     | Implementado |

## Descrição

O sistema deve permitir ao doador pesquisar e filtrar necessidades ativas de ONGs aprovadas com quantidade restante positiva, apresentando resultados paginados.

## Atores

- Doador

## Entradas e Saídas

### Entradas (Dados necessários)

- Busca por tipo de item, nome da instituição ou descrição, opcional; validação textual conforme [RF-017](RF-017-busca-e-paginacao-das-consultas.md).
- Categoria (categoria de item, opcional).
- Prioridade (prioridade da necessidade, opcional).
- Estado (identificador inteiro positivo no parâmetro stateId da URL, opcional; sem seletor na tela).
- Página e tamanho conforme RF-017; a tela não apresenta seletor de tamanho.

### Saídas (Resultados esperados)

- Cartões com instituição, item, descrição, categoria, prioridade, quantidade restante disponível e localização, quando cadastrada. A quantidade originalmente solicitada consta nos dados internos da consulta, mas não é exibida separadamente nos cartões.
- Resultados ordenados por prioridade alta, média e baixa, com data de criação decrescente dentro de cada prioridade.
- Página atual, tamanho, total de registros e total de páginas.

- Sem resultados, mensagem que distingue ausência de necessidades de ausência de correspondência aos filtros. Deve oferecer limpar filtros quando aplicados ou voltar ao painel quando não houver filtros.

## Histórico de Alterações

| Versão | Data       | Autor  | Descrição            |
|--------|------------|--------|----------------------|
| 1.0    | 2026-10-09 | Hiel Saraiva | Criação do documento |
| 1.1    | 2026-10-09 | Hiel Saraiva | Correção das entradas por URL e dos dados exibidos nos cartões |
| 1.2 | 2026-10-10 | Hiel Saraiva | Estados sem resultados e referência à política comum de consulta |
