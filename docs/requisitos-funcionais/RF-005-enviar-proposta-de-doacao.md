# RF-005 - Enviar proposta de doação

## Identificação

| Campo          | Valor                                   |
|----------------|-----------------------------------------|
| **ID**         | RF-005                                  |
| **Nome**       | Enviar proposta de doação |
| **Prioridade** | Alta |
| **Versão**     | 1.1                                     |
| **Status**     | Implementado |

## Descrição

O sistema deve permitir ao doador oferecer itens de uma doação própria a uma necessidade, validando categoria, saldos e ausência de proposta ativa para o mesmo par de doação e necessidade.

## Atores

- Doador

## Entradas e Saídas

### Entradas (Dados necessários)
- Necessidade (identificador inteiro positivo, obrigatório).
- Doação do próprio doador (identificador inteiro positivo, obrigatório).
- Quantidade oferecida (inteiro positivo, obrigatório).
- Mensagem da proposta (texto de até 1.000 caracteres, opcional).

### Saídas (Resultados esperados)
- Resumo da proposta apresentado na interface para confirmação antes do envio ao servidor.
- Proposta criada com protocolo único, status aguardando aceite e prazo de sete dias para aceite.
- Reserva da quantidade nos saldos da doação e da necessidade.
- Registro do evento e atualização do status consolidado da doação.
- Mensagem de conflito quando houver incompatibilidade, indisponibilidade, duplicidade ativa ou saldo insuficiente.

## Histórico de Alterações

| Versão | Data       | Autor  | Descrição            |
|--------|------------|--------|----------------------|
| 1.0    | 2026-10-09 | Hiel Saraiva | Criação do documento |
| 1.1    | 2026-10-09 | Hiel Saraiva | Inclusão da revisão da proposta existente na interface |
