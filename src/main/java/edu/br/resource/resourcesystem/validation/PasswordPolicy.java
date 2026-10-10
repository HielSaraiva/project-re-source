package edu.br.resource.resourcesystem.validation;

import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

public final class PasswordPolicy {
    public static final int MIN_LENGTH = 8;
    public static final int MAX_LENGTH = 12;
    public static final int MAX_BYTES = 72;
    public static final String PATTERN =
            "(?=[\\s\\S]*\\p{Lu})(?=[\\s\\S]*\\p{Ll})(?=[\\s\\S]*\\p{Nd})(?=[\\s\\S]*[^\\p{L}\\p{N}\\p{Z}\\s\\u0085\\uFEFF])[\\s\\S]{"
                    + MIN_LENGTH
                    + ","
                    + MAX_LENGTH
                    + "}";
    public static final String MESSAGE =
            "Use de "
                    + MIN_LENGTH
                    + " a "
                    + MAX_LENGTH
                    + " caracteres, com uma letra maiúscula, uma minúscula, um número e um caractere especial.";
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
