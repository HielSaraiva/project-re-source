# RF-020 - Solicitar recuperação de senha

## Identificação

| Campo | Valor |
|-------|-------|
| **ID** | RF-020 |
| **Nome** | Solicitar recuperação de senha |
| **Prioridade** | Alta |
| **Versão** | 1.0 |
| **Status** | Implementado |

## Descrição

O sistema deve oferecer a tela pública de recuperação de senha para doadores e instituições. Deve receber o e-mail cadastrado e, quando elegível conforme RN-019, enviar um link para definir outra senha, sem revelar publicamente a existência da conta.

## Atores

- Doador ou representante da ONG sem acesso à senha.

## Entradas e Saídas

### Entradas

- E-mail válido, obrigatório, até 320 caracteres.

### Saídas

- Mensagem genérica orientando a conferir a caixa de entrada e o spam.
- E-mail para a conta elegível, com identificação ReSource, logo, identidade visual do site, saudação, botão/link de recuperação, validade e orientação para ignorar uma solicitação não reconhecida.
- Mensagens de validação para e-mail inválido.
- Indicação de indisponibilidade e botão desabilitado na tela quando o serviço estiver desabilitado.
- Orientação para utilizar Google quando a conta não possui senha local.

A política de envio e limites está em [RN-019](../regras-de-negocio/RN-019-recuperacao-de-senha.md); a navegação está em [UC-018](../casos-de-uso/UC-018-solicitar-recuperacao-de-senha.md).

## Histórico de Alterações

| Versão | Data | Autor | Descrição |
|--------|------|-------|-----------|
| 1.0 | 2026-10-10 | Hiel Saraiva | Tela e envio de recuperação por e-mail |
