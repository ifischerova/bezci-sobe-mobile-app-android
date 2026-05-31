package cz.bezcisobe.backend.repository;

import cz.bezcisobe.backend.entity.TrackType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrackTypeRepository extends JpaRepository<TrackType, Long> {
}
