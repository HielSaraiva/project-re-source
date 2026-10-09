# RF-001 - Autenticar conta

## Identificação

| Campo          | Valor                                   |
|----------------|-----------------------------------------|
| **ID**         | RF-001                                  |
| **Nome**       | Autenticar conta |
| **Prioridade** | Alta |
| **Versão**     | 1.2                                     |
| **Status**     | Implementado |

## Descrição

O sistema deve autenticar doadores por e-mail e senha e instituições por CNPJ e senha (mantendo também o acesso institucional por e-mail), verificar a situação e o perfil da conta e permitir o acesso às funcionalidades autorizadas. Após a autenticação por formulário, deve direcionar o doador ao seu painel e a ONG ao painel institucional.

A página pública `/login` deve oferecer seleção de perfil, exibição de senha e mensagens de falha ou saída. Quando configurado, deve permitir autenticação de doadores com Google via OAuth 2.0 / OpenID Connect, usando e-mail verificado. O primeiro acesso Google cria uma conta de doador ativo; acessos posteriores reutilizam a identidade vinculada. O menu do perfil deve permitir encerrar a sessão por POST protegido por CSRF.

## Atores

- Doador
- ONG

## Entradas e Saídas

### Entradas (Dados necessários)
- E-mail do doador ou CNPJ da instituição (texto, obrigatório). O CNPJ aceita 14 caracteres, numéricos ou alfanuméricos nas 12 primeiras posições, com dois dígitos verificadores, com ou sem máscara.
- Senha (texto, obrigatório).
- Alternativamente, identidade Google validada pelo provedor, quando a integração estiver habilitada.

### Saídas (Resultados esperados)
- Conta autenticada com as permissões de seu perfil.
- Redirecionamento ao painel correspondente após login por formulário.
- Rejeição da autenticação para credenciais inválidas, conta desabilitada ou e-mail ambíguo entre usuário e instituição no login por e-mail.

## Histórico de Alterações

| Versão | Data       | Autor  | Descrição            |
|--------|------------|--------|----------------------|
| 1.0    | 2026-10-09 | Hiel Saraiva | Criação do documento |
| 1.1    | 2026-10-09 | Codex | Tela de login, autenticação Google opcional e saída da sessão |
| 1.2    | 2026-10-09 | Codex | Login institucional por CNPJ e senha reutilizando o fluxo de autenticação |
