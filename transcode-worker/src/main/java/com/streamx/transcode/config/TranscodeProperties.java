package com.streamx.transcode.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "streamx.transcode")
public record TranscodeProperties(List<Resolution> resolutions) {

    public record Resolution(String name, int width, int height, int bitrateKbps) {
    }
}
