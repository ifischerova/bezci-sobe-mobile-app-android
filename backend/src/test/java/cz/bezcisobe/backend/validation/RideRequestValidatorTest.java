package cz.bezcisobe.backend.validation;

import cz.bezcisobe.backend.dto.request.CreateRideRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RideRequestValidatorTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    @Test
    void offer_withoutCar_isRejected() {
        CreateRideRequest req = new CreateRideRequest(
                1L, "OFFER", "Praha", "Brno", null, 3, null);

        Set<ConstraintViolation<CreateRideRequest>> violations = validator.validate(req);

        assertTrue(violations.stream().anyMatch(v ->
                v.getPropertyPath().toString().equals("car")
                        && v.getMessage().contains("auto")));
    }

    @Test
    void offer_withCarAndSeats_isValid() {
        CreateRideRequest req = new CreateRideRequest(
                1L, "OFFER", "Praha", "Brno", "Škoda Octavia", 3, "Pojedu rychle");

        assertEquals(0, validator.validate(req).size());
    }

    @Test
    void request_withCar_isRejected() {
        CreateRideRequest req = new CreateRideRequest(
                1L, "REQUEST", "Praha", null, "Škoda", 1, null);

        Set<ConstraintViolation<CreateRideRequest>> violations = validator.validate(req);

        assertTrue(violations.stream().anyMatch(v ->
                v.getPropertyPath().toString().equals("car")
                        && v.getMessage().contains("REQUEST")));
    }

    @Test
    void request_withoutCar_isValid() {
        CreateRideRequest req = new CreateRideRequest(
                1L, "REQUEST", "Praha", null, null, 1, "Hledám svezení");

        assertEquals(0, validator.validate(req).size());
    }

    @Test
    void unknownType_isRejected() {
        CreateRideRequest req = new CreateRideRequest(
                1L, "WAT", "Praha", null, null, 1, null);

        Set<ConstraintViolation<CreateRideRequest>> violations = validator.validate(req);

        assertTrue(violations.stream().anyMatch(v ->
                v.getPropertyPath().toString().equals("type")
                        && v.getMessage().contains("OFFER")));
    }

    @Test
    void offer_withZeroSeats_isRejected() {
        CreateRideRequest req = new CreateRideRequest(
                1L, "OFFER", "Praha", "Brno", "Škoda", 0, null);

        // The bean's @Min(1) handles zero. Ensure the ride-level validator
        // doesn't *also* fire its own seats violation for OFFER, just the @Min.
        Set<ConstraintViolation<CreateRideRequest>> violations = validator.validate(req);
        assertTrue(violations.stream().anyMatch(v ->
                v.getPropertyPath().toString().equals("availableSeats")));
    }
}
