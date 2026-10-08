package edu.br.resource.resourcesystem.model.view;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public record DonationHistoryEvent(String title, String at, String actor, String description) {
    public String dateLabel() {
        return OffsetDateTime.parse(at).atZoneSameInstant(ZoneId.of("America/Fortaleza"))
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm"));
    }
}
