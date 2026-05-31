package cz.bezcisobe.backend.controller;

import cz.bezcisobe.backend.dto.response.PageResponse;
import cz.bezcisobe.backend.dto.response.RaceResponse;
import cz.bezcisobe.backend.service.RaceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/races")
@RequiredArgsConstructor
@Tag(name = "Races", description = "Browse Czech running races")
public class RaceController {

    private final RaceService raceService;

    @GetMapping
    @Operation(summary = "List all races", description = "Returns every race in the database, unpaginated. For larger result sets prefer /api/races/search.")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Race list"))
    public ResponseEntity<List<RaceResponse>> getAllRaces() {
        return ResponseEntity.ok(raceService.getAllRaces());
    }

    @GetMapping("/search")
    @Operation(
            summary = "Search races with pagination",
            description = "Case-insensitive substring search over race name and place, "
                    + "optionally filtered by minimum date and track type. Supports "
                    + "Spring's `page`, `size`, and `sort` query parameters."
    )
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Paged race results"))
    public ResponseEntity<PageResponse<RaceResponse>> searchRaces(
            @Parameter(description = "Substring to match in race name or place") @RequestParam(required = false) String q,
            @Parameter(description = "Earliest race date (inclusive), ISO yyyy-MM-dd") @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @Parameter(description = "Track type ID filter") @RequestParam(required = false) Long trackTypeId,
            @PageableDefault(size = 10, sort = "date", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(raceService.search(q, from, trackTypeId, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a single race by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Race detail"),
            @ApiResponse(responseCode = "404", description = "Race not found")
    })
    public ResponseEntity<RaceResponse> getRaceById(@PathVariable Long id) {
        return ResponseEntity.ok(raceService.getRaceById(id));
    }
}
