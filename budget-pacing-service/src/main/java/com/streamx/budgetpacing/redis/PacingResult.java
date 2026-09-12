package com.streamx.budgetpacing.redis;

public enum PacingResult {
    OK,
    DUPLICATE,
    DAILY_CAP_EXCEEDED,
    TOTAL_CAP_EXCEEDED,
    UNKNOWN_CAMPAIGN
}
