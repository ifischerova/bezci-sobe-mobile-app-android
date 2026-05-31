package cz.bezcisobe.backend.exception;

import cz.bezcisobe.backend.dto.response.ErrorResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Locale;

/**
 * Centralizes how API exceptions are turned into the consistent
 * {@link ErrorResponse} wire format.
 *
 * <p>For {@link LocalizedException} types, the message is resolved
 * through {@link MessageSource} using the request locale (resolved by
 * {@code UserLocaleResolver}: authenticated user.language → Accept-Language → cs).
 * Internal stack traces never reach the client. WARN logged for 4xx,
 * ERROR for unexpected server failures.</p>
 */
@RestControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final MessageSource messageSource;

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        String msg = resolve(ex, ex.getMessage());
        log.warn("Resource not found: {}", msg);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(404, msg));
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(BadRequestException ex) {
        String msg = resolve(ex, ex.getMessage());
        log.warn("Bad request: {}", msg);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(400, msg));
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicate(DuplicateResourceException ex) {
        String msg = resolve(ex, ex.getMessage());
        log.warn("Duplicate resource: {}", msg);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(409, msg));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex) {
        String msg = messageSource.getMessage("error.security.invalid_credentials", null, locale());
        log.warn("Bad credentials: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse(401, msg));
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ErrorResponse> handleDisabled(DisabledException ex) {
        String msg = messageSource.getMessage("error.security.disabled", null, locale());
        log.warn("Login blocked — account not verified: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse(403, msg));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        String msg = messageSource.getMessage("error.security.access_denied", null, locale());
        log.warn("Access denied: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse(403, msg));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(FieldError::getDefaultMessage)
                .orElseGet(() -> messageSource.getMessage("error.generic.bad_request", null, locale()));
        log.warn("Validation failed: {}", message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(400, message));
    }

    /**
     * Last-resort catch-all. Anything reaching here is unexpected.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex) {
        String msg = messageSource.getMessage("error.generic.internal", null, locale());
        log.error("Unexpected error", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse(500, msg));
    }

    private String resolve(LocalizedException ex, String fallback) {
        if (ex.getMessageKey() == null) return fallback;
        return messageSource.getMessage(ex.getMessageKey(), ex.getArgs(), fallback, locale());
    }

    private Locale locale() {
        return LocaleContextHolder.getLocale();
    }
}
