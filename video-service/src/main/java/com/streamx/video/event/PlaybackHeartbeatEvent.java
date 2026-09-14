package com.streamx.video.event;

import java.time.Instant;
import java.util.UUID;

public record PlaybackHeartbeatEvent(
        UUID sessionId, UUID userId, UUID videoId, Integer positionSeconds, Instant occurredAt) {
}
