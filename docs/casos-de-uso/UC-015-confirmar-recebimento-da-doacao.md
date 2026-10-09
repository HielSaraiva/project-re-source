# UC-015 - Confirmar recebimento da doação

## Identificação

| Campo          | Valor                        |
|----------------|------------------------------|
| **ID**         | UC-015                       |
| **Nome**       | Confirmar recebimento da doação |
| **Ator(es)**   | ONG |
| **Versão**     | 1.0                          |
| **Status**     | Ativo |

## Descrição

Concluir uma proposta após a conferência de todos os itens e da quantidade recebida.

## Pré-condições

- A ONG está autenticada, aprovada e é destinatária da proposta.
- A postagem está em trânsito ou a entrega presencial aguarda confirmação, com dados de entrega compatíveis.

## Fluxo Principal

1. A ONG consulta os dados da proposta e confere os itens recebidos.
2. A ONG confirma expressamente o recebimento.
3. O sistema registra a entrega como entregue e a proposta como concluída, com data e instituição responsável.
4. O sistema verifica se a soma das quantidades concluídas atende à quantidade solicitada da necessidade.
5. O sistema registra o evento e atualiza o status consolidado da doação.

## Fluxos Alternativos

### FA-01: Necessidade integralmente atendida

1. No passo 4, as quantidades concluídas atingem ou superam a quantidade solicitada.
2. O sistema altera a necessidade para atendida.

## Fluxos de Exceção

### FE-01: Recebimento indisponível

1. Falta confirmação, o doador ainda não informou entrega ou postagem, o recebimento já foi confirmado ou os estados da entrega e da proposta são incompatíveis.
2. O sistema rejeita a confirmação.
3. O recebimento não é registrado pela operação.

## Pós-condições

- A proposta está concluída e a entrega está entregue.
- A quantidade concluída continua consumindo os saldos.

## Histórico de Alterações

| Versão | Data       | Autor  | Descrição            |
|--------|------------|--------|----------------------|
| 1.0    | 2026-10-09 | Hiel Saraiva | Criação do documento |
