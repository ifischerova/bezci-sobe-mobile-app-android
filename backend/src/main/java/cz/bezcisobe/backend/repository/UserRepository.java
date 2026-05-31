package cz.bezcisobe.backend.repository;

import cz.bezcisobe.backend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    /**
     * Paginated case-insensitive search over username/email/firstName/lastName.
     * Used by the admin user-management endpoint.
     */
    @Query("""
            SELECT u FROM User u
            WHERE :query IS NULL
               OR LOWER(u.username)  LIKE LOWER(CONCAT('%', :query, '%'))
               OR LOWER(u.email)     LIKE LOWER(CONCAT('%', :query, '%'))
               OR LOWER(COALESCE(u.firstName, '')) LIKE LOWER(CONCAT('%', :query, '%'))
               OR LOWER(COALESCE(u.lastName,  '')) LIKE LOWER(CONCAT('%', :query, '%'))
            """)
    Page<User> search(@Param("query") String query, Pageable pageable);
}
