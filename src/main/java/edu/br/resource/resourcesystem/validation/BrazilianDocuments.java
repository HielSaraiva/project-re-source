package edu.br.resource.resourcesystem.validation;

import java.util.Locale;

public final class BrazilianDocuments {
    private BrazilianDocuments() {}

    public static String normalizeCnpj(String value) {
        if (value == null) return null;
        String clean = value.strip().toUpperCase(Locale.ROOT);
        return clean.matches(
                        "[A-Z0-9]{12}[0-9]{2}|[A-Z0-9]{2}\\.[A-Z0-9]{3}\\.[A-Z0-9]{3}/[A-Z0-9]{4}-[0-9]{2}")
                ? clean.replaceAll("[./-]", "")
                : clean;
    }

    public static String normalizeCpf(String value) {
        if (value == null) return null;
        String clean = value.strip();
        return clean.matches("[0-9]{11}|[0-9]{3}\\.[0-9]{3}\\.[0-9]{3}-[0-9]{2}")
                ? clean.replaceAll("[.-]", "")
                : clean;
    }

    public static boolean validCnpj(String value) {
        String cnpj = normalizeCnpj(value);
        if (cnpj == null || !cnpj.matches("[A-Z0-9]{12}[0-9]{2}") || cnpj.matches("([0-9])\\1{13}"))
            return false;

        return digit(cnpj, new int[] {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2}) == cnpj.charAt(12) - '0'
                && digit(cnpj, new int[] {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2})
                        == cnpj.charAt(13) - '0';
    }

    public static boolean validCpf(String value) {
        String cpf = normalizeCpf(value);
        if (cpf == null || !cpf.matches("[0-9]{11}") || cpf.matches("([0-9])\\1{10}")) return false;
        return digit(cpf, new int[] {10, 9, 8, 7, 6, 5, 4, 3, 2}) == cpf.charAt(9) - '0'
                && digit(cpf, new int[] {11, 10, 9, 8, 7, 6, 5, 4, 3, 2}) == cpf.charAt(10) - '0';
    }

    private static int digit(String value, int[] weights) {
        int sum = 0;
        for (int i = 0; i < weights.length; i++) sum += (value.charAt(i) - '0') * weights[i];
        int remainder = sum % 11;
        return remainder < 2 ? 0 : 11 - remainder;
    }
}
