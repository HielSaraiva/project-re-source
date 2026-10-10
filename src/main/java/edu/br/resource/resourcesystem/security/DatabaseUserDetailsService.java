package edu.br.resource.resourcesystem.security;

import edu.br.resource.resourcesystem.model.enums.AccountRole;
import edu.br.resource.resourcesystem.model.enums.AccountStatus;
import edu.br.resource.resourcesystem.model.enums.InstitutionStatus;
import edu.br.resource.resourcesystem.repository.InstitutionRepository;
import edu.br.resource.resourcesystem.repository.UserRepository;
import edu.br.resource.resourcesystem.validation.BrazilianDocuments;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DatabaseUserDetailsService implements UserDetailsService {
    private final UserRepository users;
    private final InstitutionRepository institutions;

    @Override
    public UserDetails loadUserByUsername(String identifier) {
        if (identifier == null || identifier.isBlank()) throw unavailable();
        identifier = identifier.strip();
        String normalizedCnpj = BrazilianDocuments.normalizeCnpj(identifier);
        boolean cnpj = normalizedCnpj.matches("[A-Z0-9]{12}[0-9]{2}");
        var donor =
                cnpj
                        ? Optional.<edu.br.resource.resourcesystem.model.entity.User>empty()
                        : users.findByEmailIgnoreCase(identifier);
        var institution =
                cnpj
                        ? institutions.findByCnpj(normalizedCnpj)
                        : institutions.findByEmailIgnoreCase(identifier);
        if (donor.isPresent() && institution.isPresent()) throw unavailable();
        if (donor.isPresent()) {
            var account = donor.get();
            if (account.getPasswordHash() == null) throw unavailable();
            return User.withUsername(account.getEmail())
                    .password(encoded(account.getPasswordHash()))
                    .roles(account.getRole() == AccountRole.DONOR ? "DONOR" : "ADMINISTRATOR")
                    .disabled(account.getStatus() != AccountStatus.ACTIVE)
                    .build();
        }
        var account = institution.orElseThrow(this::unavailable);
        return User.withUsername(account.getEmail())
                .password(encoded(account.getPasswordHash()))
                .roles("ONG")
                .disabled(account.getStatus() != InstitutionStatus.APPROVED)
                .build();
    }

    private UsernameNotFoundException unavailable() {
        return new UsernameNotFoundException("Conta indisponível.");
    }

    private String encoded(String hash) {

        return hash.startsWith("$") ? "{bcrypt}" + hash : hash;
    }
}
