//package com.example.user_service.kafka.topic;
//
//import com.example.user_service.util.EventType;
//import org.apache.kafka.clients.admin.NewTopic;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.kafka.config.TopicBuilder;
//
//@Configuration
//public class KafkaTopics
//{
//
//    @Bean
//    public NewTopic userRegistered() {
//        return TopicBuilder.name(String.valueOf(EventType.USER_REGISTERED))
//                .partitions(10)
//                .replicas(3)
//                .config("retention.ms", "604800000") // 7 days
//                .config("compression.type", "snappy")
//                .build();
//    }
//
//}
