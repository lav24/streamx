package com.streamx.video.dto;

import java.util.UUID;

public record AdDecisionResult(boolean filled, UUID campaignId, UUID creativeId, Integer durationSeconds) {

    public static AdDecisionResult noFill() {
        return new AdDecisionResult(false, null, null, null);
    }
}
