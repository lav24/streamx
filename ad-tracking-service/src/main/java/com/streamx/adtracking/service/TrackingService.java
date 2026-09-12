package com.streamx.adtracking.service;

import com.streamx.adtracking.dto.TrackEventRequest;
import com.streamx.adtracking.event.AdClickEvent;
import com.streamx.adtracking.event.AdImpressionEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class TrackingService {

    private static final String IMPRESSION_TOPIC = "ad.impression";
    private static final String CLICK_TOPIC = "ad.click";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void trackImpression(TrackEventRequest request) {
        kafkaTemplate.send(
                IMPRESSION_TOPIC,
                request.campaignId().toString(),
                new AdImpressionEvent(
                        request.eventId(), request.campaignId(), request.userId(), request.placement(), Instant.now()));
    }

    public void trackClick(TrackEventRequest request) {
        kafkaTemplate.send(
                CLICK_TOPIC,
                request.campaignId().toString(),
                new AdClickEvent(
                        request.eventId(), request.campaignId(), request.userId(), request.placement(), Instant.now()));
    }
}
