# UC-004 - Cadastrar itens para doação

## Identificação

| Campo          | Valor                        |
|----------------|------------------------------|
| **ID**         | UC-004                       |
| **Nome**       | Cadastrar itens para doação |
| **Ator(es)**   | Doador |
| **Versão**     | 1.1                          |
| **Status**     | Ativo |

## Descrição

Registrar itens no inventário do doador a partir de uma necessidade.

## Pré-condições

- O doador está autenticado e ativo.
- A necessidade está ativa e sua ONG está aprovada.
- Para abrir o cadastro pela interface, a necessidade possui saldo restante positivo.

## Fluxo Principal

1. O doador acessa a proposta de uma necessidade e escolhe cadastrar itens.
2. O doador informa título, quantidade, estado de conservação e, opcionalmente, descrição.
3. O sistema valida os dados e utiliza o tipo de item da necessidade selecionada.
4. O sistema registra o item, o pacote e a doação com status cadastrado, incluindo seu histórico.
5. A tela adiciona a nova doação ao seletor de inventário e permite preparar a proposta.

## Fluxos Alternativos

### FA-01: Descrição não informada

1. O doador deixa a descrição vazia.
2. O sistema cadastra o item sem descrição.

## Fluxos de Exceção

### FE-01: Cadastro inválido ou necessidade indisponível

1. Os campos obrigatórios estão ausentes, a quantidade não é positiva, o título excede 120 caracteres, a descrição excede 1.000 caracteres ou a necessidade não está disponível.
2. O sistema rejeita o cadastro.
3. Nenhuma doação é cadastrada pela operação.

## Pós-condições

- A doação está cadastrada no inventário do doador.
- O cadastro não cria uma proposta nem reserva quantidade para a necessidade.

## Histórico de Alterações

| Versão | Data       | Autor  | Descrição            |
|--------|------------|--------|----------------------|
| 1.0    | 2026-10-09 | Hiel Saraiva | Criação do documento |
| 1.1    | 2026-10-09 | Hiel Saraiva | Precisão das condições e do resultado do cadastro pela interface |
