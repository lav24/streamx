package com.streamx.analytics.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CampaignStatsResponse(
        UUID campaignId,
        Instant windowStart,
        Instant windowEnd,
        long impressions,
        long clicks,
        double ctr,
        BigDecimal estimatedRevenue) {
}
