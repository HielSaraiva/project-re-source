package edu.br.resource.resourcesystem.validation;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class PasswordPolicy {
    public static final int MIN_LENGTH = 8;
    public static final int MAX_LENGTH = 12;
    public static final int MAX_BYTES = 72;

    public record Requirement(String label, String pattern) {}

    public static final List<Requirement> REQUIREMENTS =
            List.of(
                    new Requirement("Uma letra maiúscula", "\\p{Lu}"),
                    new Requirement("Uma letra minúscula", "\\p{Ll}"),
                    new Requirement("Um número", "\\p{Nd}"),
                    new Requirement(
                            "Um caractere especial (ex.: !, @, #)",
                            "[^\\p{L}\\p{N}\\p{Z}\\s\\u0085\\uFEFF]"));
    public static final String PATTERN =
            REQUIREMENTS.stream()
                            .map(requirement -> "(?=[\\s\\S]*" + requirement.pattern() + ")")
                            .collect(Collectors.joining())
                    + "[\\s\\S]{"
                    + MIN_LENGTH
                    + ","
                    + MAX_LENGTH
                    + "}";
    public static final String MESSAGE =
            "Use de "
                    + MIN_LENGTH
                    + " a "
                    + MAX_LENGTH
                    + " caracteres, com uma letra maiúscula, uma minúscula, um número e um"
                    + " caractere especial.";
    public static final String BYTE_LIMIT_MESSAGE =
            "Essa senha excede o limite permitido. Use uma senha menor.";
    private static final Pattern FORMAT = Pattern.compile(PATTERN, Pattern.UNICODE_CHARACTER_CLASS);

    private PasswordPolicy() {}

    public static boolean validFormat(String password) {
        return FORMAT.matcher(password).matches();
    }

    public static boolean withinByteLimit(String password) {
        return password.getBytes(StandardCharsets.UTF_8).length <= MAX_BYTES;
    }
}
