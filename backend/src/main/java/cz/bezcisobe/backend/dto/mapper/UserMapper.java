package cz.bezcisobe.backend.dto.mapper;

import cz.bezcisobe.backend.dto.response.UserResponse;
import cz.bezcisobe.backend.entity.Role;
import cz.bezcisobe.backend.entity.User;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserMapper {

    public UserResponse toResponse(User user) {
        List<String> roles = user.getRoles().stream()
                .map(Role::name)
                .sorted()
                .toList();

        return new UserResponse(
                user.getId().toString(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getCity(),
                user.getLanguage(),
                roles
        );
    }
}
