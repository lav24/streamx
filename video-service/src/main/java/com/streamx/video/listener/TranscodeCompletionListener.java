package com.streamx.video.listener;

import com.streamx.video.event.VideoTranscodeCompletedEvent;
import com.streamx.video.service.TranscodeCompletionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TranscodeCompletionListener {

    private final TranscodeCompletionService transcodeCompletionService;

    @KafkaListener(topics = "video.transcode.completed")
    public void onTranscodeCompleted(VideoTranscodeCompletedEvent event) {
        log.info("Received transcode-completed event for video {}", event.videoId());
        try {
            transcodeCompletionService.handleTranscodeCompleted(event);
        } catch (Exception e) {
            log.error("Failed to process transcode completion for video {}", event.videoId(), e);
        }
    }
}
