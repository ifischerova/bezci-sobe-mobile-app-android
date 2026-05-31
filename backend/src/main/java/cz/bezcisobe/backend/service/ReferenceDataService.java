package cz.bezcisobe.backend.service;

import cz.bezcisobe.backend.dto.response.RaceCalendarResponse;
import cz.bezcisobe.backend.dto.response.RaceResponse;
import cz.bezcisobe.backend.repository.RaceCalendarRepository;
import cz.bezcisobe.backend.repository.TrackLengthRepository;
import cz.bezcisobe.backend.repository.TrackTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReferenceDataService {

    private final TrackLengthRepository trackLengthRepository;
    private final TrackTypeRepository trackTypeRepository;
    private final RaceCalendarRepository raceCalendarRepository;

    public List<RaceResponse.RefItem> getTrackLengths() {
        return trackLengthRepository.findAll().stream()
                .map(tl -> new RaceResponse.RefItem(tl.getId().toString(), tl.getName()))
                .toList();
    }

    public List<RaceResponse.RefItem> getTrackTypes() {
        return trackTypeRepository.findAll().stream()
                .map(tt -> new RaceResponse.RefItem(tt.getId().toString(), tt.getName()))
                .toList();
    }

    public List<RaceCalendarResponse> getRaceCalendars() {
        return raceCalendarRepository.findAll().stream()
                .map(rc -> new RaceCalendarResponse(
                        rc.getId().toString(),
                        rc.getYear(),
                        rc.getIsActive()
                ))
                .toList();
    }
}
