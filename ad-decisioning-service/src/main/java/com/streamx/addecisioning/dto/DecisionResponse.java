package com.streamx.addecisioning.dto;

import java.util.UUID;

public record DecisionResponse(boolean filled, UUID campaignId, String assetUrl, Integer durationSeconds) {

    public static DecisionResponse noFill() {
        return new DecisionResponse(false, null, null, null);
    }

    public static DecisionResponse filled(UUID campaignId, String assetUrl, int durationSeconds) {
        return new DecisionResponse(true, campaignId, assetUrl, durationSeconds);
    }
}
