package edu.br.resource.resourcesystem.dto.response;

import edu.br.resource.resourcesystem.model.enums.DeliveryMethod;
import edu.br.resource.resourcesystem.model.enums.DeliveryStatus;
import java.time.Instant;
import java.time.LocalDate;

public record DeliveryResponse(
        DeliveryMethod method,
        DeliveryStatus status,
        Instant methodConfirmedAt,
        String deliveryAddress,
        String carrierName,
        String trackingCode,
        LocalDate postedOn,
        LocalDate deliveredOn,
        Instant reportedAt,
        String receivedByName,
        String notes,
        Instant receiptConfirmedAt,
        Integer receiptConfirmedByInstitutionId) {}
