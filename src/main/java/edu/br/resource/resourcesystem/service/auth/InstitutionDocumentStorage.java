package edu.br.resource.resourcesystem.service.auth;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

@Component
@Slf4j
public class InstitutionDocumentStorage {
    public static final long MAX_FILE_BYTES = 5L * 1024 * 1024;
    private final Path root;

    public InstitutionDocumentStorage(
            @Value("${resource.registration.document-directory:var/private/institution-documents}")
                    String directory) {
        root = Path.of(directory).toAbsolutePath().normalize();
    }

    public Upload validate(MultipartFile file, String field) {
        if (file == null || file.isEmpty()) throw invalid(field, "Anexe o documento solicitado.");
        if (file.getSize() > MAX_FILE_BYTES)
            throw invalid(field, "Cada documento deve ter no máximo 5 MB.");
        try {
            byte[] bytes;
            try (var input = file.getInputStream()) {
                bytes = input.readNBytes((int) MAX_FILE_BYTES + 1);
            }
            if (bytes.length > MAX_FILE_BYTES)
                throw invalid(field, "Cada documento deve ter no máximo 5 MB.");
            String mime;
            if (isPdf(bytes)) mime = "application/pdf";
            else if (isJpeg(bytes)) mime = "image/jpeg";
            else throw invalid(field, "Envie um documento PDF ou JPG válido.");
            String name = file.getOriginalFilename();
            if (name == null || name.isBlank())
                name = mime.equals("application/pdf") ? "documento.pdf" : "documento.jpg";
            name = name.replace('\\', '/');
            name = name.substring(name.lastIndexOf('/') + 1).replaceAll("[\\p{Cntrl}]", "");
            if (name.isBlank()) name = "documento";
            return new Upload(field, bytes, name.substring(0, Math.min(name.length(), 255)), mime);
        } catch (IOException ex) {
            throw invalid(field, "Não foi possível ler o documento. Anexe-o novamente.");
        }
    }

    public String store(Upload upload) {
        if (!TransactionSynchronizationManager.isActualTransactionActive())
            throw new IllegalStateException("Document storage requires an active transaction.");
        String key = UUID.randomUUID().toString();
        Path destination = root.resolve(key);
        try {
            Files.createDirectories(
                    root,
                    PosixFilePermissions.asFileAttribute(
                            PosixFilePermissions.fromString("rwx------")));
            Files.createFile(
                    destination,
                    PosixFilePermissions.asFileAttribute(
                            PosixFilePermissions.fromString("rw-------")));
            Files.write(destination, upload.bytes());
        } catch (IOException ex) {
            delete(destination);
            throw new RegistrationFieldException(
                    upload.field(), "Não foi possível armazenar os documentos. Tente novamente.");
        }
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCompletion(int status) {
                        if (status != STATUS_COMMITTED) delete(destination);
                    }
                });
        return key;
    }

    private void delete(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException ex) {
            log.error("event=document_cleanup_failed errorType={}", ex.getClass().getSimpleName());
        }
    }

    private boolean isPdf(byte[] bytes) {
        if (bytes.length < 12
                || !new String(bytes, 0, 5, StandardCharsets.US_ASCII).equals("%PDF-"))
            return false;
        String tail =
                new String(
                        bytes,
                        Math.max(0, bytes.length - 1024),
                        Math.min(bytes.length, 1024),
                        StandardCharsets.US_ASCII);
        return tail.stripTrailing().endsWith("%%EOF");
    }

    private boolean isJpeg(byte[] bytes) {
        if (bytes.length < 4
                || (bytes[0] & 255) != 255
                || (bytes[1] & 255) != 216
                || (bytes[2] & 255) != 255
                || (bytes[bytes.length - 2] & 255) != 255
                || (bytes[bytes.length - 1] & 255) != 217) return false;
        try (var stream =
                javax.imageio.ImageIO.createImageInputStream(
                        new java.io.ByteArrayInputStream(bytes))) {
            var readers = javax.imageio.ImageIO.getImageReaders(stream);
            if (!readers.hasNext()) return false;
            var reader = readers.next();
            try {
                reader.setInput(stream);
                int width = reader.getWidth(0), height = reader.getHeight(0);
                return width > 0 && height > 0 && (long) width * height <= 25_000_000;
            } finally {
                reader.dispose();
            }
        } catch (IOException ex) {
            return false;
        }
    }

    private RegistrationFieldException invalid(String field, String message) {
        return new RegistrationFieldException(field, message);
    }

    public record Upload(String field, byte[] bytes, String originalName, String contentType) {}
}
