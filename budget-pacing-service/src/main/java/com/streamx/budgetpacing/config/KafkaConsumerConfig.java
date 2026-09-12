package com.streamx.budgetpacing.config;

import com.streamx.budgetpacing.event.AdImpressionEvent;
import com.streamx.budgetpacing.event.CampaignUpdatedEvent;
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
import java.util.UUID;

@Configuration
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    /**
     * Unique consumer group per instance - every replica needs its own full copy
     * of the campaign cap/CPM cache, same reasoning as ad-decisioning-service.
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, CampaignUpdatedEvent> campaignUpdatedContainerFactory() {
        String groupId = "budget-pacing-service-campaigns-" + UUID.randomUUID();
        ConsumerFactory<String, CampaignUpdatedEvent> consumerFactory =
                buildConsumerFactory(groupId, CampaignUpdatedEvent.class);

        ConcurrentKafkaListenerContainerFactory<String, CampaignUpdatedEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        return factory;
    }

    /**
     * Shared, stable consumer group - replicas split the partitions of
     * ad.impression between them, same reasoning as transcode-worker.
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, AdImpressionEvent> adImpressionContainerFactory() {
        ConsumerFactory<String, AdImpressionEvent> consumerFactory =
                buildConsumerFactory("budget-pacing-service", AdImpressionEvent.class);

        ConcurrentKafkaListenerContainerFactory<String, AdImpressionEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        return factory;
    }

    private <T> ConsumerFactory<String, T> buildConsumerFactory(String groupId, Class<T> targetType) {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        JsonDeserializer<T> valueDeserializer = new JsonDeserializer<>(targetType, false);
        valueDeserializer.addTrustedPackages("com.streamx.budgetpacing.event");

        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), valueDeserializer);
    }
}
