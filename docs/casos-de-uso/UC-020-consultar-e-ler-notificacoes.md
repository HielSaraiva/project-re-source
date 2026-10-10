# UC-020 - Consultar e ler notificações

## Identificação

| Campo | Valor |
|-------|-------|
| **ID** | UC-020 |
| **Nome** | Consultar e ler notificações |
| **Ator(es)** | Doador, ONG |
| **Versão** | 1.0 |
| **Status** | Ativo |

## Descrição

Acompanhar as atualizações pessoais pelo header e acessar a proposta correspondente, conforme RF-023.

## Pré-condições

- O ator está autenticado e autorizado em sua área.
- Para o fluxo principal, existem notificações destinadas à conta.

## Fluxo Principal

1. O sistema apresenta o contador de não lidas no header.
2. O ator abre o painel de notificações.
3. O sistema apresenta os avisos pessoais, do mais recente para o mais antigo.
4. O ator seleciona um aviso.
5. O sistema marca o aviso como lido e navega para os detalhes da proposta.
6. O acesso aos detalhes é validado conforme RN-001.

## Fluxos Alternativos

### FA-01: Marcar todas como lidas

1. O ator seleciona a ação de leitura coletiva.
2. O sistema marca os avisos próprios ainda não lidos e atualiza lista e contador.

### FA-02: Paginar

1. O ator utiliza próxima página ou página anterior.
2. O sistema consulta a página correspondente, com até vinte avisos.

### FA-03: Sem notificações

1. Não existem avisos na página consultada.
2. O sistema apresenta a mensagem de ausência e ajusta os controles conforme RF-023.

### FA-04: Fechar o painel

1. O ator aciona novamente o controle, clica ou move o foco para fora, ou pressiona Escape.
2. O painel é fechado; Escape devolve o foco ao acionador.

### FA-05: Consultar por e-mail

1. O ator recebe um e-mail de atualização previsto em RF-022.
2. Abre o link e autentica-se caso necessário para consultar a proposta.
3. Essa navegação não marca automaticamente o aviso do header como lido.

## Fluxos de Exceção

### FE-01: Falha de consulta ou de leitura

1. A requisição falha.
2. O sistema apresenta mensagem e opção de tentar novamente; não conclui a navegação dependente da leitura.

### FE-02: Notificação de outra conta

1. Uma solicitação tenta marcar um aviso que não pertence ao ator.
2. O sistema informa registro não encontrado e não altera o aviso.

## Pós-condições

- O ator consultou somente seus avisos.
- As leituras solicitadas foram registradas sem excluir notificações nem alterar o fluxo da doação.

## Histórico de Alterações

| Versão | Data | Autor | Descrição |
|--------|------|-------|-----------|
| 1.0 | 2026-10-10 | Hiel Saraiva | Consulta, leitura, paginação e acesso pelos dois canais |
