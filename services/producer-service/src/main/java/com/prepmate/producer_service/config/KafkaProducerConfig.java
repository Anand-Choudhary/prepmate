package com.prepmate.producer_service.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducerConfig
{
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${kafka.topics.email}")
    private String emailTopic;

    @Value("${kafka.topics.sms}")
    private String smsTopic;

    @Value("${kafka.topics.push}")
    private String pushTopic;

    @Value("${kafka.topics.webhook}")
    private String webhookTopic;

    @Value("${kafka.topics.dlq}")
    private String dlqTopic;

    @Value("${spring.kafka.producer.acks}")
    private String acknowledgementConfig;

    @Value("${spring.kafka.producer.retries}")
    private String retriesConfig;

    @Value("${spring.kafka.producer.batch-size}")
    private String batchSizeConfig;

    @Value("${spring.kafka.producer.linger-ms}")
    private String lingerConfig;

    @Value("${spring.kafka.buffer-memory}")
    private int bufferMemoryConfig;

    @Value("${spring.kafka.compression-type}")
    private int compressionType;

    @Value("${spring.kafka.partitions}")
    private int partitions;

    @Value("${spring.kafka.replication-factor}")
    private int replicationFactor;

    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> config = new HashMap<>();

        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        config.put(ProducerConfig.ACKS_CONFIG, acknowledgementConfig);
        config.put(ProducerConfig.RETRIES_CONFIG, retriesConfig);
        config.put(ProducerConfig.BATCH_SIZE_CONFIG, batchSizeConfig);
        config.put(ProducerConfig.LINGER_MS_CONFIG, lingerConfig);
        config.put(ProducerConfig.BUFFER_MEMORY_CONFIG, bufferMemoryConfig);
        config.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, compressionType);
        config.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5);
        config.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);


        config.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);

        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    @Bean
    public NewTopic emailTopic() {
        return TopicBuilder.name(emailTopic)
                .partitions(partitions)
                .replicas(replicationFactor)
                .config("retention.ms", "604800000") // 7 days
                .config("compression.type", "snappy")
                .build();
    }

    @Bean
    public NewTopic smsTopic() {
        return TopicBuilder.name(smsTopic)
                .partitions(partitions)
                .replicas(replicationFactor)
                .config("retention.ms", "604800000")
                .config("compression.type", "snappy")
                .build();
    }

    @Bean
    public NewTopic pushTopic() {
        return TopicBuilder.name(pushTopic)
                .partitions(partitions)
                .replicas(replicationFactor)
                .config("retention.ms", "604800000")
                .config("compression.type", "snappy")
                .build();
    }

    @Bean
    public NewTopic webhookTopic() {
        return TopicBuilder.name(webhookTopic)
                .partitions(partitions)
                .replicas(replicationFactor)
                .config("retention.ms", "604800000")
                .config("compression.type", "snappy")
                .build();
    }

    @Bean
    public NewTopic dlqTopic() {
        return TopicBuilder.name(dlqTopic)
                .partitions(partitions)
                .replicas(replicationFactor)
                .config("retention.ms", "2592000000") // 30 days
                .build();
    }


}
