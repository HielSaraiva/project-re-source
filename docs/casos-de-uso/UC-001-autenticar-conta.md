# UC-001 - Autenticar conta

## Identificação

| Campo          | Valor                        |
|----------------|------------------------------|
| **ID**         | UC-001                       |
| **Nome**       | Autenticar conta |
| **Ator(es)**   | Doador, ONG |
| **Versão**     | 1.2                          |
| **Status**     | Ativo |

## Descrição

Permitir o acesso às funcionalidades do perfil da conta.

## Pré-condições

- Para login local, a conta já existe e possui credenciais locais.
- O doador está ativo ou a ONG está aprovada.
- Para login Google, a integração está configurada e o e-mail é verificado pelo provedor. Uma identidade nova pode criar uma conta de doador ativo.

## Fluxo Principal

1. O ator acessa uma funcionalidade protegida.
2. O sistema solicita autenticação.
3. O doador informa e-mail e senha; a instituição seleciona seu perfil e informa CNPJ e senha, com ou sem máscara. O backend também mantém o acesso institucional por e-mail.
4. O sistema autentica a conta e redireciona o doador ao seu painel ou a ONG ao painel institucional.

## Fluxos Alternativos

### FA-01: Autenticação HTTP Basic

1. O ator acessa um recurso com credenciais HTTP Basic.
2. O sistema autentica a conta e atende o recurso autorizado.

### FA-02: Autenticação Google

1. O doador seleciona “Entrar com Google” na página de login.
2. O sistema redireciona ao Google e valida o retorno OAuth 2.0 / OpenID Connect.
3. O sistema verifica o e-mail, a identidade, a situação da conta e os conflitos com contas existentes.
4. Uma identidade nova cria um doador ativo; uma identidade existente reutiliza sua conta.
5. O sistema estabelece a sessão e redireciona ao painel do doador.

### FA-03: Encerrar sessão

1. O ator abre o menu do perfil e seleciona “Sair da conta”.
2. O sistema processa o POST protegido por CSRF, invalida a sessão e apresenta a página de login com confirmação da saída.

## Fluxos de Exceção

### FE-01: Credenciais ou conta inválidas

1. O e-mail, o CNPJ ou a senha é inválido, a conta está desabilitada, a instituição não está aprovada ou o mesmo e-mail existe como usuário e instituição no login por e-mail.
2. O sistema rejeita a autenticação.
3. O acesso solicitado não é concedido.

### FE-02: Autenticação Google indisponível ou inválida

1. Sem configuração do provedor, o botão Google fica desabilitado.
2. Se a autorização for cancelada, o e-mail não for verificado, a conta estiver bloqueada ou houver conflito com outra conta, o login Google é rejeitado e uma mensagem é apresentada.
3. Contas locais não são vinculadas automaticamente por coincidência de e-mail e devem utilizar senha.

## Pós-condições

- A conta está autenticada.
- As funcionalidades protegidas ficam sujeitas às permissões do perfil.

## Histórico de Alterações

| Versão | Data       | Autor  | Descrição            |
|--------|------------|--------|----------------------|
| 1.0    | 2026-10-09 | Hiel Saraiva | Criação do documento |
| 1.1    | 2026-10-09 | Codex | Login Google, tela personalizada e encerramento de sessão |
| 1.2    | 2026-10-09 | Codex | Tela institucional e autenticação por CNPJ |
