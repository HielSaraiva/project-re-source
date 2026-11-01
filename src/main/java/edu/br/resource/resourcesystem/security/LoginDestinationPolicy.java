package edu.br.resource.resourcesystem.security;

import edu.br.resource.resourcesystem.controller.error.ErrorResponses;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.security.core.Authentication;

import java.net.URI;
import java.util.Optional;

public final class LoginDestinationPolicy {
    private LoginDestinationPolicy() {}

    public static String loginPath(HttpServletRequest request) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        boolean institution = path.startsWith("/institution/") || path.startsWith("/ong/");
        return request.getContextPath()
                + "/login?profile="
                + (institution ? "institution" : "donor");
    }

    public static boolean cacheable(HttpServletRequest request) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        return "GET".equals(request.getMethod())
                && !ErrorResponses.isJsonRequest(request)
                && role(path) != null;
    }

    public static Optional<String> destination(
            String address, String contextPath, Authentication authentication) {
        try {
            URI uri = URI.create(address);
            if (uri.getFragment() != null
                    || uri.getUserInfo() != null
                    || (!uri.isAbsolute() && uri.getRawAuthority() != null)
                    || (uri.isAbsolute() && uri.getHost() == null)) return Optional.empty();
            if (uri.isAbsolute()
                    && !("http".equals(uri.getScheme()) || "https".equals(uri.getScheme())))
                return Optional.empty();
            String path = uri.getRawPath();
            if (path == null || !path.startsWith(contextPath + "/")) return Optional.empty();
            String requiredRole = role(path.substring(contextPath.length()));
            if (requiredRole == null
                    || authentication.getAuthorities().stream()
                            .noneMatch(authority -> authority.getAuthority().equals(requiredRole)))
                return Optional.empty();
            String query = uri.getRawQuery();
            if (query != null && (query.length() > 2048 || query.matches("(?i).*%0[ad].*")))
                return Optional.empty();
            return Optional.of(
                    path.substring(contextPath.length()) + (query == null ? "" : "?" + query));
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    private static String role(String path) {
        if (path.matches(
                "/donor/(dashboard|needs|donations|donation/(proposal|status|shipping(?:/in-person|/carrier)?|completed))"))
            return "ROLE_DONOR";
        if (path.matches(
                "/institution/(dashboard|necessities|donations(?:/INT-[0-9]{4}-[0-9]+)?)"))
            return "ROLE_ONG";
        return null;
    }
}
