package cz.bezcisobe.backend.service;

import cz.bezcisobe.backend.dto.mapper.UserMapper;
import cz.bezcisobe.backend.dto.request.UpdateProfileRequest;
import cz.bezcisobe.backend.dto.response.UserResponse;
import cz.bezcisobe.backend.entity.Race;
import cz.bezcisobe.backend.entity.Ride;
import cz.bezcisobe.backend.entity.RideType;
import cz.bezcisobe.backend.entity.Role;
import cz.bezcisobe.backend.entity.User;
import cz.bezcisobe.backend.exception.BadRequestException;
import cz.bezcisobe.backend.repository.PasswordResetTokenRepository;
import cz.bezcisobe.backend.repository.RideRepository;
import cz.bezcisobe.backend.repository.UserRepository;
import cz.bezcisobe.backend.repository.VerificationTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceProfileTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock EmailService emailService;
    @Mock UserMapper userMapper;
    @Mock RideRepository rideRepository;
    @Mock VerificationTokenRepository verificationTokenRepository;
    @Mock PasswordResetTokenRepository passwordResetTokenRepository;
    @InjectMocks AuthService authService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(UUID.randomUUID())
                .username("ivka")
                .email("ivka@example.com")
                .password("HASHED_OLD")
                .language("cs")
                .emailVerified(true)
                .roles(Set.of(Role.ROLE_USER))
                .build();
    }

    @Test
    void changePassword_savesAndNotifies() {
        when(userRepository.findByUsername("ivka")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("old", "HASHED_OLD")).thenReturn(true);
        when(passwordEncoder.encode("newPass")).thenReturn("HASHED_NEW");

        authService.changePassword("ivka", "old", "newPass");

        verify(userRepository).save(argThat(u -> u.getPassword().equals("HASHED_NEW")));
        verify(emailService).sendPasswordChangedEmail(eq("ivka@example.com"), any());
    }

    @Test
    void changePassword_rejectsWrongCurrent() {
        when(userRepository.findByUsername("ivka")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "HASHED_OLD")).thenReturn(false);

        assertThatThrownBy(() -> authService.changePassword("ivka", "wrong", "newPass"))
                .isInstanceOf(BadRequestException.class)
                .extracting("messageKey").isEqualTo("error.auth.invalid_current_password");

        verify(userRepository, never()).save(any());
        verify(emailService, never()).sendPasswordChangedEmail(any(), any());
    }

    @Test
    void updateProfile_partialUpdateSavesAndReturnsResponse() {
        when(userRepository.findByUsername("ivka")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userMapper.toResponse(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            return new UserResponse(
                    u.getId() == null ? null : u.getId().toString(),
                    u.getUsername(),
                    u.getEmail(),
                    u.getFirstName(),
                    u.getLastName(),
                    u.getCity(),
                    u.getLanguage(),
                    List.of("ROLE_USER")
            );
        });

        UpdateProfileRequest req = new UpdateProfileRequest("Iva", null, "Praha", "en");
        UserResponse resp = authService.updateProfile("ivka", req);

        assertThat(resp.firstName()).isEqualTo("Iva");
        assertThat(resp.city()).isEqualTo("Praha");
        assertThat(resp.language()).isEqualTo("en");
    }

    @Test
    void deleteAccount_rejectsWrongPassword() {
        when(userRepository.findByUsername("ivka")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "HASHED_OLD")).thenReturn(false);

        assertThatThrownBy(() -> authService.deleteAccount("ivka", "wrong"))
                .isInstanceOf(BadRequestException.class)
                .extracting("messageKey").isEqualTo("error.auth.invalid_current_password");

        verify(userRepository, never()).delete(any(User.class));
        verify(emailService, never()).sendAccountDeletedEmail(any(), any());
    }

    @Test
    void deleteAccount_cascadesAndSendsEmails() {
        when(userRepository.findByUsername("ivka")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("mypwd", "HASHED_OLD")).thenReturn(true);

        // Set up: user has 1 driver-ride with 1 passenger + is a passenger in 1 other ride.
        User otherDriver = makeUser("driverguy", "driver@example.com");
        User somePassenger = makeUser("pasenger", "passenger@example.com");
        Race race1 = makeFutureRace("Praha Maraton");
        Race race2 = makeFutureRace("Brno Půlmaraton");
        Ride driverRide = Ride.builder()
                .id(UUID.randomUUID())
                .user(user)
                .race(race1)
                .type(RideType.OFFER)
                .passengers(new HashSet<>(Set.of(somePassenger)))
                .occupiedSeats(1)
                .availableSeats(3)
                .build();
        Ride passengerRide = Ride.builder()
                .id(UUID.randomUUID())
                .user(otherDriver)
                .race(race2)
                .type(RideType.OFFER)
                .passengers(new HashSet<>(Set.of(user)))
                .occupiedSeats(1)
                .availableSeats(3)
                .build();

        when(rideRepository.findAllByUser(user)).thenReturn(List.of(driverRide));
        when(rideRepository.findAllByPassengersContaining(user)).thenReturn(List.of(passengerRide));

        authService.deleteAccount("ivka", "mypwd");

        // Verify passenger of driver-ride got notified
        verify(emailService).sendRideDeletedByDriverEmail(
                eq(somePassenger.getEmail()), eq(somePassenger.getUsername()),
                any(), eq(user.getEmail()),
                eq(race1.getName()), eq(race1.getDate()),
                any());

        // Verify the other driver got notified
        verify(emailService).sendRideAcceptanceCancelledEmail(
                eq(otherDriver.getEmail()), eq(otherDriver.getUsername()),
                any(), eq(user.getEmail()),
                eq(race2.getName()), eq(race2.getDate()),
                any());

        // Verify user got the account-deleted email
        verify(emailService).sendAccountDeletedEmail(eq(user.getEmail()), any());

        // Verify cascade: tokens cleared, user deleted
        verify(verificationTokenRepository).deleteAllForUser(user);
        verify(passwordResetTokenRepository).deleteAllForUser(user);
        verify(rideRepository).delete(driverRide);
        verify(userRepository).delete(user);
    }

    private static User makeUser(String username, String email) {
        return User.builder()
                .id(UUID.randomUUID())
                .username(username)
                .email(email)
                .password("HASHED")
                .language("cs")
                .emailVerified(true)
                .roles(Set.of(Role.ROLE_USER))
                .build();
    }

    private static Race makeFutureRace(String name) {
        return Race.builder()
                .id(System.nanoTime() & 0xFFFF)
                .name(name)
                .place("Praha")
                .date(LocalDate.now().plusDays(30))
                .startTime(LocalTime.of(10, 0))
                .build();
    }
}
