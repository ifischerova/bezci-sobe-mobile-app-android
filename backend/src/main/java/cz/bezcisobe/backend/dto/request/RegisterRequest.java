package cz.bezcisobe.backend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "{validation.username.required}")
        @Size(min = 3, max = 50, message = "{validation.username.size}")
        String username,

        @NotBlank(message = "{validation.email.required}")
        @Email(message = "{validation.email.format}")
        String email,

        @NotBlank(message = "{validation.password.required}")
        @Size(min = 6, message = "{validation.password.size}")
        String password,

        @Pattern(regexp = "cs|en", message = "{validation.language.invalid}")
        String language
) {}
