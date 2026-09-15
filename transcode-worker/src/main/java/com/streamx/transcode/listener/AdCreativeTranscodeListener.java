package com.streamx.transcode.listener;

import com.streamx.transcode.event.AdCreativeUploadedEvent;
import com.streamx.transcode.service.AdCreativeTranscodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdCreativeTranscodeListener {

    private final AdCreativeTranscodeService adCreativeTranscodeService;

    @KafkaListener(topics = "ad.creative.uploaded", containerFactory = "adCreativeUploadContainerFactory")
    public void onCreativeUploaded(AdCreativeUploadedEvent event) {
        log.info("Received creative-uploaded event for creative {}", event.creativeId());
        try {
            adCreativeTranscodeService.process(event);
        } catch (Exception e) {
            log.error("Transcoding failed for ad creative {}", event.creativeId(), e);
        }
    }
}
