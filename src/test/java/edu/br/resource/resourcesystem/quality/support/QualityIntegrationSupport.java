package edu.br.resource.resourcesystem.quality.support;

import edu.br.resource.resourcesystem.*;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.rabbitmq.RabbitMQContainer;

import java.nio.file.*;
import java.util.*;

@SpringBootTest(
        classes = ResourceSystemApplication.class,
        properties = "spring.config.location=classpath:quality-test.properties")
@AutoConfigureMockMvc
public abstract class QualityIntegrationSupport {
    protected static final PostgreSQLContainer POSTGRES =
            new PostgreSQLContainer("postgres:18.6")
                    .withDatabaseName("quality_only")
                    .withUsername("quality")
                    .withPassword("fake-quality-password");
    protected static final RabbitMQContainer RABBIT =
            new RabbitMQContainer("rabbitmq:4.3.5-management")
                    .withAdminUser("quality")
                    .withAdminPassword("fake-quality-password");
    protected static final Path DOCUMENTS;

    static {
        try {
            DOCUMENTS = Files.createTempDirectory("resource-quality-documents-");
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
        POSTGRES.start();
        RABBIT.start();
    }

    @DynamicPropertySource
    protected static void isolatedServices(DynamicPropertyRegistry p) {
        p.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        p.add("spring.datasource.username", POSTGRES::getUsername);
        p.add("spring.datasource.password", POSTGRES::getPassword);
        p.add("spring.rabbitmq.host", RABBIT::getHost);
        p.add("spring.rabbitmq.port", RABBIT::getAmqpPort);
        p.add("spring.rabbitmq.username", RABBIT::getAdminUsername);
        p.add("spring.rabbitmq.password", RABBIT::getAdminPassword);
        p.add("resource.registration.document-directory", DOCUMENTS::toString);
        p.add("resource.registration.brasil-api-url", () -> "http://127.0.0.1:1/never-external");
    }

    @MockitoBean protected TaskScheduler taskScheduler;
    @MockitoBean protected JavaMailSender mailSender;

    @MockitoBean
    protected edu.br.resource.resourcesystem.service.auth.CnpjLookupService companyLookup;

    @Autowired protected JdbcTemplate db;
    @Autowired protected MockMvc mvc;
    @Autowired protected TransactionTemplate transaction;
    @Autowired protected PasswordEncoder encoder;
    @Autowired protected org.springframework.amqp.rabbit.core.RabbitAdmin rabbitAdmin;

    @Autowired
    protected org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry listeners;

    protected final java.util.List<jakarta.mail.internet.MimeMessage> outgoing =
            new java.util.concurrent.CopyOnWriteArrayList<>();

    @BeforeEach
    protected void resetDisposableDatabase() {
        listeners.stop();
        rabbitAdmin.initialize();
        rabbitAdmin.purgeQueue(
                edu.br.resource.resourcesystem.config.MessagingConfiguration.QUEUE, false);
        rabbitAdmin.purgeQueue(
                edu.br.resource.resourcesystem.config.MessagingConfiguration.QUEUE + ".dead",
                false);
        outgoing.clear();
        org.mockito.Mockito.when(companyLookup.lookup(org.mockito.ArgumentMatchers.anyString()))
                .thenAnswer(
                        i ->
                                new edu.br.resource.resourcesystem.service.auth.CnpjLookupService
                                        .Company(
                                        edu.br.resource.resourcesystem.validation.BrazilianDocuments
                                                .normalizeCnpj(i.getArgument(0)),
                                        "Organização fictícia"));
        org.mockito.Mockito.doAnswer(
                        invocation -> {
                            var message =
                                    new jakarta.mail.internet.MimeMessage(
                                            jakarta.mail.Session.getInstance(new Properties()));
                            ((org.springframework.mail.javamail.MimeMessagePreparator)
                                            invocation.getArgument(0))
                                    .prepare(message);
                            message.saveChanges();
                            outgoing.add(message);
                            return null;
                        })
                .when(mailSender)
                .send(
                        org.mockito.ArgumentMatchers.any(
                                org.springframework.mail.javamail.MimeMessagePreparator.class));
        // Dynamic tables belong exclusively to the container above; Flyway history is retained.
        db.execute(
                "TRUNCATE TABLE users,institutions,item_types,states,open_days RESTART IDENTITY"
                        + " CASCADE");
        db.execute("TRUNCATE TABLE password_reset_rate_limits");
        org.mockito.Mockito.when(mailSender.createMimeMessage())
                .thenAnswer(
                        i ->
                                new jakarta.mail.internet.MimeMessage(
                                        jakarta.mail.Session.getInstance(new Properties())));
    }

    protected int createUser(String name) {
        return db.queryForObject(
                "INSERT INTO users(full_name,email,password_hash) VALUES(?,?,?) RETURNING id",
                Integer.class,
                name,
                name + "@example.test",
                encoder.encode("Quality12!"));
    }

    protected int institution(String name) {
        int id =
                db.queryForObject(
                        "INSERT INTO"
                            + " institutions(cnpj,legal_name,email,password_hash,representative_full_name,representative_cpf,status)"
                            + " VALUES(?,?,?,?,?,?,'approved') RETURNING id",
                        Integer.class,
                        String.format("%014d", 100 + Math.abs(name.hashCode())),
                        name,
                        name + "@example.test",
                        encoder.encode("Quality12!"),
                        "Responsável fictício",
                        String.format("%011d", 100 + Math.abs(name.hashCode())));
        int state =
                db.queryForObject(
                        "INSERT INTO states(name,abbreviation) VALUES('Estado de teste','TT') ON"
                                + " CONFLICT(name) DO UPDATE SET name=excluded.name RETURNING id",
                        Integer.class);
        int city =
                db.queryForObject(
                        "INSERT INTO cities(state_id,name) VALUES(?,'Cidade de teste') ON CONFLICT"
                            + " ON CONSTRAINT uq_cities_state_name DO UPDATE SET name=excluded.name"
                            + " RETURNING id",
                        Integer.class,
                        state);
        db.update(
                "INSERT INTO"
                    + " institution_addresses(institution_id,city_id,cep,street,number,district)"
                    + " VALUES(?,?,'00000000','Rua de teste','1','Centro')",
                id,
                city);
        int days =
                db.queryForObject(
                        "insert into open_days(monday,wednesday,friday) values(true,true,true) on"
                            + " conflict(monday,tuesday,wednesday,thursday,friday,saturday,sunday)"
                            + " do update set monday=excluded.monday returning id",
                        Integer.class);
        db.update(
                "insert into"
                    + " business_hours(institution_id,open_days_id,responsible_name,opening_hour,closing_hour)"
                    + " values(?,?,?,'08:00','17:00')",
                id,
                days,
                "Recebedor fictício");
        return id;
    }

    protected long count(String table) {
        return db.queryForObject("select count(*) from " + table, Long.class);
    }
}
