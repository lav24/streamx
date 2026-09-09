CREATE TABLE users (
    id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email              VARCHAR(255) NOT NULL UNIQUE,
    subscription_tier  VARCHAR(20)  NOT NULL DEFAULT 'FREE',
    created_at         TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE titles (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name          VARCHAR(255) NOT NULL,
    description   TEXT,
    genres        TEXT[]       NOT NULL DEFAULT '{}',
    release_year  SMALLINT,
    type          VARCHAR(10)  NOT NULL CHECK (type IN ('MOVIE', 'SERIES')),
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE episodes (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title_id          UUID NOT NULL REFERENCES titles(id) ON DELETE CASCADE,
    season            SMALLINT NOT NULL,
    episode_number    SMALLINT NOT NULL,
    duration_seconds  INT,
    UNIQUE (title_id, season, episode_number)
);

CREATE TABLE videos (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    episode_id   UUID NOT NULL REFERENCES episodes(id) ON DELETE CASCADE,
    status       VARCHAR(20)  NOT NULL DEFAULT 'UPLOADING'
                 CHECK (status IN ('UPLOADING', 'PROCESSING', 'READY', 'FAILED')),
    s3_raw_key   VARCHAR(500) NOT NULL,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE video_renditions (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    video_id          UUID NOT NULL REFERENCES videos(id) ON DELETE CASCADE,
    resolution        VARCHAR(10) NOT NULL,
    bitrate_kbps      INT NOT NULL,
    hls_playlist_key  VARCHAR(500),
    status            VARCHAR(20) NOT NULL DEFAULT 'PENDING'
                      CHECK (status IN ('PENDING', 'READY', 'FAILED')),
    UNIQUE (video_id, resolution)
);

CREATE TABLE watch_history (
    user_id           UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    video_id          UUID NOT NULL REFERENCES videos(id) ON DELETE CASCADE,
    position_seconds  INT NOT NULL DEFAULT 0,
    completed         BOOLEAN NOT NULL DEFAULT false,
    last_watched_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (user_id, video_id)
);

CREATE INDEX idx_episodes_title_id         ON episodes(title_id);
CREATE INDEX idx_videos_episode_id         ON videos(episode_id);
CREATE INDEX idx_video_renditions_video_id ON video_renditions(video_id);
CREATE INDEX idx_watch_history_user_id     ON watch_history(user_id);
