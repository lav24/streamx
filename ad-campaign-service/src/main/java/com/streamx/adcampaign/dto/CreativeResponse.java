package com.streamx.adcampaign.dto;

import com.streamx.adcampaign.entity.AdCreative;
import com.streamx.adcampaign.entity.AdCreativeRendition;

import java.util.List;
import java.util.UUID;

public record CreativeResponse(
        UUID id, String placement, Integer durationSeconds, String status, List<RenditionResponse> renditions) {

    public record RenditionResponse(String resolution, Integer bitrateKbps, Integer width, Integer height) {

        public static RenditionResponse from(AdCreativeRendition rendition) {
            return new RenditionResponse(
                    rendition.getResolution(), rendition.getBitrateKbps(), rendition.getWidth(), rendition.getHeight());
        }
    }

    public static CreativeResponse from(AdCreative creative, List<AdCreativeRendition> renditions) {
        return new CreativeResponse(
                creative.getId(),
                creative.getPlacement().name(),
                creative.getDurationSeconds(),
                creative.getStatus().name(),
                renditions.stream().map(RenditionResponse::from).toList());
    }
}
