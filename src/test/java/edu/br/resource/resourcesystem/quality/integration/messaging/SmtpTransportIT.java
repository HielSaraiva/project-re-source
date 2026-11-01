package edu.br.resource.resourcesystem.quality.integration.messaging;

import static org.assertj.core.api.Assertions.*;

import edu.br.resource.resourcesystem.config.PasswordRecoveryProperties;
import edu.br.resource.resourcesystem.quality.support.QualityIntegrationSupport;
import edu.br.resource.resourcesystem.service.mail.BrandedMailService;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;

@DisplayName("RF-022 — SMTP real contra servidor local fictício, sem credenciais externas")
class SmtpTransportIT extends QualityIntegrationSupport {
    @Autowired TemplateEngine templates;
    @Autowired PasswordRecoveryProperties settings;

    /** Minimal SMTP peer: accepts exactly one connection, never forwards messages. */
    static class LocalSmtp implements AutoCloseable {
        final ServerSocket server;
        final CompletableFuture<String> received = new CompletableFuture<>();

        LocalSmtp(boolean reject) throws IOException {
            server = new ServerSocket(0, 1, InetAddress.getLoopbackAddress());
            Thread.startVirtualThread(
                    () -> {
                        try (Socket socket = server.accept()) {
                            socket.setSoTimeout(3000);
                            var input =
                                    new BufferedReader(
                                            new InputStreamReader(
                                                    socket.getInputStream(),
                                                    StandardCharsets.UTF_8));
                            var output =
                                    new PrintWriter(
                                            new OutputStreamWriter(
                                                    socket.getOutputStream(),
                                                    StandardCharsets.UTF_8),
                                            true);
                            reply(output, "220 local.test ESMTP");
                            String line;
                            while ((line = input.readLine()) != null) {
                                if (line.startsWith("EHLO") || line.startsWith("HELO"))
                                    reply(output, "250 local.test");
                                else if (line.startsWith("RCPT TO") && reject)
                                    reply(output, "550 Fictitious rejection");
                                else if (line.equals("DATA")) {
                                    reply(output, "354 End with dot");
                                    var data = new StringBuilder();
                                    while ((line = input.readLine()) != null && !line.equals("."))
                                        data.append(line).append("\r\n");
                                    received.complete(data.toString());
                                    reply(output, "250 Accepted locally");
                                } else if (line.equals("QUIT")) {
                                    reply(output, "221 Closing");
                                    break;
                                } else reply(output, "250 OK");
                            }
                        } catch (Exception e) {
                            received.completeExceptionally(e);
                        }
                    });
        }

        void reply(PrintWriter writer, String message) {
            writer.print(message + "\r\n");
            writer.flush();
        }

        @Override
        public void close() throws IOException {
            server.close();
        }
    }

    BrandedMailService sender(LocalSmtp smtp) {
        var sender = new JavaMailSenderImpl();
        sender.setHost("127.0.0.1");
        sender.setPort(smtp.server.getLocalPort());
        var properties = new Properties();
        properties.setProperty("mail.smtp.auth", "false");
        properties.setProperty("mail.smtp.starttls.enable", "false");
        properties.setProperty("mail.smtp.connectiontimeout", "1000");
        properties.setProperty("mail.smtp.timeout", "3000");
        properties.setProperty("mail.smtp.writetimeout", "3000");
        sender.setJavaMailProperties(properties);
        var beans = new StaticListableBeanFactory(Map.of("localSmtp", sender));
        return new BrandedMailService(
                beans.getBeanProvider(JavaMailSender.class), templates, settings);
    }

    Context context() {
        var context = new Context(Locale.forLanguageTag("pt-BR"));
        context.setVariables(
                Map.of(
                        "name",
                        "Pessoa fictícia",
                        "title",
                        "Evento de teste",
                        "message",
                        "Mensagem fictícia",
                        "targetUrl",
                        "http://127.0.0.1:8080/donor/dashboard",
                        "actionLabel",
                        "Consultar"));
        return context;
    }

    @Test
    void send_WithLocalSmtp_DeliversBrandedMultipartOnlyToFakeRecipient() throws Exception {
        try (var smtp = new LocalSmtp(false)) {
            sender(smtp)
                    .send(
                            "recipient@example.test",
                            "Evento de teste",
                            "Texto simples",
                            "email/notification",
                            context());
            String raw = smtp.received.get(5, TimeUnit.SECONDS);
            var message =
                    new MimeMessage(
                            Session.getInstance(new Properties()),
                            new ByteArrayInputStream(raw.getBytes(StandardCharsets.UTF_8)));
            assertThat(message.getAllRecipients()[0].toString())
                    .isEqualTo("recipient@example.test");
            assertThat(message.getSubject()).isEqualTo("Evento de teste");
            assertThat(message.isMimeType("multipart/*")).isTrue();
            assertThat(raw).contains("resource-logo");
        }
    }

    @Test
    void send_WithRejectedRecipient_PropagatesSafeTransportFailure() throws Exception {
        try (var smtp = new LocalSmtp(true)) {
            assertThatThrownBy(
                            () ->
                                    sender(smtp)
                                            .send(
                                                    "recipient@example.test",
                                                    "Teste",
                                                    "Texto",
                                                    "email/notification",
                                                    context()))
                    .isInstanceOf(MailSendException.class);
        }
    }
}
