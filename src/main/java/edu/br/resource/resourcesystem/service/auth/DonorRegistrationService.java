package edu.br.resource.resourcesystem.service.auth;

import edu.br.resource.resourcesystem.dto.request.DonorRegistrationRequest;
import edu.br.resource.resourcesystem.model.entity.User;
import edu.br.resource.resourcesystem.model.enums.AccountRole;
import edu.br.resource.resourcesystem.model.enums.AccountStatus;
import edu.br.resource.resourcesystem.repository.UserRepository;
import edu.br.resource.resourcesystem.service.mail.TransactionalMailService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
@RequiredArgsConstructor
public class DonorRegistrationService {
    private final UserRepository users;
    private final AccountEmailRegistry emails;
    private final TransactionalMailService transactionalMail;
    private final PasswordEncoder passwords;

    @Transactional
    public void register(@Valid DonorRegistrationRequest request) {
        emails.requireAvailable(request.getEmail());
        User user =
                users.saveAndFlush(
                        User.builder()
                                .fullName(request.getFullName())
                                .email(request.getEmail())
                                .passwordHash(passwords.encode(request.getPassword()))
                                .role(AccountRole.DONOR)
                                .status(AccountStatus.ACTIVE)
                                .build());
        transactionalMail.welcome(user);
    }
}
