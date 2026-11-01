# RN-019 - Recuperação de senha

## Identificação

| Campo | Valor |
|-------|-------|
| **ID** | RN-019 |
| **Nome** | Recuperação de senha |
| **Versão** | 1.0 |
| **Status** | Ativa |

## Descrição

Permitir recuperar uma credencial local sem revelar a existência de contas nem permitir reutilização do link.

## Contexto

Solicitação e conclusão da recuperação por e-mail.

## Regra

- A busca usa o e-mail cadastrado sem distinção entre maiúsculas e minúsculas, com remoção dos espaços externos.
- O envio exige serviço habilitado e exatamente uma conta com esse e-mail entre usuários e instituições, com senha local existente. Uma conta exclusivamente Google não recebe link para criar senha por este fluxo.
- A elegibilidade para receber o link não exige conta ativa ou instituição aprovada. Recuperar a senha não altera essas situações; o login continua sujeito a RN-001.
- Para um e-mail de formato válido, a resposta pública deve ser genérica, inclusive quando a conta não existe, não é elegível, o serviço está desabilitado ou a solicitação é limitada.
- São admitidas até cinco solicitações por endereço de origem em uma janela de quinze minutos. O endereço considerado é o que o servidor identifica na requisição.
- Para uma mesma conta, um novo link não é emitido se houve emissão no último minuto.
- O link tem validade de trinta minutos na configuração atual e é de uso único. Solicitar outro link não invalida automaticamente os anteriores ainda válidos.
- A redefinição bem-sucedida consome o link utilizado e invalida os demais links pendentes da conta.
- A nova senha deve cumprir RN-018. Somente abrir o link não altera a senha.
- Após confirmar a redefinição, as sessões da conta registradas na instância da aplicação são marcadas para expiração. O usuário deve entrar novamente; não é autenticado automaticamente.

## Exceções

E-mail vazio, malformado ou acima de 320 caracteres gera erro de campo, sem submissão válida. Link ausente, inválido, expirado ou consumido não permite redefinição. Falha de envio não deve revelar dados da conta na resposta pública.

A validade é configurável; trinta minutos é o valor utilizado no projeto. Detalhes de persistência, execução assíncrona e limites da expiração de sessões estão em [arquitetura](../arquitetura/arquitetura.md).

## Histórico de Alterações

| Versão | Data | Autor | Descrição |
|--------|------|-------|-----------|
| 1.0 | 2026-10-10 | Hiel Saraiva | Elegibilidade, limites, validade e efeitos da recuperação implementada |
