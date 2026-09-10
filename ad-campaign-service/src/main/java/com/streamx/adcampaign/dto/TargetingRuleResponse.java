package com.streamx.adcampaign.dto;

import com.streamx.adcampaign.entity.AdTargetingRule;

import java.util.List;
import java.util.UUID;

public record TargetingRuleResponse(UUID id, String dimension, List<String> targetValues) {

    public static TargetingRuleResponse from(AdTargetingRule rule) {
        return new TargetingRuleResponse(rule.getId(), rule.getDimension().name(), List.of(rule.getTargetValues()));
    }
}
