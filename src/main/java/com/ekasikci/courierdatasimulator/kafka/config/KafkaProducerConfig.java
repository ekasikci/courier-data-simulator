package com.ekasikci.courierdatasimulator.kafka.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import com.ekasikci.courierdatasimulator.kafka.dto.MappedPackage;

@Configuration
public class KafkaProducerConfig {

    /**
     * KafkaTemplate for sending MappedPackage objects to Kafka.
     */
    @Bean
    public KafkaTemplate<String, MappedPackage> kafkaTemplate(
            ProducerFactory<String, MappedPackage> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }
}