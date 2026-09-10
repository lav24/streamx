package com.streamx.addecisioning.event;

import java.time.Instant;
import java.util.UUID;

public record AdDecisionMadeEvent(
        UUID campaignId,
        UUID userId,
        String placement,
        boolean filled,
        Instant decidedAt) {
}
