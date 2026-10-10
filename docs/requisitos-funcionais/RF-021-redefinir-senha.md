# RF-021 - Redefinir senha

## Identificação

| Campo | Valor |
|-------|-------|
| **ID** | RF-021 |
| **Nome** | Redefinir senha |
| **Prioridade** | Alta |
| **Versão** | 1.0 |
| **Status** | Implementado |

## Descrição

O sistema deve apresentar o formulário de nova senha e confirmação quando o link de recuperação for válido. Deve validar a nova credencial, concluir a redefinição e apresentar o login do perfil da conta com confirmação de sucesso.

## Atores

- Doador ou representante da ONG com link de recuperação.

## Entradas e Saídas

### Entradas

- Link de recuperação, obrigatório.
- Nova senha e confirmação conforme [RN-018](../regras-de-negocio/RN-018-politica-de-senhas.md).

### Saídas

- Formulário com controles de mostrar/ocultar senha.
- Senha alterada, links consumidos e sessões marcadas para expiração conforme [RN-019](../regras-de-negocio/RN-019-recuperacao-de-senha.md).
- Redirecionamento ao login correspondente, sem autenticação automática.
- Mensagens de validação, sem reapresentar senhas enviadas.
- Para link inválido, expirado ou utilizado, mensagem explicativa e ação para solicitar outro link, sem disponibilizar o formulário de alteração.

O fluxo de interação está em [UC-019](../casos-de-uso/UC-019-redefinir-senha.md).

## Histórico de Alterações

| Versão | Data | Autor | Descrição |
|--------|------|-------|-----------|
| 1.0 | 2026-10-10 | Hiel Saraiva | Conclusão da recuperação e tratamento de links indisponíveis |
