package cz.bezcisobe.backend.service;

import cz.bezcisobe.backend.dto.mapper.RideMapper;
import cz.bezcisobe.backend.dto.request.CreateRideRequest;
import cz.bezcisobe.backend.dto.request.UpdateRideRequest;
import cz.bezcisobe.backend.dto.response.RideResponse;
import cz.bezcisobe.backend.entity.*;
import cz.bezcisobe.backend.exception.BadRequestException;
import cz.bezcisobe.backend.exception.ResourceNotFoundException;
import cz.bezcisobe.backend.repository.RaceRepository;
import cz.bezcisobe.backend.repository.RideRepository;
import cz.bezcisobe.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RideServiceTest {

    @Mock private RideRepository rideRepository;
    @Mock private RaceRepository raceRepository;
    @Mock private UserRepository userRepository;
    @Mock private RideMapper rideMapper;
    @Mock private EmailService emailService;

    @InjectMocks
    private RideService rideService;

    private UUID rideId;
    private UUID ownerId;
    private UUID passengerId;
    private Ride ride;
    private Race futureRace;
    private User owner;
    private User passenger;

    @BeforeEach
    void setUp() {
        rideId = UUID.randomUUID();
        ownerId = UUID.randomUUID();
        passengerId = UUID.randomUUID();

        owner = User.builder()
                .id(ownerId)
                .username("owner")
                .email("owner@example.com")
                .firstName("Oto")
                .lastName("Vlk")
                .language("cs")
                .build();
        passenger = User.builder()
                .id(passengerId)
                .username("passenger")
                .email("passenger@example.com")
                .firstName("Pavel")
                .lastName("Cerny")
                .language("en")
                .build();

        futureRace = Race.builder()
                .id(1L)
                .name("Future race")
                .date(LocalDate.now().plusDays(30))
                .build();

        ride = Ride.builder()
                .id(rideId)
                .race(futureRace)
                .user(owner)
                .type(RideType.OFFER)
                .from("Praha")
                .availableSeats(3)
                .occupiedSeats(0)
                .passengers(new HashSet<>())
                .createdAt(Instant.now())
                .build();
    }

    @Test
    void acceptRide_success() {
        when(rideRepository.findById(rideId)).thenReturn(Optional.of(ride));
        when(userRepository.findById(passengerId)).thenReturn(Optional.of(passenger));
        when(rideRepository.save(any())).thenReturn(ride);
        when(rideMapper.toResponse(any())).thenReturn(
                new RideResponse(rideId.toString(), "1", false, ownerId.toString(),
                        "owner", "Owner", "Tester", "OFFER",
                        "Praha", null, null, 3, 1, List.of(passengerId.toString()), null, Instant.now().toString()));

        RideResponse result = rideService.acceptRide(rideId, passengerId);

        assertNotNull(result);
        assertEquals(1, ride.getOccupiedSeats());
        assertTrue(ride.getPassengers().contains(passenger));
    }

    @Test
    void acceptRide_full() {
        ride.setOccupiedSeats(3);
        when(rideRepository.findById(rideId)).thenReturn(Optional.of(ride));
        when(userRepository.findById(passengerId)).thenReturn(Optional.of(passenger));

        assertThrows(BadRequestException.class, () -> rideService.acceptRide(rideId, passengerId));
    }

    @Test
    void acceptRide_duplicate() {
        ride.getPassengers().add(passenger);
        when(rideRepository.findById(rideId)).thenReturn(Optional.of(ride));
        when(userRepository.findById(passengerId)).thenReturn(Optional.of(passenger));

        assertThrows(BadRequestException.class, () -> rideService.acceptRide(rideId, passengerId));
    }

    @Test
    void cancelRideAcceptance_notPassenger() {
        when(rideRepository.findById(rideId)).thenReturn(Optional.of(ride));

        assertThrows(BadRequestException.class, () -> rideService.cancelRideAcceptance(rideId, passengerId));
    }

    @Test
    void deleteRide_notOwner() {
        when(rideRepository.findById(rideId)).thenReturn(Optional.of(ride));

        assertThrows(BadRequestException.class, () -> rideService.deleteRide(rideId, passengerId));
    }

    @Test
    void deleteRide_notFound() {
        when(rideRepository.findById(rideId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> rideService.deleteRide(rideId, ownerId));
    }

    @Test
    void updateRide_byOwner_success() {
        UpdateRideRequest req = new UpdateRideRequest(
                "OFFER", "Brno", "Praha", "Škoda", 2, "Updated note");
        when(rideRepository.findById(rideId)).thenReturn(Optional.of(ride));
        when(rideRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(rideMapper.toResponse(any())).thenReturn(
                new RideResponse(rideId.toString(), "1", false, ownerId.toString(),
                        "owner", "Owner", "Tester", "OFFER",
                        "Brno", "Praha", "Škoda", 2, 0, List.of(), "Updated note", Instant.now().toString()));

        RideResponse result = rideService.updateRide(rideId, req, ownerId);

        assertNotNull(result);
        assertEquals("Brno", ride.getFrom());
        assertEquals("Škoda", ride.getCar());
        assertEquals(2, ride.getAvailableSeats());
    }

    @Test
    void updateRide_notOwner() {
        UpdateRideRequest req = new UpdateRideRequest(
                "OFFER", "Brno", "Praha", "Škoda", 2, null);
        when(rideRepository.findById(rideId)).thenReturn(Optional.of(ride));

        assertThrows(BadRequestException.class,
                () -> rideService.updateRide(rideId, req, passengerId));
    }

    @Test
    void updateRide_seatsBelowOccupied() {
        ride.setOccupiedSeats(2);
        UpdateRideRequest req = new UpdateRideRequest(
                "OFFER", "Praha", null, "Škoda", 1, null);
        when(rideRepository.findById(rideId)).thenReturn(Optional.of(ride));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> rideService.updateRide(rideId, req, ownerId));
        assertEquals("error.ride.seats_below_passengers", ex.getMessageKey(),
                "Should signal seats-below-passengers via message key");
        assertNotNull(ex.getArgs(), "Args must carry the occupant count for interpolation");
        assertEquals(2, ex.getArgs()[0],
                "Error args should mention current occupant count");
    }

    @Test
    void updateRide_notFound() {
        UpdateRideRequest req = new UpdateRideRequest(
                "OFFER", "Praha", null, "Škoda", 1, null);
        when(rideRepository.findById(rideId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> rideService.updateRide(rideId, req, ownerId));
    }

    @Test
    void deleteRideAsAdmin_success() {
        UUID adminId = UUID.randomUUID();
        when(rideRepository.findById(rideId)).thenReturn(Optional.of(ride));

        rideService.deleteRideAsAdmin(rideId, adminId);

        verify(rideRepository).delete(ride);
    }

    @Test
    void deleteRideAsAdmin_notFound() {
        when(rideRepository.findById(rideId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> rideService.deleteRideAsAdmin(rideId, UUID.randomUUID()));
    }

    @Test
    void createRide_pastRace_rejected() {
        Race pastRace = Race.builder()
                .id(99L)
                .name("Past race")
                .date(LocalDate.now().minusDays(1))
                .build();
        CreateRideRequest req = new CreateRideRequest(
                99L, "OFFER", "Praha", "Brno", "Škoda", 3, null);
        when(raceRepository.findById(99L)).thenReturn(Optional.of(pastRace));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> rideService.createRide(req, ownerId));
        assertEquals("error.ride.cannot_create_past", ex.getMessageKey(),
                "Error message key should explain the race already happened");
        verify(rideRepository, never()).save(any());
    }

    @Test
    void acceptRide_pastRace_rejected() {
        ride.setRace(Race.builder()
                .id(98L)
                .name("Past race")
                .date(LocalDate.now().minusDays(1))
                .build());
        when(rideRepository.findById(rideId)).thenReturn(Optional.of(ride));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> rideService.acceptRide(rideId, passengerId));
        assertEquals("error.ride.cannot_accept_past", ex.getMessageKey(),
                "Error message key should explain the race already happened");
        assertEquals(0, ride.getOccupiedSeats(), "Occupied seats must not change");
        assertTrue(ride.getPassengers().isEmpty(), "Passenger list must not change");
    }

    @Test
    void acceptRide_sendsEmailToDriver() {
        when(rideRepository.findById(rideId)).thenReturn(Optional.of(ride));
        when(userRepository.findById(passengerId)).thenReturn(Optional.of(passenger));
        when(rideRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(rideMapper.toResponse(any())).thenReturn(
                new RideResponse(rideId.toString(), "1", false, ownerId.toString(),
                        "owner", "Oto", "Vlk", "OFFER",
                        "Praha", null, null, 3, 1, List.of(passengerId.toString()), null, Instant.now().toString()));

        rideService.acceptRide(rideId, passengerId);

        verify(emailService).sendRideAcceptedEmail(
                eq(owner.getEmail()),
                eq(owner.getUsername()),
                eq(passenger.fullName()),
                eq(passenger.getEmail()),
                eq(futureRace.getName()),
                eq(futureRace.getDate()),
                eq(Locale.forLanguageTag(owner.getLanguage())));
    }

    @Test
    void acceptRide_mailFailure_doesNotRollback() {
        when(rideRepository.findById(rideId)).thenReturn(Optional.of(ride));
        when(userRepository.findById(passengerId)).thenReturn(Optional.of(passenger));
        when(rideRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(rideMapper.toResponse(any())).thenReturn(
                new RideResponse(rideId.toString(), "1", false, ownerId.toString(),
                        "owner", "Oto", "Vlk", "OFFER",
                        "Praha", null, null, 3, 1, List.of(passengerId.toString()), null, Instant.now().toString()));
        doThrow(new RuntimeException("smtp down"))
                .when(emailService).sendRideAcceptedEmail(
                        any(), any(), any(), any(), any(), any(), any());

        // Must not propagate — mail is best-effort.
        assertDoesNotThrow(() -> rideService.acceptRide(rideId, passengerId));
        assertEquals(1, ride.getOccupiedSeats(), "State change must persist even if email fails");
        assertTrue(ride.getPassengers().contains(passenger));
    }

    @Test
    void cancelRideAcceptance_sendsEmailToDriver() {
        ride.getPassengers().add(passenger);
        ride.setOccupiedSeats(1);
        when(rideRepository.findById(rideId)).thenReturn(Optional.of(ride));
        when(rideRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(rideMapper.toResponse(any())).thenReturn(
                new RideResponse(rideId.toString(), "1", false, ownerId.toString(),
                        "owner", "Oto", "Vlk", "OFFER",
                        "Praha", null, null, 3, 0, List.of(), null, Instant.now().toString()));

        rideService.cancelRideAcceptance(rideId, passengerId);

        verify(emailService).sendRideAcceptanceCancelledEmail(
                eq(owner.getEmail()),
                eq(owner.getUsername()),
                eq(passenger.fullName()),
                eq(passenger.getEmail()),
                eq(futureRace.getName()),
                eq(futureRace.getDate()),
                eq(Locale.forLanguageTag(owner.getLanguage())));
    }

    @Test
    void deleteRide_sendsEmailsToAllPassengers() {
        UUID secondPassengerId = UUID.randomUUID();
        User secondPassenger = User.builder()
                .id(secondPassengerId)
                .username("paco")
                .email("paco@example.com")
                .firstName("Pavla")
                .lastName("Černá")
                .language("cs")
                .build();
        ride.getPassengers().add(passenger);
        ride.getPassengers().add(secondPassenger);
        ride.setOccupiedSeats(2);
        when(rideRepository.findById(rideId)).thenReturn(Optional.of(ride));

        rideService.deleteRide(rideId, ownerId);

        verify(emailService).sendRideDeletedByDriverEmail(
                eq(passenger.getEmail()),
                eq(passenger.getUsername()),
                eq(owner.fullName()),
                eq(owner.getEmail()),
                eq(futureRace.getName()),
                eq(futureRace.getDate()),
                eq(Locale.forLanguageTag(passenger.getLanguage())));
        verify(emailService).sendRideDeletedByDriverEmail(
                eq(secondPassenger.getEmail()),
                eq(secondPassenger.getUsername()),
                eq(owner.fullName()),
                eq(owner.getEmail()),
                eq(futureRace.getName()),
                eq(futureRace.getDate()),
                eq(Locale.forLanguageTag(secondPassenger.getLanguage())));
        verify(emailService, times(2)).sendRideDeletedByDriverEmail(
                any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void deleteRide_noPassengers_sendsNoEmails() {
        // REQUEST-type or empty OFFER: no passengers means no notifications.
        when(rideRepository.findById(rideId)).thenReturn(Optional.of(ride));

        rideService.deleteRide(rideId, ownerId);

        verifyNoInteractions(emailService);
    }

    @Test
    void deleteRideAsAdmin_emailsDriverAndAllPassengers() {
        UUID adminId = UUID.randomUUID();
        UUID secondPassengerId = UUID.randomUUID();
        User secondPassenger = User.builder()
                .id(secondPassengerId)
                .username("paco")
                .email("paco@example.com")
                .firstName("Pavla")
                .lastName("Černá")
                .language("cs")
                .build();
        ride.getPassengers().add(passenger);
        ride.getPassengers().add(secondPassenger);
        ride.setOccupiedSeats(2);
        when(rideRepository.findById(rideId)).thenReturn(Optional.of(ride));

        rideService.deleteRideAsAdmin(rideId, adminId);

        verify(emailService).sendRideDeletedByAdminToDriverEmail(
                eq(owner.getEmail()),
                eq(owner.getUsername()),
                eq(futureRace.getName()),
                eq(futureRace.getDate()),
                eq(Locale.forLanguageTag(owner.getLanguage())));
        verify(emailService).sendRideDeletedByAdminToPassengerEmail(
                eq(passenger.getEmail()),
                eq(passenger.getUsername()),
                eq(futureRace.getName()),
                eq(futureRace.getDate()),
                eq(Locale.forLanguageTag(passenger.getLanguage())));
        verify(emailService).sendRideDeletedByAdminToPassengerEmail(
                eq(secondPassenger.getEmail()),
                eq(secondPassenger.getUsername()),
                eq(futureRace.getName()),
                eq(futureRace.getDate()),
                eq(Locale.forLanguageTag(secondPassenger.getLanguage())));
        verify(emailService, times(2)).sendRideDeletedByAdminToPassengerEmail(
                any(), any(), any(), any(), any());
    }

    @Test
    void deleteRideAsAdmin_requestRide_notifiesDriverOnly() {
        UUID adminId = UUID.randomUUID();
        // REQUEST rides never collect passengers, so only the driver hears about it.
        ride.setType(RideType.REQUEST);
        when(rideRepository.findById(rideId)).thenReturn(Optional.of(ride));

        rideService.deleteRideAsAdmin(rideId, adminId);

        verify(emailService).sendRideDeletedByAdminToDriverEmail(
                eq(owner.getEmail()),
                eq(owner.getUsername()),
                eq(futureRace.getName()),
                eq(futureRace.getDate()),
                eq(Locale.forLanguageTag(owner.getLanguage())));
        verify(emailService, never()).sendRideDeletedByAdminToPassengerEmail(
                any(), any(), any(), any(), any());
    }
}
