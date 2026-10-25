package edu.br.resource.resourcesystem.security;

import static org.junit.jupiter.api.Assertions.*;

import jakarta.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

class LoginDestinationPolicyTest {
    @Test
    void needsPageIsSavedForPostLoginReturn() {
        HttpServletRequest request = new MockHttpServletRequest("GET", "/institution/necessities");
        assertTrue(LoginDestinationPolicy.cacheable(request));

        var authentication =
                new UsernamePasswordAuthenticationToken(
                        "institution",
                        "credentials",
                        List.of(new SimpleGrantedAuthority("ROLE_ONG")));
        assertEquals(
                "/institution/necessities?category=furniture&page=2",
                LoginDestinationPolicy.destination(
                                "http://localhost/institution/necessities?category=furniture&page=2",
                                "",
                                authentication)
                        .orElseThrow());
    }
}
