package edu.br.resource.resourcesystem.repository.specification;

public final class SearchPatterns {
    private SearchPatterns() {}

    public static String contains(String query) {
        return "%" + query.strip().toLowerCase(java.util.Locale.ROOT)
                .replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%";
    }
}
