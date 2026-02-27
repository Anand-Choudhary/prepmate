package com.interview_platform.call_service.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic callQualityDataTopic() {
        return TopicBuilder.name("call-quality-data")
                .partitions(5)
                .replicas(2)
                .config("retention.ms", "604800000")
                .config("cleanup.policy", "delete")
                .config("compression.type", "snappy")
                .config("min.insync.replicas", "2")
                .config("segment.ms", "86400000")
                .config("max.message.bytes", "1048576")
                .build();
    }
}