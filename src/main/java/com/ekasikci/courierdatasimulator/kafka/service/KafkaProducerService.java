package com.ekasikci.courierdatasimulator.kafka.service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import com.ekasikci.courierdatasimulator.kafka.dto.MappedPackage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerService {

    // String for the key to ensure consistent hashing for Kafka partitioning and
    // compatibility with the standard StringSerializer.
    private final KafkaTemplate<String, MappedPackage> kafkaTemplate;

    @Value("${kafka.topic.packages}")
    private String packageTopic;

    /**
     * Send a single MappedPackage to Kafka topic
     */
    public void sendPackage(MappedPackage mappedPackage) {
        String key = String.valueOf(mappedPackage.getId());

        CompletableFuture<SendResult<String, MappedPackage>> future = kafkaTemplate.send(packageTopic, key,
                mappedPackage);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Successfully sent package with id={} to topic={} at partition={} with offset={}",
                        mappedPackage.getId(),
                        packageTopic,
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            } else {
                log.error("Failed to send package with id={} to topic={}",
                        mappedPackage.getId(), packageTopic, ex);
            }
        });
    }

    /**
     * Send multiple MappedPackages to Kafka topic
     */
    public void sendPackages(List<MappedPackage> mappedPackages) {
        log.info("Sending {} packages to Kafka topic: {}", mappedPackages.size(), packageTopic);

        int successCount = 0;
        int failureCount = 0;

        for (MappedPackage mappedPackage : mappedPackages) {
            try {
                sendPackage(mappedPackage);
                successCount++;
            } catch (Exception e) {
                log.error("Error sending package with id={}", mappedPackage.getId(), e);
                failureCount++;
            }
        }

        log.info("Batch send completed: {} successful, {} failed", successCount, failureCount);
    }
}