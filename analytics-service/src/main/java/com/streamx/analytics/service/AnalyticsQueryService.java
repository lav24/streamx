package com.streamx.analytics.service;

import com.streamx.analytics.config.AnalyticsTopology;
import com.streamx.analytics.dto.CampaignStatsResponse;
import com.streamx.analytics.dto.VideoStatsResponse;
import com.streamx.analytics.util.Money;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.errors.InvalidStateStoreException;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.apache.kafka.streams.state.ReadOnlyWindowStore;
import org.apache.kafka.streams.state.WindowStoreIterator;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AnalyticsQueryService {

    private final StreamsBuilderFactoryBean streamsBuilderFactoryBean;

    public CampaignStatsResponse getCurrentWindowStats(UUID campaignId) {
        Instant windowStart = Instant.now().truncatedTo(ChronoUnit.HOURS);
        Instant windowEnd = windowStart.plus(AnalyticsTopology.WINDOW_SIZE);
        String key = campaignId.toString();

        long impressions = fetchLong(AnalyticsTopology.IMPRESSION_COUNT_STORE, key, windowStart, windowEnd);
        long clicks = fetchLong(AnalyticsTopology.CLICK_COUNT_STORE, key, windowStart, windowEnd);
        long revenueMicros = fetchLong(AnalyticsTopology.REVENUE_ESTIMATE_STORE, key, windowStart, windowEnd);

        double ctr = impressions == 0 ? 0.0 : (double) clicks / impressions;

        return new CampaignStatsResponse(
                campaignId, windowStart, windowEnd, impressions, clicks, ctr, Money.toDollars(revenueMicros));
    }

    public VideoStatsResponse getVideoStats(UUID videoId) {
        String key = videoId.toString();
        long views = fetchKeyValueLong(AnalyticsTopology.VIEWS_STORE, key);
        long totalWatchSeconds = fetchKeyValueLong(AnalyticsTopology.WATCH_DURATION_STORE, key);
        return new VideoStatsResponse(videoId, views, totalWatchSeconds);
    }

    private long fetchKeyValueLong(String storeName, String key) {
        KafkaStreams streams = streamsBuilderFactoryBean.getKafkaStreams();
        if (streams == null) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Analytics engine still starting up");
        }

        try {
            ReadOnlyKeyValueStore<String, Long> store = streams.store(
                    StoreQueryParameters.fromNameAndType(storeName, QueryableStoreTypes.keyValueStore()));
            Long value = store.get(key);
            return value == null ? 0L : value;
        } catch (InvalidStateStoreException e) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE, "Analytics store not yet queryable, try again shortly");
        }
    }

    private long fetchLong(String storeName, String key, Instant from, Instant to) {
        KafkaStreams streams = streamsBuilderFactoryBean.getKafkaStreams();
        if (streams == null) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Analytics engine still starting up");
        }

        try {
            ReadOnlyWindowStore<String, Long> store = streams.store(
                    StoreQueryParameters.fromNameAndType(storeName, QueryableStoreTypes.windowStore()));

            try (WindowStoreIterator<Long> iterator = store.fetch(key, from, to)) {
                return iterator.hasNext() ? iterator.next().value : 0L;
            }
        } catch (InvalidStateStoreException e) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE, "Analytics store not yet queryable, try again shortly");
        }
    }
}
