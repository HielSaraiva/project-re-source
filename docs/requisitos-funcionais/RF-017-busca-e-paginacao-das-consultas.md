# RF-017 - Busca e paginação das consultas

## Identificação

| Campo          | Valor                                   |
|----------------|-----------------------------------------|
| **ID**         | RF-017                                  |
| **Nome**       | Busca e paginação das consultas |
| **Prioridade** | Média |
| **Versão**     | 1.2                                     |
| **Status**     | Implementado |

## Descrição

O sistema deve pesquisar, filtrar, ordenar e paginar as consultas de necessidades e propostas da ONG, aplicando os parâmetros recebidos e apresentando apenas os registros acessíveis ao ator, conforme RF-003 e RF-007.

## Atores

- Doador
- ONG

## Entradas e Saídas

### Entradas (Dados necessários)
- Busca (texto de até 200 caracteres, opcional).
- Categoria e prioridade da necessidade (valores enumerados, opcionais na consulta de necessidades).
- Estado (identificador inteiro positivo no parâmetro stateId da URL, opcional na consulta de necessidades; sem seletor na tela).
- Status e ordenação da proposta (valores enumerados, opcionais na consulta de propostas; ordem padrão de criação mais recente).
- Página (inteiro a partir de 1, opcional; padrão 1).
- Tamanho da página (inteiro de 1 a 100 no parâmetro size da URL, opcional; padrão 6; sem seletor na tela).

### Saídas (Resultados esperados)
- Resultados filtrados com busca textual sem distinção entre maiúsculas e minúsculas, remoção de espaços externos e tratamento dos caracteres de curinga como texto literal.
- Na consulta de necessidades, busca por nome do tipo de item, nome da instituição e descrição, com ordenação por prioridade alta, média e baixa e criação mais recente dentro de cada prioridade.
- Na consulta de propostas da ONG, busca por protocolo, nome do doador, título do item e tipo do item; na ordenação por criação, também por nome da instituição.
- Na ordenação por prazo, propostas por prazo crescente, com as sem prazo ao final e desempate por criação e identificador decrescentes.
- Página atual, tamanho, total de registros e total de páginas, com preservação dos filtros nos links de paginação.
- Nos painéis, limites próprios de três urgências e cinco propostas para o doador e três propostas pendentes e seis necessidades para a ONG, conforme RF-002 e RF-006.

## Histórico de Alterações

| Versão | Data       | Autor  | Descrição          |
|--------|------------|--------|--------------------|
| 1.0    | 2026-10-09 | Hiel Saraiva | Criação do documento |
| 1.1    | 2026-10-09 | Hiel Saraiva | Precisão dos filtros, da paginação e da ordenação implementados |
| 1.2    | 2026-10-09 | Hiel Saraiva | Reclassificação de RN-016 como RF-017 por descrever funcionalidades de consulta |
