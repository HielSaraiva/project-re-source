# RF-015 - Confirmar recebimento da doação

## Identificação

| Campo          | Valor                                   |
|----------------|-----------------------------------------|
| **ID**         | RF-015                                  |
| **Nome**       | Confirmar recebimento da doação |
| **Prioridade** | Alta |
| **Versão**     | 1.0                                     |
| **Status**     | Implementado |

## Descrição

O sistema deve permitir à ONG destinatária confirmar o recebimento de todos os itens e da quantidade de uma proposta cuja entrega presencial foi informada ou cujo envio está em trânsito.

## Atores

- ONG

## Entradas e Saídas

### Entradas (Dados necessários)
- Protocolo da proposta (texto, obrigatório).
- Confirmação de recebimento (booleano verdadeiro, obrigatório).

### Saídas (Resultados esperados)
- Entrega com status entregue e proposta com status concluído.
- Instante de confirmação e instituição responsável registrados; data de entrega preenchida se ainda não existir.
- Necessidade marcada como atendida quando a soma das quantidades concluídas alcançar a quantidade solicitada.
- Evento de recebimento e atualização do status consolidado da doação.
- Rejeição quando faltar confirmação ou os estados da proposta e da entrega não permitirem o recebimento.

## Histórico de Alterações

| Versão | Data       | Autor  | Descrição            |
|--------|------------|--------|----------------------|
| 1.0    | 2026-10-09 | Hiel Saraiva | Criação do documento |
