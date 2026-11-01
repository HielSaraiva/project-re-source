# RF-012 - Escolher modalidade de entrega

## Identificação

| Campo          | Valor                                   |
|----------------|-----------------------------------------|
| **ID**         | RF-012                                  |
| **Nome**       | Escolher modalidade de entrega |
| **Prioridade** | Alta |
| **Versão**     | 1.0                                     |
| **Status**     | Implementado |

## Descrição

O sistema deve permitir ao doador confirmar uma única modalidade de entrega para uma proposta aceita, registrando o endereço da ONG e o prazo da modalidade.

## Atores

- Doador

## Entradas e Saídas

### Entradas (Dados necessários)
- Protocolo da proposta (texto, obrigatório).
- Modalidade (entrega presencial ou envio pelos Correios, obrigatória).
- Confirmação da escolha (booleano verdadeiro, obrigatório).

### Saídas (Resultados esperados)
- Entrega registrada com modalidade, instante da confirmação e cópia do endereço de recebimento.
- Prazo de sete dias para informar entrega presencial ou postagem.
- Status aguardando entrega para modalidade presencial ou aguardando envio e transportadora Correios para envio.
- Evento de escolha e atualização do status consolidado da doação.
- Rejeição quando faltar endereço, já houver modalidade registrada, a modalidade não for suportada ou o estado, prazo ou confirmação forem inválidos.

## Histórico de Alterações

| Versão | Data       | Autor  | Descrição            |
|--------|------------|--------|----------------------|
| 1.0    | 2026-10-09 | Hiel Saraiva | Criação do documento |
