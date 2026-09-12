package com.streamx.adcampaign.event;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record CampaignUpdatedEvent(
        UUID campaignId,
        String status,
        BigDecimal budgetTotal,
        BigDecimal budgetDaily,
        BigDecimal cpm,
        LocalDate startDate,
        LocalDate endDate,
        List<TargetingRule> targetingRules,
        List<Creative> creatives) {

    public record TargetingRule(String dimension, List<String> targetValues) {
    }

    public record Creative(String placement, String assetUrl, int durationSeconds) {
    }
}
