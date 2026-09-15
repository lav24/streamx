ALTER TABLE ad_creatives DROP COLUMN asset_url;
ALTER TABLE ad_creatives ADD COLUMN s3_raw_key VARCHAR(500) NOT NULL DEFAULT '';
ALTER TABLE ad_creatives ALTER COLUMN s3_raw_key DROP DEFAULT;
ALTER TABLE ad_creatives ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'UPLOADING'
    CHECK (status IN ('UPLOADING', 'PROCESSING', 'READY', 'FAILED'));

CREATE TABLE ad_creative_renditions (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    creative_id       UUID NOT NULL REFERENCES ad_creatives(id) ON DELETE CASCADE,
    resolution        VARCHAR(10) NOT NULL,
    bitrate_kbps      INT NOT NULL,
    width             INT,
    height            INT,
    hls_playlist_key  VARCHAR(500) NOT NULL,
    UNIQUE (creative_id, resolution)
);

CREATE INDEX idx_creative_renditions_creative_id ON ad_creative_renditions(creative_id);
