package com.streamx.video.service;

import com.streamx.video.config.S3Properties;
import com.streamx.video.dto.UploadInitRequest;
import com.streamx.video.dto.UploadInitResponse;
import com.streamx.video.entity.Episode;
import com.streamx.video.entity.Video;
import com.streamx.video.event.VideoUploadCompletedEvent;
import com.streamx.video.repository.EpisodeRepository;
import com.streamx.video.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VideoUploadService {

    private static final String VIDEO_UPLOAD_COMPLETED_TOPIC = "video.upload.completed";
    private static final Duration UPLOAD_URL_TTL = Duration.ofMinutes(15);

    private final EpisodeRepository episodeRepository;
    private final VideoRepository videoRepository;
    private final S3Presigner s3Presigner;
    private final S3Client s3Client;
    private final S3Properties s3Properties;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Transactional
    public UploadInitResponse initUpload(UploadInitRequest request) {
        Episode episode = episodeRepository.findById(request.episodeId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Episode not found: " + request.episodeId()));

        UUID videoId = UUID.randomUUID();
        String s3Key = "raw/%s.%s".formatted(videoId, request.fileExtension());

        Video video = Video.builder()
                .id(videoId)
                .episode(episode)
                .status(Video.Status.UPLOADING)
                .s3RawKey(s3Key)
                .build();
        videoRepository.save(video);

        PresignedPutObjectRequest presigned = s3Presigner.presignPutObject(b -> b
                .signatureDuration(UPLOAD_URL_TTL)
                .putObjectRequest(p -> p.bucket(s3Properties.bucket()).key(s3Key)));

        return new UploadInitResponse(videoId, presigned.url().toString(), Instant.now().plus(UPLOAD_URL_TTL));
    }

    @Transactional
    public void completeUpload(UUID videoId) {
        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Video not found: " + videoId));

        boolean objectExists = objectExistsInS3(video.getS3RawKey());
        if (!objectExists) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "No object found at " + video.getS3RawKey() + " — upload did not complete");
        }

        video.setStatus(Video.Status.PROCESSING);
        videoRepository.save(video);

        kafkaTemplate.send(
                VIDEO_UPLOAD_COMPLETED_TOPIC,
                videoId.toString(),
                new VideoUploadCompletedEvent(videoId, video.getS3RawKey()));
    }

    private boolean objectExistsInS3(String key) {
        try {
            s3Client.headObject(HeadObjectRequest.builder()
                    .bucket(s3Properties.bucket())
                    .key(key)
                    .build());
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        }
    }
}
