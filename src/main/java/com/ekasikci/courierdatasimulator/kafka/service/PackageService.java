package com.ekasikci.courierdatasimulator.kafka.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.ekasikci.courierdatasimulator.kafka.dto.MappedPackage;
import com.ekasikci.courierdatasimulator.kafka.entitiy.Package;
import com.ekasikci.courierdatasimulator.kafka.repository.PackageRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PackageService {

    private final PackageRepository packageRepository;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final DateTimeFormatter FORMATTER_WITH_MICROSECONDS = DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");

    /**
     * Get a single package by ID and transform to MappedPackage
     * Returns empty if package is cancelled
     */
    public Optional<MappedPackage> getPackageById(Long id) {
        Optional<Package> packageOpt = packageRepository.findById(id);

        if (packageOpt.isEmpty()) {
            log.warn("Package with id {} not found", id);
            return Optional.empty();
        }

        Package pkg = packageOpt.get();

        // Filter out cancelled packages
        if (pkg.isCancelled()) {
            log.info("Package with id {} is cancelled, filtering out", id);
            return Optional.empty();
        }

        return Optional.of(mapToMappedPackage(pkg));
    }

    /**
     * Get all non-cancelled packages and transform them to MappedPackages
     */
    public List<MappedPackage> getAllNonCancelledPackages() {
        List<Package> packages = packageRepository.findAllNonCancelled();
        log.info("Found {} non-cancelled packages", packages.size());

        return packages.stream()
                .map(this::mapToMappedPackage)
                .collect(Collectors.toList());
    }

    /**
     * Transform Package entity to MappedPackage DTO
     * Implements all business logic for calculations
     */
    private MappedPackage mapToMappedPackage(Package pkg) {
        MappedPackage mapped = new MappedPackage();

        mapped.setId(pkg.getId());
        mapped.setEta(pkg.getEta());

        // Format timestamps
        mapped.setCreatedAt(formatDateTime(pkg.getCreatedAt()));
        mapped.setLastUpdatedAt(formatDateTime(pkg.getLastUpdatedAt()));

        // If package is not completed, set calculated fields to null
        if (!pkg.isCompleted()) {
            mapped.setCollectionDuration(null);
            mapped.setDeliveryDuration(null);
            mapped.setLeadTime(null);
            mapped.setOrderInTime(null);
            log.debug("Package {} is not completed, setting calculated fields to null", pkg.getId());
            return mapped;
        }

        // Calculate collection duration (created_at to picked_up_at)
        Integer collectionDuration = calculateDuration(pkg.getCreatedAt(), pkg.getPickedUpAt());
        mapped.setCollectionDuration(collectionDuration);

        // Calculate delivery duration (picked_up_at to completed_at)
        Integer deliveryDuration = calculateDuration(pkg.getPickedUpAt(), pkg.getCompletedAt());
        mapped.setDeliveryDuration(deliveryDuration);

        // Calculate lead time (created_at to completed_at)
        Integer leadTime = calculateDuration(pkg.getCreatedAt(), pkg.getCompletedAt());
        mapped.setLeadTime(leadTime);

        // Determine if order is in time (leadTime <= eta)
        Boolean orderInTime = null;
        if (leadTime != null && pkg.getEta() != null) {
            orderInTime = leadTime <= pkg.getEta();
        }
        mapped.setOrderInTime(orderInTime);

        log.debug("Mapped package {}: collectionDuration={}, deliveryDuration={}, leadTime={}, orderInTime={}",
                pkg.getId(), collectionDuration, deliveryDuration, leadTime, orderInTime);

        return mapped;
    }

    /**
     * Calculate duration in minutes between two timestamps
     * Returns null if either timestamp is null
     */
    private Integer calculateDuration(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            return null;
        }

        Duration duration = Duration.between(start, end);
        return (int) duration.toMinutes();
    }

    /**
     * Format LocalDateTime to string with appropriate format
     */
    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }

        // Check if there are nanoseconds (if so use microsecond conversion)
        if (dateTime.getNano() > 0) {
            return dateTime.format(FORMATTER_WITH_MICROSECONDS);
        } else {
            return dateTime.format(FORMATTER);
        }
    }
}