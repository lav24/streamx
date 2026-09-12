package com.streamx.budgetpacing.event;

import java.util.UUID;

public record AdImpressionEvent(UUID eventId, UUID campaignId, UUID userId, String placement) {
}
