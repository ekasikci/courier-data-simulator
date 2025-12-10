package com.ekasikci.courierdatasimulator.kafka.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.ekasikci.courierdatasimulator.kafka.dto.MappedPackage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class MappedPackageConsumer {

    @KafkaListener(topics = "${app.kafka.topics.mapped-packages}", groupId = "mapped-package-verification-consumer")
    public void consumeMappedPackage(MappedPackage mappedPackage) { // Changed from String to MappedPackage
        log.info("========================================");
        log.info("✅ CDC Pipeline Output Received");
        log.info("========================================");
        log.info("Package ID: {}", mappedPackage.getId());
        log.info("Created At: {}", mappedPackage.getCreatedAt());
        log.info("ETA: {} minutes", mappedPackage.getEta());

        if (mappedPackage.getCollectionDuration() != null) {
            log.info("Collection Duration: {} minutes", mappedPackage.getCollectionDuration());
        }

        if (mappedPackage.getDeliveryDuration() != null) {
            log.info("Delivery Duration: {} minutes", mappedPackage.getDeliveryDuration());
        }

        if (mappedPackage.getLeadTime() != null) {
            log.info("Lead Time: {} minutes", mappedPackage.getLeadTime());
            log.info("On Time: {}", mappedPackage.getOrderInTime());
        } else {
            log.info("Package not yet completed");
        }

        log.info("========================================\n");
    }
}