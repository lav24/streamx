package com.streamx.video.dto;

import java.util.UUID;

public record AdDecisionRequest(
        UUID userId,
        String subscriptionTier,
        String geo,
        String device,
        String ageBracket,
        String genre,
        String placement) {
}
