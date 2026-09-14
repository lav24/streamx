package com.streamx.analytics.controller;

import com.streamx.analytics.dto.CampaignStatsResponse;
import com.streamx.analytics.dto.VideoStatsResponse;
import com.streamx.analytics.service.AnalyticsQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsQueryService analyticsQueryService;

    @GetMapping("/campaigns/{campaignId}/stats")
    public CampaignStatsResponse getCampaignStats(@PathVariable UUID campaignId) {
        return analyticsQueryService.getCurrentWindowStats(campaignId);
    }

    @GetMapping("/videos/{videoId}/stats")
    public VideoStatsResponse getVideoStats(@PathVariable UUID videoId) {
        return analyticsQueryService.getVideoStats(videoId);
    }
}
