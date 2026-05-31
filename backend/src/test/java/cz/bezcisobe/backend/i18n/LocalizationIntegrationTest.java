package cz.bezcisobe.backend.i18n;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Smoke test for the i18n pipeline: MessageSource + UserLocaleResolver +
 * LocalValidatorFactoryBean + GlobalExceptionHandler.
 *
 * <p>Sends an empty username to {@code POST /api/auth/register} so Bean
 * Validation fails on {@code @NotBlank(message="{validation.username.required}")}.
 * The {@link org.springframework.web.bind.MethodArgumentNotValidException} is
 * caught by the global handler and the already-interpolated default message is
 * returned. We assert the Czech wording by default, and the English wording
 * when {@code Accept-Language: en} is sent.</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
class LocalizationIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void registerWithInvalidUsernameReturnsCzechByDefault() throws Exception {
        // username: null fails @NotBlank but is ignored by @Size, so the
        // resolved message is deterministically "validation.username.required".
        mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":null,\"email\":\"a@b.cz\",\"password\":\"abcdef\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("povinné")))
                .andExpect(jsonPath("$.message", not(containsString("{"))));
    }

    @Test
    void registerWithInvalidUsernameReturnsEnglishWhenAcceptLanguageEn() throws Exception {
        mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Accept-Language", "en")
                        .content("{\"username\":null,\"email\":\"a@b.cz\",\"password\":\"abcdef\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("required")));
    }
}
