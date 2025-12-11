package com.ekasikci.courierdatasimulator.kafka.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.streams.StreamsConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.annotation.KafkaStreamsDefaultConfiguration;
import org.springframework.kafka.config.KafkaStreamsConfiguration;

/**
 * Configuration for Kafka Streams
 */
@Configuration
@EnableKafkaStreams
@ConditionalOnProperty(name = "spring.kafka.streams.auto-startup", havingValue = "true", matchIfMissing = true)
public class KafkaStreamsConfig {

    @Value("${spring.kafka.streams.application-id}")
    private String applicationId;

    @Value("${spring.kafka.streams.bootstrap-servers}")
    private String bootstrapServers;

    @Bean(name = KafkaStreamsDefaultConfiguration.DEFAULT_STREAMS_CONFIG_BEAN_NAME)
    public KafkaStreamsConfiguration kStreamsConfig() {
        Map<String, Object> props = new HashMap<>();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, applicationId);
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG,
                org.apache.kafka.common.serialization.Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG,
                org.apache.kafka.common.serialization.Serdes.String().getClass());

        // Processing guarantees
        props.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG,
                StreamsConfig.EXACTLY_ONCE_V2);

        // Commit interval
        props.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 1000);

        // Disable caching for real-time processing
        props.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 0);

        return new KafkaStreamsConfiguration(props);
    }
}