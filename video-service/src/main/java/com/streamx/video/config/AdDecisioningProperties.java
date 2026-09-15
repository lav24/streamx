package com.streamx.video.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "streamx.ad-decisioning")
public record AdDecisioningProperties(String baseUrl, int timeoutMs) {
}
