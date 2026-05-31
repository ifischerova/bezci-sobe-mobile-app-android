package cz.bezcisobe.backend.dto.response;

import java.util.List;

public record RaceResponse(
        String id,
        String name,
        String place,
        String date,
        String startTime,
        String web,
        RefItem trackLength,
        RefItem trackType,
        List<RefItem> certifications,
        String raceCalendarId,
        boolean isPast
) {
    public record RefItem(String id, String name) {}
}
