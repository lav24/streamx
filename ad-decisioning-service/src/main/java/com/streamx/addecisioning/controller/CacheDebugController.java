package com.streamx.addecisioning.controller;

import com.streamx.addecisioning.cache.CampaignCache;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/debug")
@RequiredArgsConstructor
public class CacheDebugController {

    private final CampaignCache campaignCache;

    @GetMapping("/cache")
    public Map<String, Object> debugCache() {
        return Map.of(
                "size", campaignCache.size(),
                "campaigns", campaignCache.all());
    }
}
