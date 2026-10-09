# Autenticação e cadastro

## Responsabilidades

- `SecurityConfiguration`: rotas públicas, permissões, CSRF, login por formulário, HTTP Basic, logout e integração OAuth opcional.
- `DatabaseUserDetailsService`: busca por e-mail ou CNPJ, compatibilidade com hashes bcrypt dos seeds e situação/perfil da conta. Um e-mail ambíguo entre usuário e instituição não autentica.
- `GoogleOidcUserService` e `GoogleAccountService`: identidade OpenID Connect com e-mail verificado, provisionamento de doador e proteção contra vinculação automática a uma conta local. A sessão mantém o e-mail local como identificador.
- Controllers de cadastro: campos permitidos, validação do formulário e apresentação dos erros. Perfil, aprovação e razão social oficial não são controlados pelo cliente.
- Serviços de cadastro: validação de DTOs também em chamadas diretas, unicidade transacional, criação da conta e armazenamento da senha com o encoder compartilhado.
- `AccountEmailRegistry`: bloqueio transacional por e-mail, compartilhado entre cadastro de doador, instituição e provisionamento Google, seguido da consulta nas duas tabelas. Os índices únicos permanecem como proteção adicional.
- `InstitutionDocumentStorage`: conferência básica de formato/tamanho, nomes aleatórios, arquivos privados e remoção dos arquivos recém-gravados quando a transação não confirma.

## Validações compartilhadas

`PasswordPolicy` define a expressão usada tanto pelo Bean Validation quanto pelo HTML, os limites e as mensagens. O JavaScript usa o padrão renderizado pelo backend e o limite de bytes enviado no campo. A senha exige de 8 a 72 caracteres, maiúscula, minúscula, número e símbolo; espaços não contam como símbolo. O limite de 72 bytes UTF-8 evita truncamento pelo bcrypt. O mínimo considera caracteres Unicode, sem contar pares substitutos como dois caracteres.

`@StrongPassword` verifica formato e bytes; `@MatchingPasswords` verifica a confirmação e associa o erro ao campo correspondente. Essas regras são herdadas pelos dois DTOs. Não são aplicadas ao login de contas existentes nem ao provisionamento Google.

`@BrazilianDocument` reutiliza os verificadores de CPF/CNPJ e permite CNPJ alfanumérico. Os DTOs normalizam pontuação e letras antes da validação. Os serviços usam `@Validated` e parâmetros `@Valid`, sem depender exclusivamente da validação nos controllers.

As telas reutilizam os fragmentos de marca, abas, estilos, ícones, senhas, uploads e opção Google. A troca de perfil mantém o mesmo documento, preserva os formulários e usa os URLs públicos no histórico. O envio continua sendo um POST protegido por CSRF.

## Cadastro institucional

A consulta de CNPJ ocorre antes da transação de persistência. No comportamento atual, a BrasilAPI é obrigatória e o CNPJ deve estar ativo; falhas da consulta não criam conta. A aprovação administrativa continua sendo uma etapa separada.

A instituição inicia como `pending_approval`, com dois documentos `pending`. O banco guarda metadados e a chave dos arquivos; o conteúdo fica no diretório privado configurado por `INSTITUTION_DOCUMENT_DIRECTORY`. O limite por arquivo é compartilhado com o formulário; o parser também limita o tamanho total da requisição. O diretório deve ser persistente e incluído nos backups junto ao banco.

## Verificação da revisão

Foram exercitados em banco e diretório temporários:

- Login por e-mail e CNPJ, inclusive alfanumérico; senha incorreta, contas bloqueadas, pendentes, Google sem senha local e e-mail ambíguo.
- Permissões entre doador e ONG, logout e rejeição de POST sem CSRF válido.
- Cadastro, confirmação de senha, complexidade, limite UTF-8, campos adicionais enviados pelo cliente e validação em chamadas diretas aos serviços.
- CNPJ/CPF inválidos, consulta indisponível, CNPJ inativo e identificadores duplicados.
- Upload inválido, limite do parser, armazenamento privado, persistência dos dois documentos e remoção dos arquivos em rollback.
- Cadastro simultâneo de doador e instituição com o mesmo e-mail.
- Provisionamento Google com identidade simulada: criação/reuso, e-mail verificado, conflitos e bloqueio. Redirecionamento OAuth com estado, nonce e callback. Isso não equivale ao login completo em uma conta Google real.
- Renderização e interação no Chrome em desktop e celular, alternância de perfis, erros, ícones e controles de senha.

Não foram adicionados testes automatizados ao repositório.

## Limites atuais

- A tela administrativa e os endpoints de análise/aprovação e acesso autorizado aos documentos ainda não foram implementados. O administrador também ainda não tem painel próprio de destino após o login.
- A conferência de PDF usa assinatura e marcador de encerramento; JPG usa assinatura e dimensões. Não há análise documental, parsing completo de PDF ou antivírus.
- A remoção dos arquivos em rollback depende da conclusão da transação no processo em execução. Banco e sistema de arquivos não constituem uma única transação distribuída; interrupções abruptas podem deixar arquivos órfãos.
- Google exige cliente OAuth configurado no ambiente; recuperação de senha continua indisponível.
