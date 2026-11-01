# RF-023 - Consultar e ler notificações

## Identificação

| Campo | Valor |
|-------|-------|
| **ID** | RF-023 |
| **Nome** | Consultar e ler notificações |
| **Prioridade** | Média |
| **Versão** | 1.0 |
| **Status** | Implementado |

## Descrição

O sistema deve oferecer notificações no header das áreas de doador e ONG, com contador de não lidas, lista paginada, acesso à proposta e ações de leitura individual ou coletiva.

## Atores

- Doador.
- ONG.

## Entradas e Saídas

### Entradas

- Conta autenticada e autorizada.
- Página solicitada ou identificação de uma notificação própria.
- Ação de abrir a notificação ou marcar todas como lidas.

### Saídas

- Até vinte notificações por página, das mais recentes para as mais antigas, com desempate pelo identificador decrescente.
- Título, mensagem, data, indicação de leitura e link de detalhes.
- Contador total de não lidas; o indicador visual apresenta `99+` acima de 99.
- Atualização na entrada da página, ao abrir o painel e periodicamente enquanto a página estiver visível e o painel fechado, além do retorno à página visível.
- Selecionar um aviso registra sua leitura antes de navegar para a proposta.
- Marcar todas como lidas atualiza o contador sem excluir os avisos.
- Mensagem descritiva quando não houver avisos; controles de página anterior/próxima conforme disponibilidade.
- Em falha de consulta ou leitura, mensagem e opção de tentar novamente.
- Fechamento ao clicar fora, mover o foco para fora ou pressionar Escape, com retorno do foco ao acionador no último caso.

O escopo de acesso e efeitos da leitura estão em [RN-020](../regras-de-negocio/RN-020-destinatarios-e-leitura-das-notificacoes.md). A paginação das notificações é independente de RF-017. Consulte [UC-020](../casos-de-uso/UC-020-consultar-e-ler-notificacoes.md).

## Histórico de Alterações

| Versão | Data | Autor | Descrição |
|--------|------|-------|-----------|
| 1.0 | 2026-10-10 | Hiel Saraiva | Consulta no header, paginação, leitura e estados vazios |
