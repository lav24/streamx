package com.streamx.adtracking.dto;

import java.util.UUID;

public record TrackEventRequest(UUID eventId, UUID campaignId, UUID userId, String placement) {
}
