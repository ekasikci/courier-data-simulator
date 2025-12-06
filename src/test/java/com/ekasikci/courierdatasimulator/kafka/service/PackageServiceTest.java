package com.ekasikci.courierdatasimulator.kafka.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ekasikci.courierdatasimulator.kafka.dto.MappedPackage;
import com.ekasikci.courierdatasimulator.kafka.entitiy.Package;
import com.ekasikci.courierdatasimulator.kafka.repository.PackageRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("PackageService Unit Tests")
class PackageServiceTest {

    @Mock
    private PackageRepository packageRepository;

    @InjectMocks
    private PackageService packageService;

    private Package completedPackage;
    private Package cancelledPackage;
    private Package inProgressPackage;
    private Package packageWithNullTimestamps;

    @BeforeEach
    void setUp() {
        // Completed package - on time delivery
        completedPackage = createPackage(
                1L,
                LocalDateTime.of(2025, 11, 13, 10, 47, 52),
                LocalDateTime.of(2025, 11, 13, 10, 49, 50),
                LocalDateTime.of(2025, 11, 13, 11, 40, 15),
                "COMPLETED",
                0,
                277);

        // Cancelled package
        cancelledPackage = createPackage(
                2L,
                LocalDateTime.of(2025, 11, 13, 13, 0, 0),
                null,
                null,
                "CANCELLED",
                1,
                60);

        // In-progress package
        inProgressPackage = createPackage(
                3L,
                LocalDateTime.of(2025, 11, 13, 14, 0, 0),
                LocalDateTime.of(2025, 11, 13, 14, 15, 30),
                null,
                "IN_DELIVERY",
                0,
                90);

        // Package with null timestamps
        packageWithNullTimestamps = createPackage(
                4L,
                LocalDateTime.of(2025, 11, 13, 15, 0, 0),
                null,
                null,
                "WAITING_ASSIGNMENT",
                0,
                120);
    }

    @Nested
    @DisplayName("GetPackageById Tests")
    class GetPackageByIdTests {

        @Test
        @DisplayName("Should return MappedPackage for valid completed package")
        void shouldReturnMappedPackageForValidCompletedPackage() {
            // Given
            when(packageRepository.findById(1L)).thenReturn(Optional.of(completedPackage));

            // When
            Optional<MappedPackage> result = packageService.getPackageById(1L);

            // Then
            assertThat(result).isPresent();
            MappedPackage mapped = result.get();

            assertThat(mapped.getId()).isEqualTo(1L);
            assertThat(mapped.getCollectionDuration()).isEqualTo(1); // 10:49:50 - 10:47:52
            assertThat(mapped.getDeliveryDuration()).isEqualTo(50); // 11:40:15 - 10:49:50
            assertThat(mapped.getLeadTime()).isEqualTo(52); // 11:40:15 - 10:47:52
            assertThat(mapped.getEta()).isEqualTo(277);
            assertThat(mapped.getOrderInTime()).isTrue(); // 52 <= 277

            verify(packageRepository).findById(1L);
        }

        @Test
        @DisplayName("Should return empty Optional for cancelled package")
        void shouldReturnEmptyForCancelledPackage() {
            // Given
            when(packageRepository.findById(2L)).thenReturn(Optional.of(cancelledPackage));

            // When
            Optional<MappedPackage> result = packageService.getPackageById(2L);

            // Then
            assertThat(result).isEmpty();
            verify(packageRepository).findById(2L);
        }

        @Test
        @DisplayName("Should return empty Optional for non-existent package")
        void shouldReturnEmptyForNonExistentPackage() {
            // Given
            when(packageRepository.findById(999L)).thenReturn(Optional.empty());

            // When
            Optional<MappedPackage> result = packageService.getPackageById(999L);

            // Then
            assertThat(result).isEmpty();
            verify(packageRepository).findById(999L);
        }

