package com.streamx.addecisioning.controller;

import com.streamx.addecisioning.dto.DecisionRequest;
import com.streamx.addecisioning.dto.DecisionResponse;
import com.streamx.addecisioning.service.AdCreativeManifestService;
import com.streamx.addecisioning.service.AdDecisioningService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/ads")
@RequiredArgsConstructor
public class DecisionController {

    private final AdDecisioningService adDecisioningService;
    private final AdCreativeManifestService adCreativeManifestService;

    @PostMapping("/decision")
    public DecisionResponse decide(@RequestBody DecisionRequest request) {
        return adDecisioningService.decide(request);
    }

    @GetMapping(value = "/creatives/{creativeId}/manifest", produces = "application/vnd.apple.mpegurl")
    public String getManifest(@PathVariable UUID creativeId) {
        return adCreativeManifestService.getMasterPlaylist(creativeId);
    }
}
