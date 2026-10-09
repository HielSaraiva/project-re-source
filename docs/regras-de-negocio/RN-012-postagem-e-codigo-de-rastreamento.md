# RN-012 - Postagem e código de rastreamento

## Identificação

| Campo       | Valor              |
|-------------|--------------------|
| **ID**      | RN-012             |
| **Nome**    | Postagem e código de rastreamento |
| **Versão**  | 1.0                |
| **Status**  | Ativa |

## Descrição

A postagem pelos Correios exige data válida e código no formato implementado.

## Contexto

Registro do envio pelo doador.

## Regra

- A modalidade deve ser Correios, a proposta deve aguardar envio, a entrega deve estar pendente e não informada e o prazo deve estar vigente.
- A data é obrigatória, não pode ser futura e deve estar entre o dia da escolha da modalidade e o dia atual em America/Fortaleza, inclusive.
- O rastreamento é obrigatório e contém duas letras, nove dígitos e duas letras.
- O formato aceita letras maiúsculas ou minúsculas e o código é armazenado em maiúsculas.
- As observações são opcionais e possuem até 1.000 caracteres.
- A confirmação de postagem deve ser expressamente verdadeira.
- Após o registro, a proposta e a entrega ficam em trânsito.

## Exceções

A operação valida o formato do código informado; não consulta os Correios para comprovar postagem ou movimentação.

## Histórico de Alterações

| Versão | Data       | Autor  | Descrição          |
|--------|------------|--------|--------------------|
| 1.0    | 2026-10-09 | Hiel Saraiva | Criação do documento |
