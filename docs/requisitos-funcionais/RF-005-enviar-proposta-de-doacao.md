# RF-005 - Enviar proposta de doação

## Identificação

| Campo          | Valor                                   |
|----------------|-----------------------------------------|
| **ID**         | RF-005                                  |
| **Nome**       | Enviar proposta de doação |
| **Prioridade** | Alta |
| **Versão**     | 1.3                                     |
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
- Comprovante por e-mail ao doador com protocolo, ONG, item, quantidade e acesso ao acompanhamento.
- Registro do evento e atualização do status consolidado da doação.
- Mensagem de conflito quando houver incompatibilidade, indisponibilidade, duplicidade ativa ou saldo insuficiente.

- Quando não houver doação própria compatível com saldo, orientação para cadastrar itens, em vez de apresentar um seletor vazio como se a proposta pudesse ser enviada.
- Quando o saldo da necessidade for zero, mensagem de indisponibilidade e acesso a outras necessidades, sem permitir envio pela interface.
- As atualizações confirmadas também alimentam [RF-022](RF-022-notificar-atualizacoes-da-doacao.md), com destinatários definidos em RN-020.

## Histórico de Alterações

| Versão | Data       | Autor  | Descrição            |
|--------|------------|--------|----------------------|
| 1.0    | 2026-10-09 | Hiel Saraiva | Criação do documento |
| 1.1    | 2026-10-09 | Hiel Saraiva | Inclusão da revisão da proposta existente na interface |
| 1.2 | 2026-10-10 | Hiel Saraiva | Inventário vazio, necessidade sem saldo e referência às notificações |
| 1.3 | 2026-10-10 | Hiel Saraiva | Comprovante de envio da proposta ao doador |
