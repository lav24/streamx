package com.streamx.addecisioning.service;

import com.streamx.addecisioning.cache.CampaignCache;
import com.streamx.addecisioning.dto.DecisionRequest;
import com.streamx.addecisioning.dto.DecisionResponse;
import com.streamx.addecisioning.event.AdDecisionMadeEvent;
import com.streamx.addecisioning.event.CampaignUpdatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class AdDecisioningService {

    private static final Set<String> AD_ELIGIBLE_TIERS = Set.of("FREE");
    private static final String AD_DECISION_MADE_TOPIC = "ad.decision.made";

    private final CampaignCache campaignCache;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public DecisionResponse decide(DecisionRequest request) {
        if (!AD_ELIGIBLE_TIERS.contains(request.subscriptionTier())) {
            return finish(request, DecisionResponse.noFill());
        }

        List<CampaignUpdatedEvent> matchingCampaigns = campaignCache.all().stream()
                .filter(c -> "ACTIVE".equals(c.status()))
                .filter(c -> matchesTargeting(c, request))
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
