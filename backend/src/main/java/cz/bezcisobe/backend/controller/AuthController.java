package cz.bezcisobe.backend.controller;

import cz.bezcisobe.backend.dto.request.ChangePasswordRequest;
import cz.bezcisobe.backend.dto.request.DeleteAccountRequest;
import cz.bezcisobe.backend.dto.request.ForgotPasswordRequest;
import cz.bezcisobe.backend.dto.request.LoginRequest;
import cz.bezcisobe.backend.dto.request.RegisterRequest;
import cz.bezcisobe.backend.dto.request.ResendVerificationRequest;
import cz.bezcisobe.backend.dto.request.ResetPasswordRequest;
import cz.bezcisobe.backend.dto.request.UpdateProfileRequest;
import cz.bezcisobe.backend.dto.response.AuthResponse;
import cz.bezcisobe.backend.dto.response.ErrorResponse;
import cz.bezcisobe.backend.dto.response.UserResponse;
import cz.bezcisobe.backend.security.UserDetailsImpl;
import cz.bezcisobe.backend.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Login, registration, email verification, password reset")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @SecurityRequirements // public — overrides global bearerAuth
    @Operation(summary = "Authenticate and obtain a JWT")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Authentication succeeded"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Account exists but email not verified",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/register")
    @SecurityRequirements // public
    @Operation(summary = "Register a new user; sends a verification email")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User created — verification email sent"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "409", description = "Username or email already taken")
    })
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @GetMapping("/verify-email")
    @SecurityRequirements // public
    @Operation(summary = "Verify an email address using the token from the verification mail")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Email verified"),
            @ApiResponse(responseCode = "400", description = "Token missing, expired, or already used")
    })
    public ResponseEntity<Void> verifyEmail(@RequestParam("token") String token) {
        authService.verifyEmail(token);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/resend-verification")
    @SecurityRequirements // public
    @Operation(summary = "Re-send the verification email. Always returns 204 — never leaks whether the email exists.")
    public ResponseEntity<Void> resendVerification(@Valid @RequestBody ResendVerificationRequest request) {
        authService.resendVerification(request.email());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/forgot-password")
    @SecurityRequirements // public
    @Operation(summary = "Request a password-reset email. Always returns 204 — never leaks whether the email exists.")
    public ResponseEntity<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.requestPasswordReset(request.email());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reset-password")
    @SecurityRequirements // public
    @Operation(summary = "Set a new password using the token from the reset mail")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Password updated"),
            @ApiResponse(responseCode = "400", description = "Token missing, expired, or password too short")
    })
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request.token(), request.password());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Return the currently authenticated user")
    public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(authService.getCurrentUser(userDetails.getUsername()));
    }

    @PostMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Change the current user's password")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Password updated"),
            @ApiResponse(responseCode = "400", description = "Current password incorrect or new password too short",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest req,
                                                @AuthenticationPrincipal UserDetailsImpl principal) {
        authService.changePassword(principal.getUsername(), req.currentPassword(), req.newPassword());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update the current user's profile (first name, last name, city, language)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile updated"),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<UserResponse> updateProfile(@Valid @RequestBody UpdateProfileRequest req,
                                                       @AuthenticationPrincipal UserDetailsImpl principal) {
        return ResponseEntity.ok(authService.updateProfile(principal.getUsername(), req));
    }

    @PostMapping("/delete-account")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Permanently delete the current user's account. Cascades through driver rides, "
            + "passenger memberships, and verification/reset tokens; notifies affected drivers and passengers by email.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Account deleted"),
            @ApiResponse(responseCode = "400", description = "Password incorrect",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> deleteAccount(@Valid @RequestBody DeleteAccountRequest req,
                                               @AuthenticationPrincipal UserDetailsImpl principal) {
        authService.deleteAccount(principal.getUsername(), req.password());
        return ResponseEntity.noContent().build();
    }
}
