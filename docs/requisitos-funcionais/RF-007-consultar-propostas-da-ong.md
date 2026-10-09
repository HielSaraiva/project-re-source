# RF-007 - Consultar propostas da ONG

## Identificação

| Campo          | Valor                                   |
|----------------|-----------------------------------------|
| **ID**         | RF-007                                  |
| **Nome**       | Consultar propostas da ONG |
| **Prioridade** | Alta |
| **Versão**     | 1.1                                     |
| **Status**     | Implementado |

## Descrição

O sistema deve permitir à ONG consultar somente propostas destinadas à sua instituição, com busca, filtro por status, ordenação e paginação.

## Atores

- ONG

## Entradas e Saídas

### Entradas (Dados necessários)
- Busca (texto de até 200 caracteres, opcional).
- Status da proposta (status, opcional).
- Ordenação (mais recentes ou por prazo, opcional; padrão mais recentes).
- Página (inteiro a partir de 1, opcional; padrão 1).
- Tamanho da página (inteiro de 1 a 100 no parâmetro size da URL, opcional; padrão 6; sem seletor na tela).

### Saídas (Resultados esperados)
- Lista de propostas da instituição com protocolo, item, quantidade, doador, data de criação, status e prazo vigente, quando houver. A modalidade integra os dados da consulta, mas não é exibida como campo na lista.
- Ordenação por prazo crescente com propostas sem prazo ao final e desempate por criação e identificador decrescentes.
- Busca por protocolo, doador, título do item e tipo de item; na ordenação por criação, também por nome da instituição.
- Quantidade de propostas aguardando aceite e dados de paginação.

## Histórico de Alterações

| Versão | Data       | Autor  | Descrição            |
|--------|------------|--------|----------------------|
| 1.0    | 2026-10-09 | Hiel Saraiva | Criação do documento |
| 1.1    | 2026-10-09 | Hiel Saraiva | Correção dos campos exibidos e dos parâmetros da lista |
