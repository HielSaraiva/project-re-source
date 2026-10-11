# Project ReSource

O **ReSource** conecta recursos em bom estado às necessidades de Organizações Não Governamentais (ONGs). Doadores podem cadastrar itens, propor doações e acompanhar cada etapa até a confirmação de recebimento. Instituições podem solicitar cadastro, consultar propostas e acompanhar as entregas. O cadastro institucional consulta dados cadastrais pela BrasilAPI e recebe documentos privados para análise; o acesso da instituição depende de aprovação.

A rota `/` apresenta publicamente a proposta da plataforma, seu funcionamento e as opções de participação. Visitantes podem seguir para login ou cadastro; contas autenticadas continuam sendo direcionadas diretamente ao painel do perfil. O login permanece em `/login`.

As páginas `/sustentabilidade` e `/politica-de-privacidade` ficam disponíveis sem login. A apresentação da plataforma também pode ser consultada em `/sobre`, inclusive por contas autenticadas. Consulte o [guia das páginas institucionais](docs/guias/paginas-institucionais.md) para atualizar o contato de privacidade e o conteúdo.

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
