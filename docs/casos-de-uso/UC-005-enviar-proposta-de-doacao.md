# UC-005 - Enviar proposta de doação

## Identificação

| Campo          | Valor                        |
|----------------|------------------------------|
| **ID**         | UC-005                       |
| **Nome**       | Enviar proposta de doação |
| **Ator(es)**   | Doador |
| **Versão**     | 1.1                          |
| **Status**     | Ativo |

## Descrição

Oferecer uma quantidade de itens de uma doação para atender uma necessidade.

## Pré-condições

- O doador está autenticado e ativo e é proprietário da doação.
- A necessidade está ativa e a ONG está aprovada.
- A doação tem categoria compatível e saldo suficiente.

## Fluxo Principal

1. O doador acessa uma necessidade.
2. O sistema apresenta suas doações da mesma categoria com saldo disponível.
3. O doador seleciona a doação, informa quantidade positiva e, opcionalmente, mensagem de até 1.000 caracteres.
4. A tela apresenta um resumo dos itens, da quantidade, do saldo e da mensagem, e o doador confirma o envio.
5. O sistema valida saldos e ausência de proposta ativa para o mesmo par de doação e necessidade.
6. O sistema cria um protocolo, reserva a quantidade e registra a proposta como aguardando aceite, com prazo de sete dias.

## Fluxos Alternativos

### FA-01: Cadastro de novos itens

1. O doador precisa cadastrar itens antes de enviar a proposta.
2. O doador executa o cadastro de itens e seleciona a doação criada.
3. O fluxo retorna ao passo 3.

## Fluxos de Exceção

### FE-01: Proposta incompatível ou saldo insuficiente

1. A categoria difere, a quantidade excede um dos saldos, já existe proposta ativa ou a necessidade ficou indisponível.
2. O sistema rejeita a proposta e informa o conflito.
3. A proposta não é criada.

## Pós-condições

- A proposta aguarda aceite da ONG.
- A quantidade passa a consumir o saldo da doação e da necessidade.
- O evento e o status consolidado da doação são registrados.

## Histórico de Alterações

| Versão | Data       | Autor  | Descrição            |
|--------|------------|--------|----------------------|
| 1.0    | 2026-10-09 | Hiel Saraiva | Criação do documento |
| 1.1    | 2026-10-09 | Hiel Saraiva | Inclusão da revisão e confirmação existentes na interface |
