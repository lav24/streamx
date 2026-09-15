package com.streamx.video.controller;

import com.streamx.video.dto.AdBreakCue;
import com.streamx.video.dto.AdBreakRequest;
import com.streamx.video.dto.AdBreakResponse;
import com.streamx.video.service.AdBreakService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/playback")
@RequiredArgsConstructor
public class AdBreakController {

    private final AdBreakService adBreakService;

    @GetMapping("/{videoId}/ad-breaks")
    public List<AdBreakCue> getAdBreaks(@PathVariable UUID videoId) {
        return adBreakService.getAdBreaks(videoId);
    }

    @PostMapping("/{videoId}/ad-break")
    public AdBreakResponse decideAdBreak(@PathVariable UUID videoId, @RequestBody AdBreakRequest request) {
        return adBreakService.decideAdBreak(videoId, request);
    }

    @GetMapping(value = "/ad-manifest/{creativeId}", produces = "application/vnd.apple.mpegurl")
    public String getAdManifest(@PathVariable UUID creativeId) {
        return adBreakService.getAdManifest(creativeId);
    }
}
