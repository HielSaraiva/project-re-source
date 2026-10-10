package edu.br.resource.resourcesystem.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.MultipartResolver;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
@RequiredArgsConstructor
public class RegistrationUploadLimitFilter extends OncePerRequestFilter {
    private final MultipartResolver multipartResolver;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getServletPath().equals("/register/institution")
                || !request.getMethod().equals("POST");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        MultipartHttpServletRequest multipart = null;
        try {

            if (multipartResolver.isMultipart(request))
                multipart = multipartResolver.resolveMultipart(request);
            chain.doFilter(multipart == null ? request : multipart, response);
        } catch (MaxUploadSizeExceededException ex) {
            response.sendRedirect(request.getContextPath() + "/register/institution?uploadError");
        } finally {
            if (multipart != null) multipartResolver.cleanupMultipart(multipart);
        }
    }
}
