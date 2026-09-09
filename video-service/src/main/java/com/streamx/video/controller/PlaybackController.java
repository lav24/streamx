package com.streamx.video.controller;

import com.streamx.video.service.PlaybackService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/playback")
@RequiredArgsConstructor
public class PlaybackController {

    private final PlaybackService playbackService;

    @GetMapping(value = "/{videoId}/manifest", produces = "application/vnd.apple.mpegurl")
    public String getManifest(@PathVariable UUID videoId) {
        return playbackService.getMasterPlaylist(videoId);
    }
}
