package com.streamx.adcampaign.event;

import java.util.List;
import java.util.UUID;

public record AdCreativeReadyEvent(UUID creativeId, List<RenditionResult> renditions) {

    public record RenditionResult(String resolution, String hlsPlaylistKey, int bitrateKbps, int width, int height) {
    }
}
