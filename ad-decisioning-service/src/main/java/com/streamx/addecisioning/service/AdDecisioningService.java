package com.streamx.addecisioning.service;

import com.streamx.addecisioning.cache.CampaignCache;
import com.streamx.addecisioning.dto.DecisionRequest;
import com.streamx.addecisioning.dto.DecisionResponse;
import com.streamx.addecisioning.event.AdDecisionMadeEvent;
import com.streamx.addecisioning.event.CampaignUpdatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class AdDecisioningService {

    private static final Set<String> AD_ELIGIBLE_TIERS = Set.of("FREE");
    private static final String AD_DECISION_MADE_TOPIC = "ad.decision.made";

    private final CampaignCache campaignCache;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final StringRedisTemplate redisTemplate;

    public DecisionResponse decide(DecisionRequest request) {
        if (!AD_ELIGIBLE_TIERS.contains(request.subscriptionTier())) {
            return finish(request, DecisionResponse.noFill());
        }

        List<CampaignUpdatedEvent> matchingCampaigns = campaignCache.all().stream()
                .filter(c -> "ACTIVE".equals(c.status()))
                .filter(c -> matchesTargeting(c, request))
                .filter(c -> isWithinBudget(c.campaignId()))
                .toList();

        if (matchingCampaigns.isEmpty()) {
            return finish(request, DecisionResponse.noFill());
        }

        CampaignUpdatedEvent winner = pickRandom(matchingCampaigns);

        List<CampaignUpdatedEvent.Creative> eligibleCreatives = winner.creatives().stream()
                .filter(c -> c.placement().equals(request.placement()))
                .toList();

        if (eligibleCreatives.isEmpty()) {
            return finish(request, DecisionResponse.noFill());
        }

        CampaignUpdatedEvent.Creative creative = pickRandom(eligibleCreatives);

        DecisionResponse response = DecisionResponse.filled(
                winner.campaignId(), creative.assetUrl(), creative.durationSeconds());

        return finish(request, response, winner.campaignId());
    }

    private boolean matchesTargeting(CampaignUpdatedEvent campaign, DecisionRequest request) {
        for (CampaignUpdatedEvent.TargetingRule rule : campaign.targetingRules()) {
            String viewerValue = viewerValueFor(rule.dimension(), request);
            if (viewerValue == null || !rule.targetValues().contains(viewerValue)) {
                return false;
            }
        }
        return true;
    }

    private boolean isWithinBudget(UUID campaignId) {
        List<String> keys = List.of(
                "budget:cap:daily:" + campaignId,
                "budget:cap:total:" + campaignId,
                "budget:spend:daily:" + campaignId + ":" + LocalDate.now(),
                "budget:spend:total:" + campaignId);

        List<String> values = redisTemplate.opsForValue().multiGet(keys);

        String dailyCapStr = values.get(0);
        String totalCapStr = values.get(1);

        if (dailyCapStr == null || totalCapStr == null) {
            return false;
        }

        long dailySpend = parseOrZero(values.get(2));
        long totalSpend = parseOrZero(values.get(3));
        long dailyCap = Long.parseLong(dailyCapStr);
        long totalCap = Long.parseLong(totalCapStr);

        return dailySpend < dailyCap && totalSpend < totalCap;
    }

    private long parseOrZero(String value) {
        return value == null ? 0L : Long.parseLong(value);
    }

    private String viewerValueFor(String dimension, DecisionRequest request) {
        return switch (dimension) {
            case "GEO" -> request.geo();
            case "DEVICE" -> request.device();
            case "AGE_BRACKET" -> request.ageBracket();
            case "GENRE" -> request.genre();
            default -> null;
        };
    }

    private <T> T pickRandom(List<T> items) {
        return items.get(ThreadLocalRandom.current().nextInt(items.size()));
    }

    private DecisionResponse finish(DecisionRequest request, DecisionResponse response) {
        return finish(request, response, null);
    }

    private DecisionResponse finish(DecisionRequest request, DecisionResponse response, java.util.UUID campaignId) {
        kafkaTemplate.send(
                AD_DECISION_MADE_TOPIC,
                campaignId != null ? campaignId.toString() : request.userId().toString(),
                new AdDecisionMadeEvent(campaignId, request.userId(), request.placement(), response.filled(), Instant.now()));
        return response;
    }
}
