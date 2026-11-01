# RF-016 - Cancelar propostas por prazo encerrado

## Identificação

| Campo          | Valor                                   |
|----------------|-----------------------------------------|
| **ID**         | RF-016                                  |
| **Nome**       | Cancelar propostas por prazo encerrado |
| **Prioridade** | Alta |
| **Versão**     | 1.3                                     |
| **Status**     | Implementado |

## Descrição

O sistema deve executar uma rotina interna de verificação periódica para identificar e cancelar automaticamente propostas cujo prazo de aceite, escolha da modalidade, entrega presencial ou postagem terminou, liberando a reserva e registrando a etapa expirada.

## Atores

- Sistema

## Entradas e Saídas

### Entradas (Dados necessários)

- Propostas candidatas com prazo ativo encerrado, conforme RN-008.
- Estado e prazo atuais da proposta e da entrega (dados persistidos, obrigatórios).

### Saídas (Resultados esperados)

- Revalidação do estado e do prazo atuais antes de confirmar a expiração.
- Proposta e entrega vinculada, quando existente, canceladas.
- Data, motivo e etapa expirada registrados.
- Reserva liberada, evento registrado e status consolidado da doação atualizado.
- Reavaliação do prazo no aceite, na recusa, no cancelamento, na escolha da modalidade e na informação de entrega ou postagem, após validação dos campos e das permissões, com rejeição da ação quando houver expiração.
- Propostas que não possuem mais prazo ativo vencido na reconsulta são preservadas. As consultas de páginas não executam cancelamento automático.
- Registro de falha por protocolo e continuidade do processamento dos demais protocolos, com possibilidade de reavaliação em execução posterior.

A duração das etapas está em [RN-008](../regras-de-negocio/RN-008-prazos-das-etapas-da-doacao.md). A rotina técnica e sua configuração estão em [arquitetura](../arquitetura/arquitetura.md). As notificações do cancelamento seguem RF-022 e RN-020; não há alerta antecipado de vencimento.

## Histórico de Alterações

| Versão | Data       | Autor  | Descrição            |
|--------|------------|--------|----------------------|
| 1.0    | 2026-10-09 | Hiel Saraiva | Criação do documento |
| 1.1    | 2026-10-09 | Hiel Saraiva | Precisão do lote e das condições de cancelamento por prazo |
| 1.2    | 2026-10-09 | Hiel Saraiva | Consolidação do antigo UC-016, referente a uma rotina interna automatizada |
| 1.3 | 2026-10-10 | Hiel Saraiva | Separação da rotina técnica e referência ao aviso de cancelamento |
