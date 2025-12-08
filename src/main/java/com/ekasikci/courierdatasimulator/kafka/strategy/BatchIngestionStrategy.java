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
 * BATCH Ingestion Strategy
 * 
 * Characteristics:
 * - Processes packages in large batches
 * - Scheduled at fixed intervals (e.g., every 5 minutes)
 * - High throughput, high latency
 * - Most resource efficient
 * 
 * Use case: Analytics, reporting, non-time-sensitive data
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BatchIngestionStrategy implements IngestionStrategy {

    private final PackageService packageService;
    private final KafkaProducerService kafkaProducerService;
    private final IngestionStrategyConfig config;

    @Override
    public void ingest(List<Package> packages) {
        if (packages == null || packages.isEmpty()) {
            log.debug("No packages to process in batch");
            return;
        }

        long startTime = System.currentTimeMillis();

        // Respect max batch size
        int maxSize = config.getBatch().getMaxSize();
        List<Package> batch = packages.stream()
                .limit(maxSize)
                .collect(Collectors.toList());

        log.info("[BATCH] Processing {} packages (batch size limit: {})",
                batch.size(), maxSize);

        // Transform and send
        List<MappedPackage> mappedPackages = batch.stream()
                .map(pkg -> packageService.getPackageById(pkg.getId()))
                .filter(java.util.Optional::isPresent)
                .map(java.util.Optional::get)
                .collect(Collectors.toList());

        kafkaProducerService.sendPackages(mappedPackages);

        long elapsed = System.currentTimeMillis() - startTime;
        double throughput = (mappedPackages.size() * 1000.0) / elapsed;

        log.info("[BATCH] Completed: {} packages in {}ms ({} pkg/sec)",
                mappedPackages.size(), elapsed, String.format("%.2f", throughput));
    }

    @Override
    public String getStrategyName() {
        return "BATCH";
    }

    @Override
    public boolean isActive() {
        return "BATCH".equalsIgnoreCase(config.getStrategy());
    }
}