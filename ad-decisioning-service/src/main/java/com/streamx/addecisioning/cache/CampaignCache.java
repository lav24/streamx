package com.streamx.addecisioning.cache;

import com.streamx.addecisioning.event.CampaignUpdatedEvent;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class CampaignCache {

    private final Map<UUID, CampaignUpdatedEvent> campaigns = new ConcurrentHashMap<>();

    public void put(CampaignUpdatedEvent event) {
        campaigns.put(event.campaignId(), event);
    }

    public Collection<CampaignUpdatedEvent> all() {
        return campaigns.values();
    }

    public int size() {
        return campaigns.size();
    }
}
