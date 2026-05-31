package cz.bezcisobe.backend.service;

import cz.bezcisobe.backend.dto.mapper.UserMapper;
import cz.bezcisobe.backend.dto.request.LoginRequest;
import cz.bezcisobe.backend.dto.request.RegisterRequest;
import cz.bezcisobe.backend.dto.request.UpdateProfileRequest;
import cz.bezcisobe.backend.dto.response.AuthResponse;
import cz.bezcisobe.backend.dto.response.UserResponse;
import cz.bezcisobe.backend.entity.PasswordResetToken;
import cz.bezcisobe.backend.entity.Ride;
import cz.bezcisobe.backend.entity.Role;
import cz.bezcisobe.backend.entity.User;
import cz.bezcisobe.backend.entity.VerificationToken;
import cz.bezcisobe.backend.exception.BadRequestException;
import cz.bezcisobe.backend.exception.DuplicateResourceException;
import cz.bezcisobe.backend.exception.ResourceNotFoundException;
import cz.bezcisobe.backend.repository.PasswordResetTokenRepository;
import cz.bezcisobe.backend.repository.RideRepository;
import cz.bezcisobe.backend.repository.UserRepository;
import cz.bezcisobe.backend.repository.VerificationTokenRepository;
import cz.bezcisobe.backend.security.JwtTokenProvider;
import cz.bezcisobe.backend.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    private static final long VERIFICATION_TTL_HOURS = 24;
    private static final long RESET_TTL_HOURS = 1;

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final RideRepository rideRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final UserMapper userMapper;
    private final EmailService emailService;

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        log.debug("Login attempt for username='{}'", request.username());
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        String token = tokenProvider.generateToken(
                userDetails.getId().toString(),
                userDetails.getUsername(),
                roles
        );

        log.info("User '{}' logged in successfully (roles={})", userDetails.getUsername(), roles);
        return new AuthResponse(token, userDetails.getId().toString(), userDetails.getUsername(), roles);
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            log.warn("Registration rejected: username '{}' already taken", request.username());
            throw DuplicateResourceException.of("error.auth.username_exists");
        }
        if (userRepository.existsByEmail(request.email())) {
            log.warn("Registration rejected: email '{}' already taken", request.email());
            throw DuplicateResourceException.of("error.auth.email_exists");
        }

        String lang = request.language() == null || request.language().isBlank() ? "cs" : request.language();
        User user = User.builder()
                .username(request.username())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .language(lang)
                .emailVerified(false)
                .roles(Set.of(Role.ROLE_USER))
                .build();

        User saved = userRepository.save(user);
        issueVerificationToken(saved);
        log.info("New user registered (pending verification): id={}, username='{}'", saved.getId(), saved.getUsername());
        return userMapper.toResponse(saved);
    }

    @Transactional
    public void verifyEmail(String token) {
        VerificationToken vt = verificationTokenRepository.findByToken(token)
                .orElseThrow(() -> BadRequestException.of("error.auth.invalid_verification_token"));

        if (vt.isUsed()) {
            throw BadRequestException.of("error.auth.verification_token_used");
        }
        if (vt.isExpired()) {
            throw BadRequestException.of("error.auth.verification_token_expired");
        }

        User user = vt.getUser();
        user.setEmailVerified(true);
        userRepository.save(user);

        vt.setUsed(true);
        verificationTokenRepository.save(vt);
        log.info("Email verified for user id={}, username='{}'", user.getId(), user.getUsername());
    }

    /**
     * Issues a fresh verification token for an unverified user. Silent if the email
     * is unknown OR if the account is already verified so we don't leak account state.
     */
    @Transactional
    public void resendVerification(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            log.debug("Resend-verification requested for unknown email '{}', responding silently", email);
            return;
        }
        User user = userOpt.get();
        if (user.isEmailVerified()) {
            log.debug("Resend-verification requested for already-verified user '{}', responding silently", user.getUsername());
            return;
        }
        issueVerificationToken(user);
    }

    /**
     * Always responds the same way regardless of whether the email exists, so we
     * don't help attackers enumerate accounts.
     */
    @Transactional
    public void requestPasswordReset(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            log.debug("Password-reset requested for unknown email '{}', responding silently", email);
            return;
        }
        User user = userOpt.get();
        passwordResetTokenRepository.deleteAllForUser(user);

        String token = UUID.randomUUID().toString();
        PasswordResetToken prt = PasswordResetToken.builder()
                .user(user)
                .token(token)
                .expiresAt(Instant.now().plus(RESET_TTL_HOURS, ChronoUnit.HOURS))
                .build();
        passwordResetTokenRepository.save(prt);

        emailService.sendPasswordResetEmail(user.getEmail(), token, Locale.forLanguageTag(user.getLanguage()));
        log.info("Password-reset email sent to user id={}, username='{}'", user.getId(), user.getUsername());
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken prt = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> BadRequestException.of("error.auth.invalid_reset_token"));

        if (prt.isUsed()) {
            throw BadRequestException.of("error.auth.reset_token_used");
        }
        if (prt.isExpired()) {
            throw BadRequestException.of("error.auth.reset_token_expired");
        }

        User user = prt.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        // A successful password reset also implies email ownership — verify the account
        // so a user who never clicked the original verification link is not locked out.
        user.setEmailVerified(true);
        userRepository.save(user);

        prt.setUsed(true);
        passwordResetTokenRepository.save(prt);
        log.info("Password reset for user id={}, username='{}'", user.getId(), user.getUsername());
    }

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> ResourceNotFoundException.of("error.auth.user_not_found"));
        return userMapper.toResponse(user);
    }

    @Transactional
    public void changePassword(String username, String currentPassword, String newPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> ResourceNotFoundException.of("error.auth.user_not_found"));
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw BadRequestException.of("error.auth.invalid_current_password");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        try {
            emailService.sendPasswordChangedEmail(user.getEmail(), Locale.forLanguageTag(user.getLanguage()));
        } catch (Exception e) {
            log.warn("Failed to send password-changed email to {}: {}", user.getEmail(), e.getMessage());
        }
        log.info("Password changed for user id={}, username='{}'", user.getId(), user.getUsername());
    }

    @Transactional
    public UserResponse updateProfile(String username, UpdateProfileRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> ResourceNotFoundException.of("error.auth.user_not_found"));
        // Blank input is treated as "no change" rather than "clear the field" — the
        // profile form has no explicit clear action, so a submit with an empty text
        // input is almost always the user not editing that field.
        if (request.firstName() != null && !request.firstName().isBlank()) user.setFirstName(request.firstName().trim());
        if (request.lastName() != null && !request.lastName().isBlank()) user.setLastName(request.lastName().trim());
        if (request.city() != null && !request.city().isBlank()) user.setCity(request.city().trim());
        if (request.language() != null && !request.language().isBlank()) user.setLanguage(request.language());
        User saved = userRepository.save(user);
        return userMapper.toResponse(saved);
    }

    @Transactional
    public void deleteAccount(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> ResourceNotFoundException.of("error.auth.user_not_found"));
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw BadRequestException.of("error.auth.invalid_current_password");
        }

        Locale userLocale = Locale.forLanguageTag(user.getLanguage());

        // 1. Driver-rides — notify passengers, then delete
        List<Ride> driverRides = rideRepository.findAllByUser(user);
        for (Ride ride : driverRides) {
            List<User> passengersToNotify = List.copyOf(ride.getPassengers());
            for (User p : passengersToNotify) {
                tryEmail(() -> emailService.sendRideDeletedByDriverEmail(
                        p.getEmail(), p.getUsername(),
                        user.fullName(), user.getEmail(),
                        ride.getRace().getName(), ride.getRace().getDate(),
                        Locale.forLanguageTag(p.getLanguage())));
            }
            rideRepository.delete(ride);
        }

        // 2. Passenger memberships — notify driver, then remove
        List<Ride> passengerRides = rideRepository.findAllByPassengersContaining(user);
        for (Ride ride : passengerRides) {
            ride.getPassengers().remove(user);
            ride.setOccupiedSeats(ride.getOccupiedSeats() - 1);
            rideRepository.save(ride);
            tryEmail(() -> emailService.sendRideAcceptanceCancelledEmail(
                    ride.getUser().getEmail(), ride.getUser().getUsername(),
                    user.fullName(), user.getEmail(),
                    ride.getRace().getName(), ride.getRace().getDate(),
                    Locale.forLanguageTag(ride.getUser().getLanguage())));
        }

        // 3. Tokens
        verificationTokenRepository.deleteAllForUser(user);
        passwordResetTokenRepository.deleteAllForUser(user);

        // 4. Send account-deleted email BEFORE deleting the user (need email + locale)
        tryEmail(() -> emailService.sendAccountDeletedEmail(user.getEmail(), userLocale));

        // 5. Delete user
        userRepository.delete(user);
        log.info("Account deleted: id={}, username='{}'", user.getId(), user.getUsername());
    }

    private void tryEmail(Runnable send) {
        try {
            send.run();
        } catch (Exception e) {
            log.warn("Mail send failed during account deletion: {}", e.getMessage());
        }
    }

    private void issueVerificationToken(User user) {
        verificationTokenRepository.deleteAllForUser(user);
        String token = UUID.randomUUID().toString();
        VerificationToken vt = VerificationToken.builder()
                .user(user)
                .token(token)
                .expiresAt(Instant.now().plus(VERIFICATION_TTL_HOURS, ChronoUnit.HOURS))
                .build();
        verificationTokenRepository.save(vt);
        emailService.sendVerificationEmail(user.getEmail(), token, Locale.forLanguageTag(user.getLanguage()));
    }
}
