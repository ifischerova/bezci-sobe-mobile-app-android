package cz.bezcisobe.backend.validation;

/**
 * Marker interface implemented by ride request DTOs so {@link RideRequestValidator}
 * can read the cross-field combination uniformly across create and update.
 *
 * <p>Method names match the Java record accessor convention so request records
 * implement the interface implicitly.</p>
 */
public interface RideRequestPayload {
    String type();

    String car();

    Integer availableSeats();
}
