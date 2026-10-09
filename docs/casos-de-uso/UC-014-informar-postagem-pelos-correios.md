# UC-014 - Informar postagem pelos Correios

## Identificação

| Campo          | Valor                        |
|----------------|------------------------------|
| **ID**         | UC-014                       |
| **Nome**       | Informar postagem pelos Correios |
| **Ator(es)**   | Doador |
| **Versão**     | 1.1                          |
| **Status**     | Ativo |

## Descrição

Registrar a postagem dos itens e seu código de rastreamento.

## Pré-condições

- O doador está autenticado, ativo e é proprietário da proposta.
- A modalidade é envio pelos Correios, a proposta aguarda envio, a entrega está pendente e o prazo está vigente.

## Fluxo Principal

1. O doador informa a data de postagem, o código de rastreamento e, opcionalmente, observações.
2. O doador confirma expressamente que realizou a postagem.
3. O sistema valida a data e o formato do rastreamento.
4. O sistema registra o código em letras maiúsculas e altera a entrega e a proposta para em trânsito.
5. O sistema registra o evento e atualiza o status consolidado da doação.

## Fluxos Alternativos

### FA-01: Código com letras minúsculas

1. O doador informa letras minúsculas no código.
2. O sistema aceita o formato válido e armazena o código em maiúsculas.

## Fluxos de Exceção

### FE-01: Dados ou etapa inválidos

1. O código não contém duas letras, nove dígitos e duas letras, a data está fora do intervalo permitido, as observações excedem 1.000 caracteres, falta confirmação ou a entrega não está disponível.
2. O sistema rejeita o registro.
3. A postagem não é informada pela operação.

### FE-02: Prazo encerrado

1. Após validar os campos e as permissões, o sistema identifica que o prazo da postagem terminou.
2. O sistema cancela automaticamente a proposta e a entrega, libera a reserva e informa o encerramento do prazo.
3. A postagem não é informada.

## Pós-condições

- A proposta e a entrega estão em trânsito.
- O código informado está disponível para consulta e a conclusão depende da confirmação da ONG.

## Histórico de Alterações

| Versão | Data       | Autor  | Descrição            |
|--------|------------|--------|----------------------|
| 1.0    | 2026-10-09 | Hiel Saraiva | Criação do documento |
| 1.1    | 2026-10-09 | Hiel Saraiva | Inclusão do cancelamento automático ao informar fora do prazo |
