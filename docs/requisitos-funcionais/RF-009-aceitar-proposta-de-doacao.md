# RF-009 - Aceitar proposta de doação

## Identificação

| Campo          | Valor                                   |
|----------------|-----------------------------------------|
| **ID**         | RF-009                                  |
| **Nome**       | Aceitar proposta de doação |
| **Prioridade** | Alta |
| **Versão**     | 1.1                                     |
| **Status**     | Implementado |

## Descrição

O sistema deve permitir à ONG destinatária aceitar uma proposta que aguarda aceite dentro do prazo, mediante confirmação expressa.

## Atores

- ONG

## Entradas e Saídas

### Entradas (Dados necessários)
- Protocolo da proposta (texto, obrigatório).
- Confirmação do aceite (booleano verdadeiro, obrigatório).

### Saídas (Resultados esperados)
- Proposta com status aguardando envio e data do aceite.
- Prazo de sete dias para escolha da modalidade de entrega.
- Evento de aceite e atualização do status consolidado da doação.
- Rejeição para confirmação inválida, estado incompatível ou prazo encerrado; neste último caso, cancelamento automático e liberação da reserva se os campos e as permissões forem válidos.

## Histórico de Alterações

| Versão | Data       | Autor  | Descrição            |
|--------|------------|--------|----------------------|
| 1.0    | 2026-10-09 | Hiel Saraiva | Criação do documento |
| 1.1    | 2026-10-09 | Hiel Saraiva | Precisão das validações anteriores ao cancelamento por prazo |
