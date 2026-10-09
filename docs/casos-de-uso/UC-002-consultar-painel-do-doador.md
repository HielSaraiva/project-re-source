# UC-002 - Consultar painel do doador

## Identificação

| Campo          | Valor                        |
|----------------|------------------------------|
| **ID**         | UC-002                       |
| **Nome**       | Consultar painel do doador |
| **Ator(es)**   | Doador |
| **Versão**     | 1.0                          |
| **Status**     | Ativo |

## Descrição

Apresentar necessidades urgentes e o acompanhamento das propostas do doador.

## Pré-condições

- O doador está autenticado e ativo.

## Fluxo Principal

1. O doador acessa seu painel.
2. O sistema apresenta até três necessidades de prioridade alta com saldo disponível, das mais recentes para as mais antigas.
3. O sistema apresenta até cinco propostas do doador, ordenadas pela atualização mais recente.
4. O doador seleciona uma necessidade ou proposta para consultar seus detalhes.

## Fluxos Alternativos

### FA-01: Ausência de resultados

1. Não existem necessidades urgentes ou propostas vinculadas ao doador.
2. O sistema apresenta as respectivas listas vazias.

## Fluxos de Exceção

### FE-01: Acesso sem permissão

1. A conta não atende às permissões de doador ativo.
2. O sistema impede o acesso ao painel.
3. O caso de uso é encerrado.

## Pós-condições

- O doador visualizou as informações disponíveis do painel.

## Histórico de Alterações

| Versão | Data       | Autor  | Descrição            |
|--------|------------|--------|----------------------|
| 1.0    | 2026-10-09 | Hiel Saraiva | Criação do documento |
