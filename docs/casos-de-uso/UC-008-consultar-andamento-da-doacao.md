# UC-008 - Consultar andamento da doação

## Identificação

| Campo          | Valor                        |
|----------------|------------------------------|
| **ID**         | UC-008                       |
| **Nome**       | Consultar andamento da doação |
| **Ator(es)**   | Doador, ONG |
| **Versão**     | 1.0                          |
| **Status**     | Ativo |

## Descrição

Apresentar detalhes, prazos, entrega, histórico e ações disponíveis de uma proposta.

## Pré-condições

- O ator está autenticado e autorizado para seu perfil.
- O protocolo pertence ao doador ou está associado a uma necessidade da ONG autenticada.

## Fluxo Principal

1. O ator acessa uma proposta pelo protocolo.
2. O sistema consulta a proposta vinculada ao ator.
3. O sistema apresenta os itens, quantidade, participantes, status, prazos, dados de entrega e histórico cronológico.
4. O sistema disponibiliza as ações compatíveis com o estado e o prazo da proposta.

## Fluxos Alternativos

### FA-01: Página incompatível com a etapa

1. O doador acessa uma página que não corresponde ao estado atual da proposta.
2. O sistema redireciona para a página correspondente à etapa atual.

## Fluxos de Exceção

### FE-01: Protocolo não acessível

1. O protocolo não existe ou não pertence ao ator.
2. O sistema informa que o registro não foi encontrado.
3. O caso de uso é encerrado.

## Pós-condições

- O ator visualizou o estado atual e o histórico da proposta.

## Histórico de Alterações

| Versão | Data       | Autor  | Descrição            |
|--------|------------|--------|----------------------|
| 1.0    | 2026-10-09 | Hiel Saraiva | Criação do documento |
