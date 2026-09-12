package com.streamx.budgetpacing.event;

import java.math.BigDecimal;
import java.util.UUID;

public record CampaignUpdatedEvent(
        UUID campaignId,
        String status,
        BigDecimal budgetTotal,
        BigDecimal budgetDaily,
        BigDecimal cpm) {
}
