package com.ekasikci.courierdatasimulator.kafka.performance;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;

import com.ekasikci.courierdatasimulator.kafka.dto.MappedPackage;
import com.ekasikci.courierdatasimulator.kafka.entitiy.Package;
import com.ekasikci.courierdatasimulator.kafka.repository.PackageRepository;
import com.ekasikci.courierdatasimulator.kafka.service.KafkaProducerService;
import com.ekasikci.courierdatasimulator.kafka.service.PackageService;

/**
 * Comprehensive performance benchmarking suite
 * Measures throughput, latency, resource usage, and generates detailed reports
 */
@SpringBootTest
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Performance Benchmark Tests")
class PerformanceBenchmarkTest {

    @Container
    static KafkaContainer kafka = new KafkaContainer("apache/kafka-native:3.8.0");

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("scheduler.sync.enabled", () -> "false");
    }

    @Autowired
    private PackageRepository packageRepository;

    @Autowired
    private PackageService packageService;

    @Autowired
    private KafkaProducerService kafkaProducerService;

    private static final List<BenchmarkResult> benchmarkResults = new ArrayList<>();
    private static final Runtime runtime = Runtime.getRuntime();

    @BeforeEach
    void setUp() {
        packageRepository.deleteAll();
        System.gc(); // Suggest garbage collection before tests
        try {
            Thread.sleep(1000); // Wait for GC
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @AfterAll
    static void generateReport() throws IOException {
        generatePerformanceReport();
        printConsoleSummary();
    }

    @Nested
    @DisplayName("Throughput Benchmarks")
    class ThroughputBenchmarks {

        @Test
        @Order(1)
        @DisplayName("Benchmark: Single package processing")
        void benchmarkSinglePackageProcessing() {
            String testName = "Single Package Processing";
            int iterations = 1000;

            List<Long> processingTimes = new ArrayList<>();
            List<Long> memoryUsages = new ArrayList<>();

            for (int i = 0; i < iterations; i++) {
                // Setup
                Package pkg = createPackage((long) i);
                packageRepository.save(pkg);

                long startMemory = getUsedMemory();
                long startTime = System.nanoTime();

                // Execute
                Optional<MappedPackage> result = packageService.getPackageById((long) i);
                if (result.isPresent()) {
                    kafkaProducerService.sendPackage(result.get());
                }

                long endTime = System.nanoTime();
                long endMemory = getUsedMemory();

                // Record metrics
                processingTimes.add(endTime - startTime);
                memoryUsages.add(endMemory - startMemory);

                packageRepository.deleteById((long) i);
            }

            recordBenchmark(testName, iterations, processingTimes, memoryUsages);
        }

        @Test
        @Order(2)
        @DisplayName("Benchmark: Batch processing (10 packages)")
        void benchmarkBatchProcessing10() {
            benchmarkBatchProcessing("Batch Processing (10 packages)", 10, 100);
        }

        @Test
        @Order(3)
        @DisplayName("Benchmark: Batch processing (50 packages)")
        void benchmarkBatchProcessing50() {
            benchmarkBatchProcessing("Batch Processing (50 packages)", 50, 50);
        }

        @Test
        @Order(4)
        @DisplayName("Benchmark: Batch processing (100 packages)")
        void benchmarkBatchProcessing100() {
            benchmarkBatchProcessing("Batch Processing (100 packages)", 100, 20);
        }

        @Test
        @Order(5)
        @DisplayName("Benchmark: Batch processing (500 packages)")
        void benchmarkBatchProcessing500() {
            benchmarkBatchProcessing("Batch Processing (500 packages)", 500, 10);
        }

        private void benchmarkBatchProcessing(String testName, int batchSize, int iterations) {
            List<Long> processingTimes = new ArrayList<>();
            List<Long> memoryUsages = new ArrayList<>();

            for (int iteration = 0; iteration < iterations; iteration++) {
                // Setup
                List<Package> packages = new ArrayList<>();
                for (int i = 0; i < batchSize; i++) {
                    packages.add(createPackage((long) (iteration * batchSize + i)));
                }
                packageRepository.saveAll(packages);

                long startMemory = getUsedMemory();
                long startTime = System.nanoTime();

                // Execute
                List<MappedPackage> mappedPackages = packageService.getAllNonCancelledPackages();
                kafkaProducerService.sendPackages(mappedPackages);

                long endTime = System.nanoTime();
                long endMemory = getUsedMemory();

                // Record metrics
                processingTimes.add(endTime - startTime);
                memoryUsages.add(endMemory - startMemory);

                packageRepository.deleteAll();
            }

            recordBenchmark(testName, iterations * batchSize, processingTimes, memoryUsages);
        }
    }

    @Nested
    @DisplayName("Latency Benchmarks")
    class LatencyBenchmarks {

        @Test
        @Order(10)
        @DisplayName("Benchmark: Database query latency")
        void benchmarkDatabaseQueryLatency() {
            String testName = "Database Query Latency";
            int iterations = 1000;

            // Setup - create 100 packages
            for (int i = 0; i < 100; i++) {
                packageRepository.save(createPackage((long) i));
            }

            List<Long> queryTimes = new ArrayList<>();

            for (int i = 0; i < iterations; i++) {
                long startTime = System.nanoTime();
                packageRepository.findById((long) (i % 100));
                long endTime = System.nanoTime();

                queryTimes.add(endTime - startTime);
            }

            recordLatencyBenchmark(testName, queryTimes);
        }

        @Test
        @Order(11)
        @DisplayName("Benchmark: Transformation latency")
        void benchmarkTransformationLatency() {
            String testName = "Transformation Latency";
            int iterations = 1000;

            Package pkg = createPackage(1L);
            packageRepository.save(pkg);

            List<Long> transformTimes = new ArrayList<>();

            for (int i = 0; i < iterations; i++) {
                long startTime = System.nanoTime();
                packageService.getPackageById(1L);
                long endTime = System.nanoTime();

                transformTimes.add(endTime - startTime);
            }

            recordLatencyBenchmark(testName, transformTimes);
        }

        @Test
        @Order(12)
        @DisplayName("Benchmark: Kafka publishing latency")
        void benchmarkKafkaPublishingLatency() {
            String testName = "Kafka Publishing Latency";
            int iterations = 500;

            Package pkg = createPackage(1L);
            packageRepository.save(pkg);
            Optional<MappedPackage> mapped = packageService.getPackageById(1L);
            assertThat(mapped).isPresent();

            List<Long> publishTimes = new ArrayList<>();

            for (int i = 0; i < iterations; i++) {
                long startTime = System.nanoTime();
                kafkaProducerService.sendPackage(mapped.get());
                long endTime = System.nanoTime();

                publishTimes.add(endTime - startTime);
            }

            recordLatencyBenchmark(testName, publishTimes);
        }

        @Test
        @Order(13)
        @DisplayName("Benchmark: End-to-end latency")
        void benchmarkEndToEndLatency() {
            String testName = "End-to-End Latency (DB -> Kafka)";
            int iterations = 500;

            List<Long> e2eTimes = new ArrayList<>();

            for (int i = 0; i < iterations; i++) {
                Package pkg = createPackage((long) i);
                packageRepository.save(pkg);

                long startTime = System.nanoTime();

                Optional<MappedPackage> mapped = packageService.getPackageById((long) i);
                if (mapped.isPresent()) {
                    kafkaProducerService.sendPackage(mapped.get());
                }

                long endTime = System.nanoTime();

                e2eTimes.add(endTime - startTime);
                packageRepository.deleteById((long) i);
            }

            recordLatencyBenchmark(testName, e2eTimes);
        }
    }

    @Nested
    @DisplayName("Concurrency Benchmarks")
    class ConcurrencyBenchmarks {

        @Test
        @Order(20)
        @DisplayName("Benchmark: Concurrent processing (10 threads)")
        void benchmarkConcurrent10Threads() throws Exception {
            benchmarkConcurrentProcessing("Concurrent Processing (10 threads)", 10, 100);
        }

        @Test
        @Order(21)
        @DisplayName("Benchmark: Concurrent processing (50 threads)")
        void benchmarkConcurrent50Threads() throws Exception {
            benchmarkConcurrentProcessing("Concurrent Processing (50 threads)", 50, 100);
        }

        private void benchmarkConcurrentProcessing(String testName, int threadCount, int packagesPerThread)
                throws Exception {

            // Setup
            int totalPackages = threadCount * packagesPerThread;
            for (int i = 0; i < totalPackages; i++) {
                packageRepository.save(createPackage((long) i));
            }

            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            CountDownLatch latch = new CountDownLatch(threadCount);

            List<Long> threadExecutionTimes = Collections.synchronizedList(new ArrayList<>());
            long startMemory = getUsedMemory();
            long startTime = System.nanoTime();

            // Execute concurrently
            for (int t = 0; t < threadCount; t++) {
                final int threadId = t;
                executor.submit(() -> {
                    try {
                        long threadStart = System.nanoTime();

                        for (int i = 0; i < packagesPerThread; i++) {
                            long packageId = (long) (threadId * packagesPerThread + i);
                            Optional<MappedPackage> mapped = packageService.getPackageById(packageId);
                            mapped.ifPresent(kafkaProducerService::sendPackage);
                        }

                        long threadEnd = System.nanoTime();
                        threadExecutionTimes.add(threadEnd - threadStart);
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await();
            executor.shutdown();

            long endTime = System.nanoTime();
            long endMemory = getUsedMemory();

            // Calculate metrics
            long totalTime = endTime - startTime;
            double throughput = (totalPackages * 1_000_000_000.0) / totalTime; // packages per second

            BenchmarkResult result = new BenchmarkResult();
            result.testName = testName;
            result.totalPackages = totalPackages;
            result.totalTimeNanos = totalTime;
            result.throughput = throughput;
            result.memoryUsed = endMemory - startMemory;
            result.avgLatencyNanos = threadExecutionTimes.stream()
                    .mapToLong(Long::longValue)
                    .average()
                    .orElse(0.0);

            benchmarkResults.add(result);

            System.out.println("\n" + testName + ":");
            System.out.println("  Total Time: " + formatNanos(totalTime));
            System.out.println("  Throughput: " + String.format("%.2f packages/sec", throughput));
            System.out.println("  Avg Thread Time: " + formatNanos((long) result.avgLatencyNanos));
        }
    }

    @Nested
    @DisplayName("Memory Benchmarks")
    class MemoryBenchmarks {

        @Test
        @Order(30)
        @DisplayName("Benchmark: Memory usage patterns")
        void benchmarkMemoryUsagePatterns() {
            String testName = "Memory Usage Patterns";

            int[] batchSizes = { 10, 50, 100, 500, 1000 };
            List<MemoryProfile> profiles = new ArrayList<>();

            for (int batchSize : batchSizes) {
                System.gc();
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                }

                long beforeMemory = getUsedMemory();

                // Create and process batch
                List<Package> packages = new ArrayList<>();
                for (int i = 0; i < batchSize; i++) {
                    packages.add(createPackage((long) i));
                }
                packageRepository.saveAll(packages);

                List<MappedPackage> mapped = packageService.getAllNonCancelledPackages();
                kafkaProducerService.sendPackages(mapped);

                long afterMemory = getUsedMemory();

                MemoryProfile profile = new MemoryProfile();
                profile.batchSize = batchSize;
                profile.memoryUsed = afterMemory - beforeMemory;
                profile.memoryPerPackage = profile.memoryUsed / (double) batchSize;
                profiles.add(profile);

                packageRepository.deleteAll();
            }

            // Record results
            BenchmarkResult result = new BenchmarkResult();
            result.testName = testName;
            result.memoryProfiles = profiles;
            benchmarkResults.add(result);

            System.out.println("\n" + testName + ":");
            for (MemoryProfile profile : profiles) {
                System.out.println(String.format("  Batch %d: %s (%.2f KB/package)",
                        profile.batchSize,
                        formatBytes(profile.memoryUsed),
                        profile.memoryPerPackage / 1024.0));
            }
        }
    }

    // Helper Methods

    private Package createPackage(Long id) {
        Package pkg = new Package();
        pkg.setId(id);
        pkg.setCreatedAt(LocalDateTime.now().minusHours(2));
        pkg.setPickedUpAt(LocalDateTime.now().minusHours(1));
        pkg.setCompletedAt(LocalDateTime.now());
        pkg.setLastUpdatedAt(LocalDateTime.now());
        pkg.setStatus("COMPLETED");
        pkg.setCancelled(0);
        pkg.setEta(120);
        return pkg;
    }

    private long getUsedMemory() {
        return runtime.totalMemory() - runtime.freeMemory();
    }

    private void recordBenchmark(String testName, int totalPackages,
            List<Long> processingTimes, List<Long> memoryUsages) {

        BenchmarkResult result = new BenchmarkResult();
        result.testName = testName;
        result.totalPackages = totalPackages;

        result.totalTimeNanos = processingTimes.stream().mapToLong(Long::longValue).sum();
        result.avgLatencyNanos = processingTimes.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0.0);
        result.minLatencyNanos = processingTimes.stream()
                .mapToLong(Long::longValue)
                .min()
                .orElse(0L);
        result.maxLatencyNanos = processingTimes.stream()
                .mapToLong(Long::longValue)
                .max()
                .orElse(0L);
        result.p50LatencyNanos = calculatePercentile(processingTimes, 50);
        result.p95LatencyNanos = calculatePercentile(processingTimes, 95);
        result.p99LatencyNanos = calculatePercentile(processingTimes, 99);

        result.throughput = (totalPackages * 1_000_000_000.0) / result.totalTimeNanos;

        result.memoryUsed = memoryUsages.stream().mapToLong(Long::longValue).average().orElse(0.0);

        benchmarkResults.add(result);

        printBenchmarkSummary(result);
    }

    private void recordLatencyBenchmark(String testName, List<Long> latencies) {
        BenchmarkResult result = new BenchmarkResult();
        result.testName = testName;
        result.totalPackages = latencies.size();

        result.avgLatencyNanos = latencies.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0.0);
        result.minLatencyNanos = latencies.stream()
                .mapToLong(Long::longValue)
                .min()
                .orElse(0L);
        result.maxLatencyNanos = latencies.stream()
                .mapToLong(Long::longValue)
                .max()
                .orElse(0L);
        result.p50LatencyNanos = calculatePercentile(latencies, 50);
        result.p95LatencyNanos = calculatePercentile(latencies, 95);
        result.p99LatencyNanos = calculatePercentile(latencies, 99);

        benchmarkResults.add(result);

        printLatencySummary(result);
    }

    private long calculatePercentile(List<Long> values, int percentile) {
        List<Long> sorted = values.stream().sorted().collect(Collectors.toList());
        int index = (int) Math.ceil((percentile / 100.0) * sorted.size()) - 1;
        return sorted.get(Math.max(0, Math.min(index, sorted.size() - 1)));
    }

    private void printBenchmarkSummary(BenchmarkResult result) {
        System.out.println("\n" + result.testName + ":");
        System.out.println("  Total Packages: " + result.totalPackages);
        System.out.println("  Total Time: " + formatNanos(result.totalTimeNanos));
        System.out.println("  Throughput: " + String.format("%.2f packages/sec", result.throughput));
        System.out.println("  Avg Latency: " + formatNanos((long) result.avgLatencyNanos));
        System.out.println("  P50 Latency: " + formatNanos(result.p50LatencyNanos));
        System.out.println("  P95 Latency: " + formatNanos(result.p95LatencyNanos));
        System.out.println("  P99 Latency: " + formatNanos(result.p99LatencyNanos));
        System.out.println("  Memory: " + formatBytes((long) result.memoryUsed));
    }

    private void printLatencySummary(BenchmarkResult result) {
        System.out.println("\n" + result.testName + ":");
        System.out.println("  Operations: " + result.totalPackages);
        System.out.println("  Avg Latency: " + formatNanos((long) result.avgLatencyNanos));
        System.out.println("  Min Latency: " + formatNanos(result.minLatencyNanos));
        System.out.println("  Max Latency: " + formatNanos(result.maxLatencyNanos));
        System.out.println("  P50 Latency: " + formatNanos(result.p50LatencyNanos));
        System.out.println("  P95 Latency: " + formatNanos(result.p95LatencyNanos));
        System.out.println("  P99 Latency: " + formatNanos(result.p99LatencyNanos));
    }

    private static void printConsoleSummary() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("PERFORMANCE BENCHMARK SUMMARY");
        System.out.println("=".repeat(80));

        for (BenchmarkResult result : benchmarkResults) {
            System.out.println("\n" + result.testName);
            if (result.throughput > 0) {
                System.out.println("  Throughput: " + String.format("%.2f packages/sec", result.throughput));
            }
            if (result.avgLatencyNanos > 0) {
                System.out.println("  Avg Latency: " + formatNanos((long) result.avgLatencyNanos));
            }
        }
    }

    private static void generatePerformanceReport() throws IOException {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String filename = "performance_report_" + timestamp + ".md";

        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            generateMarkdownReport(writer);
        }

        System.out.println("\nPerformance report generated: " + filename);
    }

    private static void generateMarkdownReport(PrintWriter writer) {
        writer.println("# Kafka Package System - Performance Benchmark Report");
        writer.println("\nGenerated: " + LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        writer.println("\n## Test Environment");
        writer.println("- Java Version: " + System.getProperty("java.version"));
        writer.println("- OS: " + System.getProperty("os.name"));
        writer.println("- CPU Cores: " + Runtime.getRuntime().availableProcessors());
        writer.println("- Max Memory: " + formatBytes(Runtime.getRuntime().maxMemory()));

        writer.println("\n## Benchmark Results\n");

        for (BenchmarkResult result : benchmarkResults) {
            writer.println("### " + result.testName + "\n");

            if (result.totalPackages > 0) {
                writer.println("- **Total Packages**: " + result.totalPackages);
            }
            if (result.totalTimeNanos > 0) {
                writer.println("- **Total Time**: " + formatNanos(result.totalTimeNanos));
            }
            if (result.throughput > 0) {
                writer.println("- **Throughput**: " + String.format("%.2f packages/sec", result.throughput));
            }
            if (result.avgLatencyNanos > 0) {
                writer.println("- **Average Latency**: " + formatNanos((long) result.avgLatencyNanos));
                writer.println("- **Min Latency**: " + formatNanos(result.minLatencyNanos));
                writer.println("- **Max Latency**: " + formatNanos(result.maxLatencyNanos));
                writer.println("- **P50 Latency**: " + formatNanos(result.p50LatencyNanos));
                writer.println("- **P95 Latency**: " + formatNanos(result.p95LatencyNanos));
                writer.println("- **P99 Latency**: " + formatNanos(result.p99LatencyNanos));
            }
            if (result.memoryUsed > 0) {
                writer.println("- **Memory Used**: " + formatBytes((long) result.memoryUsed));
            }
            if (result.memoryProfiles != null && !result.memoryProfiles.isEmpty()) {
                writer.println("\n**Memory Profile by Batch Size**:");
                writer.println("| Batch Size | Memory Used | Per Package |");
                writer.println("|------------|-------------|-------------|");
                for (MemoryProfile profile : result.memoryProfiles) {
                    writer.println(String.format("| %d | %s | %.2f KB |",
                            profile.batchSize,
                            formatBytes(profile.memoryUsed),
                            profile.memoryPerPackage / 1024.0));
                }
            }

            writer.println();
        }
    }

    private static String formatNanos(long nanos) {
        if (nanos < 1_000) {
            return nanos + " ns";
        } else if (nanos < 1_000_000) {
            return String.format("%.2f μs", nanos / 1_000.0);
        } else if (nanos < 1_000_000_000) {
            return String.format("%.2f ms", nanos / 1_000_000.0);
        } else {
            return String.format("%.2f s", nanos / 1_000_000_000.0);
        }
    }

    private static String formatBytes(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.2f KB", bytes / 1024.0);
        } else {
            return String.format("%.2f MB", bytes / (1024.0 * 1024.0));
        }
    }

    // Data classes

    static class BenchmarkResult {
        String testName;
        int totalPackages;
        long totalTimeNanos;
        double avgLatencyNanos;
        long minLatencyNanos;
        long maxLatencyNanos;
        long p50LatencyNanos;
        long p95LatencyNanos;
        long p99LatencyNanos;
        double throughput;
        double memoryUsed;
        List<MemoryProfile> memoryProfiles;
    }

    static class MemoryProfile {
        int batchSize;
        long memoryUsed;
        double memoryPerPackage;
    }
}