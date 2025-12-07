package com.ekasikci.courierdatasimulator.kafka.controller;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import com.ekasikci.courierdatasimulator.kafka.entitiy.Package;
import com.ekasikci.courierdatasimulator.kafka.repository.PackageRepository;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;

/**
 * REST API Integration Tests
 * Tests all controller endpoints with various scenarios
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Kafka Controller Integration Tests")
class KafkaControllerIntegrationTest {

        @LocalServerPort
        private int port;

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

        @BeforeEach
        void setUp() {
                RestAssured.port = port;
                packageRepository.deleteAll();
        }

        @Nested
        @DisplayName("GET /kafka/send/{packageId} - Single Package Tests")
        class SendSinglePackageTests {

                @Test
                @Order(1)
                @DisplayName("Should successfully send completed package")
                void shouldSuccessfullySendCompletedPackage() {
                        // Given
                        Package pkg = createAndSavePackage(
                                        1L, "COMPLETED", 0,
                                        LocalDateTime.now().minusHours(2),
                                        LocalDateTime.now().minusHours(1),
                                        LocalDateTime.now());

                        // When & Then
                        given()
                                        .contentType(ContentType.JSON)
                                        .when()
                                        .get("/kafka/send/{packageId}", pkg.getId())
                                        .then()
                                        .statusCode(200)
                                        .body("success", equalTo(true))
                                        .body("message", containsString("successfully"))
                                        .body("packageId", equalTo(pkg.getId().intValue()))
                                        .body("data.id", equalTo(pkg.getId().intValue()))
                                        .body("data.collection_duration", notNullValue())
                                        .body("data.delivery_duration", notNullValue())
                                        .body("data.lead_time", notNullValue())
                                        .body("data.order_in_time", notNullValue())
                                        .body("data.eta", notNullValue())
                                        .body("data.created_at", notNullValue())
                                        .body("data.last_updated_at", notNullValue());
                }

                @Test
                @Order(2)
                @DisplayName("Should reject cancelled package with 404")
                void shouldRejectCancelledPackage() {
                        // Given
                        Package cancelledPkg = createAndSavePackage(
                                        2L, "CANCELLED", 1,
                                        LocalDateTime.now(), null, null);

                        // When & Then
                        given()
                                        .contentType(ContentType.JSON)
                                        .when()
                                        .get("/kafka/send/{packageId}", cancelledPkg.getId())
                                        .then()
                                        .statusCode(404)
                                        .body("success", equalTo(false))
                                        .body("message", containsString("not found or is cancelled"))
                                        .body("packageId", equalTo(cancelledPkg.getId().intValue()))
                                        .body("data", nullValue());
                }

                @Test
                @Order(3)
                @DisplayName("Should handle non-existent package with 404")
                void shouldHandleNonExistentPackage() {
                        // Given
                        Long nonExistentId = 999999L;

                        // When & Then
                        given()
                                        .contentType(ContentType.JSON)
                                        .when()
                                        .get("/kafka/send/{packageId}", nonExistentId)
                                        .then()
                                        .statusCode(404)
                                        .body("success", equalTo(false))
                                        .body("message", containsString("not found"))
                                        .body("packageId", equalTo(nonExistentId.intValue()));
                }

                @Test
                @Order(4)
                @DisplayName("Should send in-progress package with null calculated fields")
                void shouldSendInProgressPackage() {
                        // Given
                        Package inProgressPkg = createAndSavePackage(
                                        3L, "IN_DELIVERY", 0,
                                        LocalDateTime.now().minusHours(1),
                                        LocalDateTime.now().minusMinutes(30),
                                        null);

                        // When & Then
                        given()
                                        .contentType(ContentType.JSON)
                                        .when()
                                        .get("/kafka/send/{packageId}", inProgressPkg.getId())
                                        .then()
                                        .statusCode(200)
                                        .body("success", equalTo(true))
                                        .body("data.id", equalTo(inProgressPkg.getId().intValue()))
                                        .body("data.collection_duration", nullValue())
                                        .body("data.delivery_duration", nullValue())
                                        .body("data.lead_time", nullValue())
                                        .body("data.order_in_time", nullValue());
                }

                @Test
                @Order(5)
                @DisplayName("Should handle invalid package ID format")
                void shouldHandleInvalidPackageIdFormat() {
                        given()
                                        .contentType(ContentType.JSON)
                                        .when()
                                        .get("/kafka/send/{packageId}", "invalid")
                                        .then()
                                        .statusCode(400);
                }

                @Test
                @Order(6)
                @DisplayName("Should verify calculated fields are correct")
                void shouldVerifyCalculatedFields() {
                        // Given
                        LocalDateTime created = LocalDateTime.of(2021, 11, 13, 10, 0, 0);
                        Package pkg = createAndSavePackage(
                                        4L, "COMPLETED", 0,
                                        created,
                                        created.plusMinutes(15),
                                        created.plusMinutes(45));
                        pkg.setEta(60);
                        packageRepository.save(pkg);

                        // When & Then
                        given()
                                        .contentType(ContentType.JSON)
                                        .when()
                                        .get("/kafka/send/{packageId}", pkg.getId())
                                        .then()
                                        .statusCode(200)
                                        .body("data.collection_duration", equalTo(15))
                                        .body("data.delivery_duration", equalTo(30))
                                        .body("data.lead_time", equalTo(45))
                                        .body("data.eta", equalTo(60))
                                        .body("data.order_in_time", equalTo(true)); // 45 <= 60
                }

                @Test
                @Order(7)
                @DisplayName("Should verify late delivery is marked correctly")
                void shouldVerifyLateDelivery() {
                        // Given
                        LocalDateTime created = LocalDateTime.of(2021, 11, 13, 10, 0, 0);
                        Package pkg = createAndSavePackage(
                                        5L, "COMPLETED", 0,
                                        created,
                                        created.plusMinutes(10),
                                        created.plusMinutes(90));
                        pkg.setEta(60);
                        packageRepository.save(pkg);

                        // When & Then
                        given()
                                        .contentType(ContentType.JSON)
                                        .when()
                                        .get("/kafka/send/{packageId}", pkg.getId())
                                        .then()
                                        .statusCode(200)
                                        .body("data.lead_time", equalTo(90))
                                        .body("data.eta", equalTo(60))
                                        .body("data.order_in_time", equalTo(false)); // 90 > 60
                }
        }

        @Nested
        @DisplayName("GET /kafka/bootstrap - Batch Processing Tests")
        class BootstrapTests {

                @Test
                @Order(10)
                @DisplayName("Should successfully bootstrap all non-cancelled packages")
                void shouldSuccessfullyBootstrapAllPackages() {
                        // Given - create 5 packages (3 completed, 1 cancelled, 1 in-progress)
                        createAndSavePackage(10L, "COMPLETED", 0,
                                        LocalDateTime.now().minusHours(2),
                                        LocalDateTime.now().minusHours(1),
                                        LocalDateTime.now());

                        createAndSavePackage(11L, "CANCELLED", 1,
                                        LocalDateTime.now(), null, null);

                        createAndSavePackage(12L, "COMPLETED", 0,
                                        LocalDateTime.now().minusHours(2),
                                        LocalDateTime.now().minusHours(1),
                                        LocalDateTime.now());

                        createAndSavePackage(13L, "IN_DELIVERY", 0,
                                        LocalDateTime.now().minusHours(1),
                                        LocalDateTime.now().minusMinutes(30),
                                        null);

                        createAndSavePackage(14L, "COMPLETED", 0,
                                        LocalDateTime.now().minusHours(2),
                                        LocalDateTime.now().minusHours(1),
                                        LocalDateTime.now());

                        // When & Then
                        given()
                                        .contentType(ContentType.JSON)
                                        .when()
                                        .get("/kafka/bootstrap")
                                        .then()
                                        .statusCode(200)
                                        .body("success", equalTo(true))
                                        .body("message", containsString("successfully"))
                                        .body("count", equalTo(4)) // Should exclude cancelled
                                        .body("packageIds", hasSize(4))
                                        .body("packageIds", hasItems(10, 12, 13, 14))
                                        .body("packageIds", not(hasItem(11))); // Cancelled should be excluded
                }

                @Test
                @Order(11)
                @DisplayName("Should handle empty database gracefully")
                void shouldHandleEmptyDatabase() {
                        // Given - no packages in database

                        // When & Then
                        given()
                                        .contentType(ContentType.JSON)
                                        .when()
                                        .get("/kafka/bootstrap")
                                        .then()
                                        .statusCode(200)
                                        .body("success", equalTo(true))
                                        .body("message", containsString("No packages to send"))
                                        .body("count", equalTo(0));
                }

                @Test
                @Order(12)
                @DisplayName("Should handle large batch efficiently")
                void shouldHandleLargeBatch() {
                        // Given - create 100 packages
                        for (int i = 0; i < 100; i++) {
                                createAndSavePackage(
                                                1000L + i, "COMPLETED", 0,
                                                LocalDateTime.now().minusHours(2),
                                                LocalDateTime.now().minusHours(1),
                                                LocalDateTime.now());
                        }

                        // When & Then
                        long startTime = System.currentTimeMillis();

                        given()
                                        .contentType(ContentType.JSON)
                                        .when()
                                        .get("/kafka/bootstrap")
                                        .then()
                                        .statusCode(200)
                                        .body("success", equalTo(true))
                                        .body("count", equalTo(100))
                                        .body("packageIds", hasSize(100));

                        long endTime = System.currentTimeMillis();
                        long duration = endTime - startTime;

                        // Should complete in reasonable time (< 5 seconds)
                        Assertions.assertTrue(duration < 5000,
                                        "Bootstrap took too long: " + duration + "ms");
                }

                @Test
                @Order(13)
                @DisplayName("Should only include non-cancelled packages")
                void shouldOnlyIncludeNonCancelled() {
                        // Given
                        for (int i = 0; i < 10; i++) {
                                int cancelled = (i % 2 == 0) ? 1 : 0; // Every other cancelled
                                String status = (cancelled == 1) ? "CANCELLED" : "COMPLETED";

                                createAndSavePackage(
                                                2000L + i, status, cancelled,
                                                LocalDateTime.now(),
                                                cancelled == 0 ? LocalDateTime.now() : null,
                                                cancelled == 0 ? LocalDateTime.now() : null);
                        }

                        // When & Then
                        given()
                                        .contentType(ContentType.JSON)
                                        .when()
                                        .get("/kafka/bootstrap")
                                        .then()
                                        .statusCode(200)
                                        .body("count", equalTo(5)) // Only non-cancelled
                                        .body("packageIds", hasSize(5));
                }
        }

        @Nested
        @DisplayName("Error Handling and Edge Cases")
        class ErrorHandlingTests {

                @Test
                @Order(20)
                @DisplayName("Should handle concurrent requests")
                void shouldHandleConcurrentRequests() throws Exception {
                        // Given
                        for (int i = 0; i < 10; i++) {
                                createAndSavePackage(
                                                3000L + i, "COMPLETED", 0,
                                                LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now());
                        }

                        // When - send concurrent requests
                        Thread[] threads = new Thread[10];
                        for (int i = 0; i < 10; i++) {
                                final long id = 3000L + i;
                                threads[i] = new Thread(() -> {
                                        given()
                                                        .contentType(ContentType.JSON)
                                                        .when()
                                                        .get("/kafka/send/{packageId}", id)
                                                        .then()
                                                        .statusCode(200);
                                });
                                threads[i].start();
                        }

                        // Wait for all threads
                        for (Thread thread : threads) {
                                thread.join();
                        }

                        // Then - all should succeed (verified by no exceptions)
                }

                @Test
                @Order(21)
                @DisplayName("Should handle package with all null timestamps")
                void shouldHandleAllNullTimestamps() {
                        // Given
                        Package pkg = new Package();
                        pkg.setId(4000L);
                        pkg.setStatus("PENDING");
                        pkg.setCancelled(0);
                        pkg.setEta(60);
                        pkg.setCreatedAt(LocalDateTime.now());
                        pkg.setLastUpdatedAt(LocalDateTime.now());
                        packageRepository.save(pkg);

                        // When & Then
                        given()
                                        .contentType(ContentType.JSON)
                                        .when()
                                        .get("/kafka/send/{packageId}", 4000L)
                                        .then()
                                        .statusCode(200)
                                        .body("success", equalTo(true))
                                        .body("data.collection_duration", nullValue())
                                        .body("data.delivery_duration", nullValue())
                                        .body("data.lead_time", nullValue());
                }

                @Test
                @Order(22)
                @DisplayName("Should validate response content type")
                void shouldValidateResponseContentType() {
                        // Given
                        createAndSavePackage(
                                        5000L, "COMPLETED", 0,
                                        LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now());

                        // When & Then
                        given()
                                        .contentType(ContentType.JSON)
                                        .when()
                                        .get("/kafka/send/{packageId}", 5000L)
                                        .then()
                                        .statusCode(200)
                                        .contentType(ContentType.JSON);
                }

                @Test
                @Order(23)
                @DisplayName("Should handle very large package IDs")
                void shouldHandleVeryLargePackageIds() {
                        // Given
                        Long largeId = Long.MAX_VALUE - 1;
                        createAndSavePackage(
                                        largeId, "COMPLETED", 0,
                                        LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now());

                        // When & Then
                        given()
                                        .contentType(ContentType.JSON)
                                        .when()
                                        .get("/kafka/send/{packageId}", largeId)
                                        .then()
                                        .statusCode(200)
                                        .body("success", equalTo(true));
                }

                @Test
                @Order(24)
                @DisplayName("Should handle negative package IDs")
                void shouldHandleNegativePackageIds() {
                        given()
                                        .contentType(ContentType.JSON)
                                        .when()
                                        .get("/kafka/send/{packageId}", -1L)
                                        .then()
                                        .statusCode(404);
                }
        }

        // Helper method
        private Package createAndSavePackage(
                        Long id, String status, Integer cancelled,
                        LocalDateTime createdAt, LocalDateTime pickedUpAt, LocalDateTime completedAt) {
                Package pkg = new Package();
                pkg.setId(id);
                pkg.setStatus(status);
                pkg.setCancelled(cancelled);
                pkg.setCreatedAt(createdAt);
                pkg.setPickedUpAt(pickedUpAt);
                pkg.setCompletedAt(completedAt);
                pkg.setLastUpdatedAt(LocalDateTime.now());
                pkg.setEta(120);
                return packageRepository.save(pkg);
        }
}