package com.streamx.video.dto;

import java.time.Instant;
import java.util.UUID;

public record UploadInitResponse(UUID videoId, String uploadUrl, Instant expiresAt) {
}
