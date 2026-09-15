package com.streamx.addecisioning.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "streamx.s3")
public record S3Properties(String publicEndpoint, String bucket) {
}
