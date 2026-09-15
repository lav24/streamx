package com.streamx.transcode.service;

import com.streamx.transcode.config.S3Properties;
import com.streamx.transcode.config.TranscodeProperties;
import com.streamx.transcode.event.AdCreativeReadyEvent;
import com.streamx.transcode.event.AdCreativeUploadedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdCreativeTranscodeService {

    private static final String AD_CREATIVE_READY_TOPIC = "ad.creative.ready";

    private final S3Client s3Client;
    private final S3Properties s3Properties;
    private final TranscodeProperties transcodeProperties;
    private final FfmpegRunner ffmpegRunner;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void process(AdCreativeUploadedEvent event) throws IOException {
        Path workDir = Files.createTempDirectory("ad-transcode-" + event.creativeId());
        try {
            Path inputFile = workDir.resolve("input" + extensionOf(event.s3RawKey()));
            downloadFromS3(event.s3RawKey(), inputFile);

            List<AdCreativeReadyEvent.RenditionResult> renditions = new ArrayList<>();
            for (TranscodeProperties.Resolution resolution : transcodeProperties.resolutions()) {
                Path outputDir = workDir.resolve(resolution.name());
                Files.createDirectories(outputDir);
                ffmpegRunner.transcodeToHls(inputFile, outputDir, resolution);

                String s3Prefix = "ads/renditions/%s/%s/".formatted(event.creativeId(), resolution.name());
                uploadDirectory(outputDir, s3Prefix);

                renditions.add(new AdCreativeReadyEvent.RenditionResult(
                        resolution.name(), s3Prefix + "index.m3u8", resolution.bitrateKbps(),
                        resolution.width(), resolution.height()));

                log.info("Rendition {} done for ad creative {}", resolution.name(), event.creativeId());
            }

            kafkaTemplate.send(
                    AD_CREATIVE_READY_TOPIC,
                    event.creativeId().toString(),
                    new AdCreativeReadyEvent(event.creativeId(), renditions));

            log.info("Transcoding complete for ad creative {}", event.creativeId());
        } finally {
            deleteRecursively(workDir);
        }
    }

    private void downloadFromS3(String key, Path destination) {
        s3Client.getObject(
                GetObjectRequest.builder().bucket(s3Properties.bucket()).key(key).build(),
                destination);
    }

    private void uploadFile(Path file, String key) {
        s3Client.putObject(
                PutObjectRequest.builder().bucket(s3Properties.bucket()).key(key).build(),
                RequestBody.fromFile(file));
    }

    private void uploadDirectory(Path directory, String s3Prefix) throws IOException {
        try (Stream<Path> files = Files.list(directory)) {
            for (Path file : files.toList()) {
                uploadFile(file, s3Prefix + file.getFileName());
            }
        }
    }

    private void deleteRecursively(Path root) {
        try (Stream<Path> walk = Files.walk(root)) {
            walk.sorted(Comparator.reverseOrder()).forEach(p -> {
                try {
                    Files.delete(p);
                } catch (IOException e) {
                    log.warn("Failed to delete temp file {}", p, e);
                }
            });
        } catch (IOException e) {
            log.warn("Failed to clean up temp dir {}", root, e);
        }
    }

    private String extensionOf(String key) {
        int dot = key.lastIndexOf('.');
        return dot >= 0 ? key.substring(dot) : "";
    }
}
