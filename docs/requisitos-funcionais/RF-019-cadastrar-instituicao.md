# RF-019 - Cadastrar instituição

## Identificação

| Campo | Valor |
|-------|-------|
| **ID** | RF-019 |
| **Nome** | Cadastrar instituição |
| **Prioridade** | Alta |
| **Versão** | 1.0 |
| **Status** | Implementado |

## Descrição

O sistema deve oferecer a página pública `/register/institution` para solicitar a verificação de uma instituição. O formulário deve apresentar CNPJ, razão social consultada automaticamente, e-mail oficial, nome completo e CPF do representante legal, senha, confirmação de senha e os anexos de identidade e documentação da organização.

A consulta pública `GET /register/institution/cnpj?cnpj=...` deve validar o CNPJ e retornar sua razão social oficial quando ativo. O servidor deve repetir a consulta no envio, sem confiar na razão social ou situação enviadas pelo navegador. Deve aceitar CNPJ numérico e alfanumérico conforme RN-017.

O cadastro deve persistir a instituição pendente de aprovação e dois documentos pendentes, armazenar a senha como hash e redirecionar ao login institucional com confirmação e orientação de aguardar aprovação. O formulário deve reutilizar os componentes e comportamentos das demais telas de autenticação.

## Atores

- Visitante que representa legalmente uma instituição.

## Entradas e Saídas

### Entradas

- CNPJ ativo, e-mail válido (até 320 caracteres), representante (até 200 caracteres) e CPF válido.
- Senha e confirmação conforme os limites compartilhados de RN-016.
- Documento de identidade e estatuto social ou comprovante, em PDF ou JPG, até 5 MB cada.

### Saídas

- Conta institucional `pending_approval` e dois documentos `pending`.
- Confirmação na página de login institucional, sem autenticação automática.
- Mensagens para dados inválidos, duplicados, consulta indisponível e anexos inválidos ou excessivos.
- Rejeição de requisições sem CSRF válido.

## Limites

A análise administrativa dos documentos e a decisão de aprovação são um fluxo posterior. Esta tela apenas solicita a verificação; contas não aprovadas continuam impedidas de entrar por RF-001.

## Histórico de Alterações

| Versão | Data | Autor | Descrição |
|--------|------|-------|-----------|
| 1.0 | 2026-10-09 | Codex | Cadastro institucional, consulta de CNPJ e documentos privados |
