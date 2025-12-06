package com.ekasikci.courierdatasimulator.kafka.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ekasikci.courierdatasimulator.kafka.dto.MappedPackage;
import com.ekasikci.courierdatasimulator.kafka.service.KafkaProducerService;
import com.ekasikci.courierdatasimulator.kafka.service.PackageService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/kafka")
@RequiredArgsConstructor
@Slf4j
public class KafkaController {

    private final PackageService packageService;
    private final KafkaProducerService kafkaProducerService;

    /**
     * Endpoint: GET /kafka/send/{packageId}
     * Sends a single package to Kafka topic
     */
    @GetMapping("/send/{packageId}")
    public ResponseEntity<Map<String, Object>> sendSinglePackage(@PathVariable Long packageId) {
        log.info("Received request to send package with id: {}", packageId);

        Map<String, Object> response = new HashMap<>();

        try {
            Optional<MappedPackage> mappedPackageOpt = packageService.getPackageById(packageId);

            if (mappedPackageOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "Package not found or is cancelled");
                response.put("packageId", packageId);
                log.warn("Package {} not found or is cancelled", packageId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            MappedPackage mappedPackage = mappedPackageOpt.get();
            kafkaProducerService.sendPackage(mappedPackage);

            response.put("success", true);
            response.put("message", "Package sent to Kafka successfully");
            response.put("packageId", packageId);
            response.put("data", mappedPackage);

            log.info("Successfully processed package {}", packageId);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error processing package {}", packageId, e);
            response.put("success", false);
            response.put("message", "Error sending package to Kafka: " + e.getMessage());
            response.put("packageId", packageId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Endpoint: GET /kafka/bootstrap
     * Sends all non-cancelled packages to Kafka topic
     */
    @GetMapping("/bootstrap")
    public ResponseEntity<Map<String, Object>> bootstrapAllPackages() {
        log.info("Received request to bootstrap all packages");

        Map<String, Object> response = new HashMap<>();

        try {
            List<MappedPackage> mappedPackages = packageService.getAllNonCancelledPackages();

            if (mappedPackages.isEmpty()) {
                response.put("success", true);
                response.put("message", "No packages to send");
                response.put("count", 0);
                log.info("No packages found to bootstrap");
                return ResponseEntity.ok(response);
            }

            kafkaProducerService.sendPackages(mappedPackages);

            response.put("success", true);
            response.put("message", "All packages sent to Kafka successfully");
            response.put("count", mappedPackages.size());
            response.put("packageIds", mappedPackages.stream()
                    .map(MappedPackage::getId)
                    .toList());

            log.info("Successfully bootstrapped {} packages", mappedPackages.size());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error bootstrapping packages", e);
            response.put("success", false);
            response.put("message", "Error sending packages to Kafka: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}