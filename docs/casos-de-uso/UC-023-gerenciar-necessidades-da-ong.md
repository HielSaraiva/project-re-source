# UC-023 - Gerenciar necessidades da ONG

## Identificação

| Campo | Valor |
|---|---|
| **ID** | UC-023 |
| **Nome** | Gerenciar necessidades da ONG |
| **Ator(es)** | ONG |
| **Versão** | 1.0 |
| **Status** | Ativo |

## Descrição

Permitir que a ONG consulte as necessidades de sua instituição, cadastre novas necessidades e edite os dados e prioridades das necessidades ativas.

## Pré-condições

- A ONG está autenticada e aprovada.

## Fluxo Principal

1. A ONG abre “Minhas necessidades”.
2. O sistema apresenta as necessidades da instituição, suas quantidades, prioridades e status.
3. A ONG escolhe cadastrar uma necessidade ou editar uma necessidade ativa.
4. O sistema apresenta o formulário com categoria, nome do item, quantidade e prioridade.
5. A ONG informa ou atualiza os dados e confirma a operação.
6. O sistema valida a entrada, salva a alteração e atualiza a lista.

## Fluxos Alternativos

### FA-01: Nenhuma necessidade cadastrada

1. A instituição ainda não possui necessidades.
2. O sistema apresenta uma mensagem orientativa e permite iniciar o cadastro.

### FA-02: Quantidade abaixo das alocações existentes

1. A ONG tenta reduzir a quantidade solicitada abaixo da soma alocada em propostas existentes.
2. O sistema rejeita a atualização e informa a quantidade mínima permitida.

## Fluxos de Exceção

### FE-01: Necessidade de outra instituição

1. A requisição identifica uma necessidade que não pertence à instituição autenticada.
2. O sistema rejeita a operação sem expor os dados da necessidade.

### FE-02: Necessidade inativa

1. A ONG tenta editar uma necessidade atendida ou cancelada.
2. O sistema rejeita a edição.

## Pós-condições

- Uma necessidade nova foi cadastrada ou os dados de uma necessidade ativa foram atualizados.
- Os dados de doações e propostas existentes não foram alterados pela edição do tipo do item.

## Histórico de Alterações

| Versão | Data | Autor | Descrição |
|---|---|---|---|
| 1.0 | 2026-10-18 | João Marcelo Pimenta  | Criação do caso de uso de gerenciamento de necessidades |
