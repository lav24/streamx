package com.streamx.analytics.event;

import java.math.BigDecimal;
import java.util.UUID;

public record CampaignUpdatedEvent(UUID campaignId, BigDecimal cpm) {
}
