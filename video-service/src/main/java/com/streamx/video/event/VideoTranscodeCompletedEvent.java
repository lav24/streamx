package com.streamx.video.event;

import java.util.List;
import java.util.UUID;

public record VideoTranscodeCompletedEvent(UUID videoId, List<RenditionResult> renditions, String thumbnailKey) {

    public record RenditionResult(String resolution, String hlsPlaylistKey, int bitrateKbps, int width, int height) {
    }
}
