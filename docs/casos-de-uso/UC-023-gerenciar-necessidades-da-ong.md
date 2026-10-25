# UC-023 - Gerenciar necessidades da ONG

## Identificação

| Campo | Valor |
|---|---|
| **ID** | UC-023 |
| **Nome** | Gerenciar necessidades da ONG |
| **Ator(es)** | ONG |
| **Versão** | 1.2 |
| **Status** | Ativo |

## Descrição

Permitir que a ONG consulte as necessidades de sua instituição, cadastre novas necessidades, edite as necessidades ativas e exclua permanentemente necessidades sem propostas associadas.

## Pré-condições

- A ONG está autenticada e aprovada.

## Fluxo Principal

1. A ONG abre “Minhas necessidades”.
2. O sistema apresenta necessidades ativas paginadas por padrão, com opção de consultar as concluídas, e exibe quantidades solicitadas, reservadas, recebidas e disponíveis.
3. A ONG escolhe cadastrar, editar uma necessidade ativa ou solicitar a exclusão de uma necessidade.
4. O sistema apresenta o formulário com categoria, nome do item, quantidade e prioridade.
5. Ao editar, o sistema informa a quantidade mínima permitida e sua composição entre recebimentos e propostas reservadas; a categoria permanece somente para leitura se houver propostas associadas.
6. A ONG informa ou atualiza os dados e confirma a operação.
7. Se a redução fizer a necessidade ficar atendida, o sistema explica a consequência e solicita confirmação.
8. O sistema valida os dados atuais sob bloqueio transacional, salva a operação, atualiza a lista e apresenta uma confirmação visual.

### FA-03: Exclusão permanente

1. A ONG solicita excluir uma necessidade.
2. O sistema exibe confirmação com a descrição da necessidade e informa que a operação é permanente.
3. A ONG confirma a exclusão.
4. Se não houver propostas associadas, o sistema apaga a necessidade do banco de dados e atualiza a lista.

## Fluxos Alternativos

### FA-01: Nenhuma necessidade cadastrada

1. A instituição ainda não possui necessidades.
2. O sistema apresenta uma mensagem orientativa e permite iniciar o cadastro.

### FA-02: Quantidade abaixo das alocações existentes

1. A ONG tenta reduzir a quantidade solicitada abaixo da soma alocada em propostas existentes.
2. O sistema rejeita a atualização e informa a quantidade mínima permitida, detalhando unidades recebidas e reservadas.

### FA-04: Quantidade atendida após edição

1. A ONG reduz a quantidade solicitada para um valor igual ou inferior ao total recebido em propostas concluídas.
2. O sistema marca a necessidade como atendida e ela deixa de aparecer em “Minhas necessidades”.
3. Quantidades reservadas em propostas ainda não concluídas não são contabilizadas como recebidas.

## Fluxos de Exceção

### FE-01: Necessidade de outra instituição

1. A requisição identifica uma necessidade que não pertence à instituição autenticada.
2. O sistema rejeita a operação sem expor os dados da necessidade.

### FE-02: Necessidade inativa

1. A ONG tenta editar uma necessidade atendida ou cancelada.
2. O sistema rejeita a edição.

### FE-03: Necessidade com propostas

1. A ONG confirma a exclusão de uma necessidade que possui propostas associadas.
2. O sistema bloqueia a exclusão e preserva o histórico e os registros de doação.

### FE-04: Troca de categoria com propostas históricas

1. A ONG tenta alterar a categoria de uma necessidade que possui proposta associada, ainda que cancelada ou recusada.
2. O sistema rejeita a alteração; a ONG pode cadastrar uma nova necessidade para a outra categoria.

### FE-05: Quantidade fracionária

1. O cliente envia uma quantidade não inteira à API.
2. O sistema rejeita a requisição sem truncar ou persistir o valor.

### FE-06: Limite atualizado durante a edição

1. Uma nova proposta é registrada enquanto a ONG mantém o formulário de edição aberto.
2. O sistema rejeita a atualização se a quantidade informada ficar abaixo do novo mínimo.
3. O sistema atualiza a justificativa do mínimo sem apagar os demais dados preenchidos; a ONG pode ajustar o valor e enviar novamente.

### FE-07: Exclusão bloqueada por propostas

1. O sistema identifica propostas associadas antes de exibir uma confirmação de exclusão.
2. A exclusão não é oferecida; a ONG recebe a explicação do bloqueio e um acesso à lista de propostas vinculadas.

### FE-08: Retorno após autenticação

1. Uma ONG não autenticada solicita “Minhas necessidades”.
2. O sistema encaminha a ONG ao login e salva a rota solicitada.
3. Após autenticar, o sistema retorna à página “Minhas necessidades”.

## Pós-condições

- Uma necessidade nova foi cadastrada, os dados de uma necessidade ativa foram atualizados ou uma necessidade sem propostas foi removida permanentemente.
- Os dados de doações e propostas existentes não foram alterados pela edição do tipo do item.

## Histórico de Alterações

| Versão | Data | Autor | Descrição |
|---|---|---|---|
| 1.0 | 2026-10-18 | João Marcelo Pimenta  | Criação do caso de uso de gerenciamento de necessidades |
| 1.1 | 2026-10-25 | João Marcelo Pimenta | Regras para atendimento após edição, categoria com propostas históricas e quantidade inteira |
| 1.2 | 2026-10-25 | João Marcelo Pimenta | Acompanhamento, limites dinâmicos, concorrência, paginação e feedback de operações |
