# RN-004 - Compatibilidade de categoria

## Identificação

| Campo       | Valor              |
|-------------|--------------------|
| **ID**      | RN-004             |
| **Nome**    | Compatibilidade de categoria |
| **Versão**  | 1.1                |
| **Status**  | Ativa |

## Descrição

Os itens propostos devem pertencer à mesma categoria da necessidade.

## Contexto

Seleção de inventário e envio de propostas.

## Regra

- Uma proposta somente pode utilizar uma doação pertencente ao doador, com saldo disponível e categoria compatível com a necessidade.
- O envio compara a categoria do tipo do item da doação com a categoria do tipo do item da necessidade.
- Categorias diferentes impedem a proposta.

## Exceções

A compatibilidade é verificada por categoria; não é exigida igualdade entre os tipos de item.

## Histórico de Alterações

| Versão | Data       | Autor  | Descrição          |
|--------|------------|--------|--------------------|
| 1.0    | 2026-10-09 | Hiel Saraiva | Criação do documento |
| 1.1    | 2026-10-09 | Hiel Saraiva | Separação entre política de negócio e comportamento funcional |
