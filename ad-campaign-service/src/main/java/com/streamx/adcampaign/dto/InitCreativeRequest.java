package com.streamx.adcampaign.dto;

import com.streamx.adcampaign.entity.AdCreative;

public record InitCreativeRequest(AdCreative.Placement placement, Integer durationSeconds, String fileExtension) {
}
