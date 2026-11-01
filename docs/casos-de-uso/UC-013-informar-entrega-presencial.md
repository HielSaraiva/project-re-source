# UC-013 - Informar entrega presencial

## Identificação

| Campo          | Valor                        |
|----------------|------------------------------|
| **ID**         | UC-013                       |
| **Nome**       | Informar entrega presencial |
| **Ator(es)**   | Doador |
| **Versão**     | 1.1                          |
| **Status**     | Ativo |

## Descrição

Registrar que os itens foram entregues presencialmente à ONG.

## Pré-condições

- O doador está autenticado, ativo e é proprietário da proposta.
- A proposta aguarda entrega presencial, a entrega está pendente e o prazo está vigente.

## Fluxo Principal

1. O doador informa a data da entrega, o nome de quem recebeu e, opcionalmente, observações.
2. O doador confirma expressamente que realizou a entrega.
3. O sistema valida a data e os campos informados.
4. O sistema registra a entrega como aguardando confirmação e a proposta como aguardando confirmação da ONG.
5. O sistema registra o evento e atualiza o status consolidado da doação.

## Fluxos Alternativos

### FA-01: Sem observações

1. O doador não informa observações.
2. O sistema registra a entrega sem observações.

## Fluxos de Exceção

### FE-01: Dados ou etapa inválidos

1. A data está fora do intervalo permitido, o recebedor está vazio ou excede 120 caracteres, as observações excedem 1.000 caracteres, falta confirmação ou a entrega não está disponível.
2. O sistema rejeita o registro.
3. A entrega não é informada pela operação.

### FE-02: Prazo encerrado

1. Após validar os campos e as permissões, o sistema identifica que o prazo da entrega terminou.
2. O sistema cancela automaticamente a proposta e a entrega, libera a reserva e informa o encerramento do prazo.
3. A entrega não é informada.

## Pós-condições

- A entrega presencial foi informada com data e recebedor.
- A conclusão depende da confirmação de recebimento pela ONG.

## Histórico de Alterações

| Versão | Data       | Autor  | Descrição            |
|--------|------------|--------|----------------------|
| 1.0    | 2026-10-09 | Hiel Saraiva | Criação do documento |
| 1.1    | 2026-10-09 | Hiel Saraiva | Inclusão do cancelamento automático ao informar fora do prazo |
