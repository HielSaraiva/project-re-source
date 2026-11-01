# UC-009 - Aceitar proposta de doação

## Identificação

| Campo          | Valor                        |
|----------------|------------------------------|
| **ID**         | UC-009                       |
| **Nome**       | Aceitar proposta de doação |
| **Ator(es)**   | ONG |
| **Versão**     | 1.1                          |
| **Status**     | Ativo |

## Descrição

Aceitar os itens e a quantidade oferecidos pelo doador.

## Pré-condições

- A ONG está autenticada, aprovada e é destinatária da proposta.
- A proposta aguarda aceite e seu prazo ainda não terminou.

## Fluxo Principal

1. A ONG consulta os detalhes da proposta.
2. A ONG confirma expressamente o aceite.
3. O sistema altera a proposta para aguardando envio e registra a data do aceite.
4. O sistema inicia o prazo de sete dias para o doador escolher a modalidade de entrega e registra o evento.

## Fluxos Alternativos

### FA-01: Prazo encerrado

1. Após validar a confirmação e as permissões da solicitação de aceite, o sistema identifica que o prazo terminou.
2. O sistema cancela automaticamente a proposta e libera a reserva.
3. O sistema informa o encerramento do prazo.

## Fluxos de Exceção

### FE-01: Confirmação ausente ou estado incompatível

1. A confirmação não é verdadeira ou a proposta não aguarda aceite.
2. O sistema rejeita a operação.
3. O aceite não é registrado.

## Pós-condições

- A proposta foi aceita e aguarda a escolha da modalidade.
- O histórico e o status consolidado da doação foram atualizados.

## Histórico de Alterações

| Versão | Data       | Autor  | Descrição            |
|--------|------------|--------|----------------------|
| 1.0    | 2026-10-09 | Hiel Saraiva | Criação do documento |
| 1.1    | 2026-10-09 | Hiel Saraiva | Precisão das validações anteriores ao cancelamento por prazo |
