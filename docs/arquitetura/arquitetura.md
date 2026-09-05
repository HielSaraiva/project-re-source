# Arquitetura do Sistema

## Estilo Arquitetural

O projeto adota o estilo de um Monólito Modular baseado no padrão MVC (Model-View-Controller) clássico, com Server-Side Rendering (SSR).

## Diagrama de Componentes

```mermaid
flowchart TD
    %% Estilização UML
    classDef component fill:#e9ecef,stroke:#495057,stroke-width:2px,color:#212529;
    classDef infra fill:#0052CC,stroke:#fff,stroke-width:2px,color:#fff;
    classDef external fill:#FF9900,stroke:#fff,stroke-width:2px,color:#fff;
    classDef actor fill:#d4edda,stroke:#28a745,stroke-width:2px,color:#155724;

    Browser((Navegador Web)):::actor

    subgraph ReSourceApp [«System» Aplicação ReSource]
        direction TB
        Web["«Component»<br/>Web (Controllers)"]:::component
        View["«Component»<br/>Thymeleaf (Views)"]:::component
        Business["«Component»<br/>Business Logic (Services)"]:::component
        DataAccess["«Component»<br/>Data Access (JPA / Hibernate)"]:::component
        FileStorage["«Component»<br/>File Management (NIO)"]:::component
        Messaging["«Component»<br/>Async Messaging (AMQP)"]:::component
    end

    subgraph Infraestrutura [Infraestrutura Local / Docker]
        Postgres["«Component»<br/>PostgreSQL"]:::infra
        RabbitMQ["«Component»<br/>RabbitMQ"]:::infra
        Volume["«Component»<br/>Docker Volume"]:::infra
    end

    subgraph ServicosExternos [APIs Externas]
        BrasilAPI["«Component»<br/>BrasilAPI"]:::external
        SMTP["«Component»<br/>Servidor SMTP"]:::external
    end

    %% Relacionamentos de Dependência (UML: <<use>>)
    Browser -. "«use» HTTP" .-> Web
    Web -. "renderiza" .-> View
    Web -. "«use»" .-> Business
    
    Business -. "«use»" .-> DataAccess
    Business -. "«use»" .-> FileStorage
    Business -. "«use»" .-> Messaging
    
    %% Interfaces com Banco e Arquivos
    DataAccess -. "«use» JDBC" .-> Postgres
    FileStorage -. "«use» I/O" .-> Volume
    
    %% Interfaces de Mensageria e APIs
    Business -. "«use» HTTP/REST" .-> BrasilAPI
    Messaging -. "«use» AMQP" .-> RabbitMQ
    Messaging -. "«use» SMTP" .-> SMTP
```

## Histórico de Alterações

| Versão | Data       | Autor                 | Descrição            |
|--------|------------|-----------------------|----------------------|
| 1.0    | 2026-09-05 | Hiel Saraiva          | Criação do documento |