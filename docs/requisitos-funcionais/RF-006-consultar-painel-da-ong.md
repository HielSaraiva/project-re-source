# RF-006 - Consultar painel da ONG

## Identificação

| Campo          | Valor                                   |
|----------------|-----------------------------------------|
| **ID**         | RF-006                                  |
| **Nome**       | Consultar painel da ONG |
| **Prioridade** | Média |
| **Versão**     | 1.1                                     |
| **Status**     | Implementado |

## Descrição

O sistema deve apresentar à ONG indicadores de suas doações e necessidades, além das propostas que aguardam aceite e das necessidades ainda disponíveis.

## Atores

- ONG

## Entradas e Saídas

### Entradas (Dados necessários)
- Conta da ONG autenticada (identificação obtida da autenticação, por sessão ou HTTP Basic, obrigatória).

### Saídas (Resultados esperados)
- Nome da instituição e identificação de seu papel no painel.
- Quantidade de propostas concluídas, propostas aguardando aceite e necessidades ativas da instituição.
- Até três propostas aguardando aceite, ordenadas pelo menor prazo, com desempate por criação e identificador.
- Até seis necessidades da instituição com status ativo e saldo positivo.

## Histórico de Alterações

| Versão | Data       | Autor  | Descrição            |
|--------|------------|--------|----------------------|
| 1.0    | 2026-10-09 | Hiel Saraiva | Criação do documento |
| 1.1    | 2026-10-09 | Hiel Saraiva | Precisão sobre a origem da identificação do ator |
