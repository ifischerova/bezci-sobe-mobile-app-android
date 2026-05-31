package cz.bezcisobe.backend.dto.mapper;

import cz.bezcisobe.backend.dto.response.RaceResponse;
import cz.bezcisobe.backend.entity.Race;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Component
public class RaceMapper {

    // Carpooling is a Czech-runners platform, so "past" is judged in Czech
    // wall-clock time regardless of where the JVM happens to run.
    private static final ZoneId APP_ZONE = ZoneId.of("Europe/Prague");

    public RaceResponse toResponse(Race race) {
        List<RaceResponse.RefItem> certs = race.getCertifications().stream()
                .map(c -> new RaceResponse.RefItem(c.getId().toString(), c.getName()))
                .toList();

        boolean isPast = race.getDate().isBefore(LocalDate.now(APP_ZONE));

        return new RaceResponse(
                race.getId().toString(),
                race.getName(),
                race.getPlace(),
                race.getDate().toString(),
                race.getStartTime().toString().substring(0, 5), // HH:mm
                race.getWeb(),
                new RaceResponse.RefItem(
                        race.getTrackLength().getId().toString(),
                        race.getTrackLength().getName()
                ),
                new RaceResponse.RefItem(
                        race.getTrackType().getId().toString(),
                        race.getTrackType().getName()
                ),
                certs,
                race.getRaceCalendar() != null
                        ? race.getRaceCalendar().getId().toString()
                        : null,
                isPast
        );
    }
}
