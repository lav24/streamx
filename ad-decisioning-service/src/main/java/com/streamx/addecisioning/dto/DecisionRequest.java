package com.streamx.addecisioning.dto;

import java.util.UUID;

public record DecisionRequest(
        UUID userId,
        String subscriptionTier,
        String geo,
        String device,
        String ageBracket,
        String genre,
        String placement) {
}
