package com.streamx.addecisioning.dto;

import java.util.UUID;

public record DecisionResponse(boolean filled, UUID campaignId, UUID creativeId, Integer durationSeconds) {

    public static DecisionResponse noFill() {
        return new DecisionResponse(false, null, null, null);
    }

    public static DecisionResponse filled(UUID campaignId, UUID creativeId, int durationSeconds) {
        return new DecisionResponse(true, campaignId, creativeId, durationSeconds);
    }
}
