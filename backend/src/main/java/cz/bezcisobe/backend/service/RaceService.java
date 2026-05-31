package cz.bezcisobe.backend.service;

import cz.bezcisobe.backend.dto.mapper.RaceMapper;
import cz.bezcisobe.backend.dto.response.PageResponse;
import cz.bezcisobe.backend.dto.response.RaceResponse;
import cz.bezcisobe.backend.exception.ResourceNotFoundException;
import cz.bezcisobe.backend.repository.RaceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class RaceService {

    private final RaceRepository raceRepository;
    private final RaceMapper raceMapper;

    @Transactional(readOnly = true)
    public List<RaceResponse> getAllRaces() {
        LocalDate today = LocalDate.now(ZoneId.of("Europe/Prague"));
        return raceRepository.findAllByDateGreaterThanEqualOrderByDateAsc(today).stream()
                .map(raceMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public RaceResponse getRaceById(Long id) {
        return raceRepository.findById(id)
                .map(raceMapper::toResponse)
                .orElseThrow(() -> {
                    log.warn("Race {} not found", id);
                    return new ResourceNotFoundException("Závod nenalezen");
                });
    }

    /**
     * Paginated search backed by {@link RaceRepository#search}. Empty/whitespace
     * filters are normalized to {@code null} so the JPQL query treats them as
     * "no filter".
     */
    @Transactional(readOnly = true)
    public PageResponse<RaceResponse> search(String query,
                                             LocalDate fromDate,
                                             Long trackTypeId,
                                             Pageable pageable) {
        String normalizedQuery = (query == null || query.isBlank()) ? null : query.trim();
        log.info("Race search: q='{}', from={}, trackTypeId={}, page={}, size={}",
                normalizedQuery, fromDate, trackTypeId, pageable.getPageNumber(), pageable.getPageSize());

        return PageResponse.of(
                raceRepository.search(normalizedQuery, fromDate, trackTypeId, pageable),
                raceMapper::toResponse
        );
    }
}
