# RF-017 - Busca e paginação das consultas

## Identificação

| Campo          | Valor                                   |
|----------------|-----------------------------------------|
| **ID**         | RF-017                                  |
| **Nome**       | Busca e paginação das consultas |
| **Prioridade** | Média |
| **Versão**     | 1.3                                     |
| **Status**     | Implementado |

## Descrição

O sistema deve pesquisar, filtrar, ordenar e paginar as consultas de necessidades e propostas da ONG, aplicando os parâmetros recebidos e apresentando apenas os registros acessíveis ao ator, conforme RF-003 e RF-007.

## Atores

- Doador
- ONG

## Entradas e Saídas

### Entradas (Dados necessários)

- Busca (texto de até 200 caracteres, opcional).
- Filtros e ordenações específicos de [RF-003](RF-003-consultar-necessidades-disponiveis.md) e [RF-007](RF-007-consultar-propostas-da-ong.md).
- Página (inteiro a partir de 1, opcional; padrão 1).
- Tamanho da página (inteiro de 1 a 100 no parâmetro size da URL, opcional; padrão 6; sem seletor na tela).

### Saídas (Resultados esperados)

- Resultados filtrados com busca textual sem distinção entre maiúsculas e minúsculas, remoção de espaços externos e tratamento dos caracteres de curinga como texto literal.
- Página atual, tamanho, total de registros e total de páginas, com preservação dos filtros nos links de paginação.
- As mensagens de ausência de resultados seguem as consultas específicas. Os limites dos painéis pertencem a RF-002 e RF-006, sem paginação nessas seções.
- A consulta de notificações tem paginação própria, descrita em RF-023, e não utiliza estes filtros nem estes padrões de página.

## Histórico de Alterações

| Versão | Data       | Autor  | Descrição          |
|--------|------------|--------|--------------------|
| 1.0    | 2026-10-09 | Hiel Saraiva | Criação do documento |
| 1.1    | 2026-10-09 | Hiel Saraiva | Precisão dos filtros, da paginação e da ordenação implementados |
| 1.2    | 2026-10-09 | Hiel Saraiva | Reclassificação de RN-016 como RF-017 por descrever funcionalidades de consulta |
| 1.3 | 2026-10-10 | Hiel Saraiva | Remoção da repetição dos campos específicos e delimitação do escopo |
