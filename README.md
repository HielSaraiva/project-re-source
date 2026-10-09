# Project ReSource

O **ReSource** é uma plataforma web desenvolvida para facilitar o cadastro, a gestão e a validação de Organizações Não Governamentais (ONGs). O sistema fornece um ambiente seguro onde as ONGs podem se registrar, validar seus dados jurídicos automaticamente (via integração com a BrasilAPI) e submeter documentos institucionais sensíveis. A plataforma centraliza essas informações para otimizar processos burocráticos e estabelecer uma base confiável para futuras interações de apoio, logística e doações.

---

## Tecnologias

| Tecnologia         | Versão     |
|--------------------|------------|
| Java               | 25         |
| Spring Boot        | 4.1.1      |
| Spring Security    | —          |
| Spring Data JPA    | —          |
| Flyway             | —          |
| PostgreSQL         | 18.6       |
| RabbitMQ           | 4.3.5      |
| Thymeleaf          | —          |
| SpringDoc OpenAPI  | 3.1.0      |
| Lombok             | —          |
| Testcontainers     | —          |
| JaCoCo             | 0.8.13     |
| PIT (Pitest)       | 1.22.1     |

---

## Pré-requisitos

- [Java 25](https://www.oracle.com/java/technologies/downloads/)
- [Docker](https://www.docker.com/) e Docker Compose

---

## Executando a Aplicação

### 1. Configurar as variáveis de ambiente

Copie o arquivo de exemplo e ajuste os valores conforme necessário:

```bash
cp .env.example .env
```

O arquivo `.env` não é versionado. O `.env.example` serve como referência dos campos obrigatórios. O Spring carrega o `.env` do diretório de execução automaticamente; execute o projeto a partir da raiz do repositório ou configure esse diretório na IDE. Use entradas `CHAVE=valor`, sem `export` e sem aspas envolvendo os valores. Variáveis de ambiente da IDE ou do servidor têm prioridade sobre o arquivo. Reinicie a aplicação após alterar a configuração.

### 2. Subir a aplicação

O projeto utiliza o `spring-boot-docker-compose`, que sobe automaticamente os serviços definidos no `compose.yaml` ao iniciar a aplicação. Basta garantir que o Docker esteja em execução.

```bash
./mvnw spring-boot:run
```

Os seguintes serviços serão iniciados automaticamente:

| Serviço    | Imagem                        | Porta  |
|------------|-------------------------------|--------|
| PostgreSQL | `postgres:18.6-alpine`               | 5432   |
| RabbitMQ   | `rabbitmq:4.3.5-management-alpine`   | 5672   |

A interface de gerenciamento do RabbitMQ estará disponível em `http://localhost:15672`.

---

## Autenticação

Acesse `/login` para entrar como doador com e-mail e senha, ou `/login?profile=ong` para entrar como instituição com CNPJ e senha. O CNPJ pode ser informado com ou sem máscara. O login institucional por e-mail continua disponível no backend. O sistema verifica a situação da conta e direciona doadores ao painel do doador e instituições aprovadas ao painel da ONG. As abas identificam o perfil esperado; o backend obtém as permissões da conta persistida. HTTP Basic continua disponível para integrações. O menu do perfil permite sair por POST protegido por CSRF.

Para habilitar o login de doadores com Google, crie um cliente OAuth do tipo **Aplicativo da Web** no Google Cloud e configure a URI de redirecionamento `http://localhost:8080/login/oauth2/code/google` (em produção, use o domínio HTTPS). Preencha `GOOGLE_LOGIN_ENABLED=true`, `GOOGLE_CLIENT_ID` e `GOOGLE_CLIENT_SECRET` no `.env` da raiz do projeto e reinicie a aplicação. Também é possível configurar essas variáveis no ambiente de execução da IDE ou do servidor.

O login Google usa OpenID Connect, exige e-mail verificado e cria uma conta de doador ativo no primeiro acesso. Contas locais com o mesmo e-mail não são vinculadas automaticamente: devem continuar usando senha. Contas bloqueadas, perfis administrativos e e-mails em conflito com instituições são rejeitados. Sem as credenciais Google, o botão aparece indisponível e o login local permanece funcional.

O cadastro local de doadores está disponível em `/register/donor`, com nome, e-mail, senha e confirmação. Nome e e-mail são normalizados; e-mails utilizados por usuários ou instituições são rejeitados. A senha deve ter de 8 a 72 caracteres, conter pelo menos uma letra maiúscula, uma minúscula, um número e um caractere especial (espaço não conta como especial), e ter até 72 bytes UTF-8 e é armazenada com o PasswordEncoder compartilhado. O cadastro cria um doador ativo e redireciona ao login com confirmação, sem autenticar automaticamente. CPF e cidade não são obrigatórios no modelo atual.

O cadastro institucional está disponível em `/register/institution`, com CNPJ, e-mail oficial, nome e CPF do representante, senha, confirmação e dois anexos obrigatórios (identidade e documentação da organização). A razão social é obtida pela [BrasilAPI](https://brasilapi.com.br/docs#tag/CNPJ) e conferida novamente pelo servidor no envio. O cadastro exige CNPJ ativo, valida os dígitos verificadores de CNPJ e CPF e aceita CNPJ numérico ou alfanumérico, com ou sem máscara. CNPJ, e-mail e CPF do representante não podem estar cadastrados. A criação de contas compartilha a proteção contra e-mails duplicados entre usuários, instituições e Google, inclusive em requisições simultâneas.

Os documentos aceitam PDF ou JPG de até 5 MB cada, com conferência do conteúdo, e são armazenados fora dos arquivos públicos. A conta é criada como `pending_approval`, com dois documentos `pending`, e não pode entrar até ser aprovada. A tela administrativa de análise/aprovação não faz parte deste cadastro. Senhas e anexos devem ser informados novamente após erro; falhas na transação removem os arquivos recém-gravados.

Configure `INSTITUTION_DOCUMENT_DIRECTORY` com um diretório privado e persistente, gravável pelo processo da aplicação; inclua esse diretório nos backups junto ao banco. O padrão é `var/private/institution-documents`. `BRASIL_API_URL` permite configurar a base da consulta (padrão `https://brasilapi.com.br/api`). Essas configurações podem ser preenchidas no `.env` ou no ambiente de execução. Uma falha da BrasilAPI impede a criação da conta e permite nova tentativa pelo formulário.

A recuperação de senha ainda não está disponível; sua ação permanece desabilitada.

As responsabilidades, validações compartilhadas, verificações e limites atuais estão descritos em [Autenticação e cadastro](docs/arquitetura/autenticacao-e-cadastro.md).

---

## Executando os Testes

### Testes unitários e de integração

O comando abaixo executa os testes unitários (Surefire), os testes de integração (Failsafe) com contêineres isolados via Testcontainers, e gera o relatório de cobertura de código com JaCoCo:

```bash
./mvnw verify
```

O relatório de cobertura será gerado em:

```
target/site/jacoco/index.html
```

### Testes de mutação

```bash
./mvnw pitest:mutationCoverage
```

O relatório de mutação será gerado em:

```
target/pit-reports/
```

---

## Documentação da API

Com a aplicação em execução, a documentação interativa estará disponível em:

- **Swagger UI:** `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON:** `http://localhost:8080/v3/api-docs`

---

## Documentação do Projeto

A documentação de negócio e arquitetura está centralizada na pasta [`docs/`](docs/README.md):

| Artefato                | Localização                     |
|-------------------------|---------------------------------|
| Requisitos funcionais   | `docs/requisitos-funcionais/`   |
| Regras de negócio       | `docs/regras-de-negocio/`       |
| Casos de uso            | `docs/casos-de-uso/`            |
| Arquitetura             | `docs/arquitetura/`             |

---

## Estrutura do Projeto

```
resource-system/
├── src/
│   ├── main/
│   │   ├── java/          # Código-fonte principal
│   │   └── resources/     # Configurações e migrações Flyway
│   └── test/
│       ├── java/          # Testes unitários e de integração
│       └── resources/     # Configurações de teste
├── docs/                  # Documentação de negócio e arquitetura
├── compose.yaml           # Definição dos serviços Docker
└── pom.xml                # Configuração do Maven
```
