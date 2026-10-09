# RN-016 - Dados do cadastro do doador

## Identificação

| Campo | Valor |
|-------|-------|
| **ID** | RN-016 |
| **Nome** | Dados do cadastro do doador |
| **Versão** | 1.0 |
| **Status** | Ativa |

## Descrição

O cadastro local cria uma conta de doador ativo com credenciais próprias.

## Contexto

Cadastro público de doadores.

## Regra

- Nome e e-mail têm espaços externos removidos; o e-mail é persistido em letras minúsculas.
- O e-mail não pode estar utilizado por qualquer usuário, inclusive contas Google, nem por instituição.
- Nome, e-mail, senha e confirmação são obrigatórios. Nome e e-mail têm os limites do modelo persistido.
- A senha deve ter de 8 a 72 caracteres, conter pelo menos uma letra maiúscula, uma minúscula, um número e um caractere especial (espaço não conta como especial), ter no máximo 72 bytes UTF-8 e confirmação idêntica. A senha não sofre remoção de espaços e é armazenada com o PasswordEncoder compartilhado.
- Perfil e situação são definidos pelo servidor como doador e ativo. Campos extras enviados pelo cliente não alteram esses valores.
- CPF e cidade são opcionais no modelo atual e não são solicitados neste cadastro.
- O cadastro não autentica automaticamente: após o sucesso, o usuário entra pelo fluxo RF-001.
- O índice único de e-mail sem distinção de caixa e o bloqueio transacional compartilhado com o cadastro institucional e Google impedem cadastros simultâneos com o mesmo e-mail, inclusive entre os dois tipos de conta.

## Exceções

O cadastro Google utiliza a política de identidade verificada e de conflitos da RN-001, sem exigir senha local.

## Histórico de Alterações

| Versão | Data | Autor | Descrição |
|--------|------|-------|-----------|
| 1.0 | 2026-10-09 | Codex | Política de cadastro local do doador |
