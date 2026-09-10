package com.streamx.adcampaign.dto;

import com.streamx.adcampaign.entity.AdCampaign;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record CampaignResponse(
        UUID id,
        UUID advertiserId,
        String name,
        BigDecimal budgetTotal,
        BigDecimal budgetDaily,
        LocalDate startDate,
        LocalDate endDate,
        String status,
        Instant createdAt) {

    public static CampaignResponse from(AdCampaign campaign) {
        return new CampaignResponse(
                campaign.getId(),
                campaign.getAdvertiserId(),
                campaign.getName(),
                campaign.getBudgetTotal(),
                campaign.getBudgetDaily(),
                campaign.getStartDate(),
                campaign.getEndDate(),
                campaign.getStatus().name(),
                campaign.getCreatedAt());
    }
}
