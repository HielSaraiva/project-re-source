# RF-001 - Autenticar conta

## Identificação

| Campo          | Valor                                   |
|----------------|-----------------------------------------|
| **ID**         | RF-001                                  |
| **Nome**       | Autenticar conta |
| **Prioridade** | Alta |
| **Versão**     | 1.0                                     |
| **Status**     | Implementado |

## Descrição

O sistema deve autenticar contas por e-mail e senha, verificar a situação e o perfil da conta e permitir o acesso às funcionalidades autorizadas. Após a autenticação por formulário, deve direcionar o doador ao seu painel e a ONG ao painel institucional.

## Atores

- Doador
- ONG

## Entradas e Saídas

### Entradas (Dados necessários)
- E-mail (texto, obrigatório).
- Senha (texto, obrigatório).

### Saídas (Resultados esperados)
- Conta autenticada com as permissões de seu perfil.
- Redirecionamento ao painel correspondente após login por formulário.
- Rejeição da autenticação para credenciais inválidas, conta desabilitada ou e-mail ambíguo entre usuário e instituição.

## Histórico de Alterações

| Versão | Data       | Autor  | Descrição            |
|--------|------------|--------|----------------------|
| 1.0    | 2026-10-09 | Hiel Saraiva | Criação do documento |
