# RF-007 - Consultar propostas da ONG

## Identificação

| Campo          | Valor                                   |
|----------------|-----------------------------------------|
| **ID**         | RF-007                                  |
| **Nome**       | Consultar propostas da ONG |
| **Prioridade** | Alta |
| **Versão**     | 1.2                                     |
| **Status**     | Implementado |

## Descrição

O sistema deve permitir à ONG consultar somente propostas destinadas à sua instituição, com busca, filtro por status, ordenação e paginação.

## Atores

- ONG

## Entradas e Saídas

### Entradas (Dados necessários)

- Busca opcional; validação textual conforme [RF-017](RF-017-busca-e-paginacao-das-consultas.md).
- Status da proposta (status, opcional).
- Ordenação (mais recentes ou por prazo, opcional; padrão mais recentes).
- Página e tamanho conforme RF-017; a tela não apresenta seletor de tamanho.

### Saídas (Resultados esperados)

- Lista de propostas da instituição com protocolo, item, quantidade, doador, data de criação, status e prazo vigente, quando houver. A modalidade integra os dados da consulta, mas não é exibida como campo na lista.
- Ordenação por prazo crescente com propostas sem prazo ao final e desempate por criação e identificador decrescentes.
- Busca por protocolo, doador, título do item e tipo de item; na ordenação por criação, também por nome da instituição.
- Quantidade de propostas aguardando aceite e dados de paginação.

- Sem propostas, mensagem descritiva em lugar de uma tabela contendo apenas cabeçalhos. Com filtros aplicados, deve oferecer limpar filtros; sem filtros, deve orientar sobre futuras propostas e oferecer retorno ao painel.

## Histórico de Alterações

| Versão | Data       | Autor  | Descrição            |
|--------|------------|--------|----------------------|
| 1.0    | 2026-10-09 | Hiel Saraiva | Criação do documento |
| 1.1    | 2026-10-09 | Hiel Saraiva | Correção dos campos exibidos e dos parâmetros da lista |
| 1.2 | 2026-10-10 | Hiel Saraiva | Estado sem propostas e referência à política comum de consulta |
