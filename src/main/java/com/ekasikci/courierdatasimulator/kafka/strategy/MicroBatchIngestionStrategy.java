package com.ekasikci.courierdatasimulator.kafka.strategy;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.ekasikci.courierdatasimulator.kafka.config.IngestionStrategyConfig;
import com.ekasikci.courierdatasimulator.kafka.dto.MappedPackage;
import com.ekasikci.courierdatasimulator.kafka.entitiy.Package;
import com.ekasikci.courierdatasimulator.kafka.service.KafkaProducerService;
import com.ekasikci.courierdatasimulator.kafka.service.PackageService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * MICRO-BATCH Ingestion Strategy
 * 
 * Characteristics:
 * - Processes packages in small batches
 * - Frequent intervals (e.g., every 1 second)
 * - Balance of latency and efficiency
 * - Moderate resource usage
 * 
 * Use case: Near real-time tracking, operational dashboards
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MicroBatchIngestionStrategy implements IngestionStrategy {

    private final PackageService packageService;
    private final KafkaProducerService kafkaProducerService;
    private final IngestionStrategyConfig config;

    @Override
    public void ingest(List<Package> packages) {
        if (packages == null || packages.isEmpty()) {
            return;
        }

        long startTime = System.currentTimeMillis();

        // Respect max micro-batch size
        int maxSize = config.getMicrobatch().getMaxSize();
        List<Package> microBatch = packages.stream()
                .limit(maxSize)
                .collect(Collectors.toList());

        log.debug("[MICRO-BATCH] Processing {} packages (limit: {})",
                microBatch.size(), maxSize);

        // Transform and send
        List<MappedPackage> mappedPackages = microBatch.stream()
                .map(pkg -> packageService.getPackageById(pkg.getId()))
                .filter(java.util.Optional::isPresent)
                .map(java.util.Optional::get)
                .collect(Collectors.toList());

        if (!mappedPackages.isEmpty()) {
            kafkaProducerService.sendPackages(mappedPackages);

            long elapsed = System.currentTimeMillis() - startTime;
            double avgLatency = elapsed / (double) mappedPackages.size();

            log.info("[MICRO-BATCH] Sent {} packages in {}ms (avg latency: {}ms/pkg)",
                    mappedPackages.size(), elapsed, String.format("%.2f", avgLatency));
        }
    }

    @Override
    public String getStrategyName() {
        return "MICROBATCH";
    }

    @Override
    public boolean isActive() {
        return "MICROBATCH".equalsIgnoreCase(config.getStrategy());
    }
}