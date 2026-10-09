# RN-014 - Consolidação do status da doação

## Identificação

| Campo       | Valor              |
|-------------|--------------------|
| **ID**      | RN-014             |
| **Nome**    | Consolidação do status da doação |
| **Versão**  | 1.0                |
| **Status**  | Ativa |

## Descrição

O status da doação é calculado a partir de suas propostas vinculadas.

## Contexto

Atualização do inventário após eventos do fluxo.

## Regra

- Se houver propostas ativas, o status da doação corresponde ao da proposta ativa criada primeiro.
- O status proposta criada é convertido em aguardando aceite na consolidação.
- Sem propostas ativas, a doação fica concluída se a quantidade consumida atingir ou superar a quantidade cadastrada.
- Sem propostas ativas e com saldo disponível, a doação fica cadastrada.
- Uma mudança do status consolidado gera registro no histórico da doação.

## Exceções

O status consolidado não substitui os estados individuais das propostas, que continuam disponíveis por protocolo.

## Histórico de Alterações

| Versão | Data       | Autor  | Descrição          |
|--------|------------|--------|--------------------|
| 1.0    | 2026-10-09 | Hiel Saraiva | Criação do documento |
