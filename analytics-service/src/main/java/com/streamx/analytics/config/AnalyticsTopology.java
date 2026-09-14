package com.streamx.analytics.config;

import com.streamx.analytics.event.AdClickEvent;
import com.streamx.analytics.event.AdImpressionEvent;
import com.streamx.analytics.event.CampaignUpdatedEvent;
import com.streamx.analytics.event.PlaybackHeartbeatEvent;
import com.streamx.analytics.event.SessionSummary;
import com.streamx.analytics.util.Money;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.WindowStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.support.serializer.JsonSerde;

import java.time.Duration;

@Configuration
@EnableKafkaStreams
public class AnalyticsTopology {

    public static final String IMPRESSION_COUNT_STORE = "impression-count-store";
    public static final String CLICK_COUNT_STORE = "click-count-store";
    public static final String REVENUE_ESTIMATE_STORE = "revenue-estimate-store";
    public static final String VIEWS_STORE = "views-store";
    public static final String WATCH_DURATION_STORE = "watch-duration-store";
    public static final Duration WINDOW_SIZE = Duration.ofHours(1);

    @Bean
    public KStream<String, AdImpressionEvent> buildAnalyticsTopology(StreamsBuilder streamsBuilder) {
        GlobalKTable<String, CampaignUpdatedEvent> campaignTable = streamsBuilder.globalTable(
                "campaign.updated",
                Consumed.with(Serdes.String(), jsonSerde(CampaignUpdatedEvent.class)));

        KStream<String, AdImpressionEvent> impressions = streamsBuilder.stream(
                "ad.impression", Consumed.with(Serdes.String(), jsonSerde(AdImpressionEvent.class)));

        impressions
                .groupByKey(Grouped.with(Serdes.String(), jsonSerde(AdImpressionEvent.class)))
                .windowedBy(TimeWindows.ofSizeWithNoGrace(WINDOW_SIZE))
                .count(Materialized.<String, Long, WindowStore<Bytes, byte[]>>as(IMPRESSION_COUNT_STORE)
                        .withKeySerde(Serdes.String())
                        .withValueSerde(Serdes.Long()));

        impressions
                .join(campaignTable,
                        (campaignId, impression) -> campaignId,
                        (impression, campaign) -> campaign != null && campaign.cpm() != null
                                ? Money.toMicros(campaign.cpm()) / 1000L
                                : 0L)
                .groupByKey(Grouped.with(Serdes.String(), Serdes.Long()))
                .windowedBy(TimeWindows.ofSizeWithNoGrace(WINDOW_SIZE))
                .reduce(Long::sum, Materialized.<String, Long, WindowStore<Bytes, byte[]>>as(REVENUE_ESTIMATE_STORE)
                        .withKeySerde(Serdes.String())
                        .withValueSerde(Serdes.Long()));

        KStream<String, AdClickEvent> clicks = streamsBuilder.stream(
                "ad.click", Consumed.with(Serdes.String(), jsonSerde(AdClickEvent.class)));

        clicks
                .groupByKey(Grouped.with(Serdes.String(), jsonSerde(AdClickEvent.class)))
                .windowedBy(TimeWindows.ofSizeWithNoGrace(WINDOW_SIZE))
                .count(Materialized.<String, Long, WindowStore<Bytes, byte[]>>as(CLICK_COUNT_STORE)
                        .withKeySerde(Serdes.String())
                        .withValueSerde(Serdes.Long()));

        KStream<String, PlaybackHeartbeatEvent> heartbeats = streamsBuilder.stream(
                "playback.heartbeat", Consumed.with(Serdes.String(), jsonSerde(PlaybackHeartbeatEvent.class)));

        // Stage 1: collapse many heartbeats per session into one continuously-updated
        // row per sessionId, holding the highest position seen so far.
        KTable<String, SessionSummary> sessionTable = heartbeats
                .groupByKey(Grouped.with(Serdes.String(), jsonSerde(PlaybackHeartbeatEvent.class)))
                .aggregate(
                        () -> new SessionSummary(null, 0),
                        (sessionId, heartbeat, current) -> new SessionSummary(
                                heartbeat.videoId(), Math.max(current.maxPositionSeconds(), heartbeat.positionSeconds())),
                        Materialized.<String, SessionSummary, KeyValueStore<Bytes, byte[]>>as("session-summary-store")
                                .withKeySerde(Serdes.String())
                                .withValueSerde(jsonSerde(SessionSummary.class)));

        // Stage 2: re-key that table by videoId. KGroupedTable's count/aggregate use an
        // adder AND a subtractor, so when a session's row changes, its old contribution
        // is automatically retracted before the new one is added - this is what keeps
        // "views" as a distinct-session count rather than a heartbeat count.
        KGroupedTable<String, SessionSummary> byVideo = sessionTable.groupBy(
                (sessionId, summary) -> KeyValue.pair(summary.videoId().toString(), summary),
                Grouped.with(Serdes.String(), jsonSerde(SessionSummary.class)));

        byVideo.count(Materialized.<String, Long, KeyValueStore<Bytes, byte[]>>as(VIEWS_STORE)
                .withKeySerde(Serdes.String())
                .withValueSerde(Serdes.Long()));

        byVideo.aggregate(
                () -> 0L,
                (videoId, summary, current) -> current + summary.maxPositionSeconds(),
                (videoId, summary, current) -> current - summary.maxPositionSeconds(),
                Materialized.<String, Long, KeyValueStore<Bytes, byte[]>>as(WATCH_DURATION_STORE)
                        .withKeySerde(Serdes.String())
                        .withValueSerde(Serdes.Long()));

        return impressions;
    }

    private static <T> JsonSerde<T> jsonSerde(Class<T> targetType) {
        return new JsonSerde<>(targetType).ignoreTypeHeaders();
    }
}
