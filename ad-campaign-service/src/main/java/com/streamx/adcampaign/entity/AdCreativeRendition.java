package com.streamx.adcampaign.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(
    name = "ad_creative_renditions",
    uniqueConstraints = @UniqueConstraint(columnNames = {"creative_id", "resolution"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdCreativeRendition {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "creative_id", nullable = false)
    private AdCreative creative;

    @Column(nullable = false)
    private String resolution;

    @Column(name = "bitrate_kbps", nullable = false)
    private Integer bitrateKbps;

    private Integer width;

    private Integer height;

    @Column(name = "hls_playlist_key", nullable = false)
    private String hlsPlaylistKey;
}
