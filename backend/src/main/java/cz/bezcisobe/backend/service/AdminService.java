package cz.bezcisobe.backend.service;

import cz.bezcisobe.backend.dto.mapper.UserMapper;
import cz.bezcisobe.backend.dto.response.PageResponse;
import cz.bezcisobe.backend.dto.response.UserResponse;
import cz.bezcisobe.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Admin-facing operations. Authorization itself is enforced at the controller
 * via {@code @PreAuthorize}; this service trusts that callers already passed
 * the role check.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    public PageResponse<UserResponse> listUsers(String query, Pageable pageable) {
        String normalized = (query == null || query.isBlank()) ? null : query.trim();
        log.info("Admin user listing: q='{}', page={}, size={}",
                normalized, pageable.getPageNumber(), pageable.getPageSize());
        return PageResponse.of(
                userRepository.search(normalized, pageable),
                userMapper::toResponse
        );
    }
}
