# RF-014 - Informar postagem pelos Correios

## Identificação

| Campo          | Valor                                   |
|----------------|-----------------------------------------|
| **ID**         | RF-014                                  |
| **Nome**       | Informar postagem pelos Correios |
| **Prioridade** | Alta |
| **Versão**     | 1.0                                     |
| **Status**     | Implementado |

## Descrição

O sistema deve permitir ao doador informar uma postagem pelos Correios pendente dentro do prazo, registrando a data, o rastreamento e a declaração de postagem realizada.

## Atores

- Doador

## Entradas e Saídas

### Entradas (Dados necessários)
- Protocolo da proposta (texto, obrigatório).
- Data da postagem (data entre o dia da escolha da modalidade e o dia atual em America/Fortaleza, obrigatória).
- Código de rastreamento (texto com duas letras, nove dígitos e duas letras, obrigatório).
- Observações (texto de até 1.000 caracteres, opcional).
- Confirmação de postagem realizada (booleano verdadeiro, obrigatório).

### Saídas (Resultados esperados)
- Data da postagem, instante do registro, observações e código de rastreamento em letras maiúsculas armazenados.
- Entrega e proposta em trânsito.
- Evento de postagem informada e atualização do status consolidado da doação.
- Rejeição para dados, confirmação, modalidade, estado ou prazo incompatíveis.

## Histórico de Alterações

| Versão | Data       | Autor  | Descrição            |
|--------|------------|--------|----------------------|
| 1.0    | 2026-10-09 | Hiel Saraiva | Criação do documento |
