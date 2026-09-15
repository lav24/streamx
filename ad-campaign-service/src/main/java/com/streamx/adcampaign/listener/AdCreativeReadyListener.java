package com.streamx.adcampaign.listener;

import com.streamx.adcampaign.event.AdCreativeReadyEvent;
import com.streamx.adcampaign.service.CampaignService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdCreativeReadyListener {

    private final CampaignService campaignService;

    @KafkaListener(topics = "ad.creative.ready")
    public void onCreativeReady(AdCreativeReadyEvent event) {
        log.info("Received creative-ready event for creative {}", event.creativeId());
        try {
            campaignService.handleCreativeReady(event);
        } catch (Exception e) {
            log.error("Failed to process creative-ready for creative {}", event.creativeId(), e);
        }
    }
}
