package cz.bezcisobe.backend.dto.response;

import java.util.List;

public record RideResponse(
        String id,
        String raceId,
        boolean racePast,
        String userId,
        String userUsername,
        String userFirstName,
        String userLastName,
        String type,
        String from,
        String to,
        String car,
        int availableSeats,
        int occupiedSeats,
        List<String> passengers,
        String notes,
        String createdAt
) {}
