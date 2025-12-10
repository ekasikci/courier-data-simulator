package com.ekasikci.courierdatasimulator.kafka.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import com.ekasikci.courierdatasimulator.kafka.dto.MappedPackage;
import com.ekasikci.courierdatasimulator.kafka.entitiy.Package;
import com.ekasikci.courierdatasimulator.kafka.repository.PackageRepository;
import com.ekasikci.courierdatasimulator.kafka.service.KafkaProducerService;
import com.ekasikci.courierdatasimulator.kafka.service.PackageService;

/**
 * Full integration tests using Testcontainers for Kafka
 * FIXED: Proper test isolation and message consumption
 */
@SpringBootTest
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Kafka Integration Tests")
class KafkaIntegrationTest {

    @Container
    static KafkaContainer kafka = new KafkaContainer(
            DockerImageName.parse("apache/kafka-native:3.8.0"));

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

    private KafkaConsumer<String, MappedPackage> testConsumer;

    private String testTopic;

    @Autowired
    private Environment env;

    @BeforeEach
    void setUp() {
        // Clean database
        packageRepository.deleteAll();

        this.testTopic = env.getProperty("kafka.topic.packages");

        // Create NEW consumer for EACH test with unique group ID
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "test-group-" + UUID.randomUUID());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JacksonJsonDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(JacksonJsonDeserializer.TRUSTED_PACKAGES, "*");
        props.put(JacksonJsonDeserializer.VALUE_DEFAULT_TYPE, MappedPackage.class.getName());

        testConsumer = new KafkaConsumer<>(props);
        testConsumer.subscribe(Collections.singletonList(testTopic));

