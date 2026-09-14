package com.streamx.analytics.event;

import java.util.UUID;

public record AdClickEvent(UUID eventId, UUID campaignId, UUID userId, String placement) {
}
