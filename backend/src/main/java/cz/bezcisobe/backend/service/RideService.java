package cz.bezcisobe.backend.service;

import cz.bezcisobe.backend.dto.mapper.RideMapper;
import cz.bezcisobe.backend.dto.request.CreateRideRequest;
import cz.bezcisobe.backend.dto.request.UpdateRideRequest;
import cz.bezcisobe.backend.dto.response.RideResponse;
import cz.bezcisobe.backend.entity.Race;
import cz.bezcisobe.backend.entity.Ride;
import cz.bezcisobe.backend.entity.RideType;
import cz.bezcisobe.backend.entity.User;
import cz.bezcisobe.backend.exception.BadRequestException;
import cz.bezcisobe.backend.exception.ResourceNotFoundException;
import cz.bezcisobe.backend.repository.RaceRepository;
import cz.bezcisobe.backend.repository.RideRepository;
import cz.bezcisobe.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Business logic for ride OFFERs and REQUESTs. Owns ownership and seat-count
 * invariants — the controller layer never mutates rides directly.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class RideService {

    // Past-race check uses Czech wall-clock so the cutoff matches what
    // Czech users see in the UI, regardless of JVM timezone.
    private static final ZoneId APP_ZONE = ZoneId.of("Europe/Prague");

    private final RideRepository rideRepository;
    private final RaceRepository raceRepository;
    private final UserRepository userRepository;
    private final RideMapper rideMapper;
    private final EmailService emailService;

    /**
     * Returns all rides for one race, OFFER and REQUEST mixed.
     */
    @Transactional(readOnly = true)
    public List<RideResponse> getRidesByRace(Long raceId) {
        return rideRepository.findByRaceId(raceId).stream()
                .map(rideMapper::toResponse)
                .toList();
    }

    /**
     * Creates a new ride owned by {@code userId}. The cross-field validation
     * has already enforced OFFER/REQUEST semantics on the request body.
     */
    @Transactional
    public RideResponse createRide(CreateRideRequest request, UUID userId) {
        Race race = raceRepository.findById(request.raceId())
                .orElseThrow(() -> {
                    log.warn("Race {} not found while creating ride", request.raceId());
                    return ResourceNotFoundException.of("error.ride.race_not_found");
                });
        rejectIfRaceAlreadyHappened(race, "error.ride.cannot_create_past");

        User user = userRepository.findById(userId)
                .orElseThrow(() -> ResourceNotFoundException.of("error.auth.user_not_found"));

        RideType type = parseType(request.type());

        Ride ride = Ride.builder()
                .race(race)
                .user(user)
                .type(type)
                .from(request.from())
                .to(request.to())
                .car(request.car())
                .availableSeats(request.availableSeats())
                .occupiedSeats(0)
                .notes(request.notes())
                .build();

        Ride saved = rideRepository.save(ride);
        log.info("Ride {} ({}) created by user {} for race {}",
                saved.getId(), saved.getType(), userId, race.getId());
        return rideMapper.toResponse(saved);
    }

    /**
     * Updates an existing ride. Only the ride's owner can update it. The new
     * {@code availableSeats} value cannot drop below the number of passengers
     * already accepted — that would silently strand them.
     */
    @Transactional
    public RideResponse updateRide(UUID rideId, UpdateRideRequest request, UUID userId) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> ResourceNotFoundException.of("error.ride.not_found"));

        if (!ride.getUser().getId().equals(userId)) {
            log.warn("User {} attempted to update ride {} owned by {}",
                    userId, rideId, ride.getUser().getId());
            throw BadRequestException.of("error.ride.no_permission_to_update");
        }

        if (request.availableSeats() < ride.getOccupiedSeats()) {
            throw BadRequestException.of("error.ride.seats_below_passengers", ride.getOccupiedSeats());
        }

        ride.setType(parseType(request.type()));
        ride.setFrom(request.from());
        ride.setTo(request.to());
        ride.setCar(request.car());
        ride.setAvailableSeats(request.availableSeats());
        ride.setNotes(request.notes());

        Ride saved = rideRepository.save(ride);
        log.info("Ride {} updated by owner {}", rideId, userId);
        return rideMapper.toResponse(saved);
    }

    /**
     * Deletes a ride. Only the owner can delete their own ride; admins go
     * through {@link #deleteRideAsAdmin}. Each current passenger receives a
     * best-effort email notification.
     */
    @Transactional
    public void deleteRide(UUID rideId, UUID userId) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> ResourceNotFoundException.of("error.ride.not_found"));

        if (!ride.getUser().getId().equals(userId)) {
            log.warn("User {} attempted to delete ride {} owned by {}",
                    userId, rideId, ride.getUser().getId());
            throw BadRequestException.of("error.ride.no_permission_to_delete");
        }

        // Snapshot recipients before deletion so we still have a usable list
        // after the JPA cascade removes the passenger join rows.
        User driver = ride.getUser();
        Race race = ride.getRace();
        List<User> passengersToNotify = List.copyOf(ride.getPassengers());

        rideRepository.delete(ride);
        log.info("Ride {} deleted by owner {}", rideId, userId);

        for (User p : passengersToNotify) {
            notifyPassengerOfDriverDeletion(p, driver, race);
        }
    }

    /**
     * Admin-only force delete: removes a ride regardless of ownership.
     * Authorization is enforced at the controller via {@code @PreAuthorize}.
     * The driver and every current passenger receive a best-effort email.
     */
    @Transactional
    public void deleteRideAsAdmin(UUID rideId, UUID adminId) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> ResourceNotFoundException.of("error.ride.not_found"));

        User driver = ride.getUser();
        Race race = ride.getRace();
        List<User> passengersToNotify = List.copyOf(ride.getPassengers());

        rideRepository.delete(ride);
        log.warn("Admin {} force-deleted ride {} (was owned by {})",
                adminId, rideId, driver.getId());

        notifyDriverOfAdminDeletion(driver, race);
        for (User p : passengersToNotify) {
            notifyPassengerOfAdminDeletion(p, race);
        }
    }

    /**
     * Adds {@code passengerId} to the ride's passenger list and increments
     * {@code occupiedSeats}. Rejects double-bookings and full rides.
     */
    @Transactional
    public RideResponse acceptRide(UUID rideId, UUID passengerId) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> ResourceNotFoundException.of("error.ride.not_found"));
        rejectIfRaceAlreadyHappened(ride.getRace(), "error.ride.cannot_accept_past");

        User passenger = userRepository.findById(passengerId)
                .orElseThrow(() -> ResourceNotFoundException.of("error.auth.user_not_found"));

        if (ride.getOccupiedSeats() >= ride.getAvailableSeats()) {
            throw BadRequestException.of("error.ride.no_seats");
        }

        boolean alreadyPassenger = ride.getPassengers().stream()
                .anyMatch(p -> p.getId().equals(passengerId));
        if (alreadyPassenger) {
            throw BadRequestException.of("error.ride.already_joined");
        }

        ride.getPassengers().add(passenger);
        ride.setOccupiedSeats(ride.getOccupiedSeats() + 1);
        Ride saved = rideRepository.save(ride);
        log.info("User {} accepted ride {} (now {}/{} seats)",
                passengerId, rideId, saved.getOccupiedSeats(), saved.getAvailableSeats());

        notifyDriverOfAcceptance(saved.getUser(), passenger, saved.getRace());
        return rideMapper.toResponse(saved);
    }

    /**
     * Removes the current user from a ride's passenger list.
     */
    @Transactional
    public RideResponse cancelRideAcceptance(UUID rideId, UUID passengerId) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> ResourceNotFoundException.of("error.ride.not_found"));

        // Find the passenger user object before removing it so we can include
        // their details in the email to the driver.
        User passenger = ride.getPassengers().stream()
                .filter(p -> p.getId().equals(passengerId))
                .findFirst()
                .orElse(null);

        boolean removed = ride.getPassengers().removeIf(p -> p.getId().equals(passengerId));
        if (!removed) {
            throw BadRequestException.of("error.ride.not_joined");
        }

        ride.setOccupiedSeats(ride.getOccupiedSeats() - 1);
        Ride saved = rideRepository.save(ride);
        log.info("User {} cancelled their seat on ride {}", passengerId, rideId);

        if (passenger != null) {
            notifyDriverOfCancellation(saved.getUser(), passenger, saved.getRace());
        }
        return rideMapper.toResponse(saved);
    }

    private RideType parseType(String value) {
        try {
            return RideType.valueOf(value);
        } catch (IllegalArgumentException e) {
            throw BadRequestException.of("error.ride.invalid_type");
        }
    }

    /**
     * Carpooling is meaningful only up to race day. Once the race date has
     * passed, no new rides may be created and no offers may be accepted —
     * editing or deleting existing rides remains allowed so users can clean
     * up their history.
     */
    private void rejectIfRaceAlreadyHappened(Race race, String messageKey) {
        if (race.getDate().isBefore(LocalDate.now(APP_ZONE))) {
            log.warn("Rejected action on past race {} (date={})", race.getId(), race.getDate());
            throw BadRequestException.of(messageKey);
        }
    }

    // ---- Best-effort email notifications -----------------------------------
    //
    // Each helper wraps the EmailService call in a try/catch that logs WARN
    // and swallows the exception. Mail delivery problems must NOT roll back
    // the ride-state mutation that already succeeded.

    private void notifyDriverOfAcceptance(User driver, User passenger, Race race) {
        try {
            emailService.sendRideAcceptedEmail(
                    driver.getEmail(), driver.getUsername(),
                    passenger.fullName(), passenger.getEmail(),
                    race.getName(), race.getDate(),
                    Locale.forLanguageTag(driver.getLanguage()));
        } catch (Exception e) {
            log.warn("Failed to send ride-accepted email to driver {}: {}",
                    driver.getEmail(), e.getMessage());
        }
    }

    private void notifyDriverOfCancellation(User driver, User passenger, Race race) {
        try {
            emailService.sendRideAcceptanceCancelledEmail(
                    driver.getEmail(), driver.getUsername(),
                    passenger.fullName(), passenger.getEmail(),
                    race.getName(), race.getDate(),
                    Locale.forLanguageTag(driver.getLanguage()));
        } catch (Exception e) {
            log.warn("Failed to send ride-acceptance-cancelled email to driver {}: {}",
                    driver.getEmail(), e.getMessage());
        }
    }

    private void notifyPassengerOfDriverDeletion(User passenger, User driver, Race race) {
        try {
            emailService.sendRideDeletedByDriverEmail(
                    passenger.getEmail(), passenger.getUsername(),
                    driver.fullName(), driver.getEmail(),
                    race.getName(), race.getDate(),
                    Locale.forLanguageTag(passenger.getLanguage()));
        } catch (Exception e) {
            log.warn("Failed to send ride-deleted email to passenger {}: {}",
                    passenger.getEmail(), e.getMessage());
        }
    }

    private void notifyDriverOfAdminDeletion(User driver, Race race) {
        try {
            emailService.sendRideDeletedByAdminToDriverEmail(
                    driver.getEmail(), driver.getUsername(),
                    race.getName(), race.getDate(),
                    Locale.forLanguageTag(driver.getLanguage()));
        } catch (Exception e) {
            log.warn("Failed to send admin-deleted email to driver {}: {}",
                    driver.getEmail(), e.getMessage());
        }
    }

    private void notifyPassengerOfAdminDeletion(User passenger, Race race) {
        try {
            emailService.sendRideDeletedByAdminToPassengerEmail(
                    passenger.getEmail(), passenger.getUsername(),
                    race.getName(), race.getDate(),
                    Locale.forLanguageTag(passenger.getLanguage()));
        } catch (Exception e) {
            log.warn("Failed to send admin-deleted email to passenger {}: {}",
                    passenger.getEmail(), e.getMessage());
        }
    }
}
