# UC-017 - Cadastrar instituição

## Identificação

| Campo | Valor |
|-------|-------|
| **ID** | UC-017 |
| **Nome** | Cadastrar instituição |
| **Ator(es)** | Representante legal, visitante |
| **Versão** | 1.0 |
| **Status** | Ativo |

## Pré-condições

- Visitante não autenticado, com CNPJ ativo e acesso aos documentos exigidos.
- BrasilAPI disponível e armazenamento privado gravável.

## Fluxo Principal

1. O visitante seleciona “Instituição (CNPJ)” no cadastro ou acessa `/register/institution`.
2. Informa o CNPJ e utiliza a consulta; o sistema preenche a razão social oficial. A consulta também pode ocorrer ao sair do campo.
3. Informa e-mail oficial, nome e CPF do representante, senha e confirmação.
4. Anexa identidade e documentação da organização em PDF ou JPG.
5. Seleciona “Solicitar Verificação da Conta”.
6. O sistema valida os dados, repete a consulta oficial do CNPJ e confere a disponibilidade dos identificadores.
7. Cria a instituição pendente de aprovação e os dois documentos pendentes, com arquivos privados e senha protegida por hash.
8. Redireciona ao login institucional com confirmação e orientação de aguardar aprovação.

## Fluxos Alternativos

- Sem JavaScript, o envio ainda consulta e persiste a razão social oficial pelo servidor.
- Uma conta já autenticada que acessa o cadastro é redirecionada ao seu painel.

## Fluxos de Exceção

- Dados inválidos, CNPJ inativo ou inexistente, identificadores duplicados, confirmação diferente ou documentos inválidos: o cadastro é rejeitado, os campos indicam o problema e senhas e arquivos precisam ser informados novamente.
- Falha da consulta externa: o sistema orienta a tentar novamente e não cria a instituição.
- Upload acima do limite do parser: o sistema volta ao formulário com mensagem de limite e solicita novo preenchimento.
- Falha na persistência: a transação não cria a instituição e remove arquivos recém-gravados.
- Token CSRF ausente ou inválido: a requisição é rejeitada sem criação de conta.

## Pós-condições

A instituição e os dois documentos aguardam análise. O visitante não está autenticado e só poderá entrar após aprovação, conforme UC-001. A análise administrativa não é realizada por este caso de uso.

## Histórico de Alterações

| Versão | Data | Autor | Descrição |
|--------|------|-------|-----------|
| 1.0 | 2026-10-09 | Codex | Solicitação de cadastro institucional |
