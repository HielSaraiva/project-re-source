# RF-024 - Encerrar sessão

## Identificação

| Campo | Valor |
|-------|-------|
| **ID** | RF-024 |
| **Nome** | Encerrar sessão |
| **Prioridade** | Alta |
| **Versão** | 1.0 |
| **Status** | Implementado |

## Descrição

O sistema deve permitir encerrar a sessão pelo menu do perfil nas áreas de doador e ONG e retornar ao login com confirmação da saída.

## Atores

- Doador.
- ONG.

## Entradas e Saídas

### Entradas

- Sessão autenticada.
- Ação “Sair da conta” no menu do perfil.

### Saídas

- Sessão encerrada e página de login com mensagem de sucesso.
- Acesso posterior às áreas protegidas sujeito a nova autenticação.
- Menu com abertura e fechamento animados, respeitando a preferência de redução de movimento.
- Fechamento ao clicar ou mover o foco para fora e ao pressionar Escape; neste último caso, o foco volta ao acionador.

A proteção da requisição está em [arquitetura](../arquitetura/arquitetura.md). O encerramento não desconecta a conta do usuário no Google. Consulte [UC-021](../casos-de-uso/UC-021-encerrar-sessao.md).

## Histórico de Alterações

| Versão | Data | Autor | Descrição |
|--------|------|-------|-----------|
| 1.0 | 2026-10-10 | Hiel Saraiva | Separação do encerramento de sessão do caso de autenticação |
