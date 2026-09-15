package com.streamx.addecisioning.cache;

import com.streamx.addecisioning.event.CampaignUpdatedEvent;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class CampaignCache {

    private final Map<UUID, CampaignUpdatedEvent> campaigns = new ConcurrentHashMap<>();

    /**
     * Drops creatives with a null creativeId before caching: campaign.updated replays
     * from the start of the topic on every restart (see application.yml), so older
     * messages produced under a previous event schema can still be in there. A creative
     * with no id is unusable regardless of how it got here, so it's filtered out once,
     * at the point it enters the cache, rather than every place the cache is read.
     */
    public void put(CampaignUpdatedEvent event) {
        List<CampaignUpdatedEvent.Creative> validCreatives = event.creatives().stream()
                .filter(c -> c.creativeId() != null)
                .toList();
        campaigns.put(event.campaignId(), new CampaignUpdatedEvent(
                event.campaignId(),
                event.status(),
                event.startDate(),
                event.endDate(),
                event.targetingRules(),
                validCreatives));
    }

    public Collection<CampaignUpdatedEvent> all() {
        return campaigns.values();
    }

    public int size() {
        return campaigns.size();
    }
}
