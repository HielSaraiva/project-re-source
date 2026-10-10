# RN-001 - Autenticação e autorização por perfil

## Identificação

| Campo       | Valor              |
|-------------|--------------------|
| **ID**      | RN-001             |
| **Nome**    | Autenticação e autorização por perfil |
| **Versão**  | 1.4                |
| **Status**  | Ativa |

## Descrição

O acesso às funcionalidades depende das credenciais, do perfil e da situação da conta.

## Contexto

Autenticação e acesso às áreas de doador e ONG.

## Regra

- O e-mail é consultado sem distinção entre letras maiúsculas e minúsculas.
- O doador deve possuir perfil de doador e conta ativa para acessar sua área.
- A ONG deve estar aprovada para acessar sua área.
- Um e-mail encontrado simultaneamente como usuário e instituição impede a autenticação por e-mail.
- O CNPJ identifica exclusivamente uma instituição. A autenticação aceita 14 caracteres, numéricos ou alfanuméricos nas 12 primeiras posições e dois dígitos verificadores, com ou sem máscara. O identificador é normalizado sem pontuação e com letras maiúsculas. O acesso exige instituição aprovada.
- O login Google é exclusivo de doadores ativos e exige e-mail verificado pelo provedor. Uma identidade nova cria um doador ativo; a identidade existente é localizada pelo provedor e identificador de usuário, mantendo o e-mail local da conta.
- Um e-mail coincidente não vincula automaticamente Google a uma conta local. Conflitos com outra conta ou instituição impedem o acesso Google.
- Selecionar um perfil na tela não concede permissões: o acesso depende do perfil e da situação da conta persistida.
- As operações e consultas por protocolo são restritas ao proprietário da doação ou à instituição destinatária.

## Exceções

A página inicial, login, cadastros, consulta pública de CNPJ, recuperação e redefinição de senha, recursos estáticos e página de erro são públicos. O login Google habilitado também permite iniciar e retornar da autenticação sem sessão anterior. O sistema pode processar expiração e notificações sem uma conta interativamente autenticada.

A política de recuperação está em RN-019. As notificações respeitam a propriedade definida em RN-020. O papel administrativo existe no modelo, mas não há fluxo administrativo de aprovação implementado.

## Histórico de Alterações

| Versão | Data       | Autor  | Descrição          |
|--------|------------|--------|--------------------|
| 1.0    | 2026-10-09 | Hiel Saraiva | Criação do documento |
| 1.1    | 2026-10-09 | Hiel Saraiva | Separação entre política de negócio e comportamento funcional |
| 1.2    | 2026-10-09 | Hiel Saraiva | Política de autenticação Google e seleção de perfil |
| 1.3    | 2026-10-09 | Hiel Saraiva | Identificação da instituição pelo CNPJ |
| 1.4 | 2026-10-10 | Hiel Saraiva | Políticas de acesso sem detalhes de sessão e inclusão dos fluxos públicos |
