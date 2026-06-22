package cz.bezcisobe.backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Issues and verifies HS256-signed JWTs.
 *
 * <p>Tokens carry the user's id as the JWT subject and the username + role
 * list as custom claims. The secret and expiration are sourced from
 * {@code app.jwt.*} in {@code application.yml} and must be at least 256 bits
 * for HS256 to be acceptable to {@link Keys#hmacShaKeyFor(byte[])}.</p>
 */
@Component
@Slf4j
public class JwtTokenProvider {

    /**
     * The committed development placeholder from {@code application.yml}. It MUST
     * never sign tokens in a real deployment — the fail-fast guard below refuses
     * to start any non-dev/test profile that is still using it.
     */
    static final String PLACEHOLDER_SECRET =
            "dev-only-placeholder-rotate-me-before-deploying-to-any-real-environment";

    /** HS256 requires a key of at least 256 bits. */
    private static final int MIN_SECRET_BYTES = 32;

    /** Profiles under which the placeholder secret is tolerated for convenience. */
    private static final List<String> LENIENT_PROFILES = List.of("dev", "test");

    private final String secret;
    private final Environment environment;
    private final SecretKey key;
    private final long expirationMs;

    public JwtTokenProvider(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-ms}") long expirationMs,
            Environment environment) {
        this.secret = secret;
        this.environment = environment;
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    /**
     * Refuses to start a production-like deployment that is still signing with the
     * committed placeholder secret (or any secret too short for HS256). Dev and test
     * profiles boot out of the box; everything else must supply a real
     * {@code JWT_SECRET} of at least 256 bits.
     */
    @PostConstruct
    void validateSecret() {
        boolean lenient = Arrays.stream(environment.getActiveProfiles())
                .anyMatch(LENIENT_PROFILES::contains);
        if (lenient) {
            return;
        }

        if (PLACEHOLDER_SECRET.equals(secret)) {
            throw new IllegalStateException(
                    "Refusing to start: app.jwt.secret is still the committed development "
                    + "placeholder. Set a strong JWT_SECRET (>= 32 bytes) for this environment.");
        }
        if (secret.getBytes(StandardCharsets.UTF_8).length < MIN_SECRET_BYTES) {
            throw new IllegalStateException(
                    "Refusing to start: app.jwt.secret is too short for HS256. "
                    + "Provide a JWT_SECRET of at least 32 bytes (256 bits).");
        }
    }

    /**
     * Builds a signed JWT for the given identity. The {@code roles} claim is
     * informational on the client side; authorization on the server is rebuilt
     * from {@link UserDetailsServiceImpl} on every request.
     */
    public String generateToken(String userId, String username, List<String> roles) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(userId)
                .claim("username", username)
                .claim("roles", roles)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    public String getUserIdFromToken(String token) {
        return parseClaims(token).getSubject();
    }

    public String getUsernameFromToken(String token) {
        return parseClaims(token).get("username", String.class);
    }

    /**
     * @return {@code true} if the signature verifies and the token is not
     *         expired, {@code false} for any failure (also logged at debug
     *         level — never at info, to avoid leaking that someone is probing
     *         the API with junk tokens at high volume).
     */
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("JWT validation failed: {}", e.getMessage());
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
