package edu.br.resource.resourcesystem.quality.support;

import edu.br.resource.resourcesystem.dto.request.*;
import edu.br.resource.resourcesystem.dto.response.*;
import edu.br.resource.resourcesystem.model.enums.*;
import edu.br.resource.resourcesystem.service.*;
import edu.br.resource.resourcesystem.service.match.*;
import edu.br.resource.resourcesystem.service.necessity.*;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.*;

public abstract class WorkflowSupport extends QualityIntegrationSupport {
    @Autowired protected MatchWorkflowService workflow;
    @Autowired protected MatchPageService pages;
    @Autowired protected DonationManagementService inventory;
    @Autowired protected DonationHistoryService inventoryView;
    @Autowired protected NecessityManagementService needs;
    protected int donor, otherDonor, ong, otherOng, necessity, donation;

    @BeforeEach
    protected void createBusinessFixture() {
        donor = createUser("donor");
        otherDonor = createUser("other-donor");
        ong = institution("ong");
        otherOng = institution("other-ong");
        int type =
                db.queryForObject(
                        "INSERT INTO item_types(category,name) VALUES('electronics','Equipamento de"
                                + " teste') RETURNING id",
                        Integer.class);
        necessity =
                db.queryForObject(
                        "INSERT INTO"
                            + " necessities(institution_id,item_type_id,quantity_requested,description,priority)"
                            + " VALUES(?,?,10,'Equipamento para oficina','high') RETURNING id",
                        Integer.class,
                        ong,
                        type);
        donation = stock(donor, type, 10, "Equipamento oferecido");
    }

    protected int stock(int owner, int type, int quantity, String title) {
        int item =
                db.queryForObject(
                        "INSERT INTO items(item_type_id,title,condition,description)"
                                + " VALUES(?,?,'good','Funcionando, dado fictício') RETURNING id",
                        Integer.class,
                        type,
                        title);
        int pack =
                db.queryForObject(
                        "INSERT INTO packages(item_id,quantity) VALUES(?,?) RETURNING id",
                        Integer.class,
                        item,
                        quantity);
        return db.queryForObject(
                "INSERT INTO donations(donor_user_id,package_id) VALUES(?,?) RETURNING id",
                Integer.class,
                owner,
                pack);
    }

    protected MatchDetailResponse propose(int quantity) {
        return workflow.propose(
                donor,
                new SubmitProposalRequest(necessity, donation, quantity, "Contribuição de teste"));
    }

    protected LocalDate today() {
        return LocalDate.now(ZoneId.of("America/Fortaleza"));
    }

    protected enum Stage {
        PROPOSED,
        ACCEPTED,
        PERSONAL,
        CARRIER,
        REPORTED,
        POSTED,
        COMPLETED,
        REJECTED,
        CANCELLED
    }

    protected MatchDetailResponse stage(Stage stage) {
        var result = propose(4);
        String p = result.protocol();
        if (stage == Stage.PROPOSED) return result;
        if (stage == Stage.REJECTED) return workflow.reject(ong, p, "Não atende à especificação");
        if (stage == Stage.CANCELLED) return workflow.cancel(donor, p);
        result = workflow.accept(ong, p);
        if (stage == Stage.ACCEPTED) return result;
        boolean carrier = stage == Stage.CARRIER || stage == Stage.POSTED;
        result =
                workflow.selectMethod(
                        donor, p, carrier ? DeliveryMethod.CARRIER : DeliveryMethod.IN_PERSON);
        if (stage == Stage.PERSONAL || stage == Stage.CARRIER) return result;
        if (carrier)
            return workflow.reportShipment(
                    donor,
                    p,
                    new ConfirmCarrierShipmentRequest(today(), "AB123456789BR", "Embalado", true));
        result =
                workflow.reportDelivery(
                        donor,
                        p,
                        new ConfirmInPersonDeliveryRequest(
                                today(), "Recebedora fictícia", "Portaria", true));
        return stage == Stage.COMPLETED ? workflow.receive(ong, p) : result;
    }

    protected MatchDetailResponse action(String action, String p) {
        return switch (action) {
            case "accept" -> workflow.accept(ong, p);
            case "reject" -> workflow.reject(ong, p, "Motivo de teste");
            case "cancel" -> workflow.cancel(donor, p);
            case "method" -> workflow.selectMethod(donor, p, DeliveryMethod.IN_PERSON);
            case "personal" ->
                    workflow.reportDelivery(
                            donor,
                            p,
                            new ConfirmInPersonDeliveryRequest(
                                    today(), "Pessoa fictícia", null, true));
            case "shipment" ->
                    workflow.reportShipment(
                            donor,
                            p,
                            new ConfirmCarrierShipmentRequest(
                                    today(), "AB123456789BR", null, true));
            case "receive" -> workflow.receive(ong, p);
            default -> throw new IllegalArgumentException(action);
        };
    }

    protected int needFor(int institution, int type, int quantity) {
        return db.queryForObject(
                "INSERT INTO"
                    + " necessities(institution_id,item_type_id,quantity_requested,description,priority)"
                    + " VALUES(?,?,?,'Necessidade secundária','medium') RETURNING id",
                Integer.class,
                institution,
                type,
                quantity);
    }

    protected int type() {
        return db.queryForObject(
                "SELECT item_type_id FROM necessities WHERE id=?", Integer.class, necessity);
    }

    protected DonationEditForm edit(int quantity, long version) {
        var f = new DonationEditForm();
        f.setTitle("Título atualizado");
        f.setQuantity(quantity);
        f.setCondition(ItemCondition.NEW);
        f.setDescription("Descrição atualizada");
        f.setVersion(version);
        return f;
    }
}
