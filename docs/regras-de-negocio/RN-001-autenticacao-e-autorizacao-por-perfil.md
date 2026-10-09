# RN-001 - Autenticação e autorização por perfil

## Identificação

| Campo       | Valor              |
|-------------|--------------------|
| **ID**      | RN-001             |
| **Nome**    | Autenticação e autorização por perfil |
| **Versão**  | 1.1                |
| **Status**  | Ativa |

## Descrição

O acesso às funcionalidades depende das credenciais, do perfil e da situação da conta.

## Contexto

Autenticação e acesso às áreas de doador e ONG.

## Regra

- O e-mail é consultado sem distinção entre letras maiúsculas e minúsculas.
- O doador deve possuir perfil de doador, conta ativa e autoridade de doador na autenticação.
- A ONG deve estar aprovada e possuir autoridade de ONG na autenticação.
- Um e-mail encontrado simultaneamente como usuário e instituição impede a autenticação local.
- As operações e consultas por protocolo são restritas ao proprietário da doação ou à instituição destinatária.

## Exceções

Recursos estáticos e a página de erro possuem acesso público. O sistema pode processar expiração sem uma conta autenticada.

## Histórico de Alterações

| Versão | Data       | Autor  | Descrição          |
|--------|------------|--------|--------------------|
| 1.0    | 2026-10-09 | Hiel Saraiva | Criação do documento |
| 1.1    | 2026-10-09 | Hiel Saraiva | Separação entre política de negócio e comportamento funcional |
