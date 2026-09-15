package com.streamx.adcampaign.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

@Configuration
@EnableConfigurationProperties(S3Properties.class)
public class S3Config {

    @Bean
    public S3Client s3Client(S3Properties props) {
        return S3Client.builder()
                .endpointOverride(URI.create(props.endpoint()))
                .region(Region.US_EAST_1)
                .credentialsProvider(credentials(props))
                .serviceConfiguration(pathStyleConfig())
                .build();
    }

    @Bean
    public S3Presigner s3Presigner(S3Properties props) {
        return S3Presigner.builder()
                .endpointOverride(URI.create(props.publicEndpoint()))
                .region(Region.US_EAST_1)
                .credentialsProvider(credentials(props))
                .serviceConfiguration(pathStyleConfig())
                .build();
    }

    private StaticCredentialsProvider credentials(S3Properties props) {
        return StaticCredentialsProvider.create(
                AwsBasicCredentials.create(props.accessKey(), props.secretKey()));
    }

    private S3Configuration pathStyleConfig() {
        return S3Configuration.builder()
                .pathStyleAccessEnabled(true)
                .build();
    }
}
