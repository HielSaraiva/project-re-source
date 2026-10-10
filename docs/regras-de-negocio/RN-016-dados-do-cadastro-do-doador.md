# RN-016 - Dados do cadastro do doador

## Identificação

| Campo | Valor |
|-------|-------|
| **ID** | RN-016 |
| **Nome** | Dados do cadastro do doador |
| **Versão** | 1.1 |
| **Status** | Ativa |

## Descrição

O cadastro local cria uma conta de doador ativo com credenciais próprias.

## Contexto

Cadastro público de doadores.

## Regra

- Nome e e-mail têm espaços externos removidos; o e-mail é persistido em letras minúsculas.
- O e-mail não pode estar utilizado por qualquer usuário, inclusive contas Google, nem por instituição.
- Nome, e-mail, senha e confirmação são obrigatórios. O nome tem até 200 caracteres e o e-mail válido tem até 320.
- A senha e sua confirmação seguem [RN-018](RN-018-politica-de-senhas.md).
- Perfil e situação são definidos pelo servidor como doador e ativo. Campos extras enviados pelo cliente não alteram esses valores.
- CPF e cidade não são exigidos neste cadastro.
- A criação local não concede sessão autenticada. A apresentação da confirmação e o retorno ao login estão em RF-018.
- A unicidade do e-mail deve ser preservada inclusive em cadastros simultâneos, entre contas locais, Google e instituições.

## Exceções

O cadastro Google utiliza a política de identidade verificada e de conflitos da RN-001, sem exigir senha local.

## Histórico de Alterações

| Versão | Data | Autor | Descrição |
|--------|------|-------|-----------|
| 1.0 | 2026-10-09 | Hiel Saraiva | Política de cadastro local do doador |
| 1.1 | 2026-10-10 | Hiel Saraiva | Centralização de senha e separação dos mecanismos de persistência |
