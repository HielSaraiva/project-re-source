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
import edu.br.resource.resourcesystem.repository.DonationRepository;
import edu.br.resource.resourcesystem.repository.DonationStatusHistoryRepository;
import edu.br.resource.resourcesystem.repository.ItemRepository;
import edu.br.resource.resourcesystem.repository.ItemTypeRepository;
import edu.br.resource.resourcesystem.repository.UserRepository;
import edu.br.resource.resourcesystem.repository.DonationPackageRepository;
import edu.br.resource.resourcesystem.service.match.DonationStatusPolicy;
import edu.br.resource.resourcesystem.service.match.WorkflowConflictException;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collection;

@Service
public class DonationManagementService {
    static final String EDIT_COMPLETED =
            "Não é possível editar uma doação concluída.";
    static final String EDIT_BLOCKED =
            "Não é possível editar a doação enquanto houver propostas ativas.";
    static final String DELETE_BLOCKED =
            "Não é possível excluir uma doação que possui propostas vinculadas. O histórico é"
                    + " preservado.";

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

    public DonationManagementService(
            DonationRepository donations,
            DonationPackageRepository packages,
            ItemRepository items,
            DonationMatchRepository matches,
            DonationStatusHistoryRepository donationHistory,
            ItemTypeRepository itemTypes,
            UserRepository users) {
        this.donations = donations;
        this.packages = packages;
        this.items = items;
        this.matches = matches;
        this.donationHistory = donationHistory;
        this.itemTypes = itemTypes;
        this.users = users;
    }

    @Transactional
    public Integer register(Integer donorId, NewDonationForm form) {
        var donor = users.findById(donorId).orElseThrow(this::missing);
        ItemType type =
                itemTypes.findAllByCategoryOrderByNameAsc(form.getCategory()).stream()
                        .findFirst()
                        .orElseGet(
                                () ->
                                        itemTypes.save(
                                                ItemType.builder()
                                                        .category(form.getCategory())
                                                        .name(form.getCategory().getLabel())
                                                        .build()));
        Item item =
                items.save(
                        Item.builder()
                                .itemType(type)
                                .title(form.getTitle().strip())
                                .condition(form.getCondition())
                                .build());
        DonationPackage pack =
                packages.save(
                        DonationPackage.builder().item(item).quantity(form.getQuantity()).build());
        Donation donation =
                donations.saveAndFlush(Donation.builder().donor(donor).donationPackage(pack).build());
        donationHistory.save(
                DonationStatusHistory.builder()
                        .donation(donation)
                        .newStatus(DonationStatus.REGISTERED)
                        .changedByUser(donor)
                        .notes("Itens cadastrados para doação.")
                        .build());
        return donation.getId();
    }

    static boolean canEdit(Collection<DonationMatch> donationMatches) {
        return DonationStatusPolicy.activeCount(donationMatches) == 0;
    }

    static boolean canEdit(DonationStatus status, Collection<DonationMatch> donationMatches) {
        return status != DonationStatus.COMPLETED && canEdit(donationMatches);
    }

    static boolean canDelete(Collection<DonationMatch> donationMatches) {
        return donationMatches.isEmpty();
    }

    @Transactional
    public void update(Integer donorId, Integer donationId, DonationEditForm form) {
        var donation =
                donations.findForUpdate(donationId, donorId).orElseThrow(this::missing);
        var donationMatches = matches.findAllByDonationId(donationId);
        if (donation.getStatus() == DonationStatus.COMPLETED)
            throw new WorkflowConflictException(EDIT_COMPLETED);
        if (!canEdit(donationMatches)) throw new WorkflowConflictException(EDIT_BLOCKED);

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
    public void delete(Integer donorId, Integer donationId) {
        var donation =
                donations.findForUpdate(donationId, donorId).orElseThrow(this::missing);
        if (!canDelete(matches.findAllByDonationId(donationId)))
            throw new WorkflowConflictException(DELETE_BLOCKED);
        var pack = donation.getDonationPackage();
        var item = pack.getItem();
        donations.delete(donation);
        packages.delete(pack);
        items.delete(item);
        donations.flush();
    }

    private static String clean(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }

    private ResponseStatusException missing() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Registro não encontrado.");
    }
}
