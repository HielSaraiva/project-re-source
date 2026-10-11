# RN-018 - Política de senhas

## Identificação

| Campo | Valor |
|-------|-------|
| **ID** | RN-018 |
| **Nome** | Política de senhas |
| **Versão** | 1.0 |
| **Status** | Ativa |

## Descrição

Estabelecer uma política única para criação e redefinição de senhas locais.

## Contexto

Cadastro de doador, cadastro institucional e recuperação de senha.

## Regra

- A senha deve conter de 8 a 12 caracteres, pelo menos uma letra maiúscula, uma minúscula, um número e um caractere especial.
- Espaços não contam como caractere especial; não são removidos da senha.
- A confirmação deve ser idêntica à senha.
- Senhas não podem ser armazenadas nem reapresentadas ao usuário em texto claro.
- A redefinição altera somente a credencial: não aprova instituições, não reativa contas e não modifica o perfil.
- O login verifica a credencial existente; não representa uma nova operação de cadastro ou redefinição.

## Exceções

Contas criadas exclusivamente pelo Google não exigem senha local. A recuperação dessas contas segue as condições de [RN-019](RN-019-recuperacao-de-senha.md).

## Histórico de Alterações

| Versão | Data | Autor | Descrição |
|--------|------|-------|-----------|
| 1.0 | 2026-10-10 | Hiel Saraiva | Centralização da política compartilhada pelos cadastros e pela redefinição |
