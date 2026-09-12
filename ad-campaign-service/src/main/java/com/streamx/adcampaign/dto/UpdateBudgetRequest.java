package com.streamx.adcampaign.dto;

import java.math.BigDecimal;

public record UpdateBudgetRequest(BigDecimal budgetTotal, BigDecimal budgetDaily, BigDecimal cpm) {
}
