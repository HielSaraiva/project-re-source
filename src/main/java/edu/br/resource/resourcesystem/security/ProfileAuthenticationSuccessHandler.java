package edu.br.resource.resourcesystem.security;

import edu.br.resource.resourcesystem.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ProfileAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final UserRepository users;

    public static String destination(Authentication authentication) {
        return authentication.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_ONG"))
                ? "/ong/dashboard"
                : "/donor/dashboard";
    }

    @Override
    @Transactional
    public void onAuthenticationSuccess(
            HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {
        if (authentication.getAuthorities().stream()
                .noneMatch(a -> a.getAuthority().equals("ROLE_ONG"))) {
            users.findByEmailIgnoreCase(authentication.getName())
                    .ifPresent(
                            user -> {
                                user.setLastLoginAt(Instant.now());
                                users.save(user);
                            });
        }
        clearAuthenticationAttributes(request);
        new HttpSessionRequestCache().removeRequest(request, response);
        getRedirectStrategy().sendRedirect(request, response, destination(authentication));
    }
}
