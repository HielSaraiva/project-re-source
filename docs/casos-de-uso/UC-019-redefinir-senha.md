# UC-019 - Redefinir senha

## Identificação

| Campo | Valor |
|-------|-------|
| **ID** | UC-019 |
| **Nome** | Redefinir senha |
| **Ator(es)** | Doador, representante da ONG |
| **Versão** | 1.0 |
| **Status** | Ativo |

## Descrição

Definir outra senha a partir de um link de recuperação, conforme RF-021, RN-018 e RN-019.

## Pré-condições

- O ator possui um link válido emitido para uma conta com senha local.

## Fluxo Principal

1. O ator abre o link recebido por e-mail.
2. O sistema valida o link e apresenta nova senha e confirmação.
3. O ator preenche os campos e solicita salvar a nova senha.
4. O sistema revalida o link e a política RN-018, altera a credencial e aplica os efeitos de RN-019.
5. O sistema redireciona ao login do perfil da conta com confirmação de sucesso.
6. O ator autentica-se conforme UC-001; situação e perfil continuam sendo verificados.

## Fluxos Alternativos

### FA-01: Mostrar ou ocultar senha

1. O ator utiliza o controle do campo desejado.
2. O sistema alterna a visibilidade apenas daquele campo.

### FA-02: Link indisponível

1. O link está ausente, inválido, expirado ou já foi utilizado.
2. O sistema apresenta mensagem explicativa e ação para executar UC-018, sem formulário de alteração.

## Fluxos de Exceção

### FE-01: Senha fora da política ou confirmação diferente

1. O sistema rejeita os campos e apresenta orientações.
2. A senha não é alterada; os campos de senha ficam vazios para novo preenchimento.

### FE-02: Link expira ou é consumido durante o preenchimento

1. A revalidação ao salvar identifica o link indisponível.
2. O sistema não altera a credencial por essa solicitação e oferece nova recuperação.

### FE-03: Submissão sem proteção válida

1. A requisição não possui token CSRF válido.
2. O sistema rejeita a redefinição.

## Pós-condições

- A nova senha está definida sem modificar perfil ou situação da conta.
- Os links pendentes da conta foram invalidados e as sessões registradas na instância foram marcadas para expiração.
- O ator precisa autenticar-se novamente.

## Histórico de Alterações

| Versão | Data | Autor | Descrição |
|--------|------|-------|-----------|
| 1.0 | 2026-10-10 | Hiel Saraiva | Redefinição, links inválidos e expiração das sessões registradas |
