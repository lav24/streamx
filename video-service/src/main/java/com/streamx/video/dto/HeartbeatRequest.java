package com.streamx.video.dto;

import java.util.UUID;

public record HeartbeatRequest(UUID userId, UUID videoId, Integer positionSeconds, Boolean completed) {
}
