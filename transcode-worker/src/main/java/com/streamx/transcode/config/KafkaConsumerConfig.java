package com.streamx.transcode.config;

import com.streamx.transcode.event.AdCreativeUploadedEvent;
import com.streamx.transcode.event.VideoUploadCompletedEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Two listener container factories, one per topic/event type — a single
 * JsonDeserializer default-type only works for one type, so each topic gets its own
 * factory. Both share the "transcode-worker" consumer group: this is a worker pool
 * (competing consumers), not a per-instance cache, so replicas should split partitions
 * of each topic between them rather than each getting a full copy.
 */
@Configuration
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, VideoUploadCompletedEvent> videoUploadContainerFactory() {
        ConsumerFactory<String, VideoUploadCompletedEvent> consumerFactory =
                buildConsumerFactory(VideoUploadCompletedEvent.class);

        ConcurrentKafkaListenerContainerFactory<String, VideoUploadCompletedEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, AdCreativeUploadedEvent> adCreativeUploadContainerFactory() {
        ConsumerFactory<String, AdCreativeUploadedEvent> consumerFactory =
                buildConsumerFactory(AdCreativeUploadedEvent.class);

        ConcurrentKafkaListenerContainerFactory<String, AdCreativeUploadedEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        return factory;
    }

    private <T> ConsumerFactory<String, T> buildConsumerFactory(Class<T> targetType) {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "transcode-worker");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        JsonDeserializer<T> valueDeserializer = new JsonDeserializer<>(targetType, false);
        valueDeserializer.addTrustedPackages("com.streamx.transcode.event");

        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), valueDeserializer);
    }
}
