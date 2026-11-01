# Estabilização do MVP — 01/11/2026

Execute `LOG_FORMAT= NOTIFICATION_EMAIL_ENABLED=false ./mvnw verify -B` com Java 25 e Docker disponíveis.

## Regressões permanentes acrescentadas

- Retries do cadastro contextual retornam a mesma doação; payload alterado com chave usada retorna 409, sem erro transacional 500.
- Frações nas quantidades e identificadores do matching retornam 400 sem criar cadastro/reserva.
- Nome, condição e descrição de propostas concluídas permanecem iguais após editar o saldo remanescente. A busca por nome original funciona nas ordenações recente e por prazo.
- Página fora do intervalo retorna a última página existente, preservando texto, necessidade, status e ordenação.
- Duas propostas simultâneas de sete unidades para necessidade de dez: uma aceita, outra em conflito; nunca aloca quatorze.
- Aceite expirado cancela a proposta, libera o estoque e mantém a mudança mesmo retornando conflito.
- Página da ONG apresenta dados da entrega presencial; nome do recebedor continua no histórico e separado do responsável pelo evento.

## Segunda bateria exploratória

Em ambiente descartável, verificar: atualização V036 → V037 com propostas existentes; fluxos presencial/Correios, recebimento repetido, cancelamento, recusa, expiração; isolamento de contas, perfil e CSRF; três visões do inventário; navegação com filtros e rejeição de retorno externo; retry após resposta perdida e reload; modais e rotas públicas em 1440 e 390 px.

SMTP real, OAuth Google e BrasilAPI não fazem parte dessa validação isolada. Não enviar mensagens a usuários reais. Dados anteriores já sobrescritos não podem ser recuperados pela migração.

## Resultado executado

- Suíte final: **50 testes aprovados**, nenhum erro/falha/ignorado.
- Migração V037 aplicada em banco da versão anterior contendo propostas; todos os protocolos receberam nome, condição e categoria preservados. Aplicação iniciou normalmente após a atualização.
- Fluxos presencial e Correios, recebimento duplicado, concorrência, expiração e autorização repetidos via HTTP com dados fictícios. Quantidades fracionárias agora retornam 400; concorrência retorna 200/409; segunda confirmação retorna 409.
- 14 páginas/estados autenticados e 8 rotas públicas conferidos em desktop/celular, sem imagens quebradas, erros JS ou overflow horizontal nos cenários inspecionados.
- Falha de comunicação após persistência + recarga da página + retry no cadastro contextual: mesmo identificador retornado nas duas requisições, sem duplicação.
- Retorno à lista preservando query/visão/página; valor externo adulterado em sessionStorage descartado.
- Na segunda bateria também foram corrigidos: busca histórica ainda usando o nome editado do estoque; duplicação da política de consolidação com desempate diferente; restauração do campo `item` colidindo com o método `item()` da coleção de campos do formulário. O cadastro recupera esse campo via `namedItem`.
