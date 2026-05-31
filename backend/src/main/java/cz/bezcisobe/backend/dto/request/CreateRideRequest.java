package cz.bezcisobe.backend.dto.request;

import cz.bezcisobe.backend.validation.RideRequestPayload;
import cz.bezcisobe.backend.validation.ValidRideRequest;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@ValidRideRequest
public record CreateRideRequest(
        @NotNull(message = "{validation.ride.race.required}")
        Long raceId,

        @NotBlank(message = "{validation.ride.type.required}")
        String type,

        @NotBlank(message = "{validation.ride.origin.required}")
        String from,

        String to,

        String car,

        @NotNull(message = "{validation.ride.seats.required}")
        @Min(value = 1, message = "{validation.ride.seats.min}")
        @Max(value = 10, message = "{validation.ride.seats.max}")
        Integer availableSeats,

        String notes
) implements RideRequestPayload {}
