package com.streamx.video.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "watch_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WatchHistory {

    @EmbeddedId
    private WatchHistoryId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("videoId")
    @JoinColumn(name = "video_id")
    private Video video;

    @Column(name = "position_seconds", nullable = false)
    @Builder.Default
    private Integer positionSeconds = 0;

    @Column(nullable = false)
    @Builder.Default
    private Boolean completed = false;

    @Column(name = "last_watched_at", nullable = false)
    private Instant lastWatchedAt;
}
