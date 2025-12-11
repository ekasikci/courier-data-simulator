package com.ekasikci.courierdatasimulator.kafka.scheduler;

import java.util.List;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.ekasikci.courierdatasimulator.kafka.config.DataGenerationConfig;
import com.ekasikci.courierdatasimulator.kafka.config.IngestionStrategyConfig;
import com.ekasikci.courierdatasimulator.kafka.entitiy.Package;
import com.ekasikci.courierdatasimulator.kafka.repository.PackageRepository;
import com.ekasikci.courierdatasimulator.kafka.simulator.PackageDataGenerator;
import com.ekasikci.courierdatasimulator.kafka.strategy.IngestionStrategy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Orchestrates data generation and ingestion
 * - Generates realistic package data
 * - Routes to appropriate ingestion strategy
 * - Handles scheduling based on configuration
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataIngestionOrchestrator {

    private final DataGenerationConfig dataConfig;
    private final IngestionStrategyConfig ingestionConfig;
    private final PackageDataGenerator dataGenerator;
    private final PackageRepository packageRepository;
    private final List<IngestionStrategy> strategies;

    @EventListener(ApplicationReadyEvent.class)
    public void onStartup() {
        log.info("=".repeat(80));
        log.info("DATA INGESTION ORCHESTRATOR - CONFIGURATION");
        log.info("=".repeat(80));
        log.info("Data Generation: {}", dataConfig.isEnabled() ? "ENABLED" : "DISABLED");
        log.info("Traffic Pattern: {}", dataConfig.getPattern());
        log.info("Base Rate: {} packages/second", dataConfig.getBaseRate());
        log.info("Ingestion Enabled: {}", ingestionConfig.isEnabled());
        log.info("Ingestion Strategy: {}", ingestionConfig.getStrategy());

        if (!dataConfig.isEnabled()) {
            log.info("⚠ Data generation is DISABLED. Exiting orchestrator.");
            return;
        }

        if (dataConfig.isEnabled()) {
            log.info("✓ Data generation is ENABLED");
        }

        if (!ingestionConfig.isEnabled()) {
            log.info("⚠ Ingestion is DISABLED");
        }

        if ("BATCH".equalsIgnoreCase(ingestionConfig.getStrategy())) {
            log.info("Batch Interval: {} minutes", ingestionConfig.getBatch().getIntervalMinutes());
        } else if ("MICROBATCH".equalsIgnoreCase(ingestionConfig.getStrategy())) {
            log.info("Micro-batch Interval: {} seconds", ingestionConfig.getMicrobatch().getIntervalSeconds());
        }

        log.info("=".repeat(80));
    }

    /**
     * BATCH Strategy - Runs every N minutes (configured)
     * High throughput, high latency
     */
    @Scheduled(initialDelayString = "${ingestion.batch.interval-minutes}000", fixedRateString = "${ingestion.batch.interval-minutes}0000")
    public void batchIngestion() {
        if (!"BATCH".equalsIgnoreCase(ingestionConfig.getStrategy()) || !dataConfig.isEnabled()) {
            return;
        }
        log.info("========== BATCH INGESTION CYCLE START ==========");

        List<Package> packages = getPackages();
        ingestIfEnabled(packages);

        log.info("========== BATCH INGESTION CYCLE END ==========");
    }

    /**
     * MICRO-BATCH Strategy - Runs every N seconds (configured)
     * Balance of latency and throughput
     */
    @Scheduled(initialDelay = 1000, fixedRateString = "${ingestion.microbatch.interval-seconds}000")
    public void microBatchIngestion() {
        if (!"MICROBATCH".equalsIgnoreCase(ingestionConfig.getStrategy()) || !dataConfig.isEnabled()) {
            return;
        }

        List<Package> packages = getPackages();
        ingestIfEnabled(packages);
    }

    /**
     * STREAMING Strategy - Continuous processing
     * Low latency, real-time
     */
    @Scheduled(fixedRate = 100) // Check every 100ms for new packages
    public void streamingIngestion() {
        if (!"STREAMING".equalsIgnoreCase(ingestionConfig.getStrategy()) || !dataConfig.isEnabled()) {
            return;
        }

        List<Package> packages = getPackages();
        ingestIfEnabled(packages);
    }

    private void ingestIfEnabled(List<Package> packages) {
        if (!ingestionConfig.isEnabled()) {
            return;
        }

        IngestionStrategy strategy = getActiveStrategy();
        if (strategy != null && !packages.isEmpty()) {
            strategy.ingest(packages);
        }
    }

    /**
     * Get packages - either generate new ones or fetch from DB
     */
    private List<Package> getPackages() {
        if (dataConfig.isEnabled()) {
            // Generate realistic data
            List<Package> generated = dataGenerator.generateBatch();

            // Save to database
            List<Package> saved = packageRepository.saveAll(generated);
            log.debug("Generated and saved {} packages to database", saved.size());

            return saved;
        } else {
            // Fetch existing packages from DB that haven't been processed
            return packageRepository.findAll();
        }
    }

    /**
     * Get the currently active ingestion strategy
     */
    private IngestionStrategy getActiveStrategy() {
        return strategies.stream()
                .filter(IngestionStrategy::isActive)
                .findFirst()
                .orElse(null);
    }

    /**
     * Manual trigger endpoint (
     */
    public void manualTrigger() {
        log.info("Manual ingestion trigger activated");
        IngestionStrategy strategy = getActiveStrategy();

        if (strategy != null) {
            List<Package> packages = getPackages();
            if (!packages.isEmpty()) {
                strategy.ingest(packages);
                log.info("Manual trigger: Processed {} packages using {}",
                        packages.size(), strategy.getStrategyName());
            } else {
                log.warn("Manual trigger: No packages available");
            }
        } else {
            log.error("Manual trigger: No active strategy found!");
        }
    }
}