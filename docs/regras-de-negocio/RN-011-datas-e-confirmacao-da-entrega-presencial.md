# RN-011 - Datas e confirmação da entrega presencial

## Identificação

| Campo       | Valor              |
|-------------|--------------------|
| **ID**      | RN-011             |
| **Nome**    | Datas e confirmação da entrega presencial |
| **Versão**  | 1.0                |
| **Status**  | Ativa |

## Descrição

A informação de entrega presencial deve identificar a data e quem recebeu os itens.

## Contexto

Registro da entrega pelo doador.

## Regra

- A modalidade deve ser presencial, a proposta deve aguardar entrega e o prazo deve estar vigente.
- A entrega deve estar pendente e ainda não ter sido informada.
- A data é obrigatória, não pode ser futura e deve estar entre o dia da escolha da modalidade e o dia atual em America/Fortaleza, inclusive.
- O nome do recebedor é obrigatório e possui até 120 caracteres.
- As observações são opcionais e possuem até 1.000 caracteres.
- A confirmação de entrega deve ser expressamente verdadeira.
- O sistema registra a data e o instante da informação; a entrega passa a aguardar confirmação e a proposta passa a aguardar confirmação da ONG.

## Exceções

Informar entrega não conclui a proposta; a ONG deve confirmar o recebimento.

## Histórico de Alterações

| Versão | Data       | Autor  | Descrição          |
|--------|------------|--------|--------------------|
| 1.0    | 2026-10-09 | Hiel Saraiva | Criação do documento |
