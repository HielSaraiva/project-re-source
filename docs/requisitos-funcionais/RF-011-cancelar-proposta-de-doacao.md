# RF-011 - Cancelar proposta de doação

## Identificação

| Campo          | Valor                                   |
|----------------|-----------------------------------------|
| **ID**         | RF-011                                  |
| **Nome**       | Cancelar proposta de doação |
| **Prioridade** | Alta |
| **Versão**     | 1.1                                     |
| **Status**     | Implementado |

## Descrição

O sistema deve permitir ao doador cancelar uma proposta própria ainda ativa, dentro do prazo e antes de informar entrega ou postagem, mediante confirmação expressa.

## Atores

- Doador

## Entradas e Saídas

### Entradas (Dados necessários)
- Protocolo da proposta (texto, obrigatório).
- Confirmação do cancelamento (booleano verdadeiro, obrigatório).

### Saídas (Resultados esperados)
- Proposta cancelada com data e motivo.
- Entrega vinculada cancelada, quando existente, e reserva liberada.
- Evento de cancelamento e atualização do status consolidado da doação.
- Rejeição quando a confirmação for inválida ou a proposta estiver encerrada, em trânsito ou aguardando confirmação da ONG.
- Cancelamento automático por prazo quando a etapa vigente já tiver expirado e a solicitação tiver confirmação e permissões válidas.

## Histórico de Alterações

| Versão | Data       | Autor  | Descrição            |
|--------|------------|--------|----------------------|
| 1.0    | 2026-10-09 | Hiel Saraiva | Criação do documento |
| 1.1    | 2026-10-09 | Hiel Saraiva | Precisão das validações anteriores ao cancelamento por prazo |
