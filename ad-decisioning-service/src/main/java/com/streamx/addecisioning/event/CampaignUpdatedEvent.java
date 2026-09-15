package com.streamx.addecisioning.event;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record CampaignUpdatedEvent(
        UUID campaignId,
        String status,
        LocalDate startDate,
        LocalDate endDate,
        List<TargetingRule> targetingRules,
        List<Creative> creatives) {

    public record TargetingRule(String dimension, List<String> targetValues) {
    }

    public record Creative(UUID creativeId, String placement, int durationSeconds, List<Rendition> renditions) {
    }

    public record Rendition(String resolution, String hlsPlaylistKey, int bitrateKbps, int width, int height) {
    }
}
