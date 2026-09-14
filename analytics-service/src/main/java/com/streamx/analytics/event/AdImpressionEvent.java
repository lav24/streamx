package com.streamx.analytics.event;

import java.util.UUID;

public record AdImpressionEvent(UUID eventId, UUID campaignId, UUID userId, String placement) {
}
