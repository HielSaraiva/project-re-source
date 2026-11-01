# UC-018 - Solicitar recuperação de senha

## Identificação

| Campo | Valor |
|-------|-------|
| **ID** | UC-018 |
| **Nome** | Solicitar recuperação de senha |
| **Ator(es)** | Doador, representante da ONG |
| **Versão** | 1.0 |
| **Status** | Ativo |

## Descrição

Solicitar acesso à redefinição de uma senha esquecida, conforme RF-020 e RN-019.

## Pré-condições

- O ator pode acessar o e-mail informado.
- O serviço de recuperação está habilitado para executar o fluxo com envio.

## Fluxo Principal

1. O ator seleciona “Esqueceu a senha?” no login.
2. O sistema apresenta a tela de recuperação.
3. O ator informa seu e-mail e solicita o link.
4. O sistema valida o formato e apresenta confirmação genérica, orientando a conferir caixa de entrada e spam.
5. O sistema processa a elegibilidade e os limites de RN-019 e envia o e-mail para a conta elegível.
6. O ator abre o link e segue [UC-019](UC-019-redefinir-senha.md).

## Fluxos Alternativos

### FA-01: Conta ausente, ambígua, exclusivamente Google ou solicitação limitada

1. O processamento identifica uma condição de inelegibilidade de RN-019.
2. Não emite novo link e mantém a mesma resposta pública.
3. A página orienta contas exclusivamente Google a utilizar o login Google.

### FA-02: Serviço desabilitado

1. A tela informa indisponibilidade e desabilita o botão de envio.
2. Uma submissão direta de e-mail válido mantém a resposta genérica, sem emissão de link.

### FA-03: Nova solicitação

1. Após a confirmação, o ator seleciona “Solicitar outro link”.
2. O sistema volta ao formulário; uma nova submissão continua sujeita aos limites de RN-019.

## Fluxos de Exceção

### FE-01: E-mail inválido

1. O sistema identifica e-mail ausente, inválido ou acima do limite.
2. Apresenta erro de campo e permite correção.

### FE-02: Falha de processamento ou envio

1. A solicitação não pode ser processada ou o envio falha.
2. A confirmação pública não expõe a existência da conta nem garante entrega.
3. O ator pode tentar novamente respeitando os limites.

### FE-03: Submissão sem proteção válida

1. A requisição de alteração não possui proteção CSRF válida.
2. O sistema rejeita a submissão.

## Pós-condições

- A senha permanece inalterada.
- Quando o envio é bem-sucedido, há um link disponível para a conta elegível.

## Histórico de Alterações

| Versão | Data | Autor | Descrição |
|--------|------|-------|-----------|
| 1.0 | 2026-10-10 | Hiel Saraiva | Interação de solicitação e confirmação sem enumeração de contas |
