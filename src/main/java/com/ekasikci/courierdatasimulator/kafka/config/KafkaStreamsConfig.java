package com.ekasikci.courierdatasimulator.kafka.config;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.streams.StreamsConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.annotation.KafkaStreamsDefaultConfiguration;
import org.springframework.kafka.config.KafkaStreamsConfiguration;
import org.springframework.kafka.config.TopicBuilder;

import jakarta.annotation.PostConstruct;

@Configuration
@EnableKafkaStreams
@ConditionalOnProperty(name = "spring.kafka.streams.auto-startup", havingValue = "true", matchIfMissing = true)
public class KafkaStreamsConfig {

    private static final Logger log = LoggerFactory.getLogger(KafkaStreamsConfig.class);

    @Value("${spring.kafka.streams.application-id}")
    private String applicationId;

    @Value("${spring.kafka.streams.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${app.kafka.topics.cdc-packages}")
    private String cdcTopic;

    @Value("${app.kafka.topics.mapped-packages}")
    private String mappedTopic;

    @PostConstruct
    public void createTopicsIfNeeded() {
        Map<String, Object> adminProps = new HashMap<>();
        adminProps.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        adminProps.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, 10000);

        try (AdminClient adminClient = AdminClient.create(adminProps)) {
            NewTopic cdcTopicConfig = TopicBuilder
                    .name(cdcTopic)
                    .replicas(1)
                    .config("cleanup.policy", "delete")
                    .config("retention.ms", "604800000")
                    .config("min.insync.replicas", "1")
                    .build();

            NewTopic mappedTopicConfig = TopicBuilder
                    .name(mappedTopic)
                    .replicas(1)
                    .config("cleanup.policy", "delete")
                    .config("min.insync.replicas", "1")
                    .build();

            adminClient.createTopics(java.util.Arrays.asList(cdcTopicConfig, mappedTopicConfig))
                    .all()
                    .get(30, TimeUnit.SECONDS);

            log.info("✅ Topics created successfully: {}, {}", cdcTopic, mappedTopic);
        } catch (org.apache.kafka.common.errors.TopicExistsException e) {
            log.info("✅ Topics already exist: {}, {}", cdcTopic, mappedTopic);
        } catch (Exception e) {
            if (e.getCause() instanceof org.apache.kafka.common.errors.TopicExistsException) {
                log.info("✅ Topics already exist");
            } else {
                log.warn("⚠️  Could not create topics (they may already exist): {}", e.getMessage());
            }
        }
    }

    @Bean(name = KafkaStreamsDefaultConfiguration.DEFAULT_STREAMS_CONFIG_BEAN_NAME)
    public KafkaStreamsConfiguration kStreamsConfig() {
        Map<String, Object> props = new HashMap<>();

        // Basic Configuration
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, applicationId);
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG,
                org.apache.kafka.common.serialization.Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG,
                org.apache.kafka.common.serialization.Serdes.String().getClass());

        // EXACTLY-ONCE SEMANTICS V2
        props.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, StreamsConfig.EXACTLY_ONCE_V2);

        // Producer configuration for output topics
        props.put(StreamsConfig.producerPrefix(ProducerConfig.ACKS_CONFIG), "all");
        props.put(StreamsConfig.producerPrefix(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG), true);
        props.put(StreamsConfig.producerPrefix(ProducerConfig.RETRIES_CONFIG), Integer.MAX_VALUE);
        props.put(StreamsConfig.producerPrefix(ProducerConfig.COMPRESSION_TYPE_CONFIG), "snappy");

        // Consumer configuration for input topics
        props.put(StreamsConfig.consumerPrefix(ConsumerConfig.ISOLATION_LEVEL_CONFIG), "read_committed");
        props.put(StreamsConfig.consumerPrefix(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG), "earliest");
        props.put(StreamsConfig.consumerPrefix(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG), 30000);

        log.info("🔧 Kafka Streams configured with EXACTLY_ONCE_V2 guarantee");

        return new KafkaStreamsConfiguration(props);
    }
}