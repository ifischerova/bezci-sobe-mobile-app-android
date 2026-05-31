package cz.bezcisobe.backend.dto.response;

import java.util.List;

public record UserResponse(
        String id,
        String username,
        String email,
        String firstName,
        String lastName,
        String city,
        String language,
        List<String> roles
) {}
