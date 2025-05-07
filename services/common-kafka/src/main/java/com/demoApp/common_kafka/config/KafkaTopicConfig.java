package com.demoApp.common_kafka.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    // Core topics
    @Bean
    public NewTopic userCreatedTopic() {
        return TopicBuilder.name("user-created")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic authSuccessTopic() {
        return TopicBuilder.name("auth-success")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic otpGeneratedTopic() {
        return TopicBuilder.name("otp-generated")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic menuUpdatedTopic() {
        return TopicBuilder.name("menu-updated")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic messCreatedTopic() {
        return TopicBuilder.name("mess-created")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic subscriptionCreatedTopic() {
        return TopicBuilder.name("subscription-created")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic paymentProcessedTopic() {
        return TopicBuilder.name("payment-processed")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic deliveryAssignedTopic() {
        return TopicBuilder.name("delivery-assigned")
                .partitions(3)
                .replicas(1)
                .build();
    }
}
