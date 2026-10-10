package edu.br.resource.resourcesystem.service.auth;

import edu.br.resource.resourcesystem.dto.request.InstitutionRegistrationRequest;
import edu.br.resource.resourcesystem.model.entity.Institution;
import edu.br.resource.resourcesystem.model.entity.InstitutionDocument;
import edu.br.resource.resourcesystem.model.enums.DocumentType;
import edu.br.resource.resourcesystem.model.enums.InstitutionStatus;
import edu.br.resource.resourcesystem.repository.InstitutionDocumentRepository;
import edu.br.resource.resourcesystem.repository.InstitutionRepository;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Validated
@RequiredArgsConstructor
public class InstitutionRegistrationService {
    private final InstitutionRepository institutions;
    private final InstitutionDocumentRepository documents;
    private final InstitutionDocumentStorage storage;
    private final AccountEmailRegistry emails;
    private final PasswordEncoder passwords;

    @Transactional
    public void register(
            @Valid InstitutionRegistrationRequest request,
            CnpjLookupService.Company company,
            InstitutionDocumentStorage.Upload identity,
            InstitutionDocumentStorage.Upload organization) {
        emails.requireAvailable(request.getEmail());
        if (institutions.findByCnpj(company.cnpj()).isPresent())
            throw new RegistrationFieldException("cnpj", "Este CNPJ já possui cadastro.");
        if (institutions.existsByRepresentativeCpf(request.getRepresentativeCpf()))
            throw new RegistrationFieldException(
                    "representativeCpf", "Este CPF já está associado a uma instituição.");
        var institution =
                institutions.saveAndFlush(
                        Institution.builder()
                                .cnpj(company.cnpj())
                                .legalName(company.legalName())
                                .email(request.getEmail())
                                .representativeFullName(request.getRepresentativeFullName())
                                .representativeCpf(request.getRepresentativeCpf())
                                .passwordHash(passwords.encode(request.getPassword()))
                                .status(InstitutionStatus.PENDING_APPROVAL)
                                .build());
        saveDocument(institution, DocumentType.IDENTITY_DOCUMENT, identity);
        saveDocument(institution, DocumentType.ORGANIZATION_DOCUMENT, organization);
        documents.flush();
    }

    private void saveDocument(
            Institution institution, DocumentType type, InstitutionDocumentStorage.Upload upload) {
        String key = storage.store(upload);
        documents.save(
                InstitutionDocument.builder()
                        .institution(institution)
                        .type(type)
                        .originalFileName(upload.originalName())
                        .storageKey(key)
                        .contentType(upload.contentType())
                        .fileSizeBytes(upload.bytes().length)
                        .build());
    }
}
