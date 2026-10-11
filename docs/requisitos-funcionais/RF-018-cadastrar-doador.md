# RF-018 - Cadastrar doador

## Identificação

| Campo | Valor |
|-------|-------|
| **ID** | RF-018 |
| **Nome** | Cadastrar doador |
| **Prioridade** | Alta |
| **Versão** | 1.2 |
| **Status** | Implementado |

## Descrição

O sistema deve oferecer a página pública `/register/donor`, com nome completo, e-mail, senha e confirmação de senha. Deve validar os dados, criar uma conta local de doador ativo com senha armazenada como hash e redirecionar à página de login com confirmação de sucesso.

O formulário deve permitir mostrar e ocultar cada senha separadamente, com os controles de visibilidade e troca de perfil descritos em RF-001. Em dispositivos móveis, deve priorizar o formulário e apresentar a marca em header compacto; em telas maiores, deve manter o painel de marca lateral. Em caso de erro, deve preservar nome e e-mail, indicar os campos inválidos e solicitar novamente as senhas, sem devolvê-las no HTML.

Quando configurada, a opção “Cadastrar com Google” deve reutilizar o fluxo OAuth 2.0 / OpenID Connect descrito em RF-001. O link de cadastro do perfil doador na tela de login deve abrir este formulário. Contas já autenticadas devem ser redirecionadas ao respectivo painel.

## Atores

- Visitante que deseja atuar como doador.

## Entradas e Saídas

### Entradas (Dados necessários)

- Nome completo, obrigatório, até 200 caracteres.
- E-mail válido, obrigatório, até 320 caracteres.
- Senha obrigatória conforme [RN-018](../regras-de-negocio/RN-018-politica-de-senhas.md).
- Confirmação de senha, obrigatória e igual à senha.
- Alternativamente, identidade Google validada pelo provedor.

### Saídas (Resultados esperados)

- Conta criada com perfil de doador e situação ativa.
- Confirmação de sucesso na página de login.
- E-mail de boas-vindas após a criação da conta local ou pelo Google, com acesso à consulta de necessidades. Acessos posteriores pelo Google não repetem o envio.
- Rejeição para campos inválidos ou e-mail já utilizado por usuário ou instituição.
- Rejeição de submissão sem token CSRF válido.

## Histórico de Alterações

| Versão | Data | Autor | Descrição |
|--------|------|-------|-----------|
| 1.0 | 2026-10-09 | Hiel Saraiva | Cadastro local do doador e integração com o login |
| 1.1 | 2026-10-10 | Hiel Saraiva | Política de senha centralizada e comportamento compartilhado das telas |
| 1.2 | 2026-10-10 | Hiel Saraiva | E-mail de boas-vindas no cadastro local e Google |
