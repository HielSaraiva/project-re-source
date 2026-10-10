# RN-017 - Dados do cadastro institucional

## Identificação

| Campo | Valor |
|-------|-------|
| **ID** | RN-017 |
| **Nome** | Dados do cadastro institucional |
| **Versão** | 1.1 |
| **Status** | Ativa |

## Descrição

Definir os dados exigidos para uma solicitação institucional e manter a conta e os documentos pendentes de análise.

## Contexto

Cadastro público de instituições que desejam receber recursos.

## Regra

- CNPJ e CPF aceitam valores completos com ou sem máscara; são persistidos sem pontuação. Letras do CNPJ são convertidas para maiúsculas. Os dígitos verificadores são conferidos pelo servidor; sequências numéricas repetidas são rejeitadas.
- O CNPJ tem 12 caracteres numéricos ou alfanuméricos e dois dígitos verificadores. O CPF tem 11 dígitos.
- A razão social é obtida da consulta cadastral utilizada pelo sistema. O cadastro exige que o CNPJ retornado corresponda ao solicitado e tenha situação cadastral ativa. Dados incompletos ou falha na consulta impedem o cadastro.
- CNPJ e CPF do representante são únicos entre instituições. O e-mail, normalizado em minúsculas e sem espaços externos, é único entre usuários e instituições, inclusive em solicitações simultâneas.
- Nome do representante (até 200 caracteres), e-mail válido (até 320), senha, confirmação e os dois anexos são obrigatórios. Senha e confirmação seguem RN-018.
- A situação institucional é definida pelo servidor como `pending_approval`. Não é possível atribuir aprovação ou permissões pelo formulário.
- São exigidos documento de identidade e documentação da organização, não vazios, em PDF ou JPG válido, até 5 MB cada. A conferência de formato não substitui a análise administrativa do conteúdo.
- Os documentos são privados e devem permanecer associados à instituição e ao tipo de documento.
- Os dois documentos começam pendentes. A solicitação não pode ser concluída com apenas parte da conta e dos documentos registrada.
- A criação não concede sessão autenticada nem aprovação. O acesso ao painel exige aprovação conforme RN-001.

## Exceções

Não há análise administrativa implementada neste cadastro. A consulta de dados cadastrais não aprova a organização nem seus documentos. O tratamento de erros e os mecanismos de validação e armazenamento estão em RF-019, UC-017 e na documentação de arquitetura.

## Histórico de Alterações

| Versão | Data | Autor | Descrição |
|--------|------|-------|-----------|
| 1.0 | 2026-10-09 | Hiel Saraiva | Dados oficiais, unicidade, credenciais e documentos institucionais |
| 1.1 | 2026-10-10 | Hiel Saraiva | Separação de política, mensagens do formulário e armazenamento técnico |
