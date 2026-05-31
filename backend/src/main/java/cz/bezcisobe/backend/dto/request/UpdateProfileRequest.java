package cz.bezcisobe.backend.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @Size(max = 50, message = "{validation.firstName.size}")
        String firstName,

        @Size(max = 50, message = "{validation.lastName.size}")
        String lastName,

        @Size(max = 50, message = "{validation.city.size}")
        String city,

        @Pattern(regexp = "cs|en", message = "{validation.language.invalid}")
        String language
) {}
