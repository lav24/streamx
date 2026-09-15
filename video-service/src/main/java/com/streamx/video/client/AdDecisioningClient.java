package com.streamx.video.client;

import com.streamx.video.dto.AdDecisionRequest;
import com.streamx.video.dto.AdDecisionResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Optional;
import java.util.UUID;

/**
 * Ads must never be able to block or break content playback: any failure talking to
 * ad-decisioning-service (timeout, connection refused, 5xx) is swallowed here and
 * collapsed to a safe "no ad" result instead of propagating to the caller.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AdDecisioningClient {

    private final RestClient adDecisioningRestClient;

    public AdDecisionResult decide(AdDecisionRequest request) {
        try {
            AdDecisionResult result = adDecisioningRestClient.post()
                    .uri("/ads/decision")
                    .body(request)
                    .retrieve()
                    .body(AdDecisionResult.class);
            return result != null ? result : AdDecisionResult.noFill();
        } catch (Exception e) {
            log.warn("Ad decision call failed, falling back to no-fill: {}", e.toString());
            return AdDecisionResult.noFill();
        }
    }

    public Optional<String> fetchManifest(UUID creativeId) {
        try {
            String manifest = adDecisioningRestClient.get()
                    .uri("/ads/creatives/{creativeId}/manifest", creativeId)
                    .retrieve()
                    .body(String.class);
            return Optional.ofNullable(manifest);
        } catch (Exception e) {
            log.warn("Ad manifest fetch failed for creative {}: {}", creativeId, e.toString());
            return Optional.empty();
        }
    }
}
