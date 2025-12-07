package com.ekasikci.courierdatasimulator.kafka.simulator;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Component;

import com.ekasikci.courierdatasimulator.kafka.config.DataGenerationConfig;
import com.ekasikci.courierdatasimulator.kafka.entitiy.Package;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Generates realistic package data with various patterns and quality issues
 * Simulates real-world courier data: spikes, delays, duplicates, errors
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PackageDataGenerator {

    private final DataGenerationConfig config;
    private final AtomicLong packageIdCounter = new AtomicLong(100000);
    private final Random random = new Random();

    /**
     * Generate a batch of packages based on current configuration
     */
    public List<Package> generateBatch() {
        TrafficPattern pattern = TrafficPattern.valueOf(config.getPattern());
        LocalTime now = LocalTime.now();

        int actualRate = pattern.calculateRate(config.getBaseRate(), now);

        List<Package> packages = new ArrayList<>();
        for (int i = 0; i < actualRate; i++) {
            Package pkg = generateSinglePackage();
            packages.add(pkg);

            // Simulate duplicates
            if (random.nextDouble() < config.getDuplicateRate()) {
                packages.add(pkg); // Add duplicate
                log.debug("Generated duplicate package: {}", pkg.getId());
            }
        }

        log.info("Generated {} packages (pattern: {}, multiplier: {}x)",
                packages.size(), pattern, pattern.getMultiplier(now));

        return packages;
    }

    /**
     * Generate a single realistic package
     */
    public Package generateSinglePackage() {
        Package pkg = new Package();

        long id = packageIdCounter.incrementAndGet();
        pkg.setId(id);

        LocalDateTime now = LocalDateTime.now();
        pkg.setCreatedAt(now);
        pkg.setLastUpdatedAt(now);

        // Simulate network delay
        if (random.nextDouble() < config.getDelayProbability()) {
            int delaySeconds = random.nextInt(config.getMaxDelaySeconds());
            pkg.setCreatedAt(now.minusSeconds(delaySeconds));
            log.debug("Package {} delayed by {}s", id, delaySeconds);
        }

        // Determine package status
        double statusRoll = random.nextDouble();

        if (statusRoll < config.getCancellationRate()) {
            // Cancelled package
            pkg.setStatus("CANCELLED");
            pkg.setCancelled(1);
            pkg.setCancelledAt(now.minusMinutes(random.nextInt(30)));
            pkg.setCancelReason(getRandomCancelReason());
        } else if (statusRoll < config.getCancellationRate() + config.getCompletionRate()) {
            // Completed package
            pkg.setStatus("COMPLETED");
            pkg.setCancelled(0);

            LocalDateTime pickupTime = pkg.getCreatedAt().plusMinutes(random.nextInt(30) + 5);
            LocalDateTime completeTime = pickupTime.plusMinutes(random.nextInt(60) + 20);

            pkg.setPickedUpAt(pickupTime);
            pkg.setCompletedAt(completeTime);
            pkg.setCollected(1);
            pkg.setCollectedAt(pkg.getCreatedAt().plusMinutes(2));
        } else {
            // In-progress package
            String[] inProgressStatuses = { "WAITING_ASSIGNMENT", "IN_DELIVERY", "OUT_FOR_DELIVERY" };
            pkg.setStatus(inProgressStatuses[random.nextInt(inProgressStatuses.length)]);
            pkg.setCancelled(0);

            if (!pkg.getStatus().equals("WAITING_ASSIGNMENT")) {
                pkg.setPickedUpAt(pkg.getCreatedAt().plusMinutes(random.nextInt(20) + 5));
                pkg.setCollected(1);
                pkg.setCollectedAt(pkg.getCreatedAt().plusMinutes(2));
            }
        }

        // Set ETA (estimated time in minutes)
        pkg.setEta(random.nextInt(120) + 30); // 30-150 minutes

        // Set IDs
        pkg.setCustomerId(20000000000000L + random.nextInt(1000000));
        pkg.setStoreId(20000000000000L + random.nextInt(10000));
        pkg.setOrderId(random.nextLong(900000000) + 100000000);
        pkg.setUserId(50000000000000L + random.nextInt(100000));
        pkg.setOriginAddressId(999000000000000L + random.nextInt(1000000));

        // Package type
        String[] types = { "REGULAR", "EXPRESS", "SAME_DAY", "PRIORITY" };
        pkg.setType(types[random.nextInt(types.length)]);

        // Delivery date
        pkg.setDeliveryDate(now.toLocalDate().toString());

        // Simulate data quality issues
        if (random.nextDouble() < config.getErrorRate()) {
            simulateDataError(pkg);
        }

        return pkg;
    }

    /**
     * Simulate various data quality issues
     */
    private void simulateDataError(Package pkg) {
        int errorType = random.nextInt(4);

        switch (errorType) {
            case 0:
                // Missing customer ID
                pkg.setCustomerId(null);
                log.debug("Package {} has missing customer ID", pkg.getId());
                break;
            case 1:
                // Inconsistent timestamps (pickup before creation)
                if (pkg.getPickedUpAt() != null) {
                    pkg.setPickedUpAt(pkg.getCreatedAt().minusMinutes(5));
                    log.debug("Package {} has inconsistent timestamps", pkg.getId());
                }
                break;
            case 2:
                // Invalid status combination
                if (pkg.getStatus().equals("COMPLETED") && pkg.getCompletedAt() == null) {
                    pkg.setCompletedAt(null);
                    log.debug("Package {} has invalid status combination", pkg.getId());
                }
                break;
            case 3:
                // Missing ETA
                pkg.setEta(null);
                log.debug("Package {} has missing ETA", pkg.getId());
                break;
        }
    }

    private String getRandomCancelReason() {
        String[] reasons = {
                "Customer requested cancellation",
                "Address not found",
                "Store closed",
                "Product unavailable",
                "Duplicate order",
                "Payment failed"
        };
        return reasons[random.nextInt(reasons.length)];
    }

    /**
     * Get current traffic rate based on pattern
     */
    public int getCurrentRate() {
        TrafficPattern pattern = TrafficPattern.valueOf(config.getPattern());
        return pattern.calculateRate(config.getBaseRate(), LocalTime.now());
    }
}