package edu.br.resource.resourcesystem.security;

import edu.br.resource.resourcesystem.model.entity.*;
import edu.br.resource.resourcesystem.model.enums.*;
import edu.br.resource.resourcesystem.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CurrentActor {
    private final UserRepository users;
    private final InstitutionRepository institutions;

    public User donor(Authentication auth) {
        if (auth == null || auth.getAuthorities().stream().noneMatch(a -> a.getAuthority().equals("ROLE_DONOR")))
            throw denied();
        return users.findByEmailIgnoreCase(auth.getName())
                .filter(u -> u.getRole() == AccountRole.DONOR && u.getStatus() == AccountStatus.ACTIVE)
                .orElseThrow(this::denied);
    }

    public Institution institution(Authentication auth) {
        if (auth == null || auth.getAuthorities().stream().noneMatch(a -> a.getAuthority().equals("ROLE_ONG")))
            throw denied();
        return institutions.findByEmailIgnoreCase(auth.getName())
                .filter(i -> i.getStatus() == InstitutionStatus.APPROVED).orElseThrow(this::denied);
    }

    private ResponseStatusException denied() {
        return new ResponseStatusException(HttpStatus.FORBIDDEN, "Conta sem permissão para esta operação.");
    }
}
