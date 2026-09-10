package com.streamx.adcampaign.dto;

import java.util.List;

public record CampaignDetailResponse(
        CampaignResponse campaign,
        List<TargetingRuleResponse> targetingRules,
        List<CreativeResponse> creatives) {
}
