package cz.bezcisobe.backend.dto.mapper;

import cz.bezcisobe.backend.dto.response.RideResponse;
import cz.bezcisobe.backend.entity.Ride;
import cz.bezcisobe.backend.entity.User;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Component
public class RideMapper {

    private static final ZoneId APP_ZONE = ZoneId.of("Europe/Prague");

    public RideResponse toResponse(Ride ride) {
        List<String> passengerIds = ride.getPassengers().stream()
                .map(User::getId)
                .map(Object::toString)
                .toList();

        User owner = ride.getUser();
        boolean racePast = ride.getRace().getDate().isBefore(LocalDate.now(APP_ZONE));

        return new RideResponse(
                ride.getId().toString(),
                ride.getRace().getId().toString(),
                racePast,
                owner.getId().toString(),
                owner.getUsername(),
                owner.getFirstName(),
                owner.getLastName(),
                ride.getType().name(),
                ride.getFrom(),
                ride.getTo(),
                ride.getCar(),
                ride.getAvailableSeats(),
                ride.getOccupiedSeats(),
                passengerIds,
                ride.getNotes(),
                ride.getCreatedAt().toString()
        );
    }
}
