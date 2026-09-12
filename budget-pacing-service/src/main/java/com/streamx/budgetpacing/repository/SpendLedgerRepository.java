package com.streamx.budgetpacing.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class SpendLedgerRepository {

    private final JdbcTemplate jdbcTemplate;

    public void recordSpend(UUID campaignId, LocalDate date, BigDecimal amount) {
        jdbcTemplate.update(
                """
                INSERT INTO ad_spend_ledger (campaign_id, date, amount_spent)
                VALUES (?, ?, ?)
                ON CONFLICT (campaign_id, date)
                DO UPDATE SET amount_spent = ad_spend_ledger.amount_spent + EXCLUDED.amount_spent
                """,
                campaignId, date, amount);
    }
}
