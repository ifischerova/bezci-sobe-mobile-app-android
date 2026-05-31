package cz.bezcisobe.backend.dto.request;

import jakarta.validation.constraints.NotBlank;

public record DeleteAccountRequest(
        @NotBlank(message = "{validation.password.required}")
        String password
) {}
