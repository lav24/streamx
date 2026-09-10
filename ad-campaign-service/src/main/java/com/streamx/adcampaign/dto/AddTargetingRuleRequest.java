package com.streamx.adcampaign.dto;

import com.streamx.adcampaign.entity.AdTargetingRule;

import java.util.List;

public record AddTargetingRuleRequest(AdTargetingRule.Dimension dimension, List<String> targetValues) {
}
