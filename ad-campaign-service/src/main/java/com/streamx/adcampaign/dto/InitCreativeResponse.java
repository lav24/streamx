package com.streamx.adcampaign.dto;

import java.time.Instant;
import java.util.UUID;

public record InitCreativeResponse(UUID creativeId, String uploadUrl, Instant expiresAt) {
}
