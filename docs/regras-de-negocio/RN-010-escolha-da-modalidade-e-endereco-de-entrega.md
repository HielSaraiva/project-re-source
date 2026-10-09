# RN-010 - Escolha da modalidade e endereço de entrega

## Identificação

| Campo       | Valor              |
|-------------|--------------------|
| **ID**      | RN-010             |
| **Nome**    | Escolha da modalidade e endereço de entrega |
| **Versão**  | 1.2                |
| **Status**  | Ativa |

## Descrição

A modalidade é confirmada uma única vez e exige endereço de recebimento da ONG.

## Contexto

Preparação da entrega de uma proposta aceita.

## Regra

- São permitidas entrega presencial e envio por transportadora, identificado como Correios.
- A escolha exige confirmação expressa verdadeira.
- A proposta deve estar aguardando envio ou aceita, dentro do prazo e sem entrega registrada.
- A ONG deve possuir endereço de recebimento.
- Cada entrega fica vinculada ao endereço de recebimento informado no momento da escolha da modalidade.
- Entrega presencial altera a proposta para aguardando entrega; Correios define a proposta como aguardando envio e registra a transportadora.
- Depois da confirmação, a modalidade não pode ser alterada.

## Exceções

Outras modalidades existentes no modelo não são aceitas por esta operação.

## Histórico de Alterações

| Versão | Data       | Autor  | Descrição          |
|--------|------------|--------|--------------------|
| 1.0    | 2026-10-09 | Hiel Saraiva | Criação do documento |
| 1.1    | 2026-10-09 | Hiel Saraiva | Precisão da transição de status para envio pelos Correios |
| 1.2    | 2026-10-09 | Hiel Saraiva | Separação entre política de negócio e comportamento funcional |
