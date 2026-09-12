package com.streamx.budgetpacing.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PacingRedisService {

    private static final Duration DEDUP_TTL = Duration.ofDays(7);

    private final StringRedisTemplate redisTemplate;
    private final RedisScript<String> checkAndSpendScript;

    public void storeCampaignCaps(UUID campaignId, long dailyCapMicros, long totalCapMicros, long cpmMicros) {
        redisTemplate.opsForValue().set(dailyCapKey(campaignId), String.valueOf(dailyCapMicros));
        redisTemplate.opsForValue().set(totalCapKey(campaignId), String.valueOf(totalCapMicros));
        redisTemplate.opsForValue().set(cpmKey(campaignId), String.valueOf(cpmMicros));
    }

    public Long getCpmMicros(UUID campaignId) {
        String value = redisTemplate.opsForValue().get(cpmKey(campaignId));
        return value == null ? null : Long.parseLong(value);
    }

    public PacingResult checkAndSpend(UUID eventId, UUID campaignId, long costMicros) {
        List<String> keys = List.of(
                dedupKey(eventId),
                dailySpendKey(campaignId),
                totalSpendKey(campaignId),
                dailyCapKey(campaignId),
                totalCapKey(campaignId));

        String result = redisTemplate.execute(
                checkAndSpendScript,
                keys,
                String.valueOf(DEDUP_TTL.toSeconds()),
                String.valueOf(costMicros));

        return PacingResult.valueOf(result);
    }

    private String dedupKey(UUID eventId) {
        return "dedup:impression:" + eventId;
    }

    private String dailySpendKey(UUID campaignId) {
        return "budget:spend:daily:" + campaignId + ":" + LocalDate.now();
    }

    private String totalSpendKey(UUID campaignId) {
        return "budget:spend:total:" + campaignId;
    }

    private String dailyCapKey(UUID campaignId) {
        return "budget:cap:daily:" + campaignId;
    }

    private String totalCapKey(UUID campaignId) {
        return "budget:cap:total:" + campaignId;
    }

    private String cpmKey(UUID campaignId) {
        return "budget:cpm:" + campaignId;
    }
}
