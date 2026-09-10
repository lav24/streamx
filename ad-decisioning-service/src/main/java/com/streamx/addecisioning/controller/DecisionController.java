package com.streamx.addecisioning.controller;

import com.streamx.addecisioning.dto.DecisionRequest;
import com.streamx.addecisioning.dto.DecisionResponse;
import com.streamx.addecisioning.service.AdDecisioningService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ads")
@RequiredArgsConstructor
public class DecisionController {

    private final AdDecisioningService adDecisioningService;

    @PostMapping("/decision")
    public DecisionResponse decide(@RequestBody DecisionRequest request) {
        return adDecisioningService.decide(request);
    }
}