        @Test
        @DisplayName("Should set calculated fields to null for in-progress package")
        void shouldSetNullForInProgressPackage() {
            // Given
            when(packageRepository.findById(3L)).thenReturn(Optional.of(inProgressPackage));

            // When
            Optional<MappedPackage> result = packageService.getPackageById(3L);

            // Then
            assertThat(result).isPresent();
            MappedPackage mapped = result.get();

            assertThat(mapped.getId()).isEqualTo(3L);
            assertThat(mapped.getCollectionDuration()).isNull();
            assertThat(mapped.getDeliveryDuration()).isNull();
            assertThat(mapped.getLeadTime()).isNull();
            assertThat(mapped.getOrderInTime()).isNull();
            assertThat(mapped.getEta()).isEqualTo(90);
        }

        @Test
        @DisplayName("Should handle package with null timestamps")
        void shouldHandleNullTimestamps() {
            // Given
            when(packageRepository.findById(4L)).thenReturn(Optional.of(packageWithNullTimestamps));

            // When
            Optional<MappedPackage> result = packageService.getPackageById(4L);

            // Then
            assertThat(result).isPresent();
            MappedPackage mapped = result.get();

            assertThat(mapped.getCollectionDuration()).isNull();
            assertThat(mapped.getDeliveryDuration()).isNull();
            assertThat(mapped.getLeadTime()).isNull();
        }
    }

    @Nested
    @DisplayName("GetAllNonCancelledPackages Tests")
    class GetAllNonCancelledPackagesTests {

        @Test
        @DisplayName("Should return all non-cancelled packages")
        void shouldReturnAllNonCancelledPackages() {
            // Given
            List<Package> packages = Arrays.asList(completedPackage, inProgressPackage);
            when(packageRepository.findAllNonCancelled()).thenReturn(packages);

            // When
            List<MappedPackage> result = packageService.getAllNonCancelledPackages();

            // Then
            assertThat(result).hasSize(2);
            assertThat(result).extracting(MappedPackage::getId).containsExactly(1L, 3L);
            verify(packageRepository).findAllNonCancelled();
        }

        @Test
        @DisplayName("Should return empty list when no packages exist")
        void shouldReturnEmptyListWhenNoPackages() {
            // Given
            when(packageRepository.findAllNonCancelled()).thenReturn(List.of());

            // When
            List<MappedPackage> result = packageService.getAllNonCancelledPackages();

            // Then
            assertThat(result).isEmpty();
            verify(packageRepository).findAllNonCancelled();
        }

        @Test
        @DisplayName("Should handle mix of completed and in-progress packages")
        void shouldHandleMixedPackageStatuses() {
            // Given
            List<Package> packages = Arrays.asList(
                    completedPackage,
                    inProgressPackage,
                    packageWithNullTimestamps);
            when(packageRepository.findAllNonCancelled()).thenReturn(packages);

            // When
            List<MappedPackage> result = packageService.getAllNonCancelledPackages();

            // Then
            assertThat(result).hasSize(3);

            // First should have calculated values
            assertThat(result.get(0).getLeadTime()).isNotNull();

            // Others should have nulls
            assertThat(result.get(1).getLeadTime()).isNull();
            assertThat(result.get(2).getLeadTime()).isNull();
        }
    }

    @Nested
    @DisplayName("Business Logic - Duration Calculations")
    class DurationCalculationTests {

        @ParameterizedTest(name = "Test case {index}: {0}")
        @MethodSource("durationTestCases")
        @DisplayName("Should calculate durations correctly")
        void shouldCalculateDurationsCorrectly(
                String description,
                LocalDateTime created,
                LocalDateTime pickedUp,
                LocalDateTime completed,
                Integer expectedCollection,
                Integer expectedDelivery,
                Integer expectedLead) {
            // Given
            Package pkg = createPackage(100L, created, pickedUp, completed, "COMPLETED", 0, 60);
            when(packageRepository.findById(100L)).thenReturn(Optional.of(pkg));

            // When
            Optional<MappedPackage> result = packageService.getPackageById(100L);

            // Then
            assertThat(result).isPresent();
            MappedPackage mapped = result.get();

            assertThat(mapped.getCollectionDuration())
                    .as("Collection duration for: " + description)
                    .isEqualTo(expectedCollection);

            assertThat(mapped.getDeliveryDuration())
                    .as("Delivery duration for: " + description)
                    .isEqualTo(expectedDelivery);

            assertThat(mapped.getLeadTime())
                    .as("Lead time for: " + description)
                    .isEqualTo(expectedLead);
        }

