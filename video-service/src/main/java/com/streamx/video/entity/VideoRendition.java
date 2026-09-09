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
    name = "video_renditions",
    uniqueConstraints = @UniqueConstraint(columnNames = {"video_id", "resolution"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VideoRendition {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "video_id", nullable = false)
    private Video video;

    @Column(nullable = false)
    private String resolution;

    @Column(name = "bitrate_kbps", nullable = false)
    private Integer bitrateKbps;

    private Integer width;

    private Integer height;

    @Column(name = "hls_playlist_key")
    private String hlsPlaylistKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Status status = Status.PENDING;

    public enum Status { PENDING, READY, FAILED }
}
