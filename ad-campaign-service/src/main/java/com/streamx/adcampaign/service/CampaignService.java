package com.streamx.adcampaign.service;

import com.streamx.adcampaign.config.S3Properties;
import com.streamx.adcampaign.dto.*;
import com.streamx.adcampaign.entity.AdCampaign;
import com.streamx.adcampaign.entity.AdCreative;
import com.streamx.adcampaign.entity.AdCreativeRendition;
import com.streamx.adcampaign.entity.AdTargetingRule;
import com.streamx.adcampaign.event.AdCreativeReadyEvent;
import com.streamx.adcampaign.event.AdCreativeUploadedEvent;
import com.streamx.adcampaign.event.CampaignUpdatedEvent;
import com.streamx.adcampaign.repository.AdCampaignRepository;
import com.streamx.adcampaign.repository.AdCreativeRenditionRepository;
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
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CampaignService {

    private static final String CAMPAIGN_UPDATED_TOPIC = "campaign.updated";
    private static final String AD_CREATIVE_UPLOADED_TOPIC = "ad.creative.uploaded";
    private static final Duration UPLOAD_URL_TTL = Duration.ofMinutes(15);

    private final AdCampaignRepository campaignRepository;
    private final AdTargetingRuleRepository targetingRuleRepository;
    private final AdCreativeRepository creativeRepository;
    private final AdCreativeRenditionRepository creativeRenditionRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final S3Presigner s3Presigner;
    private final S3Client s3Client;
    private final S3Properties s3Properties;

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
    public InitCreativeResponse initCreative(UUID campaignId, InitCreativeRequest request) {
        AdCampaign campaign = getCampaignOrThrow(campaignId);

        UUID creativeId = UUID.randomUUID();
        String s3Key = "ads/raw/%s.%s".formatted(creativeId, request.fileExtension());

        AdCreative creative = AdCreative.builder()
                .id(creativeId)
                .campaign(campaign)
                .placement(request.placement())
                .durationSeconds(request.durationSeconds())
                .status(AdCreative.Status.UPLOADING)
                .s3RawKey(s3Key)
                .build();
        creativeRepository.save(creative);

        PresignedPutObjectRequest presigned = s3Presigner.presignPutObject(b -> b
                .signatureDuration(UPLOAD_URL_TTL)
                .putObjectRequest(p -> p.bucket(s3Properties.bucket()).key(s3Key)));

        return new InitCreativeResponse(creativeId, presigned.url().toString(), Instant.now().plus(UPLOAD_URL_TTL));
    }

    @Transactional
    public void completeCreative(UUID campaignId, UUID creativeId) {
        AdCreative creative = getCreativeOrThrow(campaignId, creativeId);

        if (!objectExistsInS3(creative.getS3RawKey())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "No object found at " + creative.getS3RawKey() + " — upload did not complete");
        }

        creative.setStatus(AdCreative.Status.PROCESSING);
        creativeRepository.save(creative);

        kafkaTemplate.send(
                AD_CREATIVE_UPLOADED_TOPIC,
                creativeId.toString(),
                new AdCreativeUploadedEvent(creativeId, creative.getS3RawKey()));
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
                .map(c -> CreativeResponse.from(c, creativeRenditionRepository.findByCreativeId(c.getId())))
                .toList();
        return new CampaignDetailResponse(CampaignResponse.from(campaign), rules, creatives);
    }

    @Transactional
    public void handleCreativeReady(AdCreativeReadyEvent event) {
        AdCreative creative = creativeRepository.findById(event.creativeId())
                .orElseThrow(() -> new IllegalStateException(
                        "Creative not found for transcode completion: " + event.creativeId()));

        creative.setStatus(AdCreative.Status.READY);
        creativeRepository.save(creative);

        for (AdCreativeReadyEvent.RenditionResult r : event.renditions()) {
            AdCreativeRendition rendition = AdCreativeRendition.builder()
                    .creative(creative)
                    .resolution(r.resolution())
                    .bitrateKbps(r.bitrateKbps())
                    .width(r.width())
                    .height(r.height())
                    .hlsPlaylistKey(r.hlsPlaylistKey())
                    .build();
            creativeRenditionRepository.save(rendition);
        }

        publishCampaignUpdated(creative.getCampaign().getId());
    }

    private AdCampaign getCampaignOrThrow(UUID campaignId) {
        return campaignRepository.findById(campaignId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Campaign not found: " + campaignId));
    }

    private AdCreative getCreativeOrThrow(UUID campaignId, UUID creativeId) {
        AdCreative creative = creativeRepository.findById(creativeId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Creative not found: " + creativeId));
        if (!creative.getCampaign().getId().equals(campaignId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Creative not found on campaign " + campaignId + ": " + creativeId);
        }
        return creative;
    }

    private boolean objectExistsInS3(String key) {
        try {
            s3Client.headObject(HeadObjectRequest.builder()
                    .bucket(s3Properties.bucket())
                    .key(key)
                    .build());
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        }
    }

    private void publishCampaignUpdated(UUID campaignId) {
        AdCampaign campaign = getCampaignOrThrow(campaignId);

        List<CampaignUpdatedEvent.TargetingRule> rules = targetingRuleRepository.findByCampaignId(campaignId).stream()
                .map(r -> new CampaignUpdatedEvent.TargetingRule(r.getDimension().name(), List.of(r.getTargetValues())))
                .toList();

        List<CampaignUpdatedEvent.Creative> creatives = creativeRepository.findByCampaignId(campaignId).stream()
                .filter(c -> c.getStatus() == AdCreative.Status.READY)
                .map(c -> new CampaignUpdatedEvent.Creative(
                        c.getId(),
                        c.getPlacement().name(),
                        c.getDurationSeconds(),
                        creativeRenditionRepository.findByCreativeId(c.getId()).stream()
                                .map(r -> new CampaignUpdatedEvent.Rendition(
                                        r.getResolution(), r.getHlsPlaylistKey(), r.getBitrateKbps(),
                                        r.getWidth(), r.getHeight()))
                                .toList()))
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
