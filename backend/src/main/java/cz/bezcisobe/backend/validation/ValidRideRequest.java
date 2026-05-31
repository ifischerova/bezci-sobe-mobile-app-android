package cz.bezcisobe.backend.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Cross-field constraint for ride creation/update payloads. Enforces the
 * OFFER vs. REQUEST semantics that single-field annotations cannot express:
 *
 * <ul>
 *     <li>{@code type=OFFER} – {@code car} is required (a driver must declare
 *         the car) and {@code availableSeats} must be at least 1.</li>
 *     <li>{@code type=REQUEST} – {@code car} must be empty and
 *         {@code availableSeats} represents how many seats the runner needs
 *         (still validated by {@code @Min(1)} on the field).</li>
 * </ul>
 *
 * Implemented by {@link RideRequestValidator}.
 */
@Documented
@Constraint(validatedBy = RideRequestValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidRideRequest {

    String message() default "{validation.ride.combination.invalid}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
