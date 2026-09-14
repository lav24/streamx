package com.streamx.analytics.dto;

import java.util.UUID;

public record VideoStatsResponse(UUID videoId, long views, long totalWatchSeconds) {
}
