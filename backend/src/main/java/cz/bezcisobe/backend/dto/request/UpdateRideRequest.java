package cz.bezcisobe.backend.dto.request;

import cz.bezcisobe.backend.validation.RideRequestPayload;
import cz.bezcisobe.backend.validation.ValidRideRequest;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Payload for {@code PUT /api/rides/{id}}. Same shape and constraints as
 * {@link CreateRideRequest} except {@code raceId} cannot be changed (a ride
 * is bound to its race for life).
 */
@ValidRideRequest
public record UpdateRideRequest(
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
