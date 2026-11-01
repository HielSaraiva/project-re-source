package edu.br.resource.resourcesystem.quality.unit.validation;

import static org.assertj.core.api.Assertions.*;

import edu.br.resource.resourcesystem.service.auth.*;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.support.*;

import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

import javax.imageio.ImageIO;

@DisplayName("RN-017 — documentos privados, formato, limite e rollback")
class DocumentStorageRulesTest {
    @TempDir Path directory;

    byte[] pdf() {
        return "%PDF-1.7\nFictitious document\n%%EOF".getBytes(StandardCharsets.US_ASCII);
    }

    InstitutionDocumentStorage storage() {
        return new InstitutionDocumentStorage(directory.toString());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(
            strings = {
                "",
                "bad.pdf",
                "../folder/document.pdf",
                "C:\\folder\\document.pdf",
                "\u0000"
            })
    void validate_WithPdfName_SanitizesWithoutTrustingExtension(String name) {
        var file = new MockMultipartFile("identity", name, "application/unknown", pdf());
        var upload = storage().validate(file, "identity");
        assertThat(upload.contentType()).isEqualTo("application/pdf");
        assertThat(upload.originalName()).doesNotContain("/", "\\", "\u0000");
    }

    @Test
    void validate_WithLongName_LimitsTo255() {
        assertThat(
                        storage()
                                .validate(
                                        new MockMultipartFile("x", "a".repeat(260), "", pdf()),
                                        "identity")
                                .originalName())
                .hasSize(255);
    }

    @Test
    void validate_WithEmptyUpload_RejectsField() {
        assertThatThrownBy(() -> storage().validate(null, "identity"))
                .isInstanceOf(RegistrationFieldException.class);
        assertThatThrownBy(
                        () ->
                                storage()
                                        .validate(
                                                new MockMultipartFile("x", new byte[0]),
                                                "organization"))
                .isInstanceOf(RegistrationFieldException.class);
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "not a pdf",
                "%PDF-incomplete",
                "%PDF-1.7 but no final marker",
                "fake image"
            })
    void validate_WithSpoofedContent_Rejects(String text) {
        assertThatThrownBy(
                        () ->
                                storage()
                                        .validate(
                                                new MockMultipartFile(
                                                        "x",
                                                        "valid.pdf",
                                                        "application/pdf",
                                                        text.getBytes()),
                                                "identity"))
                .isInstanceOf(RegistrationFieldException.class);
    }

    @Test
    void validate_WithOversizedFile_Rejects() {
        assertThatThrownBy(
                        () ->
                                storage()
                                        .validate(
                                                new MockMultipartFile(
                                                        "x", new byte[5 * 1024 * 1024 + 1]),
                                                "identity"))
                .isInstanceOf(RegistrationFieldException.class);
    }

    @Test
    void validate_WithGeneratedJpeg_Accepts() throws Exception {
        var out = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB), "jpeg", out);
        assertThat(
                        storage()
                                .validate(
                                        new MockMultipartFile(
                                                "x", "image.jpg", "image/jpeg", out.toByteArray()),
                                        "identity")
                                .contentType())
                .isEqualTo("image/jpeg");
    }

    @Test
    void validate_WithTruncatedJpeg_Rejects() {
        assertThatThrownBy(
                        () ->
                                storage()
                                        .validate(
                                                new MockMultipartFile(
                                                        "x",
                                                        new byte[] {
                                                            (byte) 255,
                                                            (byte) 216,
                                                            (byte) 255,
                                                            (byte) 255,
                                                            (byte) 217
                                                        }),
                                                "identity"))
                .isInstanceOf(RegistrationFieldException.class);
    }

    @Test
    void store_WithoutTransaction_Rejects() {
        assertThatIllegalStateException()
                .isThrownBy(
                        () ->
                                storage()
                                        .store(
                                                storage()
                                                        .validate(
                                                                new MockMultipartFile("x", pdf()),
                                                                "identity")));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2})
    void store_WithTransactionOutcome_PreservesOnlyCommittedFile(int status) throws Exception {
        TransactionSynchronizationManager.setActualTransactionActive(true);
        TransactionSynchronizationManager.initSynchronization();
        try {
            String key =
                    storage()
                            .store(
                                    storage()
                                            .validate(
                                                    new MockMultipartFile("x", pdf()), "identity"));
            assertThat(Files.readAllBytes(directory.resolve(key))).isEqualTo(pdf());
            TransactionSynchronizationManager.getSynchronizations()
                    .forEach(s -> s.afterCompletion(status));
            assertThat(Files.exists(directory.resolve(key)))
                    .isEqualTo(status == TransactionSynchronization.STATUS_COMMITTED);
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
            TransactionSynchronizationManager.setActualTransactionActive(false);
        }
    }

    @Test
    void store_WithUnwritableRoot_ReturnsFieldError() throws Exception {
        Path root = Files.createFile(directory.resolve("file"));
        TransactionSynchronizationManager.setActualTransactionActive(true);
        TransactionSynchronizationManager.initSynchronization();
        try {
            assertThatThrownBy(
                            () ->
                                    new InstitutionDocumentStorage(root.toString())
                                            .store(
                                                    storage()
                                                            .validate(
                                                                    new MockMultipartFile(
                                                                            "x", pdf()),
                                                                    "identity")))
                    .isInstanceOf(RegistrationFieldException.class);
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
            TransactionSynchronizationManager.setActualTransactionActive(false);
        }
    }
}
