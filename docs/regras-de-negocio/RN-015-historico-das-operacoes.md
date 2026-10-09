# RN-015 - Histórico das operações

## Identificação

| Campo       | Valor              |
|-------------|--------------------|
| **ID**      | RN-015             |
| **Nome**    | Histórico das operações |
| **Versão**  | 1.1                |
| **Status**  | Ativa |

## Descrição

Cada operação do fluxo de doação deve ser rastreável, com identificação do ocorrido e de seu responsável.

## Contexto

Rastreabilidade de propostas e doações.

## Regra

- Criação de proposta, aceite, recusa, cancelamento, escolha da modalidade, informação de entrega, postagem e recebimento registram evento no histórico da proposta.
- O histórico registra estado anterior, novo estado, tipo de evento, título, observações e responsável.
- O responsável é identificado pelo nome do doador, nome da instituição ou Sistema nas operações automáticas.
- A rastreabilidade abrange também operações que não alteram o status da proposta.
- A escolha da modalidade Correios integra o histórico mesmo quando a proposta permanece aguardando envio.

## Exceções

O cadastro de itens registra o histórico da doação, pois ainda não existe proposta vinculada.

## Histórico de Alterações

| Versão | Data       | Autor  | Descrição          |
|--------|------------|--------|--------------------|
| 1.0    | 2026-10-09 | Hiel Saraiva | Criação do documento |
| 1.1    | 2026-10-09 | Hiel Saraiva | Separação entre política de negócio e comportamento funcional |
