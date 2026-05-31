package cz.bezcisobe.backend.controller;

import cz.bezcisobe.backend.dto.request.CreateRideRequest;
import cz.bezcisobe.backend.dto.request.UpdateRideRequest;
import cz.bezcisobe.backend.dto.response.ErrorResponse;
import cz.bezcisobe.backend.dto.response.RideResponse;
import cz.bezcisobe.backend.security.UserDetailsImpl;
import cz.bezcisobe.backend.service.RideService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/rides")
@RequiredArgsConstructor
@Tag(name = "Rides", description = "Ride OFFERs and REQUESTs for a race")
public class RideController {

    private final RideService rideService;

    @GetMapping
    @Operation(summary = "List rides for a race")
    public ResponseEntity<List<RideResponse>> getRidesByRace(@RequestParam Long raceId) {
        return ResponseEntity.ok(rideService.getRidesByRace(raceId));
    }

    @PostMapping
    @Operation(summary = "Create a ride", description = "Authenticated users only. The OFFER/REQUEST cross-field rules are enforced by the @ValidRideRequest validator.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Ride created"),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT")
    })
    public ResponseEntity<RideResponse> createRide(
            @Valid @RequestBody CreateRideRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(rideService.createRide(request, userDetails.getId()));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing ride", description = "Owner-only. New `availableSeats` cannot drop below currently occupied count.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ride updated"),
            @ApiResponse(responseCode = "400", description = "Validation or seat-count conflict"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT"),
            @ApiResponse(responseCode = "404", description = "Ride not found")
    })
    public ResponseEntity<RideResponse> updateRide(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateRideRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(rideService.updateRide(id, request, userDetails.getId()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete own ride")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Ride deleted"),
            @ApiResponse(responseCode = "400", description = "Caller is not the owner"),
            @ApiResponse(responseCode = "404", description = "Ride not found")
    })
    public ResponseEntity<Void> deleteRide(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        rideService.deleteRide(id, userDetails.getId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/accept")
    @Operation(summary = "Accept a ride OFFER")
    public ResponseEntity<RideResponse> acceptRide(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(rideService.acceptRide(id, userDetails.getId()));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel previously-accepted ride")
    public ResponseEntity<RideResponse> cancelRideAcceptance(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(rideService.cancelRideAcceptance(id, userDetails.getId()));
    }
}
