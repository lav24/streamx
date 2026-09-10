CREATE TABLE ad_campaigns (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    advertiser_id  UUID NOT NULL,
    name           VARCHAR(255) NOT NULL,
    budget_total   NUMERIC(12, 2) NOT NULL,
    budget_daily   NUMERIC(12, 2) NOT NULL,
    start_date     DATE NOT NULL,
    end_date       DATE NOT NULL,
    status         VARCHAR(20) NOT NULL DEFAULT 'DRAFT'
                   CHECK (status IN ('DRAFT', 'ACTIVE', 'PAUSED', 'ENDED')),
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE ad_targeting_rules (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    campaign_id  UUID NOT NULL REFERENCES ad_campaigns(id) ON DELETE CASCADE,
    dimension       VARCHAR(20) NOT NULL
                    CHECK (dimension IN ('GEO', 'AGE_BRACKET', 'DEVICE', 'GENRE')),
    target_values   TEXT[] NOT NULL
);

CREATE TABLE ad_creatives (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    campaign_id       UUID NOT NULL REFERENCES ad_campaigns(id) ON DELETE CASCADE,
    placement         VARCHAR(10) NOT NULL CHECK (placement IN ('PRE_ROLL', 'MID_ROLL')),
    asset_url         VARCHAR(500) NOT NULL,
    duration_seconds  INT NOT NULL
);

CREATE INDEX idx_targeting_rules_campaign_id ON ad_targeting_rules(campaign_id);
CREATE INDEX idx_creatives_campaign_id       ON ad_creatives(campaign_id);
