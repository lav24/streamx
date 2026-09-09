package com.streamx.video.controller;

import com.streamx.video.dto.UploadInitRequest;
import com.streamx.video.dto.UploadInitResponse;
import com.streamx.video.service.VideoUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/upload")
@RequiredArgsConstructor
public class UploadController {

    private final VideoUploadService videoUploadService;

    @PostMapping("/init")
    public UploadInitResponse init(@RequestBody UploadInitRequest request) {
        return videoUploadService.initUpload(request);
    }

    @PostMapping("/{videoId}/complete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void complete(@PathVariable UUID videoId) {
        videoUploadService.completeUpload(videoId);
    }
}
