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

O arquivo `.env` não é versionado. O `.env.example` serve como referência dos campos obrigatórios.

### 2. Subir a aplicação

O projeto utiliza o `spring-boot-docker-compose`, que sobe automaticamente os serviços definidos no `compose.yaml` ao iniciar a aplicação. Basta garantir que o Docker esteja em execução.

```bash
./mvnw spring-boot:run
```

Os seguintes serviços serão iniciados automaticamente:

| Serviço    | Imagem                        | Porta  |
|------------|-------------------------------|--------|
| PostgreSQL | `postgres:18.6`               | 5432   |
| RabbitMQ   | `rabbitmq:4.3.5-management`   | 5672   |

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

| Artefato                | Localização                  |
|-------------------------|------------------------------|
| Regras de negócio       | `docs/regras-de-negocio/`    |
| Casos de uso            | `docs/casos-de-uso/`         |
| Arquitetura             | `docs/arquitetura/`          |

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