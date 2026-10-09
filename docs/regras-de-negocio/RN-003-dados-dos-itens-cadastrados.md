# RN-003 - Dados dos itens cadastrados

## Identificação

| Campo       | Valor              |
|-------------|--------------------|
| **ID**      | RN-003             |
| **Nome**    | Dados dos itens cadastrados |
| **Versão**  | 1.0                |
| **Status**  | Ativa |

## Descrição

O cadastro cria uma doação no inventário sem enviar automaticamente uma proposta.

## Contexto

Cadastro de itens a partir de uma necessidade.

## Regra

- A necessidade deve ser identificada por um número positivo.
- O título é obrigatório e possui até 120 caracteres; a quantidade é inteira e positiva; o estado de conservação é obrigatório.
- A descrição é opcional e possui até 1.000 caracteres.
- O tipo do item é obtido da necessidade selecionada.
- O item e seu pacote são associados ao doador autenticado e a doação inicia com status cadastrado.
- O título tem espaços externos removidos; descrições vazias são armazenadas sem valor.
- O cadastro registra um histórico de criação e não reserva quantidade para a necessidade.

## Exceções

Não há exceções implementadas.

## Histórico de Alterações

| Versão | Data       | Autor  | Descrição          |
|--------|------------|--------|--------------------|
| 1.0    | 2026-10-09 | Hiel Saraiva | Criação do documento |
