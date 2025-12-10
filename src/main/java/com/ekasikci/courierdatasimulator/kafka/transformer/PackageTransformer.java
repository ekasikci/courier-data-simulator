package com.ekasikci.courierdatasimulator.kafka.transformer;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.springframework.stereotype.Component;

import com.ekasikci.courierdatasimulator.kafka.dto.MappedPackage;
import com.ekasikci.courierdatasimulator.kafka.dto.PackageCDC;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class PackageTransformer {

    private static final DateTimeFormatter OUTPUT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");

    private static final DateTimeFormatter[] FORMATTERS = {
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    };

    public MappedPackage transform(PackageCDC packageCDC) {
        if (packageCDC == null) {
            return null;
        }

        // Convert timestamps to formatted strings
        String createdAtFormatted = formatTimestamp(packageCDC.getCreatedAt());
        String lastUpdatedAtFormatted = formatTimestamp(packageCDC.getLastUpdatedAt());

        MappedPackage.MappedPackageBuilder builder = MappedPackage.builder()
                .id(packageCDC.getId())
                .createdAt(createdAtFormatted)
                .lastUpdatedAt(lastUpdatedAtFormatted)
                .eta(packageCDC.getEta());

        // Calculate durations
        Integer collectionDuration = calculateDuration(
                packageCDC.getCreatedAt(),
                packageCDC.getCollectedAt());
        builder.collectionDuration(collectionDuration);

        Integer deliveryDuration = calculateDuration(
                packageCDC.getPickedUpAt(),
                packageCDC.getCompletedAt());
        builder.deliveryDuration(deliveryDuration);

        Integer leadTime = calculateDuration(
                packageCDC.getCreatedAt(),
                packageCDC.getCompletedAt());
        builder.leadTime(leadTime);

        // Calculate orderInTime
        Boolean orderInTime = null;
        if (leadTime != null && packageCDC.getEta() != null) {
            orderInTime = leadTime <= packageCDC.getEta();
        }
        builder.orderInTime(orderInTime);

        return builder.build();
    }

    /**
     * Format timestamp - handles both epoch milliseconds and formatted strings
     */
    private String formatTimestamp(String timestamp) {
        if (timestamp == null || timestamp.isEmpty()) {
            return null;
        }

        try {
            // Try to parse as epoch milliseconds first
            long epochMilli = Long.parseLong(timestamp);
            LocalDateTime dateTime = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(epochMilli),
                    ZoneId.systemDefault());
            return dateTime.format(OUTPUT_FORMATTER);
        } catch (NumberFormatException e) {
            // Not a number, already formatted - return as is
            return timestamp;
        }
    }

    /**
     * Calculate duration in minutes between two timestamps
     */
    private Integer calculateDuration(String startStr, String endStr) {
        if (startStr == null || endStr == null ||
                startStr.isEmpty() || endStr.isEmpty()) {
            return null;
        }

        try {
            LocalDateTime start = parseDateTime(startStr);
            LocalDateTime end = parseDateTime(endStr);

            if (start == null || end == null) {
                return null;
            }

            Duration duration = Duration.between(start, end);
            return (int) duration.toMinutes();

        } catch (Exception e) {
            log.warn("Failed to calculate duration between {} and {}: {}",
                    startStr, endStr, e.getMessage());
            return null;
        }
    }

    /**
     * Parse datetime - handles both epoch milliseconds and formatted strings
     */
    private LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isEmpty()) {
            return null;
        }

        // Try to parse as epoch milliseconds first
        try {
            long epochMilli = Long.parseLong(dateTimeStr);
            return LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(epochMilli),
                    ZoneId.systemDefault());
        } catch (NumberFormatException e) {
            // Not a number, try formatted string parsers
        }

        // Try formatted string parsers
        for (DateTimeFormatter formatter : FORMATTERS) {
            try {
                return LocalDateTime.parse(dateTimeStr, formatter);
            } catch (DateTimeParseException e) {
                // Try next formatter
            }
        }

        log.warn("Failed to parse datetime: {}", dateTimeStr);
        return null;
    }
}