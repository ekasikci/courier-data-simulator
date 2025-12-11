package com.ekasikci.courierdatasimulator.kafka.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;

import com.ekasikci.courierdatasimulator.kafka.dto.MappedPackage;

@Configuration
public class KafkaConsumerConfig {

    @Bean
    public ConsumerFactory<String, MappedPackage> mappedPackageConsumerFactory() {

        JacksonJsonDeserializer<MappedPackage> jsonDeserializer = new JacksonJsonDeserializer<>(MappedPackage.class);
        jsonDeserializer.addTrustedPackages("*");

        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "mapped-package-verification-consumer");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, jsonDeserializer);

        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), jsonDeserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, MappedPackage> mappedPackageKafkaListenerFactory() {

        ConcurrentKafkaListenerContainerFactory<String, MappedPackage> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(mappedPackageConsumerFactory());
        return factory;
    }
}
