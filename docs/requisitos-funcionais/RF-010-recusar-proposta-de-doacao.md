# RF-010 - Recusar proposta de doação

## Identificação

| Campo          | Valor                                   |
|----------------|-----------------------------------------|
| **ID**         | RF-010                                  |
| **Nome**       | Recusar proposta de doação |
| **Prioridade** | Alta |
| **Versão**     | 1.1                                     |
| **Status**     | Implementado |

## Descrição

O sistema deve permitir à ONG destinatária recusar uma proposta que aguarda aceite dentro do prazo, registrando o motivo e liberando a quantidade reservada.

## Atores

- ONG

## Entradas e Saídas

### Entradas (Dados necessários)
- Protocolo da proposta (texto, obrigatório).
- Motivo da recusa (texto não vazio de até 1.000 caracteres, obrigatório).

### Saídas (Resultados esperados)
- Proposta com status recusado, data e motivo da recusa.
- Liberação da quantidade reservada na doação e na necessidade.
- Evento de recusa e atualização do status consolidado da doação.
- Rejeição para motivo inválido, estado incompatível ou prazo encerrado; neste último caso, cancelamento automático se os campos e as permissões forem válidos.

## Histórico de Alterações

| Versão | Data       | Autor  | Descrição            |
|--------|------------|--------|----------------------|
| 1.0    | 2026-10-09 | Hiel Saraiva | Criação do documento |
| 1.1    | 2026-10-09 | Hiel Saraiva | Precisão das validações anteriores ao cancelamento por prazo |
