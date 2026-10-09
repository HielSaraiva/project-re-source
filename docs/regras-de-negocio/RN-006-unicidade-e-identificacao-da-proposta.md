# RN-006 - Unicidade e identificação da proposta

## Identificação

| Campo       | Valor              |
|-------------|--------------------|
| **ID**      | RN-006             |
| **Nome**    | Unicidade e identificação da proposta |
| **Versão**  | 1.0                |
| **Status**  | Ativa |

## Descrição

Uma doação não pode manter mais de uma proposta ativa para a mesma necessidade.

## Contexto

Criação e identificação das propostas de doação.

## Regra

- Uma proposta é ativa quando não está recusada, cancelada nem concluída.
- Uma proposta ativa para o mesmo par de doação e necessidade impede nova proposta para esse par.
- Cada proposta recebe protocolo no formato INT-AAAA-N, com o ano em America/Fortaleza e o número obtido da sequência de protocolos.
- A proposta inicia aguardando aceite, com quantidade alocada e mensagem opcional de até 1.000 caracteres.

## Exceções

Uma proposta anterior recusada, cancelada ou concluída não impede outra para o mesmo par, desde que os demais critérios e saldos permitam.

## Histórico de Alterações

| Versão | Data       | Autor  | Descrição          |
|--------|------------|--------|--------------------|
| 1.0    | 2026-10-09 | Hiel Saraiva | Criação do documento |
