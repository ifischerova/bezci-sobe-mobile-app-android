package cz.bezcisobe.backend.dto.response;

import java.util.List;

public record AuthResponse(
        String token,
        String userId,
        String username,
        List<String> roles
) {}
