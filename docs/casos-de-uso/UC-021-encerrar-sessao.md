# UC-021 - Encerrar sessão

## Identificação

| Campo | Valor |
|-------|-------|
| **ID** | UC-021 |
| **Nome** | Encerrar sessão |
| **Ator(es)** | Doador, ONG |
| **Versão** | 1.0 |
| **Status** | Ativo |

## Descrição

Finalizar a sessão da aplicação, conforme RF-024.

## Pré-condições

- O ator possui sessão autenticada na aplicação.

## Fluxo Principal

1. O ator abre o menu do perfil no header.
2. Seleciona “Sair da conta”.
3. O sistema encerra a sessão e redireciona ao login com confirmação.
4. A próxima tentativa de acessar uma área protegida exige autenticação.

## Fluxos Alternativos

### FA-01: Fechar o menu sem sair

1. O ator clica ou move o foco para fora, pressiona Escape ou aciona novamente o menu.
2. O menu fecha e a sessão permanece autenticada.

## Fluxos de Exceção

### FE-01: Requisição sem proteção válida

1. A solicitação de saída não possui token CSRF válido.
2. O sistema rejeita a requisição; a saída não é concluída por essa solicitação.

## Pós-condições

- A sessão do ReSource está encerrada.
- O encerramento local não desconecta o usuário do Google.

## Histórico de Alterações

| Versão | Data | Autor | Descrição |
|--------|------|-------|-----------|
| 1.0 | 2026-10-10 | Hiel Saraiva | Caso independente de encerramento da sessão |
