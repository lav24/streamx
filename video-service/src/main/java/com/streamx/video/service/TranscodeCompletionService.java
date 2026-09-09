package com.streamx.video.service;

import com.streamx.video.entity.Video;
import com.streamx.video.entity.VideoRendition;
import com.streamx.video.event.VideoTranscodeCompletedEvent;
import com.streamx.video.repository.VideoRenditionRepository;
import com.streamx.video.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TranscodeCompletionService {

    private final VideoRepository videoRepository;
    private final VideoRenditionRepository videoRenditionRepository;

    @Transactional
    public void handleTranscodeCompleted(VideoTranscodeCompletedEvent event) {
        Video video = videoRepository.findById(event.videoId())
                .orElseThrow(() -> new IllegalStateException(
                        "Video not found for transcode completion: " + event.videoId()));

        video.setStatus(Video.Status.READY);
        video.setThumbnailKey(event.thumbnailKey());
        videoRepository.save(video);

        for (VideoTranscodeCompletedEvent.RenditionResult r : event.renditions()) {
            VideoRendition rendition = VideoRendition.builder()
                    .video(video)
                    .resolution(r.resolution())
                    .bitrateKbps(r.bitrateKbps())
                    .width(r.width())
                    .height(r.height())
                    .hlsPlaylistKey(r.hlsPlaylistKey())
                    .status(VideoRendition.Status.READY)
                    .build();
            videoRenditionRepository.save(rendition);
        }

        log.info("Video {} marked READY with {} renditions", event.videoId(), event.renditions().size());
    }
}
