package edu.br.resource.resourcesystem.service.auth;

import edu.br.resource.resourcesystem.repository.InstitutionRepository;
import edu.br.resource.resourcesystem.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AccountEmailRegistry {
    private final JdbcTemplate jdbc;
    private final UserRepository users;
    private final InstitutionRepository institutions;

    @Transactional(propagation = Propagation.MANDATORY)
    public void lock(String email) {
        jdbc.queryForList("select pg_advisory_xact_lock(hashtextextended(lower(?), 0))", email);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void requireAvailable(String email) {
        lock(email);
        loginProfile(email)
                .ifPresent(
                        profile -> {
                            throw new EmailAlreadyRegisteredException(profile);
                        });
    }

    @Transactional(readOnly = true)
    public Optional<String> loginProfile(String email) {
        if (users.findByEmailIgnoreCase(email).isPresent()) return Optional.of("donor");
        return institutions.findByEmailIgnoreCase(email).map(institution -> "institution");
    }
}
