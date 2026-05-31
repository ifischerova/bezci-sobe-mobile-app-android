package cz.bezcisobe.backend.controller;

import cz.bezcisobe.backend.dto.response.PageResponse;
import cz.bezcisobe.backend.dto.response.UserResponse;
import cz.bezcisobe.backend.security.UserDetailsImpl;
import cz.bezcisobe.backend.service.AdminService;
import cz.bezcisobe.backend.service.RideService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Endpoints reserved for users with {@code ROLE_ADMIN}.
 *
 * <p>Authorization is enforced at two layers: at the URL filter level in
 * {@link cz.bezcisobe.backend.config.SecurityConfig} and again per-method via
 * {@code @PreAuthorize} below — defence in depth.</p>
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin", description = "Administrative operations (ROLE_ADMIN required)")
public class AdminController {

    private final AdminService adminService;
    private final RideService rideService;

    @GetMapping("/users")
    @Operation(summary = "List users with optional substring search and pagination")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Paged user list"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT"),
            @ApiResponse(responseCode = "403", description = "Caller does not have ROLE_ADMIN")
    })
    public ResponseEntity<PageResponse<UserResponse>> listUsers(
            @RequestParam(required = false) String q,
            @PageableDefault(size = 20, sort = "username", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(adminService.listUsers(q, pageable));
    }

    @DeleteMapping("/rides/{id}")
    @Operation(summary = "Force-delete any ride", description = "Bypasses the owner check. Use only when a ride is abusive or stale.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Ride deleted"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT"),
            @ApiResponse(responseCode = "403", description = "Caller does not have ROLE_ADMIN"),
            @ApiResponse(responseCode = "404", description = "Ride not found")
    })
    public ResponseEntity<Void> forceDeleteRide(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetailsImpl admin) {
        rideService.deleteRideAsAdmin(id, admin.getId());
        return ResponseEntity.noContent().build();
    }
}
