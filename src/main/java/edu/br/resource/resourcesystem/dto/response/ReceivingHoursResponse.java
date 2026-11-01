package edu.br.resource.resourcesystem.dto.response;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

public record ReceivingHoursResponse(
        List<DayOfWeek> days,
        LocalTime openingHour,
        LocalTime closingHour,
        String responsibleName) {

    public ReceivingHoursResponse {
        days = List.copyOf(days);
    }
}
