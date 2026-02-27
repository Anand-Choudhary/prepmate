package com.interview_platform.call_service.config;


import com.interview_platform.call_service.dto.MinuteQualityDto;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka Configuration for Quality Data Pipeline
 */
@Configuration
public class KafkaConfig {

    @Value("${kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${kafka.producer.acks}")
    private String acknowledgementConfig;

    @Value("${kafka.producer.retries}")
    private Integer retriesConfig;

    @Value("${kafka.producer.batch-size}")
    private Integer batchSizeConfig;

    @Value("${kafka.producer.linger-ms}")
    private Integer lingerConfig;

    @Value("${kafka.producer.buffer-memory}")
    private Long bufferMemoryConfig;

    @Value("${kafka.producer.compression-type}")
    private String compressionType;

    @Value("${kafka.producer.request-timeout-ms:3000}")
    private Integer requestTimeoutMs;

    @Value("${kafka.producer.delivery-timeout-ms:5000}")
    private Integer deliveryTimeoutMs;

    @Value("${kafka.consumer.quality-cache-group-id:quality-cache-group}")
    private String qualityCacheGroupId;

    @Value("${kafka.consumer.session-timeout-ms:30000}")
    private Integer sessionTimeoutMs;

    @Value("${kafka.consumer.heartbeat-interval-ms:10000}")
    private Integer heartbeatIntervalMs;

    @Value("${kafka.consumer.max-poll-records:50}")
    private Integer maxPollRecords;

    @Value("${kafka.consumer.max-poll-interval-ms:300000}")
    private Integer maxPollIntervalMs;

    @Bean
    public ProducerFactory<String, MinuteQualityDto> callQualityProducerFactory() {
        Map<String, Object> config = new HashMap<>();

        // Basic configuration
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        // Reliability settings - CRITICAL for billing data
        config.put(ProducerConfig.ACKS_CONFIG, acknowledgementConfig); // "all" or "-1"
        config.put(ProducerConfig.RETRIES_CONFIG, retriesConfig); // 3-5 retries
        config.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5);
        config.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true); // Prevent duplicates

        // Performance settings - Quality data comes every minute (moderate volume)
        config.put(ProducerConfig.BATCH_SIZE_CONFIG, batchSizeConfig); // 16384 bytes
        config.put(ProducerConfig.LINGER_MS_CONFIG, lingerConfig); // 10-50ms
        config.put(ProducerConfig.BUFFER_MEMORY_CONFIG, bufferMemoryConfig); // 32MB
        config.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, compressionType); // "snappy" or "lz4"

        // Timeout settings - IMPORTANT for fallback detection
        config.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, requestTimeoutMs); // 3 seconds
        config.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, deliveryTimeoutMs); // 5 seconds

        // JSON serializer settings
        config.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);

        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public KafkaTemplate<String, MinuteQualityDto> callQualityKafkaTemplate() {
        return new KafkaTemplate<>(callQualityProducerFactory());
    }

    @Bean
    public ConsumerFactory<String, MinuteQualityDto> callQualityConsumerFactory() {
        Map<String, Object> config = new HashMap<>();

        // Basic configuration
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, qualityCacheGroupId);
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);

        // Error handling deserializer wraps JSON deserializer
        config.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class);
        config.put(JsonDeserializer.VALUE_DEFAULT_TYPE, MinuteQualityDto.class.getName());
        config.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        config.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);

        // Offset management - Manual commit for better control
        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"); // Start from beginning if no offset

        // Performance tuning for quality data caching
        config.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, maxPollRecords); // 50 records per poll
        config.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, 1024); // Wait for at least 1KB
        config.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, 500); // Wait max 500ms before returning

        // Session and heartbeat settings
        config.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, sessionTimeoutMs); // 30 seconds
        config.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, heartbeatIntervalMs); // 10 seconds
        config.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, maxPollIntervalMs); // 5 minutes

        // Isolation level - Read only committed messages
        config.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");

        return new DefaultKafkaConsumerFactory<>(
                config,
                new StringDeserializer(),
                new ErrorHandlingDeserializer<>(new JsonDeserializer<>(MinuteQualityDto.class, false))
        );
    }

    /**
     * Listener Container Factory for Call Quality Data
     * Concurrency: 3-5 threads for parallel processing
     * Acknowledgment: BATCH mode (commit after processing a batch)
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, MinuteQualityDto>
    callQualityListenerContainerFactory() {

        ConcurrentKafkaListenerContainerFactory<String, MinuteQualityDto> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(callQualityConsumerFactory());

        // Concurrency: 3 threads = can process 3 partitions in parallel
        factory.setConcurrency(3);

        // Acknowledgment mode: BATCH
        // Commits offset after processing all records in a batch
        // Good for caching (if some fail, they'll be reprocessed)
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.BATCH);

        // Poll timeout: how long to wait for new messages
        factory.getContainerProperties().setPollTimeout(3000); // 3 seconds

        // Commit interval: auto-commit after this many milliseconds
//        factory.getContainerProperties().setCommitInterval(5000); // 5 seconds

        // Error handling: Default error handler with exponential backoff
        factory.setCommonErrorHandler(
                new org.springframework.kafka.listener.DefaultErrorHandler(
                        new org.springframework.util.backoff.FixedBackOff(1000L, 3L) // Retry 3 times with 1s delay
                )
        );

        // Enable batch listener (process multiple records at once)
        factory.setBatchListener(false); // Set to true if you want batch processing

        return factory;
    }



}