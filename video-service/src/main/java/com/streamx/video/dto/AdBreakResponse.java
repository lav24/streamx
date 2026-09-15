package com.streamx.video.dto;

import java.util.UUID;

public record AdBreakResponse(
        boolean filled, UUID campaignId, UUID creativeId, Integer durationSeconds, String manifestUrl) {

    public static AdBreakResponse noFill() {
        return new AdBreakResponse(false, null, null, null, null);
    }
}
