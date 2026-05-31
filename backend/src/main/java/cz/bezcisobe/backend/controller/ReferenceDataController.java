package cz.bezcisobe.backend.controller;

import cz.bezcisobe.backend.dto.response.RaceCalendarResponse;
import cz.bezcisobe.backend.dto.response.RaceResponse;
import cz.bezcisobe.backend.service.ReferenceDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reference")
@RequiredArgsConstructor
@Tag(name = "Reference data", description = "Read-only enums used by races and rides")
public class ReferenceDataController {

    private final ReferenceDataService referenceDataService;

    @GetMapping("/track-lengths")
    @Operation(summary = "List all track lengths (5K, 10K, half-marathon, …)")
    public ResponseEntity<List<RaceResponse.RefItem>> getTrackLengths() {
        return ResponseEntity.ok(referenceDataService.getTrackLengths());
    }

    @GetMapping("/track-types")
    @Operation(summary = "List all track types (road, trail, …)")
    public ResponseEntity<List<RaceResponse.RefItem>> getTrackTypes() {
        return ResponseEntity.ok(referenceDataService.getTrackTypes());
    }

    @GetMapping("/race-calendars")
    @Operation(summary = "List all race calendars (by year)")
    public ResponseEntity<List<RaceCalendarResponse>> getRaceCalendars() {
        return ResponseEntity.ok(referenceDataService.getRaceCalendars());
    }
}
