package cz.bezcisobe.backend.repository;

import cz.bezcisobe.backend.entity.RaceCalendar;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RaceCalendarRepository extends JpaRepository<RaceCalendar, Long> {
}
