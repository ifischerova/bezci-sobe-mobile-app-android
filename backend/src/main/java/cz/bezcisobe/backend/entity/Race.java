package cz.bezcisobe.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "races")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Race {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, length = 200)
    private String place;

    @Column(nullable = false)
    private LocalDate date;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(length = 500)
    private String web;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "track_length_id", nullable = false)
    private TrackLength trackLength;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "track_type_id", nullable = false)
    private TrackType trackType;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "race_certifications",
            joinColumns = @JoinColumn(name = "race_id"),
            inverseJoinColumns = @JoinColumn(name = "certification_id")
    )
    @Builder.Default
    private Set<Certification> certifications = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "race_calendar_id")
    private RaceCalendar raceCalendar;
}
