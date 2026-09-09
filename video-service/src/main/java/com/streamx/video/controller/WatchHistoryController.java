package com.streamx.video.controller;

import com.streamx.video.dto.ContinueWatchingItem;
import com.streamx.video.dto.HeartbeatRequest;
import com.streamx.video.service.WatchHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/history")
@RequiredArgsConstructor
public class WatchHistoryController {

    private final WatchHistoryService watchHistoryService;

    @PostMapping("/heartbeat")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void heartbeat(@RequestBody HeartbeatRequest request) {
        watchHistoryService.recordHeartbeat(request);
    }

    @GetMapping("/continue-watching")
    public List<ContinueWatchingItem> continueWatching(@RequestParam UUID userId) {
        return watchHistoryService.getContinueWatching(userId);
    }
}