        static Stream<Arguments> durationTestCases() {
            LocalDateTime base = LocalDateTime.of(2025, 11, 13, 10, 0, 0);

            return Stream.of(
                    Arguments.of(
                            "Same hour delivery",
                            base,
                            base.plusMinutes(15),
                            base.plusMinutes(45),
                            15, 30, 45),
                    Arguments.of(
                            "Cross-hour delivery",
                            base.withHour(9).withMinute(50),
                            base.withHour(10).withMinute(5),
                            base.withHour(11).withMinute(10),
                            15, 65, 80),
                    Arguments.of(
                            "Very quick delivery (1 minute each)",
                            base,
                            base.plusMinutes(1),
                            base.plusMinutes(2),
                            1, 1, 2),
                    Arguments.of(
                            "Long delivery (hours)",
                            base,
                            base.plusHours(2),
                            base.plusHours(5),
                            120, 180, 300),
                    Arguments.of(
                            "Cross-day delivery",
                            base.withHour(23).withMinute(50),
                            base.plusDays(1).withHour(0).withMinute(10),
                            base.plusDays(1).withHour(1).withMinute(30),
                            20, 80, 100));
        }
    }

    @Nested
    @DisplayName("Business Logic - Order In Time Validation")
    class OrderInTimeTests {

        @ParameterizedTest(name = "{0}")
        @MethodSource("orderInTimeTestCases")
        @DisplayName("Should correctly determine if order is in time")
        void shouldDetermineOrderInTimeCorrectly(
                String description,
                int leadTimeMinutes,
                int etaMinutes,
                boolean expectedOrderInTime) {
            // Given
            LocalDateTime created = LocalDateTime.of(2025, 11, 13, 10, 0, 0);
            Package pkg = createPackage(
                    200L,
                    created,
                    created.plusMinutes(10),
                    created.plusMinutes(leadTimeMinutes),
                    "COMPLETED",
                    0,
                    etaMinutes);
            when(packageRepository.findById(200L)).thenReturn(Optional.of(pkg));

            // When
            Optional<MappedPackage> result = packageService.getPackageById(200L);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getOrderInTime())
                    .as(description)
                    .isEqualTo(expectedOrderInTime);
        }

        static Stream<Arguments> orderInTimeTestCases() {
            return Stream.of(
                    Arguments.of("Well within ETA", 30, 60, true),
                    Arguments.of("Exactly on ETA", 60, 60, true),
                    Arguments.of("One minute late", 61, 60, false),
                    Arguments.of("Significantly late", 120, 60, false),
                    Arguments.of("Very early delivery", 10, 60, true),
                    Arguments.of("Edge case - zero ETA", 0, 0, true),
                    Arguments.of("Large ETA margin", 100, 300, true));
        }

