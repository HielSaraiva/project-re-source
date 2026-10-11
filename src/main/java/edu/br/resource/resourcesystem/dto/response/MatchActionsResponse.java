package edu.br.resource.resourcesystem.dto.response;

public record MatchActionsResponse(
        boolean canAccept,
        boolean canReject,
        boolean canCancel,
        boolean canSelectDeliveryMethod,
        boolean canConfirmInPersonDelivery,
        boolean canConfirmShipment,
        boolean canConfirmReceipt) {}
