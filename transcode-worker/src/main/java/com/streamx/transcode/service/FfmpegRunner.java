package com.streamx.transcode.service;

import com.streamx.transcode.config.TranscodeProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

@Component
@Slf4j
public class FfmpegRunner {

    public void transcodeToHls(Path input, Path outputDir, TranscodeProperties.Resolution resolution) throws IOException {
        List<String> command = List.of(
                "ffmpeg", "-y",
                "-i", input.toString(),
                "-vf", "scale=%d:%d".formatted(resolution.width(), resolution.height()),
                "-c:v", "h264", "-b:v", resolution.bitrateKbps() + "k",
                "-c:a", "aac",
                "-hls_time", "6",
                "-hls_playlist_type", "vod",
                "-hls_segment_filename", outputDir.resolve("segment_%03d.ts").toString(),
                outputDir.resolve("index.m3u8").toString());
        run(command);
    }

    public void extractThumbnail(Path input, Path output) throws IOException {
        List<String> command = List.of(
                "ffmpeg", "-y",
                "-i", input.toString(),
                "-ss", "00:00:01",
                "-vframes", "1",
                output.toString());
        run(command);
    }

    private void run(List<String> command) throws IOException {
        Process process = new ProcessBuilder(command)
                .redirectErrorStream(true)
                .start();

        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = process.inputReader()) {
            reader.lines().forEach(line -> output.append(line).append('\n'));
        }

        int exitCode;
        try {
            exitCode = process.waitFor();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("ffmpeg process interrupted", e);
        }

        if (exitCode != 0) {
            throw new IllegalStateException(
                    "ffmpeg exited with code %d for command: %s\n--- ffmpeg output ---\n%s"
                            .formatted(exitCode, command, output));
        }

        log.info("[ffmpeg] {}", output);
    }
}
