# Validação de Minhas necessidades

## Matriz de disponibilidade

| Necessidade/propostas | Editar | Excluir | Histórico |
|---|---|---|---|
| Ativa sem propostas | Sim | Sim, remoção física | Sem protocolos |
| Ativa, todas canceladas/recusadas | Sim | Sim, exclusão lógica | Preservado |
| Ativa, qualquer proposta em andamento | Não | Não | Preservado |
| Ativa, qualquer proposta concluída | Não | Não | Preservado |
| Concluída ou cancelada | Não | Não | Preservado |
| Excluída logicamente | Não aparece na gestão/descoberta | Não | Protocolos continuam acessíveis |

## Heurísticas de Nielsen aplicadas

| Heurística | Decisão na interface |
|---|---|
| Visibilidade do estado | Carregamento de dados, confirmação de sucesso, contagem e estados das necessidades; bloqueio explicado no card |
| Linguagem do usuário | “Concluída”, “Nome do item”, “Quantidade solicitada”; motivo da restrição em português |
| Controle e liberdade | Cancelar/Manter necessidade, Escape, foco de retorno e recarga explícita sem descartar campos silenciosamente |
| Consistência | Mesmos `ui-dialog`, cabeçalho, ícone, espaçamento, botões e ações dos modais existentes |
| Prevenção de erros | Edição/exclusão bloqueadas no front e no servidor, confirmação identificada, versão do registro e prevenção de envio duplicado |
| Reconhecimento | Nome e número na exclusão; rótulos visíveis e resumo das quantidades nos cards |
| Eficiência | Filtros/página preservados, Enter/botão Buscar sem interromper digitação, agregados consultados em lote |
| Design minimalista | Reuso dos componentes, prioridade com seleção explícita, remoção do modal de confirmação de atendimento incompatível com a nova regra |
| Recuperação de erros | Campos preservados, explicação de conflito, recarga e links para propostas/lista; erro focado e anunciado |
| Ajuda contextual | Exemplos, tamanho do nome, unidade/limites de quantidade e explicação sobre preservação do histórico |

## Regressões automatizadas

Execute `LOG_FORMAT= NOTIFICATION_EMAIL_ENABLED=false ./mvnw verify -B`, com Docker disponível.

Os testes utilizam PostgreSQL/RabbitMQ descartáveis e verificam a criação concorrente de nomes com capitalizações diferentes, todos os estados bloqueantes, cancelamento e recusa reais, histórico misto, preservação de protocolos, dados compartilhados, versões antigas, corrida entre proposta e edição, ordenação por prazo filtrada e fronteiras HTTP/CSRF/perfil.

A migração V035 inclui conciliação dos tipos já duplicados antes de criar a unicidade normalizada. Antes de promover, conferir sua execução em cópia do banco do ambiente. Não editar migrações anteriores.

## Verificações de navegador

- Desktop 1440×1000 e celular 375×812, sem transbordamento horizontal.
- Modais de cadastro, edição e exclusão seguem os componentes compartilhados; ações e campos utilizam foco visível, nomes acessíveis e alvos de ao menos 44 px.
- Escape fecha o modal e retorna o foco; durante gravação a saída e o reenvio ficam bloqueados.
- Somente canceladas/recusadas liberam ações; pendentes/em entrega bloqueiam ambas.
- Formulário antigo mantém valores digitados e exige recarga antes de salvar.
- Atrasar a consulta de A, fechar e abrir B não pode aplicar dados/erros de A em B.
- Exclusão com recusa remove o card, mas mantém necessidade lógica, proposta, histórico e eventos.
- Concluídas usam a legenda “Concluída”.
- Estado vazio com busca/filtros orienta limpar ou ajustar a consulta.

As verificações visuais não substituem auditoria com leitores de tela, outros navegadores e testes de carga prolongados.
