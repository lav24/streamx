package com.streamx.video.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(
    name = "episodes",
    uniqueConstraints = @UniqueConstraint(columnNames = {"title_id", "season", "episode_number"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Episode {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "title_id", nullable = false)
    private Title title;

    @Column(nullable = false)
    private Short season;

    @Column(name = "episode_number", nullable = false)
    private Short episodeNumber;

    @Column(name = "duration_seconds")
    private Integer durationSeconds;
}
