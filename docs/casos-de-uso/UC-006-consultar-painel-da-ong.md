# UC-006 - Consultar painel da ONG

## Identificação

| Campo          | Valor                        |
|----------------|------------------------------|
| **ID**         | UC-006                       |
| **Nome**       | Consultar painel da ONG |
| **Ator(es)**   | ONG |
| **Versão**     | 1.0                          |
| **Status**     | Ativo |

## Descrição

Apresentar os indicadores e as pendências da instituição.

## Pré-condições

- A ONG está autenticada e aprovada.

## Fluxo Principal

1. A ONG acessa seu painel.
2. O sistema apresenta a quantidade de propostas concluídas, propostas aguardando aceite e necessidades ativas da instituição.
3. O sistema apresenta até três propostas aguardando aceite, priorizando o menor prazo, e até seis necessidades disponíveis da instituição.
4. A ONG seleciona uma proposta para consultar os detalhes.

## Fluxos Alternativos

### FA-01: Ausência de pendências

1. Não existem propostas aguardando aceite ou necessidades com saldo.
2. O sistema apresenta os indicadores correspondentes e listas vazias.

## Fluxos de Exceção

### FE-01: Instituição sem permissão

1. A conta não representa uma ONG aprovada.
2. O sistema impede o acesso.
3. O caso de uso é encerrado.

## Pós-condições

- A ONG visualizou os indicadores e os registros de sua instituição.

## Histórico de Alterações

| Versão | Data       | Autor  | Descrição            |
|--------|------------|--------|----------------------|
| 1.0    | 2026-10-09 | Hiel Saraiva | Criação do documento |
