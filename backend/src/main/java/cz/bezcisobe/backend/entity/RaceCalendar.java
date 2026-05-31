package cz.bezcisobe.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "race_calendars")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RaceCalendar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer year;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;
}
