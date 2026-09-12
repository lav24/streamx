package com.streamx.adcampaign.service;

import com.streamx.adcampaign.dto.*;
import com.streamx.adcampaign.entity.AdCampaign;
import com.streamx.adcampaign.entity.AdCreative;
import com.streamx.adcampaign.entity.AdTargetingRule;
import com.streamx.adcampaign.event.CampaignUpdatedEvent;
import com.streamx.adcampaign.repository.AdCampaignRepository;
import com.streamx.adcampaign.repository.AdCreativeRepository;
import com.streamx.adcampaign.repository.AdTargetingRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CampaignService {

    private static final String CAMPAIGN_UPDATED_TOPIC = "campaign.updated";

    private final AdCampaignRepository campaignRepository;
    private final AdTargetingRuleRepository targetingRuleRepository;
    private final AdCreativeRepository creativeRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Transactional
    public CampaignResponse createCampaign(CreateCampaignRequest request) {
        AdCampaign campaign = AdCampaign.builder()
                .advertiserId(request.advertiserId())
                .name(request.name())
                .budgetTotal(request.budgetTotal())
                .budgetDaily(request.budgetDaily())
                .cpm(request.cpm())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .build();
        campaignRepository.save(campaign);
        publishCampaignUpdated(campaign.getId());
        return CampaignResponse.from(campaign);
    }

    @Transactional
    public TargetingRuleResponse addTargetingRule(UUID campaignId, AddTargetingRuleRequest request) {
        AdCampaign campaign = getCampaignOrThrow(campaignId);
        AdTargetingRule rule = AdTargetingRule.builder()
                .campaign(campaign)
                .dimension(request.dimension())
                .targetValues(request.targetValues().toArray(new String[0]))
                .build();
        targetingRuleRepository.save(rule);
        publishCampaignUpdated(campaignId);
        return TargetingRuleResponse.from(rule);
    }

    @Transactional
    public CreativeResponse addCreative(UUID campaignId, AddCreativeRequest request) {
        AdCampaign campaign = getCampaignOrThrow(campaignId);
        AdCreative creative = AdCreative.builder()
                .campaign(campaign)
                .placement(request.placement())
                .assetUrl(request.assetUrl())
                .durationSeconds(request.durationSeconds())
                .build();
        creativeRepository.save(creative);
        publishCampaignUpdated(campaignId);
        return CreativeResponse.from(creative);
    }

    @Transactional
    public CampaignResponse updateBudget(UUID campaignId, UpdateBudgetRequest request) {
        AdCampaign campaign = getCampaignOrThrow(campaignId);
        campaign.setBudgetTotal(request.budgetTotal());
        campaign.setBudgetDaily(request.budgetDaily());
        campaign.setCpm(request.cpm());
        campaignRepository.save(campaign);
        publishCampaignUpdated(campaignId);
        return CampaignResponse.from(campaign);
    }

    @Transactional
    public CampaignResponse activateCampaign(UUID campaignId) {
        AdCampaign campaign = getCampaignOrThrow(campaignId);
        campaign.setStatus(AdCampaign.Status.ACTIVE);
        campaignRepository.save(campaign);
        publishCampaignUpdated(campaignId);
        return CampaignResponse.from(campaign);
    }

    @Transactional(readOnly = true)
    public Page<CampaignResponse> listCampaigns(Pageable pageable) {
        return campaignRepository.findAll(pageable).map(CampaignResponse::from);
    }

    @Transactional(readOnly = true)
    public CampaignDetailResponse getCampaignDetail(UUID campaignId) {
        AdCampaign campaign = getCampaignOrThrow(campaignId);
        List<TargetingRuleResponse> rules = targetingRuleRepository.findByCampaignId(campaignId).stream()
                .map(TargetingRuleResponse::from)
                .toList();
        List<CreativeResponse> creatives = creativeRepository.findByCampaignId(campaignId).stream()
                .map(CreativeResponse::from)
                .toList();
        return new CampaignDetailResponse(CampaignResponse.from(campaign), rules, creatives);
    }

    private AdCampaign getCampaignOrThrow(UUID campaignId) {
        return campaignRepository.findById(campaignId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Campaign not found: " + campaignId));
    }

    private void publishCampaignUpdated(UUID campaignId) {
        AdCampaign campaign = getCampaignOrThrow(campaignId);

        List<CampaignUpdatedEvent.TargetingRule> rules = targetingRuleRepository.findByCampaignId(campaignId).stream()
                .map(r -> new CampaignUpdatedEvent.TargetingRule(r.getDimension().name(), List.of(r.getTargetValues())))
                .toList();

        List<CampaignUpdatedEvent.Creative> creatives = creativeRepository.findByCampaignId(campaignId).stream()
                .map(c -> new CampaignUpdatedEvent.Creative(
                        c.getPlacement().name(), c.getAssetUrl(), c.getDurationSeconds()))
                .toList();

        CampaignUpdatedEvent event = new CampaignUpdatedEvent(
                campaignId,
                campaign.getStatus().name(),
                campaign.getBudgetTotal(),
                campaign.getBudgetDaily(),
                campaign.getCpm(),
                campaign.getStartDate(),
                campaign.getEndDate(),
                rules,
                creatives);

        kafkaTemplate.send(CAMPAIGN_UPDATED_TOPIC, campaignId.toString(), event);
    }
}
