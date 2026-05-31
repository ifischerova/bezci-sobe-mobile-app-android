package cz.bezcisobe.backend.repository;

import cz.bezcisobe.backend.entity.Certification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CertificationRepository extends JpaRepository<Certification, Long> {
}
