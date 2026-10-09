# UC-001 - Autenticar conta

## Identificação

| Campo          | Valor                        |
|----------------|------------------------------|
| **ID**         | UC-001                       |
| **Nome**       | Autenticar conta |
| **Ator(es)**   | Doador, ONG |
| **Versão**     | 1.0                          |
| **Status**     | Ativo |

## Descrição

Permitir o acesso às funcionalidades do perfil da conta.

## Pré-condições

- A conta já existe e possui credenciais locais.
- O doador está ativo ou a ONG está aprovada.

## Fluxo Principal

1. O ator acessa uma funcionalidade protegida.
2. O sistema solicita autenticação.
3. O ator informa e-mail e senha.
4. O sistema autentica a conta e redireciona o doador ao seu painel ou a ONG ao painel institucional.

## Fluxos Alternativos

### FA-01: Autenticação HTTP Basic

1. O ator acessa um recurso com credenciais HTTP Basic.
2. O sistema autentica a conta e atende o recurso autorizado.

## Fluxos de Exceção

### FE-01: Credenciais ou conta inválidas

1. O e-mail ou a senha é inválido, a conta está desabilitada ou o mesmo e-mail existe como usuário e instituição.
2. O sistema rejeita a autenticação.
3. O acesso solicitado não é concedido.

## Pós-condições

- A conta está autenticada.
- As funcionalidades protegidas ficam sujeitas às permissões do perfil.

## Histórico de Alterações

| Versão | Data       | Autor  | Descrição            |
|--------|------------|--------|----------------------|
| 1.0    | 2026-10-09 | Hiel Saraiva | Criação do documento |
