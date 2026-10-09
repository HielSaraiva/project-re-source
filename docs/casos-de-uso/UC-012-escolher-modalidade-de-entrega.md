# UC-012 - Escolher modalidade de entrega

## Identificação

| Campo          | Valor                        |
|----------------|------------------------------|
| **ID**         | UC-012                       |
| **Nome**       | Escolher modalidade de entrega |
| **Ator(es)**   | Doador |
| **Versão**     | 1.1                          |
| **Status**     | Ativo |

## Descrição

Definir como os itens de uma proposta aceita serão entregues à ONG.

## Pré-condições

- O doador está autenticado, ativo e é proprietário da proposta.
- A proposta está aguardando envio ou aceita, dentro do prazo e sem modalidade registrada.
- A ONG possui endereço de recebimento cadastrado.

## Fluxo Principal

1. O doador consulta os dados de recebimento da ONG.
2. O doador escolhe entrega presencial ou envio pelos Correios e confirma expressamente a escolha.
3. O sistema registra a modalidade e uma cópia do endereço de entrega.
4. O sistema inicia prazo de sete dias para informar entrega ou postagem e registra o evento.

## Fluxos Alternativos

### FA-01: Entrega presencial

1. O doador seleciona entrega presencial no passo 2.
2. O sistema altera a proposta para aguardando entrega e define o prazo presencial.

## Fluxos de Exceção

### FE-01: Modalidade indisponível ou já confirmada

1. A modalidade não é suportada, já existe entrega registrada, falta endereço, a confirmação é inválida ou o estado não permite a escolha.
2. O sistema rejeita a escolha.
3. Nenhuma nova modalidade é registrada.

### FE-02: Prazo encerrado

1. Após validar os campos e as permissões, o sistema identifica que o prazo de escolha terminou.
2. O sistema cancela automaticamente a proposta, libera a reserva e informa o encerramento do prazo.
3. A modalidade não é registrada.

## Pós-condições

- A modalidade foi registrada e não pode ser alterada.
- No envio pelos Correios, a proposta fica aguardando envio e a transportadora é registrada como Correios.

## Histórico de Alterações

| Versão | Data       | Autor  | Descrição            |
|--------|------------|--------|----------------------|
| 1.0    | 2026-10-09 | Hiel Saraiva | Criação do documento |
| 1.1    | 2026-10-09 | Hiel Saraiva | Precisão do status de envio e da exceção de prazo encerrado |
