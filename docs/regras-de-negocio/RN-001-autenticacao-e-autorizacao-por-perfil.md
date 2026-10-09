# RN-001 - Autenticação e autorização por perfil

## Identificação

| Campo       | Valor              |
|-------------|--------------------|
| **ID**      | RN-001             |
| **Nome**    | Autenticação e autorização por perfil |
| **Versão**  | 1.3                |
| **Status**  | Ativa |

## Descrição

O acesso às funcionalidades depende das credenciais, do perfil e da situação da conta.

## Contexto

Autenticação e acesso às áreas de doador e ONG.

## Regra

- O e-mail é consultado sem distinção entre letras maiúsculas e minúsculas.
- O doador deve possuir perfil de doador, conta ativa e autoridade de doador na autenticação.
- A ONG deve estar aprovada e possuir autoridade de ONG na autenticação.
- Um e-mail encontrado simultaneamente como usuário e instituição impede a autenticação por e-mail.
- O CNPJ identifica exclusivamente uma instituição. A autenticação aceita 14 caracteres, numéricos ou alfanuméricos nas 12 primeiras posições e dois dígitos verificadores, com ou sem máscara. Consulta o valor sem pontuação, com letras maiúsculas, e mantém o e-mail institucional como identificador da sessão para os serviços existentes. O acesso exige instituição aprovada.
- O login Google é exclusivo de doadores ativos e exige e-mail verificado pelo provedor. Uma identidade nova cria um doador ativo; a identidade existente é localizada pelo provedor e identificador de usuário, mantendo o e-mail local da conta.
- Um e-mail coincidente não vincula automaticamente Google a uma conta local. Conflitos com outra conta ou instituição impedem o acesso Google.
- As abas da tela de login não concedem permissões: o perfil e as autoridades são obtidos da conta persistida.
- As operações e consultas por protocolo são restritas ao proprietário da doação ou à instituição destinatária.

## Exceções

Recursos estáticos, a página de login e a página de erro possuem acesso público. O sistema pode processar expiração sem uma conta autenticada.

## Histórico de Alterações

| Versão | Data       | Autor  | Descrição          |
|--------|------------|--------|--------------------|
| 1.0    | 2026-10-09 | Hiel Saraiva | Criação do documento |
| 1.1    | 2026-10-09 | Hiel Saraiva | Separação entre política de negócio e comportamento funcional |
| 1.2    | 2026-10-09 | Codex | Política de autenticação Google e seleção de perfil |
| 1.3    | 2026-10-09 | Codex | Identificação da instituição pelo CNPJ |
