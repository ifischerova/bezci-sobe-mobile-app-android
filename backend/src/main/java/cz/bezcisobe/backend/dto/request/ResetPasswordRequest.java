package cz.bezcisobe.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
        @NotBlank(message = "{validation.token.required}")
        String token,

        @NotBlank(message = "{validation.password.required}")
        @Size(min = 6, message = "{validation.password.size}")
        String password
) {}
