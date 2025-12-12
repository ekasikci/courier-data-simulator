package com.ekasikci.courierdatasimulator.kafka.streams;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.ekasikci.courierdatasimulator.kafka.dto.MappedPackage;
import com.ekasikci.courierdatasimulator.kafka.dto.PackageCDC;
import com.ekasikci.courierdatasimulator.kafka.transformer.PackageTransformer;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Kafka Streams processor that:
 * 1. Consumes from Debezium CDC topic (dbserver.package_db.package)
 * 2. Filters out cancelled packages
 * 3. Transforms Package -> MappedPackage
 * 4. Produces to mapped-packages topic
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.kafka.streams.auto-startup", havingValue = "true", matchIfMissing = true)
public class PackageStreamProcessor {

    private final PackageTransformer packageTransformer;
    private final ObjectMapper objectMapper;

    @Value("${app.kafka.topics.cdc-packages}")
    private String cdcTopic;

    @Value("${app.kafka.topics.mapped-packages}")
    private String mappedTopic;

    @Bean
    public KStream<String, String> packageStream(StreamsBuilder streamsBuilder) {

        // Create stream from CDC topic
        KStream<String, String> cdcStream = streamsBuilder
                .stream(cdcTopic, Consumed.with(Serdes.String(), Serdes.String()));

        // Process: Filter -> Transform -> Output
        cdcStream
                // Parse JSON to PackageCDC
                .mapValues(this::parsePackageCDC)

                // Filter out nulls (parse failures)
                .filter((key, pkg) -> pkg != null)

                // Filter out canceled packages
                .filter((key, pkg) -> !pkg.isCancelled())

                // Log incoming packages (optional, for debugging)
                .peek((key, pkg) -> log.debug("Processing package: id={}, status={}",
                        pkg.getId(), pkg.getStatus()))

                // Transform to MappedPackage
                .mapValues(packageTransformer::transform)

                // Filter out null transformations
                .filter((key, mapped) -> mapped != null)

                // Convert to JSON
                .mapValues(this::toJson)

                // Filter out serialization failures
                .filter((key, json) -> json != null)

                // Log output (optional, for debugging)
                .peek((key, json) -> log.info("Sending to {}: {}", mappedTopic, json))

                // Send to output topic
                .to(mappedTopic, Produced.with(Serdes.String(), Serdes.String()));

        log.info("Kafka Streams topology built: {} -> {}", cdcTopic, mappedTopic);

        return cdcStream;
    }

    /**
     * Parse JSON string to PackageCDC object
     */
    private PackageCDC parsePackageCDC(String json) {
        try {
            return objectMapper.readValue(json, PackageCDC.class);
        } catch (Exception e) {
            log.error("Failed to parse PackageCDC from JSON: {}", json, e);
            return null;
        }
    }

    /**
     * Convert MappedPackage to JSON string
     */
    private String toJson(MappedPackage mappedPackage) {
        try {
            return objectMapper.writeValueAsString(mappedPackage);
        } catch (Exception e) {
            log.error("Failed to serialize MappedPackage to JSON: {}", mappedPackage, e);
            return null;
        }
    }
}