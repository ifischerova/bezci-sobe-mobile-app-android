package cz.bezcisobe.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private final JavaMailSender mailSender;
    private final MessageSource messageSource;

    @Value("${app.url}")
    private String appUrl;

    @Value("${app.mail.from}")
    private String fromAddress;

    @Value("${app.mail.log-only:false}")
    private boolean logOnly;

    public void sendVerificationEmail(String to, String token, Locale locale) {
        String link = buildUrl("/verify-email", token);
        send(to,
             msg("email.verification.subject", locale),
             msg("email.verification.body", locale, link));
    }

    public void sendPasswordResetEmail(String to, String token, Locale locale) {
        String link = buildUrl("/reset-password", token);
        send(to,
             msg("email.password_reset.subject", locale),
             msg("email.password_reset.body", locale, link));
    }

    public void sendPasswordChangedEmail(String to, Locale locale) {
        send(to,
             msg("email.password_changed.subject", locale),
             msg("email.password_changed.body", locale));
    }

    public void sendAccountDeletedEmail(String to, Locale locale) {
        send(to,
             msg("email.account_deleted.subject", locale),
             msg("email.account_deleted.body", locale));
    }

    public void sendRideAcceptedEmail(String driverEmail, String driverUsername,
                                      String passengerName, String passengerEmail,
                                      String raceName, LocalDate raceDate,
                                      Locale locale) {
        send(driverEmail,
             msg("email.ride.accepted.subject", locale),
             msg("email.ride.accepted.body", locale,
                 driverUsername, passengerName, passengerEmail, raceName, raceDate.format(DATE_FMT)));
    }

    public void sendRideAcceptanceCancelledEmail(String driverEmail, String driverUsername,
                                                 String passengerName, String passengerEmail,
                                                 String raceName, LocalDate raceDate,
                                                 Locale locale) {
        send(driverEmail,
             msg("email.ride.acceptance_cancelled.subject", locale),
             msg("email.ride.acceptance_cancelled.body", locale,
                 driverUsername, passengerName, passengerEmail, raceName, raceDate.format(DATE_FMT)));
    }

    public void sendRideDeletedByDriverEmail(String passengerEmail, String passengerUsername,
                                             String driverName, String driverEmail,
                                             String raceName, LocalDate raceDate,
                                             Locale locale) {
        send(passengerEmail,
             msg("email.ride.deleted.subject", locale),
             msg("email.ride.deleted.body", locale,
                 passengerUsername, driverName, driverEmail, raceName, raceDate.format(DATE_FMT)));
    }

    public void sendRideDeletedByAdminToPassengerEmail(String passengerEmail, String passengerUsername,
                                                       String raceName, LocalDate raceDate,
                                                       Locale locale) {
        send(passengerEmail,
             msg("email.ride.deleted_by_admin.passenger.subject", locale),
             msg("email.ride.deleted_by_admin.passenger.body", locale,
                 passengerUsername, raceName, raceDate.format(DATE_FMT)));
    }

    public void sendRideDeletedByAdminToDriverEmail(String driverEmail, String driverUsername,
                                                    String raceName, LocalDate raceDate,
                                                    Locale locale) {
        send(driverEmail,
             msg("email.ride.deleted_by_admin.driver.subject", locale),
             msg("email.ride.deleted_by_admin.driver.body", locale,
                 driverUsername, raceName, raceDate.format(DATE_FMT)));
    }

    private String msg(String key, Locale locale, Object... args) {
        return messageSource.getMessage(key, args, locale);
    }

    private void send(String to, String subject, String body) {
        if (logOnly) {
            log.info("[MAIL log-only] To: {} | Subject: {}\n{}", to, subject, body);
            return;
        }
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(fromAddress);
        msg.setTo(to);
        msg.setSubject(subject);
        msg.setText(body);
        try {
            mailSender.send(msg);
            log.info("Email sent to {} ({})", to, subject);
        } catch (Exception ex) {
            log.error("Failed to send email to {} ({}): {}", to, subject, ex.getMessage());
            throw ex;
        }
    }

    private String buildUrl(String path, String token) {
        String base = appUrl.endsWith("/") ? appUrl.substring(0, appUrl.length() - 1) : appUrl;
        String encoded = URLEncoder.encode(token, StandardCharsets.UTF_8);
        return URI.create(base + path + "?token=" + encoded).toString();
    }
}
