package com.streamx.video.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "streamx.playback")
public record PlaybackProperties(int midRollIntervalSeconds) {
}
