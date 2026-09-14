package com.streamx.analytics.event;

import java.util.UUID;

public record PlaybackHeartbeatEvent(UUID sessionId, UUID userId, UUID videoId, Integer positionSeconds) {
}
