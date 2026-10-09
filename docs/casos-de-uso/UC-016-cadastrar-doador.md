# UC-016 - Cadastrar doador

## Identificação

| Campo | Valor |
|-------|-------|
| **ID** | UC-016 |
| **Nome** | Cadastrar doador |
| **Ator(es)** | Visitante |
| **Versão** | 1.0 |
| **Status** | Ativo |

## Descrição

Permitir que um visitante crie uma conta de doador para utilizar a plataforma.

## Pré-condições

- O visitante não está autenticado.
- O e-mail não está utilizado por usuário ou instituição.

## Fluxo Principal

1. O visitante abre “Pessoa Física” na tela de login ou acessa `/register/donor`.
2. O sistema apresenta o formulário de nome completo, e-mail, senha e confirmação.
3. O visitante preenche os campos e seleciona “Cadastrar”.
4. O sistema valida os dados e cria uma conta de doador ativo com senha protegida por hash.
5. O sistema redireciona ao login e informa que a conta foi criada.
6. O visitante entra pela autenticação descrita em UC-001.

## Fluxos Alternativos

### FA-01: Cadastro com Google

1. Com a integração configurada, o visitante seleciona “Cadastrar com Google”.
2. O sistema utiliza o fluxo Google de UC-001 para criar uma conta nova ou autenticar a identidade já vinculada.

### FA-02: Conta já autenticada

1. O usuário acessa o cadastro com sessão autenticada.
2. O sistema redireciona ao painel correspondente ao seu perfil.

## Fluxos de Exceção

### FE-01: Dados inválidos ou e-mail utilizado

1. O sistema rejeita campos inválidos, confirmação diferente da senha ou e-mail utilizado.
2. O formulário apresenta mensagens de erro, preserva nome e e-mail e mantém as senhas vazias.
3. O visitante corrige os dados e tenta novamente.

### FE-02: Submissão sem proteção CSRF

1. O sistema rejeita a requisição sem token CSRF válido.
2. Nenhuma conta é criada.

## Pós-condições

- Conta local de doador ativa e apta ao login; o cadastro local não estabelece sessão autenticada.

## Histórico de Alterações

| Versão | Data | Autor | Descrição |
|--------|------|-------|-----------|
| 1.0 | 2026-10-09 | Codex | Cadastro de doador |
