package com.ekasikci.courierdatasimulator.kafka.transformer;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Component;

import com.ekasikci.courierdatasimulator.kafka.dto.MappedPackage;
import com.ekasikci.courierdatasimulator.kafka.dto.PackageCDC;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class PackageTransformer {

    private static final DateTimeFormatter FORMAT_MICROS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");

    private static final DateTimeFormatter FORMAT_MILLIS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    private static final DateTimeFormatter FORMAT_SECONDS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public MappedPackage transform(PackageCDC packageCDC) {
        if (packageCDC == null)
            return null;

        String createdAtFormatted = fromEpochMicros(toEpochMicros(packageCDC.getCreatedAt()));
        String lastUpdatedAtFormatted = fromEpochMicros(toEpochMicros(packageCDC.getLastUpdatedAt()));

        MappedPackage.MappedPackageBuilder builder = MappedPackage.builder()
                .id(packageCDC.getId())
                .createdAt(createdAtFormatted)
                .lastUpdatedAt(lastUpdatedAtFormatted)
                .eta(packageCDC.getEta());

        builder.collectionDuration(
                calculateDuration(packageCDC.getCreatedAt(), packageCDC.getPickedUpAt()));
        builder.deliveryDuration(
                calculateDuration(packageCDC.getPickedUpAt(), packageCDC.getCompletedAt()));
        builder.leadTime(
                calculateDuration(packageCDC.getCreatedAt(), packageCDC.getCompletedAt()));

        Integer leadTime = builder.build().getLeadTime();
        if (leadTime != null && packageCDC.getEta() != null) {
            builder.orderInTime(leadTime <= packageCDC.getEta());
        }

        return builder.build();
    }

    /** Converts ANY input string to epoch microseconds. */
    private Long toEpochMicros(String input) {
        if (input == null || input.isEmpty())
            return null;

        if (input.matches("\\d+")) {
            long value = Long.parseLong(input);
            if (value > 9_000_000_000_000L) { // bigger than millis range -> micros
                return value;
            } else {
                return value * 1000; // millis → micros
            }
        }

        try {
            LocalDateTime ldt = parseFlexibleDate(input);
            return ldt.toInstant(ZoneOffset.UTC).getEpochSecond() * 1_000_000
                    + ldt.getNano() / 1000;
        } catch (Exception e) {
            log.warn("Failed to parse datetime: {}", input);
            return null;
        }
    }

    /** Converts epoch microseconds to string format automatically. */
    private String fromEpochMicros(Long micros) {
        if (micros == null)
            return null;

        long seconds = micros / 1_000_000;
        long microsPart = micros % 1_000_000;

        Instant instant = Instant.ofEpochSecond(seconds, microsPart * 1000);

        LocalDateTime dt = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());

        if (microsPart == 0) {
            return dt.format(FORMAT_SECONDS);
        }

        return dt.format(FORMAT_MICROS);
    }

    /** Tries parsing seconds, millis, and micros formats. */
    private LocalDateTime parseFlexibleDate(String input) {
        try {
            return LocalDateTime.parse(input, FORMAT_MICROS);
        } catch (Exception ignored) {
        }

        try {
            return LocalDateTime.parse(input, FORMAT_MILLIS);
        } catch (Exception ignored) {
        }

        try {
            return LocalDateTime.parse(input, FORMAT_SECONDS);
        } catch (Exception ignored) {
        }

        throw new RuntimeException("Unparseable datetime: " + input);
    }

    private Integer calculateDuration(String start, String end) {
        Long startMicros = toEpochMicros(start);
        Long endMicros = toEpochMicros(end);

        if (startMicros == null || endMicros == null)
            return null;

        long diffMicros = endMicros - startMicros;
        return (int) (diffMicros / 1_000_000L / 60L); // minutes
    }
}
