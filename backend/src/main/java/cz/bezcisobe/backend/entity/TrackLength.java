package cz.bezcisobe.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "track_lengths")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrackLength {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;
}
