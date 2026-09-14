package com.streamx.analytics.event;

import java.util.UUID;

public record SessionSummary(UUID videoId, int maxPositionSeconds) {
}
