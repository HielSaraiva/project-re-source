# RF-013 - Informar entrega presencial

## Identificação

| Campo          | Valor                                   |
|----------------|-----------------------------------------|
| **ID**         | RF-013                                  |
| **Nome**       | Informar entrega presencial |
| **Prioridade** | Alta |
| **Versão**     | 1.0                                     |
| **Status**     | Implementado |

## Descrição

O sistema deve permitir ao doador informar uma entrega presencial pendente dentro do prazo, registrando a data, o recebedor e a declaração de entrega realizada.

## Atores

- Doador

## Entradas e Saídas

### Entradas (Dados necessários)
- Protocolo da proposta (texto, obrigatório).
- Data da entrega (data entre o dia da escolha da modalidade e o dia atual em America/Fortaleza, obrigatória).
- Nome de quem recebeu (texto não vazio de até 120 caracteres, obrigatório).
- Observações (texto de até 1.000 caracteres, opcional).
- Confirmação de entrega realizada (booleano verdadeiro, obrigatório).

### Saídas (Resultados esperados)
- Data da entrega, instante do registro, nome do recebedor e observações armazenados.
- Entrega aguardando confirmação e proposta aguardando confirmação da ONG.
- Evento de entrega informada e atualização do status consolidado da doação.
- Rejeição para dados, confirmação, modalidade, estado ou prazo incompatíveis.

## Histórico de Alterações

| Versão | Data       | Autor  | Descrição            |
|--------|------------|--------|----------------------|
| 1.0    | 2026-10-09 | Hiel Saraiva | Criação do documento |
