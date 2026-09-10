package com.streamx.adcampaign.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreateCampaignRequest(
        UUID advertiserId,
        String name,
        BigDecimal budgetTotal,
        BigDecimal budgetDaily,
        LocalDate startDate,
        LocalDate endDate) {
}
