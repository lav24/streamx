package com.streamx.adcampaign.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "ad_creatives")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdCreative {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "campaign_id", nullable = false)
    private AdCampaign campaign;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Placement placement;

    @Column(name = "asset_url", nullable = false)
    private String assetUrl;

    @Column(name = "duration_seconds", nullable = false)
    private Integer durationSeconds;

    public enum Placement { PRE_ROLL, MID_ROLL }
}
