package com.streamx.video.dto;

import java.util.UUID;

public record HeartbeatRequest(UUID sessionId, UUID userId, UUID videoId, Integer positionSeconds, Boolean completed) {
}
