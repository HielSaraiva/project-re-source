package edu.br.resource.resourcesystem.security;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;

import java.util.Optional;

class LoginDestinationPolicyDonationsTest {
    private final TestingAuthenticationToken donor =
            new TestingAuthenticationToken("donor@example.com", "x", "ROLE_DONOR");

    @Test
    void returnsToMyDonationsKeepingThePageNumber() {
        assertEquals(
                Optional.of("/donor/donations?page=3"),
                LoginDestinationPolicy.destination(
                        "http://localhost/donor/donations?page=3", "", donor));
    }
}
