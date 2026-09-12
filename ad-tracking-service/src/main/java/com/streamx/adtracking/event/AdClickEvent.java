package com.streamx.adtracking.event;

import java.time.Instant;
import java.util.UUID;

public record AdClickEvent(
        UUID eventId, UUID campaignId, UUID userId, String placement, Instant occurredAt) {
}
