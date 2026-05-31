package cz.bezcisobe.backend.repository;

import cz.bezcisobe.backend.entity.Race;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface RaceRepository extends JpaRepository<Race, Long> {

    /**
     * Races taking place on {@code date} or later, sorted by date ascending
     * (soonest first). Used to populate the "all races" dropdown — past races
     * are filtered at the DB level so we never overfetch.
     */
    List<Race> findAllByDateGreaterThanEqualOrderByDateAsc(LocalDate date);


    /**
     * Paginated full-text-ish search over races. Matches the substring
     * (case-insensitive) against {@code name} or {@code place} and optionally
     * filters out races held before {@code fromDate}. Joins {@code raceCalendar}
     * and {@code trackType} to demonstrate a non-trivial JPQL query that goes
     * beyond a derived method.
     *
     * @param query     substring to match in name/place (nullable -> matches all)
     * @param fromDate  earliest race date to include (nullable -> matches all)
     * @param trackTypeId optional track-type filter (nullable -> matches all)
     * @param pageable  pagination + sort
     * @return paged result
     */
    @Query("""
            SELECT r FROM Race r
            LEFT JOIN FETCH r.raceCalendar
            WHERE (:query IS NULL OR LOWER(r.name)  LIKE LOWER(CONCAT('%', :query, '%'))
                                  OR LOWER(r.place) LIKE LOWER(CONCAT('%', :query, '%')))
              AND (:fromDate IS NULL OR r.date >= :fromDate)
              AND (:trackTypeId IS NULL OR r.trackType.id = :trackTypeId)
            """)
    Page<Race> search(@Param("query") String query,
                      @Param("fromDate") LocalDate fromDate,
                      @Param("trackTypeId") Long trackTypeId,
                      Pageable pageable);
}
