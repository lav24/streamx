package com.streamx.budgetpacing.listener;

import com.streamx.budgetpacing.event.AdImpressionEvent;
import com.streamx.budgetpacing.redis.PacingRedisService;
import com.streamx.budgetpacing.redis.PacingResult;
import com.streamx.budgetpacing.repository.SpendLedgerRepository;
import com.streamx.budgetpacing.util.Money;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdImpressionListener {

    private static final long IMPRESSIONS_PER_CPM_UNIT = 1000L;

    private final PacingRedisService pacingRedisService;
    private final SpendLedgerRepository spendLedgerRepository;

    @KafkaListener(topics = "ad.impression", containerFactory = "adImpressionContainerFactory")
    public void onImpression(AdImpressionEvent event) {
        Long cpmMicros = pacingRedisService.getCpmMicros(event.campaignId());
        if (cpmMicros == null) {
            log.warn("No cached CPM for campaign {}, dropping impression {}", event.campaignId(), event.eventId());
            return;
        }

        long costMicros = cpmMicros / IMPRESSIONS_PER_CPM_UNIT;

        PacingResult result = pacingRedisService.checkAndSpend(event.eventId(), event.campaignId(), costMicros);

        switch (result) {
            case OK -> {
                spendLedgerRepository.recordSpend(event.campaignId(), LocalDate.now(), Money.toDollars(costMicros));
                log.info("Recorded spend for campaign {} (impression {}): {}",
                        event.campaignId(), event.eventId(), Money.toDollars(costMicros));
            }
            case DUPLICATE -> log.info("Duplicate impression {} for campaign {}, skipped", event.eventId(), event.campaignId());
            case DAILY_CAP_EXCEEDED -> log.info("Campaign {} exceeded daily cap, impression {} not counted", event.campaignId(), event.eventId());
            case TOTAL_CAP_EXCEEDED -> log.info("Campaign {} exceeded total budget, impression {} not counted", event.campaignId(), event.eventId());
            case UNKNOWN_CAMPAIGN -> log.warn("Campaign {} has no cached caps, impression {} not counted", event.campaignId(), event.eventId());
        }
    }
}
