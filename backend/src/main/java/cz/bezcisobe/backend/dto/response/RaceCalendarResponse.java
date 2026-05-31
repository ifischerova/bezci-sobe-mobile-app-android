package cz.bezcisobe.backend.dto.response;

public record RaceCalendarResponse(
        String id,
        int year,
        boolean isActive
) {}
