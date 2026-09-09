package com.streamx.video.service;

import com.streamx.video.dto.ContinueWatchingItem;
import com.streamx.video.dto.HeartbeatRequest;
import com.streamx.video.entity.User;
import com.streamx.video.entity.Video;
import com.streamx.video.entity.WatchHistory;
import com.streamx.video.entity.WatchHistoryId;
import com.streamx.video.repository.UserRepository;
import com.streamx.video.repository.VideoRepository;
import com.streamx.video.repository.WatchHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WatchHistoryService {

    private final WatchHistoryRepository watchHistoryRepository;
    private final UserRepository userRepository;
    private final VideoRepository videoRepository;

    @Transactional
    public void recordHeartbeat(HeartbeatRequest request) {
        WatchHistoryId id = new WatchHistoryId(request.userId(), request.videoId());

        WatchHistory watchHistory = watchHistoryRepository.findById(id)
                .orElseGet(() -> newWatchHistory(id, request));

        watchHistory.setPositionSeconds(request.positionSeconds());
        watchHistory.setCompleted(Boolean.TRUE.equals(request.completed()));
        watchHistory.setLastWatchedAt(Instant.now());

        watchHistoryRepository.save(watchHistory);
    }

    private WatchHistory newWatchHistory(WatchHistoryId id, HeartbeatRequest request) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found: " + request.userId()));
        Video video = videoRepository.findById(request.videoId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Video not found: " + request.videoId()));

        return WatchHistory.builder()
                .id(id)
                .user(user)
                .video(video)
                .build();
    }

    @Transactional(readOnly = true)
    public List<ContinueWatchingItem> getContinueWatching(UUID userId) {
        return watchHistoryRepository.findContinueWatching(userId).stream()
                .map(ContinueWatchingItem::from)
                .toList();
    }
}
