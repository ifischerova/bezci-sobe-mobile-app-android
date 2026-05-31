package cz.bezcisobe.backend.config;

import cz.bezcisobe.backend.security.UserDetailsImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.LocaleResolver;

import java.util.List;
import java.util.Locale;
import java.util.Set;

@Component
public class UserLocaleResolver implements LocaleResolver {

    private static final Set<String> SUPPORTED = Set.of("cs", "en");
    private static final Locale DEFAULT = Locale.forLanguageTag("cs");

    @Override
    public Locale resolveLocale(HttpServletRequest request) {
        Locale fromUser = localeFromAuthenticatedUser();
        if (fromUser != null) return fromUser;

        Locale fromHeader = localeFromAcceptLanguage(request);
        if (fromHeader != null) return fromHeader;

        return DEFAULT;
    }

    @Override
    public void setLocale(HttpServletRequest request, HttpServletResponse response, Locale locale) {
        // No-op: locale is derived from user preference / header, not session-mutable here.
    }

    private Locale localeFromAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;
        Object principal = auth.getPrincipal();
        if (!(principal instanceof UserDetailsImpl user)) return null;
        String tag = user.getLanguage();
        if (tag == null || !SUPPORTED.contains(tag)) return null;
        return Locale.forLanguageTag(tag);
    }

    private Locale localeFromAcceptLanguage(HttpServletRequest request) {
        String header = request.getHeader("Accept-Language");
        if (header == null || header.isBlank()) return null;
        List<Locale.LanguageRange> ranges;
        try {
            ranges = Locale.LanguageRange.parse(header);
        } catch (Exception e) {
            return null;
        }
        for (Locale.LanguageRange r : ranges) {
            String tag = r.getRange().toLowerCase();
            if (tag.startsWith("cs")) return Locale.forLanguageTag("cs");
            if (tag.startsWith("en")) return Locale.forLanguageTag("en");
        }
        return null;
    }
}
