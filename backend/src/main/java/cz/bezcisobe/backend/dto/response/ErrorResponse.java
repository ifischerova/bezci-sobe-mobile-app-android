package cz.bezcisobe.backend.dto.response;

public record ErrorResponse(
        int status,
        String message
) {}
