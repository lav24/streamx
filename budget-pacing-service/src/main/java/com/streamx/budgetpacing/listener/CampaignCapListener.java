package com.streamx.budgetpacing.listener;

import com.streamx.budgetpacing.event.CampaignUpdatedEvent;
import com.streamx.budgetpacing.redis.PacingRedisService;
import com.streamx.budgetpacing.util.Money;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CampaignCapListener {

    private final PacingRedisService pacingRedisService;

    @KafkaListener(topics = "campaign.updated", containerFactory = "campaignUpdatedContainerFactory")
    public void onCampaignUpdated(CampaignUpdatedEvent event) {
        if (event.budgetTotal() == null || event.budgetDaily() == null || event.cpm() == null) {
            log.warn("Campaign {} missing budget/cpm fields, skipping cap update", event.campaignId());
            return;
        }

        long dailyCapMicros = Money.toMicros(event.budgetDaily());
        long totalCapMicros = Money.toMicros(event.budgetTotal());
        long cpmMicros = Money.toMicros(event.cpm());

        pacingRedisService.storeCampaignCaps(event.campaignId(), dailyCapMicros, totalCapMicros, cpmMicros);

        log.info("Cached caps for campaign {} (daily={}, total={}, cpm={})",
                event.campaignId(), event.budgetDaily(), event.budgetTotal(), event.cpm());
    }
}
