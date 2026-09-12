package com.streamx.adtracking.controller;

import com.streamx.adtracking.dto.TrackEventRequest;
import com.streamx.adtracking.service.TrackingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/track")
@RequiredArgsConstructor
public class TrackingController {

    private final TrackingService trackingService;

    @PostMapping("/impression")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void trackImpression(@RequestBody TrackEventRequest request) {
        trackingService.trackImpression(request);
    }

    @PostMapping("/click")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void trackClick(@RequestBody TrackEventRequest request) {
        trackingService.trackClick(request);
    }
}
