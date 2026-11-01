# Verificação do inventário do doador

Execute `LOG_FORMAT= NOTIFICATION_EMAIL_ENABLED=false ./mvnw verify -B`, com Docker disponível. Os testes usam PostgreSQL e RabbitMQ descartáveis.

## Regressões

- Retry simultâneo de cadastro: uma doação, um item/pacote e um histórico de criação.
- Chave reutilizada com dados diferentes ou após exclusão: rejeição, sem recriar estoque.
- Edição em duas abas: versão antiga não reverte alterações em item ou pacote.
- Histórico com mais de oito protocolos: todas as páginas e estados acessíveis, inclusive recusas/cancelamentos.
- Exclusão com cancelamentos/recusas: estoque oculto, protocolos preservados, consulta em Excluídas disponível apenas ao proprietário, nova proposta bloqueada.
- Propostas ativas/concluídas: bloqueio de exclusão; edição respeita estados e quantidade recebida.
- Busca por protocolo/número/ONG, filtros e paginação: isolamento e preservação do recorte.
- JSON inválido, quantidade fracionária, CSRF ausente, perfil incorreto e outro proprietário: rejeição sem persistência indevida.
- Filtro principal sem estados inúteis/ilegítimos: cancelada/aceita não são opções do inventário; recusas/cancelamentos ficam no filtro das propostas.

- Visão Concluídas: total recebido, isolamento por proprietário e ausência no inventário padrão; visões inválidas são rejeitadas.

## Verificação visual

Desktop e celular; cards e modais consistentes com Minhas necessidades; botões legíveis, foco visível, Escape e retorno de foco; campos preservados em 400/409/falha de rede; recarga explícita de versão; resposta atrasada de A não altera modal de B; reenvio com resposta lenta não duplica cadastro; consulta de Excluídas sem ação de doar/editar/excluir.

O rascunho de cadastro fica na sessão da aba, separado por doador, para que um retry após falha de comunicação mantenha a chave original. Ele é removido após confirmação do cadastro.

## Resultado da validação — 01/11/2026

- `./mvnw verify -B`: 43 testes, nenhuma falha/erro, nenhum ignorado, com PostgreSQL/RabbitMQ reais e descartáveis; envio de e-mails desabilitado.
- Chrome em 1440 px e 390 px: três visões separadas, filtros correspondentes, acesso aos protocolos de concluídas/excluídas, bloqueios de ações, foco/Escape e ausência de rolagem horizontal.
- Conflito real entre duas edições: campos preservados e recarga explícita da versão atual.
- Resposta atrasada de outro registro: sem contaminação do modal recém-aberto.
- Resposta de cadastro perdida após persistência: retry com a mesma chave, uma única doação criada.
- Consulta paginada de propostas e isolamento por proprietário exercitados nos testes de integração. Não representa validação completa de todos os fluxos do MVP em navegador.

### Histórico da entrega presencial

O evento “Entrega informada” também exibe “Recebedor informado pelo doador”, usando o nome já persistido na entrega. O responsável pelo evento continua sendo o doador; a confirmação da ONG permanece um evento separado. Isso contempla registros anteriores sem reescrever o histórico; sem nome registrado, nenhum recebedor é inventado.

Regressão adicionada: informar entrega presencial, conferir o nome e o responsável no evento, renderizar a página da ONG antes da confirmação, confirmar recebimento e renderizar a página de conclusão do doador com o nome preservado.