        @Test
        @DisplayName("Should return null for orderInTime when leadTime is null")
        void shouldReturnNullWhenLeadTimeIsNull() {
            // Given
            when(packageRepository.findById(3L)).thenReturn(Optional.of(inProgressPackage));

            // When
            Optional<MappedPackage> result = packageService.getPackageById(3L);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getOrderInTime()).isNull();
        }
    }

    @Nested
    @DisplayName("Edge Cases and Boundary Conditions")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle package with microsecond precision")
        void shouldHandleMicrosecondPrecision() {
            // Given
            LocalDateTime created = LocalDateTime.of(2025, 11, 13, 10, 47, 52, 675248000);
            Package pkg = createPackage(
                    300L,
                    created,
                    created.plusMinutes(2).plusNanos(500000000),
                    created.plusMinutes(52).plusNanos(639092000),
                    "COMPLETED",
                    0,
                    277);
            when(packageRepository.findById(300L)).thenReturn(Optional.of(pkg));

            // When
            Optional<MappedPackage> result = packageService.getPackageById(300L);

            // Then
            assertThat(result).isPresent();
            MappedPackage mapped = result.get();

            // Durations should be in minutes (ignoring microseconds for minute calculation)
            assertThat(mapped.getLeadTime()).isEqualTo(52);

            // Timestamps should preserve microseconds in formatting
            assertThat(mapped.getCreatedAt()).contains(".");
        }

        @Test
        @DisplayName("Should handle same timestamp for pickup and completion")
        void shouldHandleSameTimestamps() {
            // Given
            LocalDateTime time = LocalDateTime.of(2025, 11, 13, 10, 0, 0);
            Package pkg = createPackage(
                    400L,
                    time,
                    time,
                    time,
                    "COMPLETED",
                    0,
                    60);
            when(packageRepository.findById(400L)).thenReturn(Optional.of(pkg));

            // When
            Optional<MappedPackage> result = packageService.getPackageById(400L);

            // Then
            assertThat(result).isPresent();
            MappedPackage mapped = result.get();

            assertThat(mapped.getCollectionDuration()).isEqualTo(0);
            assertThat(mapped.getDeliveryDuration()).isEqualTo(0);
            assertThat(mapped.getLeadTime()).isEqualTo(0);
            assertThat(mapped.getOrderInTime()).isTrue();
        }

        @Test
        @DisplayName("Should handle cancelled value as 0 vs null")
        void shouldHandleCancelledValues() {
            // Given - cancelled = 0 (not cancelled)
            Package pkg1 = createPackage(500L,
                    LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(),
                    "COMPLETED", 0, 60);

            // cancelled = 1 (cancelled)
            Package pkg2 = createPackage(501L,
                    LocalDateTime.now(), null, null,
                    "CANCELLED", 1, 60);

            when(packageRepository.findById(500L)).thenReturn(Optional.of(pkg1));
            when(packageRepository.findById(501L)).thenReturn(Optional.of(pkg2));

            // When
            Optional<MappedPackage> result1 = packageService.getPackageById(500L);
            Optional<MappedPackage> result2 = packageService.getPackageById(501L);

            // Then
            assertThat(result1).isPresent(); // cancelled = 0 should be returned
            assertThat(result2).isEmpty(); // cancelled = 1 should be filtered
        }

        @Test
        @DisplayName("Should handle null ETA")
        void shouldHandleNullEta() {
            // Given
            Package pkg = createPackage(
                    600L,
                    LocalDateTime.now(),
                    LocalDateTime.now().plusMinutes(10),
                    LocalDateTime.now().plusMinutes(30),
                    "COMPLETED",
                    0,
                    null);
            when(packageRepository.findById(600L)).thenReturn(Optional.of(pkg));

            // When
            Optional<MappedPackage> result = packageService.getPackageById(600L);

            // Then
            assertThat(result).isPresent();
            MappedPackage mapped = result.get();

            assertThat(mapped.getEta()).isNull();
            assertThat(mapped.getOrderInTime()).isNull(); // Can't determine without ETA
        }
    }

    @Nested
    @DisplayName("Date Formatting Tests")
    class DateFormattingTests {

        @Test
        @DisplayName("Should format dates without microseconds correctly")
        void shouldFormatDatesWithoutMicroseconds() {
            // Given
            LocalDateTime dateTime = LocalDateTime.of(2025, 11, 13, 10, 47, 52, 0);
            Package pkg = createPackage(
                    700L, dateTime, dateTime, dateTime, "COMPLETED", 0, 60);
            when(packageRepository.findById(700L)).thenReturn(Optional.of(pkg));

            // When
            Optional<MappedPackage> result = packageService.getPackageById(700L);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getCreatedAt()).isEqualTo("2025-11-13 10:47:52");
        }

        @Test
        @DisplayName("Should format dates with microseconds correctly")
        void shouldFormatDatesWithMicroseconds() {
            // Given
            LocalDateTime dateTime = LocalDateTime.of(2025, 11, 13, 10, 47, 52, 675248000);
            Package pkg = createPackage(
                    701L, dateTime, dateTime, dateTime, "COMPLETED", 0, 60);
            when(packageRepository.findById(701L)).thenReturn(Optional.of(pkg));

            // When
            Optional<MappedPackage> result = packageService.getPackageById(701L);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getCreatedAt()).matches("2025-11-13 10:47:52\\.\\d{6}");
        }
    }

    // Helper method to create package
    private Package createPackage(
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
        pkg.setStatus(status);
        pkg.setCancelled(cancelled);
        pkg.setEta(eta);
        pkg.setLastUpdatedAt(LocalDateTime.now());
        return pkg;
    }
}