# RF-002 - Consultar painel do doador

## Identificação

| Campo          | Valor                                   |
|----------------|-----------------------------------------|
| **ID**         | RF-002                                  |
| **Nome**       | Consultar painel do doador |
| **Prioridade** | Média |
| **Versão**     | 1.1                                     |
| **Status**     | Implementado |

## Descrição

O sistema deve apresentar ao doador suas propostas atualizadas recentemente e necessidades urgentes disponíveis para doação.

## Atores

- Doador

## Entradas e Saídas

### Entradas (Dados necessários)
- Conta do doador autenticado (identificação obtida da autenticação, por sessão ou HTTP Basic, obrigatória).

### Saídas (Resultados esperados)
- Nome do doador.
- Até três necessidades de prioridade alta, ativas, de ONGs aprovadas e com saldo positivo, ordenadas por criação mais recente.
- Até cinco propostas do doador, ordenadas por atualização mais recente, com protocolo, item, destinatário, status e prazo da etapa vigente, quando houver. A quantidade integra os dados preparados para o painel, mas não é exibida na tabela.

## Histórico de Alterações

| Versão | Data       | Autor  | Descrição            |
|--------|------------|--------|----------------------|
| 1.0    | 2026-10-09 | Hiel Saraiva | Criação do documento |
| 1.1    | 2026-10-09 | Hiel Saraiva | Correção dos dados exibidos e da identificação do ator após conferência do código |
