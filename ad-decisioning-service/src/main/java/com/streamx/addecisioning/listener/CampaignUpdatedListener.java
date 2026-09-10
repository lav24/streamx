package com.streamx.addecisioning.listener;

import com.streamx.addecisioning.cache.CampaignCache;
import com.streamx.addecisioning.event.CampaignUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CampaignUpdatedListener {

    private final CampaignCache campaignCache;

    @KafkaListener(topics = "campaign.updated")
    public void onCampaignUpdated(CampaignUpdatedEvent event) {
        campaignCache.put(event);
        log.info("Cached campaign {} (status={}, {} rules, {} creatives)",
                event.campaignId(), event.status(), event.targetingRules().size(), event.creatives().size());
    }
}
