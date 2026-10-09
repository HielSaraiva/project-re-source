# RF-008 - Consultar andamento da doação

## Identificação

| Campo          | Valor                                   |
|----------------|-----------------------------------------|
| **ID**         | RF-008                                  |
| **Nome**       | Consultar andamento da doação |
| **Prioridade** | Alta |
| **Versão**     | 1.2                                     |
| **Status**     | Implementado |

## Descrição

O sistema deve permitir ao doador e à ONG destinatária consultar os detalhes de uma proposta por protocolo, incluindo andamento, entrega, prazos, histórico e ações disponíveis. Para o doador, deve direcionar a página à etapa atual.

## Atores

- Doador
- ONG

## Entradas e Saídas

### Entradas (Dados necessários)
- Protocolo da proposta (texto, obrigatório).
- Conta autenticada e autorizada (identificação obtida da autenticação, por sessão ou HTTP Basic, obrigatória).

### Saídas (Resultados esperados)
- Dados da proposta, doador, instituição destinatária, doação, necessidade e quantidade alocada.
- Status, datas, motivos de recusa ou cancelamento, etapa expirada e prazos.
- Endereço, contatos e horários de recebimento cadastrados da ONG, além dos dados da entrega, quando existentes.
- Histórico em ordem cronológica, com desempate pelo identificador, e ações permitidas pelo estado e prazo da proposta.
- Redirecionamento do doador à página da etapa atual quando necessário.
- Registro não encontrado quando o protocolo não existe ou não pertence ao ator.

## Histórico de Alterações

| Versão | Data       | Autor  | Descrição            |
|--------|------------|--------|----------------------|
| 1.0    | 2026-10-09 | Hiel Saraiva | Criação do documento |
| 1.1    | 2026-10-09 | Hiel Saraiva | Precisão sobre a origem da identificação do ator |
| 1.2    | 2026-10-09 | Hiel Saraiva | Precisão da apresentação funcional do histórico, antes descrita em RN-015 |
