package com.ekasikci.courierdatasimulator.kafka.strategy;

import com.ekasikci.courierdatasimulator.kafka.config.IngestionStrategyConfig;
import com.ekasikci.courierdatasimulator.kafka.dto.MappedPackage;
import com.ekasikci.courierdatasimulator.kafka.entitiy.Package;
import com.ekasikci.courierdatasimulator.kafka.service.KafkaProducerService;
import com.ekasikci.courierdatasimulator.kafka.service.PackageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * STREAMING Ingestion Strategy
 * 
 * Characteristics:
 * - Processes each package immediately as it arrives
 * - Real-time, sub-second latency
 * - Highest resource usage
 * - Can use async processing
 * 
 * Use case: Real-time tracking, instant notifications, premium services
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class StreamingIngestionStrategy implements IngestionStrategy {

    private final PackageService packageService;
    private final KafkaProducerService kafkaProducerService;
    private final IngestionStrategyConfig config;

    @Override
    public void ingest(List<Package> packages) {
        if (packages == null || packages.isEmpty()) {
            return;
        }

        long startTime = System.currentTimeMillis();
        int successCount = 0;
        int failureCount = 0;

        log.debug("[STREAMING] Processing {} packages individually", packages.size());

        for (Package pkg : packages) {
            try {
                if (config.getStreaming().isAsync()) {
                    // Async processing (non-blocking)
                    processPackageAsync(pkg);
                } else {
                    // Sync processing (blocking)
                    processPackageSync(pkg);
                }
                successCount++;
            } catch (Exception e) {
                log.error("[STREAMING] Failed to process package {}: {}",
                        pkg.getId(), e.getMessage());
                failureCount++;
            }
        }

        long elapsed = System.currentTimeMillis() - startTime;
        double avgLatency = elapsed / (double) packages.size();

        log.info("[STREAMING] Processed {} packages in {}ms (avg: {}ms/pkg, success: {}, failed: {})",
                packages.size(), elapsed, String.format("%.2f", avgLatency),
                successCount, failureCount);
    }

    /**
     * Process package synchronously (blocking)
     */
    private void processPackageSync(Package pkg) {
        long pkgStartTime = System.nanoTime();

        Optional<MappedPackage> mapped = packageService.getPackageById(pkg.getId());
        if (mapped.isPresent()) {
            kafkaProducerService.sendPackage(mapped.get());

            long pkgElapsed = (System.nanoTime() - pkgStartTime) / 1_000_000; // Convert to ms
            log.debug("[STREAMING] Package {} sent in {}ms", pkg.getId(), pkgElapsed);
        }
    }

    /**
     * Process package asynchronously (non-blocking)
     */
    @Async
    public CompletableFuture<Void> processPackageAsync(Package pkg) {
        return CompletableFuture.runAsync(() -> {
            long pkgStartTime = System.nanoTime();

            Optional<MappedPackage> mapped = packageService.getPackageById(pkg.getId());
            if (mapped.isPresent()) {
                kafkaProducerService.sendPackage(mapped.get());

                long pkgElapsed = (System.nanoTime() - pkgStartTime) / 1_000_000;
                log.debug("[STREAMING-ASYNC] Package {} sent in {}ms", pkg.getId(), pkgElapsed);
            }
        });
    }

    @Override
    public String getStrategyName() {
        return config.getStreaming().isAsync() ? "STREAMING-ASYNC" : "STREAMING-SYNC";
    }

    @Override
    public boolean isActive() {
        return "STREAMING".equalsIgnoreCase(config.getStrategy());
    }
}