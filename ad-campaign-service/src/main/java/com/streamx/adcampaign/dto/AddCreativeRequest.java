package com.streamx.adcampaign.dto;

import com.streamx.adcampaign.entity.AdCreative;

public record AddCreativeRequest(AdCreative.Placement placement, String assetUrl, Integer durationSeconds) {
}
