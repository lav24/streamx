package com.streamx.adcampaign.dto;

import com.streamx.adcampaign.entity.AdCreative;

import java.util.UUID;

public record CreativeResponse(UUID id, String placement, String assetUrl, Integer durationSeconds) {

    public static CreativeResponse from(AdCreative creative) {
        return new CreativeResponse(
                creative.getId(), creative.getPlacement().name(), creative.getAssetUrl(), creative.getDurationSeconds());
    }
}
