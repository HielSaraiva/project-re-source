# RN-007 - Aceite e recusa pela ONG

## Identificação

| Campo       | Valor              |
|-------------|--------------------|
| **ID**      | RN-007             |
| **Nome**    | Aceite e recusa pela ONG |
| **Versão**  | 1.1                |
| **Status**  | Ativa |

## Descrição

A instituição destinatária pode aceitar ou recusar apenas propostas que aguardam aceite dentro do prazo.

## Contexto

Análise de propostas pela ONG.

## Regra

- O aceite exige confirmação expressa verdadeira.
- O aceite registra a data, altera a proposta para aguardando envio e inicia o prazo de escolha da modalidade.
- A recusa exige motivo não vazio de até 1.000 caracteres.
- A recusa registra data e motivo, altera a proposta para recusada e libera a reserva.
- O motivo da recusa é armazenado com espaços externos removidos.

## Exceções

Se o prazo já terminou e a solicitação passa pela validação dos campos e das permissões, o serviço cancela automaticamente a proposta e rejeita o aceite ou a recusa. Uma solicitação com confirmação ou motivo inválidos é rejeitada antes dessa verificação.

## Histórico de Alterações

| Versão | Data       | Autor  | Descrição          |
|--------|------------|--------|--------------------|
| 1.0    | 2026-10-09 | Hiel Saraiva | Criação do documento |
| 1.1    | 2026-10-09 | Hiel Saraiva | Precisão da ordem de validação e expiração |
