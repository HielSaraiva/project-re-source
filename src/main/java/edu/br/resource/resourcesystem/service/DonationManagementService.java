package edu.br.resource.resourcesystem.service;

import edu.br.resource.resourcesystem.dto.request.DonationEditForm;
import edu.br.resource.resourcesystem.dto.request.NewDonationForm;
import edu.br.resource.resourcesystem.model.entity.Donation;
import edu.br.resource.resourcesystem.model.entity.DonationMatch;
import edu.br.resource.resourcesystem.model.entity.DonationPackage;
import edu.br.resource.resourcesystem.model.entity.DonationStatusHistory;
import edu.br.resource.resourcesystem.model.entity.Item;
import edu.br.resource.resourcesystem.model.entity.ItemType;
import edu.br.resource.resourcesystem.model.enums.DonationStatus;
import edu.br.resource.resourcesystem.repository.DonationMatchRepository;
import edu.br.resource.resourcesystem.repository.DonationPackageRepository;
import edu.br.resource.resourcesystem.repository.DonationRepository;
import edu.br.resource.resourcesystem.repository.DonationStatusHistoryRepository;
import edu.br.resource.resourcesystem.repository.ItemRepository;
import edu.br.resource.resourcesystem.repository.ItemTypeRepository;
import edu.br.resource.resourcesystem.repository.UserRepository;
import edu.br.resource.resourcesystem.service.match.DonationStatusPolicy;
import edu.br.resource.resourcesystem.service.match.WorkflowConflictException;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collection;

@Service
public class DonationManagementService {
    static final String EDIT_COMPLETED = "Não é possível editar uma doação concluída.";
    static final String EDIT_BLOCKED =
            "Não é possível editar a doação enquanto houver propostas ativas.";
    static final String DELETE_BLOCKED =
            "Não é possível excluir enquanto houver propostas em andamento ou concluídas. O"
                    + " histórico será preservado.";

    public static class QuantityBelowCommittedException extends WorkflowConflictException {
        public QuantityBelowCommittedException(String reason) {
            super(reason);
        }
    }

    private final DonationRepository donations;
    private final DonationPackageRepository packages;
    private final ItemRepository items;
    private final DonationMatchRepository matches;
    private final DonationStatusHistoryRepository donationHistory;
    private final ItemTypeRepository itemTypes;
    private final UserRepository users;
    private final edu.br.resource.resourcesystem.repository.MatchLockPolicy locks;
    private final org.springframework.jdbc.core.JdbcTemplate db;

    public DonationManagementService(
            DonationRepository donations,
            DonationPackageRepository packages,
            ItemRepository items,
            DonationMatchRepository matches,
            DonationStatusHistoryRepository donationHistory,
            ItemTypeRepository itemTypes,
            UserRepository users,
            edu.br.resource.resourcesystem.repository.MatchLockPolicy locks,
            org.springframework.jdbc.core.JdbcTemplate db) {
        this.donations = donations;
        this.packages = packages;
        this.items = items;
        this.matches = matches;
        this.donationHistory = donationHistory;
        this.itemTypes = itemTypes;
        this.users = users;
        this.locks = locks;
        this.db = db;
    }

    @Transactional
    public Integer register(Integer donorId, NewDonationForm form) {
        locks.apply();
        if (form.getSubmissionKey() == null)
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Atualize a página antes de cadastrar.");
        String fingerprint = fingerprint(form);
        // Serialize identical submissions, then reuse the original result. The table's primary
        // key also protects retries that arrive through a different application instance.
        db.queryForObject(
                "select pg_advisory_xact_lock(hashtextextended(?,0))",
                Object.class,
                "donation-create:" + donorId + ":" + form.getSubmissionKey());
        var previous =
                db.query(
                        "select donation_id,request_fingerprint from donation_creation_requests"
                                + " where donor_user_id=? and request_key=?",
                        (rs, row) ->
                                new CreationResult(
                                        rs.getObject("donation_id", Integer.class),
                                        rs.getString("request_fingerprint")),
                        donorId,
                        form.getSubmissionKey());
        if (!previous.isEmpty()) {
            var result = previous.getFirst();
            if (!result.fingerprint().equals(fingerprint))
                throw new WorkflowConflictException(
                        "Este cadastro já foi recebido com outros dados. Atualize a lista para"
                                + " conferir o resultado.");
            if (result.id() == null
                    || donations
                            .findById(result.id())
                            .map(d -> d.getDeletedAt() != null)
                            .orElse(true))
                throw new WorkflowConflictException(
                        "Este cadastro já foi processado e a doação foi excluída. Atualize a"
                                + " lista.");
            return result.id();
        }
        var donor = users.findById(donorId).orElseThrow(this::missing);
        var typeId =
                itemTypes.obtainId(form.getCategory().getValue(), form.getCategory().getLabel());
        ItemType type = itemTypes.findById(typeId).orElseThrow();
        Item item =
                items.save(
                        Item.builder()
                                .itemType(type)
                                .title(form.getTitle().strip())
                                .condition(form.getCondition())
                                .description(clean(form.getDescription()))
                                .build());
        DonationPackage pack =
                packages.save(
                        DonationPackage.builder().item(item).quantity(form.getQuantity()).build());
        Donation donation =
                donations.saveAndFlush(
                        Donation.builder().donor(donor).donationPackage(pack).build());
        donationHistory.save(
                DonationStatusHistory.builder()
                        .donation(donation)
                        .newStatus(DonationStatus.REGISTERED)
                        .changedByUser(donor)
                        .notes("Itens cadastrados para doação.")
                        .build());
        db.update(
                "insert into"
                    + " donation_creation_requests(donor_user_id,request_key,request_fingerprint,donation_id)"
                    + " values(?,?,?,?)",
                donorId,
                form.getSubmissionKey(),
                fingerprint,
                donation.getId());
        return donation.getId();
    }

