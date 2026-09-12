CREATE TABLE ad_spend_ledger (
    campaign_id   UUID NOT NULL,
    date          DATE NOT NULL,
    amount_spent  NUMERIC(14, 6) NOT NULL DEFAULT 0,
    PRIMARY KEY (campaign_id, date)
);
