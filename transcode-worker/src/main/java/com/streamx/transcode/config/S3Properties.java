package com.streamx.transcode.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "streamx.s3")
public record S3Properties(String endpoint, String accessKey, String secretKey, String bucket) {
}