    static boolean canEdit(Collection<DonationMatch> donationMatches) {
        return DonationStatusPolicy.activeCount(donationMatches) == 0;
    }

    static boolean canEdit(DonationStatus status, Collection<DonationMatch> donationMatches) {
        return status != DonationStatus.COMPLETED && canEdit(donationMatches);
    }

    static boolean canDelete(Collection<DonationMatch> donationMatches) {
        return donationMatches.stream()
                .allMatch(
                        m ->
                                m.getStatus()
                                                == edu.br.resource.resourcesystem.model.enums
                                                        .MatchStatus.CANCELLED
                                        || m.getStatus()
                                                == edu.br.resource.resourcesystem.model.enums
                                                        .MatchStatus.REJECTED);
    }

    @Transactional
    public void update(Integer donorId, Integer donationId, DonationEditForm form) {
        locks.apply();
        var donation = donations.findForUpdate(donationId, donorId).orElseThrow(this::missing);
        if (donation.getDeletedAt() != null) throw missing();
        var donationMatches = matches.findAllByDonationId(donationId);
        if (donation.getStatus() == DonationStatus.COMPLETED)
            throw new WorkflowConflictException(EDIT_COMPLETED);
        if (!canEdit(donationMatches)) throw new WorkflowConflictException(EDIT_BLOCKED);
        requireVersion(donation, form.getVersion());

        long committed = DonationStatusPolicy.allocatedQuantity(donationMatches);
        if (form.getQuantity() < committed)
            throw new QuantityBelowCommittedException(
                    "A quantidade não pode ser menor que "
                            + committed
                            + " (já comprometida ou recebida em propostas anteriores).");

        var pack = donation.getDonationPackage();
        var item = pack.getItem();
        item.setTitle(form.getTitle().strip());
        item.setCondition(form.getCondition());
        item.setDescription(clean(form.getDescription()));
        pack.setQuantity(form.getQuantity());
        donation.setEditVersion(donation.getEditVersion() + 1);

        var next = DonationStatusPolicy.consolidate(form.getQuantity(), donationMatches);
        if (donation.getStatus() != next) {
            var previous = donation.getStatus();
            donation.setStatus(next);
            donationHistory.save(
                    DonationStatusHistory.builder()
                            .donation(donation)
                            .previousStatus(previous)
                            .newStatus(next)
                            .changedByUser(donation.getDonor())
                            .notes("Status consolidado após edição da doação.")
                            .build());
        }
    }

    @Transactional
    public void delete(Integer donorId, Integer donationId, long version) {
        locks.apply();
        var donation = donations.findForUpdate(donationId, donorId).orElseThrow(this::missing);
        if (donation.getDeletedAt() != null) throw missing();
        var donationMatches = matches.findAllByDonationId(donationId);
        if (!canDelete(donationMatches)) throw new WorkflowConflictException(DELETE_BLOCKED);
        requireVersion(donation, version);
        if (!donationMatches.isEmpty()) {
            donation.setDeletedAt(java.time.Instant.now());
            donation.setEditVersion(donation.getEditVersion() + 1);
            return; // Keep protocols, delivery records, notifications and history intact.
        }
        var pack = donation.getDonationPackage();
        var item = pack.getItem();
        donations.delete(donation);
        packages.delete(pack);
        items.delete(item);
        donations.flush();
    }

    private record CreationResult(Integer id, String fingerprint) {}

    private static String fingerprint(NewDonationForm form) {
        var values =
                java.util.List.of(
                        form.getCategory().getValue(),
                        form.getTitle().strip(),
                        String.valueOf(form.getQuantity()),
                        form.getCondition().getValue(),
                        java.util.Objects.toString(clean(form.getDescription()), ""));
        var canonical = new StringBuilder();
        for (String value : values) canonical.append(value.length()).append(':').append(value);
        try {
            return java.util.HexFormat.of()
                    .formatHex(
                            java.security.MessageDigest.getInstance("SHA-256")
                                    .digest(
                                            canonical
                                                    .toString()
                                                    .getBytes(
                                                            java.nio.charset.StandardCharsets
                                                                    .UTF_8)));
        } catch (java.security.NoSuchAlgorithmException impossible) {
            throw new IllegalStateException(impossible);
        }
    }

    private void requireVersion(Donation donation, Long version) {
        if (version == null || version != donation.getEditVersion())
            throw new WorkflowConflictException(
                    "Esta doação foi alterada em outra sessão. Seus dados foram preservados;"
                            + " recarregue a versão atual antes de salvar.");
    }

    private static String clean(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }

    private ResponseStatusException missing() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Registro não encontrado.");
    }
}
