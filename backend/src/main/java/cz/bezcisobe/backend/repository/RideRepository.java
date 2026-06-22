package cz.bezcisobe.backend.repository;

import cz.bezcisobe.backend.entity.Ride;
import cz.bezcisobe.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface RideRepository extends JpaRepository<Ride, UUID> {
    /**
     * Rides for one race with driver + race eagerly fetched in a single query,
     * avoiding the per-ride lazy loads {@code RideMapper} would otherwise trigger.
     * Passengers (an EAGER {@code @ManyToMany}) are intentionally NOT join-fetched
     * here — adding them would produce a cartesian product / duplicate rows; they
     * are batch-loaded separately. Ordered by creation time for a stable list.
     */
    @Query("SELECT r FROM Ride r "
            + "JOIN FETCH r.user "
            + "JOIN FETCH r.race "
            + "WHERE r.race.id = :raceId "
            + "ORDER BY r.createdAt")
    List<Ride> findByRaceIdWithUserAndRace(@Param("raceId") Long raceId);

    List<Ride> findByRaceId(Long raceId);
    List<Ride> findByUserId(UUID userId);

    List<Ride> findAllByUser(User user);
    List<Ride> findAllByPassengersContaining(User user);
}
