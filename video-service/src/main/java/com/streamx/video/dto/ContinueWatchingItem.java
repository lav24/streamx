package com.streamx.video.dto;

import com.streamx.video.entity.WatchHistory;

import java.time.Instant;
import java.util.UUID;

public record ContinueWatchingItem(UUID videoId, Integer positionSeconds, String thumbnailKey, Instant lastWatchedAt) {

    public static ContinueWatchingItem from(WatchHistory watchHistory) {
        return new ContinueWatchingItem(
                watchHistory.getVideo().getId(),
                watchHistory.getPositionSeconds(),
                watchHistory.getVideo().getThumbnailKey(),
                watchHistory.getLastWatchedAt());
    }
}