        // Drain all existing messages from previous tests
        drainExistingMessages();
    }

    /**
     * Consume and discard all existing messages in the topic
     */
    private void drainExistingMessages() {
        boolean hasMessages = true;
        while (hasMessages) {
            ConsumerRecords<String, MappedPackage> records = testConsumer.poll(Duration.ofMillis(500));
            hasMessages = !records.isEmpty();
        }
        // One more poll to ensure we're caught up
        testConsumer.poll(Duration.ofMillis(100));
    }

    @AfterEach
    void tearDown() {
        if (testConsumer != null) {
            testConsumer.close();
        }
        packageRepository.deleteAll();
    }

    @Nested
    @DisplayName("Single Package Publishing Tests")
    class SinglePackageTests {

        @Test
        @DisplayName("Should successfully publish completed package to Kafka")
        void shouldPublishCompletedPackageToKafka() throws Exception {
            // Given
            Package pkg = createAndSavePackage(
                    1001L,
                    LocalDateTime.of(2025, 11, 13, 10, 0, 0, 0),
                    LocalDateTime.of(2025, 11, 13, 10, 15, 0, 0),
                    LocalDateTime.of(2025, 11, 13, 10, 45, 0, 0),
                    "COMPLETED",
                    0,
                    60);

            // When
            Optional<MappedPackage> mappedPackage = packageService.getPackageById(pkg.getId());
            assertThat(mappedPackage).isPresent();

            kafkaProducerService.sendPackage(mappedPackage.get());

            // Then
            MappedPackage consumed = consumeOneMessage(Duration.ofSeconds(10));

            assertThat(consumed).isNotNull();
            assertThat(consumed.getId()).isEqualTo(1001L);
            assertThat(consumed.getCollectionDuration()).isEqualTo(15);
            assertThat(consumed.getDeliveryDuration()).isEqualTo(30);
            assertThat(consumed.getLeadTime()).isEqualTo(45);
            assertThat(consumed.getOrderInTime()).isTrue();
        }

        @Test
        @DisplayName("Should not publish cancelled package")
        void shouldNotPublishCancelledPackage() {
            // Given
            Package cancelledPkg = createAndSavePackage(
                    1002L,
                    LocalDateTime.now(),
                    null,
                    null,
                    "CANCELLED",
                    1,
                    60);

            // When
            Optional<MappedPackage> result = packageService.getPackageById(cancelledPkg.getId());

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should publish in-progress package with null calculated fields")
        void shouldPublishInProgressPackage() throws Exception {
            // Given
            Package inProgressPkg = createAndSavePackage(
                    1003L,
                    LocalDateTime.now(),
                    LocalDateTime.now().plusMinutes(10),
                    null,
                    "IN_DELIVERY",
                    0,
                    60);

            // When
            Optional<MappedPackage> mappedPackage = packageService.getPackageById(inProgressPkg.getId());
            assertThat(mappedPackage).isPresent();

            kafkaProducerService.sendPackage(mappedPackage.get());

            // Then
            MappedPackage consumed = consumeOneMessage(Duration.ofSeconds(10));

            assertThat(consumed).isNotNull();
            assertThat(consumed.getId()).isEqualTo(1003L);
            assertThat(consumed.getCollectionDuration()).isNull();
            assertThat(consumed.getDeliveryDuration()).isNull();
            assertThat(consumed.getLeadTime()).isNull();
            assertThat(consumed.getOrderInTime()).isNull();
        }
    }

    @Nested
    @DisplayName("Batch Publishing Tests")
    class BatchPublishingTests {

        @Test
        @DisplayName("Should publish multiple packages in batch")
        void shouldPublishMultiplePackagesInBatch() throws Exception {
            // Given - create 5 packages
            List<Package> packages = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                packages.add(createAndSavePackage(
                        2000L + i,
                        LocalDateTime.now().minusHours(2),
                        LocalDateTime.now().minusHours(1),
                        LocalDateTime.now(),
                        "COMPLETED",
                        0,
                        120));
            }

            // When
            List<MappedPackage> mappedPackages = packageService.getAllNonCancelledPackages();
            kafkaProducerService.sendPackages(mappedPackages);

            // Wait for async processing
            Thread.sleep(2000);

            // Then - should receive exactly 5 messages
            List<MappedPackage> consumed = consumeMultipleMessages(5, Duration.ofSeconds(10));

            assertThat(consumed)
                    .hasSize(5)
                    .extracting(MappedPackage::getId)
                    .containsExactlyInAnyOrder(2000L, 2001L, 2002L, 2003L, 2004L);
        }

        @Test
        @DisplayName("Should filter cancelled packages in batch operation")
        void shouldFilterCancelledInBatch() throws Exception {
            // Given - 3 completed, 2 cancelled
            createAndSavePackage(3000L, LocalDateTime.now(), LocalDateTime.now(),
                    LocalDateTime.now(), "COMPLETED", 0, 60);
            createAndSavePackage(3001L, LocalDateTime.now(), null, null,
                    "CANCELLED", 1, 60);
            createAndSavePackage(3002L, LocalDateTime.now(), LocalDateTime.now(),
                    LocalDateTime.now(), "COMPLETED", 0, 60);
            createAndSavePackage(3003L, LocalDateTime.now(), null, null,
                    "CANCELLED", 1, 60);
            createAndSavePackage(3004L, LocalDateTime.now(), LocalDateTime.now(),
                    LocalDateTime.now(), "COMPLETED", 0, 60);

            // When
            List<MappedPackage> mappedPackages = packageService.getAllNonCancelledPackages();
            kafkaProducerService.sendPackages(mappedPackages);

            // Wait for async processing
            Thread.sleep(2000);

            // Then - should receive only 3 messages
            List<MappedPackage> consumed = consumeMultipleMessages(3, Duration.ofSeconds(10));

            assertThat(consumed)
                    .hasSize(3)
                    .extracting(MappedPackage::getId)
                    .containsExactlyInAnyOrder(3000L, 3002L, 3004L);
        }
    }

    @Nested
    @DisplayName("Message Format Validation Tests")
    class MessageFormatTests {

        @Test
        @DisplayName("Should produce valid JSON messages")
        void shouldProduceValidJsonMessages() throws Exception {
            // Given - package with microseconds
            Package pkg = createAndSavePackage(
                    4000L,
                    LocalDateTime.of(2025, 11, 13, 10, 47, 52, 675248000),
                    LocalDateTime.of(2025, 11, 13, 10, 49, 50, 278087000),
                    LocalDateTime.of(2025, 11, 13, 11, 40, 15, 314340000),
                    "COMPLETED",
                    0,
                    277);

            // When
            Optional<MappedPackage> mappedPackage = packageService.getPackageById(pkg.getId());
            assertThat(mappedPackage).isPresent();

            kafkaProducerService.sendPackage(mappedPackage.get());

            // Then
            MappedPackage consumed = consumeOneMessage(Duration.ofSeconds(10));

            assertThat(consumed).isNotNull();
            assertThat(consumed.getId()).isNotNull();
            assertThat(consumed.getCreatedAt()).isNotNull();
            assertThat(consumed.getLastUpdatedAt()).isNotNull();
            assertThat(consumed.getEta()).isNotNull();
            assertThat(consumed.getCollectionDuration()).isNotNull();
            assertThat(consumed.getDeliveryDuration()).isNotNull();
            assertThat(consumed.getLeadTime()).isNotNull();
            assertThat(consumed.getOrderInTime()).isNotNull();

            // Validate date format (with microseconds)
            assertThat(consumed.getCreatedAt())
                    .matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}(\\.\\d{6})?");
        }

        @Test
        @DisplayName("Should use package ID as message key")
        void shouldUsePackageIdAsKey() throws Exception {
            // Given
            Package pkg = createAndSavePackage(
                    5000L,
                    LocalDateTime.now(),
                    LocalDateTime.now(),
                    LocalDateTime.now(),
                    "COMPLETED",
                    0,
                    60);

            // When
            Optional<MappedPackage> mappedPackage = packageService.getPackageById(pkg.getId());
            assertThat(mappedPackage).isPresent();

            kafkaProducerService.sendPackage(mappedPackage.get());

            // Then
            ConsumerRecords<String, MappedPackage> records = testConsumer.poll(Duration.ofSeconds(10));

            assertThat(records.isEmpty()).isFalse();

            ConsumerRecord<String, MappedPackage> record = records.iterator().next();
            assertThat(record.key()).isEqualTo("5000");
        }
    }

    @Nested
    @DisplayName("Performance and Reliability Tests")
    class PerformanceTests {

        @Test
        @DisplayName("Should handle high volume of messages")
        @Timeout(value = 30, unit = TimeUnit.SECONDS)
        void shouldHandleHighVolume() throws Exception {
            // Given - 100 packages
            int packageCount = 100;
            for (int i = 0; i < packageCount; i++) {
                createAndSavePackage(
                        6000L + i,
                        LocalDateTime.now(),
                        LocalDateTime.now().plusMinutes(10),
                        LocalDateTime.now().plusMinutes(30),
                        "COMPLETED",
                        0,
                        60);
            }

            // When
            long startTime = System.currentTimeMillis();
            List<MappedPackage> mappedPackages = packageService.getAllNonCancelledPackages();
            kafkaProducerService.sendPackages(mappedPackages);
            long endTime = System.currentTimeMillis();

            // Then
            assertThat(mappedPackages).hasSize(packageCount);

            long processingTime = endTime - startTime;
            assertThat(processingTime).isLessThan(5000);
        }

        @Test
        @DisplayName("Should handle concurrent publishing")
        void shouldHandleConcurrentPublishing() throws Exception {
            // Given
            int threadCount = 10;
            CountDownLatch latch = new CountDownLatch(threadCount);
            List<Long> publishedIds = Collections.synchronizedList(new ArrayList<>());

            // Create packages
            for (int i = 0; i < threadCount; i++) {
                createAndSavePackage(
                        7000L + i,
                        LocalDateTime.now(),
                        LocalDateTime.now().plusMinutes(10),
                        LocalDateTime.now().plusMinutes(30),
                        "COMPLETED",
                        0,
                        60);
            }

            // When - publish concurrently
            for (int i = 0; i < threadCount; i++) {
                final long packageId = 7000L + i;
                new Thread(() -> {
                    try {
                        Optional<MappedPackage> mapped = packageService.getPackageById(packageId);
                        if (mapped.isPresent()) {
                            kafkaProducerService.sendPackage(mapped.get());
                            publishedIds.add(packageId);
                        }
                    } finally {
                        latch.countDown();
                    }
                }).start();
            }

            // Wait for all threads
            assertThat(latch.await(10, TimeUnit.SECONDS)).isTrue();

            // Then
            assertThat(publishedIds).hasSize(threadCount);

            // Wait for async processing
            Thread.sleep(2000);

            // Verify all messages received
            List<MappedPackage> consumed = consumeMultipleMessages(
                    threadCount,
                    Duration.ofSeconds(10));
            assertThat(consumed).hasSize(threadCount);
        }
    }

    // Helper methods

    private Package createAndSavePackage(
            Long id,
            LocalDateTime createdAt,
            LocalDateTime pickedUpAt,
            LocalDateTime completedAt,
            String status,
            Integer cancelled,
            Integer eta) {
        Package pkg = new Package();
        pkg.setId(id);
        pkg.setCreatedAt(createdAt);
        pkg.setPickedUpAt(pickedUpAt);
        pkg.setCompletedAt(completedAt);
        pkg.setLastUpdatedAt(LocalDateTime.now());
        pkg.setStatus(status);
        pkg.setCancelled(cancelled);
        pkg.setEta(eta);

        return packageRepository.save(pkg);
    }

    private MappedPackage consumeOneMessage(Duration timeout) {
        ConsumerRecords<String, MappedPackage> records = testConsumer.poll(timeout);
        if (records.isEmpty()) {
            return null;
        }
        return records.iterator().next().value();
    }

    private List<MappedPackage> consumeMultipleMessages(int expectedCount, Duration timeout) {
        List<MappedPackage> messages = new ArrayList<>();
        long endTime = System.currentTimeMillis() + timeout.toMillis();

        while (messages.size() < expectedCount && System.currentTimeMillis() < endTime) {
            ConsumerRecords<String, MappedPackage> records = testConsumer.poll(Duration.ofMillis(500));

            records.forEach(record -> messages.add(record.value()));

            if (messages.size() >= expectedCount) {
                break;
            }
        }

        return messages;
    }
}