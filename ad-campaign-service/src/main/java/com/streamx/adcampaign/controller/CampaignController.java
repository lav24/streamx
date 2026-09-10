package com.streamx.adcampaign.controller;

import com.streamx.adcampaign.dto.*;
import com.streamx.adcampaign.service.CampaignService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/campaigns")
@RequiredArgsConstructor
public class CampaignController {

    private final CampaignService campaignService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CampaignResponse createCampaign(@RequestBody CreateCampaignRequest request) {
        return campaignService.createCampaign(request);
    }

    @PostMapping("/{campaignId}/targeting-rules")
    @ResponseStatus(HttpStatus.CREATED)
    public TargetingRuleResponse addTargetingRule(
            @PathVariable UUID campaignId, @RequestBody AddTargetingRuleRequest request) {
        return campaignService.addTargetingRule(campaignId, request);
    }

    @PostMapping("/{campaignId}/creatives")
    @ResponseStatus(HttpStatus.CREATED)
    public CreativeResponse addCreative(
            @PathVariable UUID campaignId, @RequestBody AddCreativeRequest request) {
        return campaignService.addCreative(campaignId, request);
    }

    @PostMapping("/{campaignId}/activate")
    public CampaignResponse activateCampaign(@PathVariable UUID campaignId) {
        return campaignService.activateCampaign(campaignId);
    }

    @GetMapping
    public Page<CampaignResponse> listCampaigns(Pageable pageable) {
        return campaignService.listCampaigns(pageable);
    }

    @GetMapping("/{campaignId}")
    public CampaignDetailResponse getCampaignDetail(@PathVariable UUID campaignId) {
        return campaignService.getCampaignDetail(campaignId);
    }
}
