# RN-008 - Prazos das etapas da doação

## Identificação

| Campo       | Valor              |
|-------------|--------------------|
| **ID**      | RN-008             |
| **Nome**    | Prazos das etapas da doação |
| **Versão**  | 1.2                |
| **Status**  | Ativa |

## Descrição

As etapas anteriores ao envio ou à informação de entrega possuem prazo de sete dias.

## Contexto

Aceite, escolha da modalidade, entrega presencial e postagem.

## Regra

- O prazo de aceite inicia na criação da proposta.
- O prazo de escolha da modalidade inicia no aceite da ONG.
- O prazo de entrega presencial ou postagem inicia na confirmação da modalidade.
- Cada prazo corresponde a uma duração de sete dias a partir do instante de início da etapa.
- A proposta expira quando o instante atual é igual ou posterior ao prazo ativo.
- A expiração cancela a proposta e a entrega vinculada, libera a reserva e registra motivo e etapa expirada.
- Aceite, recusa, cancelamento manual, escolha da modalidade e informação de entrega ou postagem não podem ser efetivados após o encerramento do prazo vigente.
- O processamento do cancelamento por expiração é descrito em RF-016.

## Exceções

Não há prazo ativo de cancelamento automático para propostas em trânsito, aguardando confirmação da ONG, concluídas, recusadas ou canceladas.

## Histórico de Alterações

| Versão | Data       | Autor  | Descrição          |
|--------|------------|--------|--------------------|
| 1.0    | 2026-10-09 | Hiel Saraiva | Criação do documento |
| 1.1    | 2026-10-09 | Hiel Saraiva | Precisão do processamento em lotes e dos momentos de verificação |
| 1.2    | 2026-10-09 | Hiel Saraiva | Separação entre política de negócio e comportamento funcional |
