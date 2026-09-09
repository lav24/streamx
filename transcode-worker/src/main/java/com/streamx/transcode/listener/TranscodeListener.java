package com.streamx.transcode.listener;

import com.streamx.transcode.event.VideoUploadCompletedEvent;
import com.streamx.transcode.service.TranscodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TranscodeListener {

    private final TranscodeService transcodeService;

    @KafkaListener(topics = "video.upload.completed")
    public void onVideoUploaded(VideoUploadCompletedEvent event) {
        log.info("Received upload-completed event for video {}", event.videoId());
        try {
            transcodeService.process(event);
        } catch (Exception e) {
            log.error("Transcoding failed for video {}", event.videoId(), e);
        }
    }
}
