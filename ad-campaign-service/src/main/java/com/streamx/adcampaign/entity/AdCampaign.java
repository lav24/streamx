package com.streamx.adcampaign.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "ad_campaigns")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdCampaign {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "advertiser_id", nullable = false)
    private UUID advertiserId;

    @Column(nullable = false)
    private String name;

    @Column(name = "budget_total", nullable = false)
    private BigDecimal budgetTotal;

    @Column(name = "budget_daily", nullable = false)
    private BigDecimal budgetDaily;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Status status = Status.DRAFT;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }

    public enum Status { DRAFT, ACTIVE, PAUSED, ENDED }
}
