package cz.bezcisobe.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "track_types")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrackType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;
}
