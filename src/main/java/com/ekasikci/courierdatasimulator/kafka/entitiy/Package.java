package com.ekasikci.courierdatasimulator.kafka.entitiy;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "packages")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Package {

    @Id
    private Long id;

    @Column(name = "arrival_for_delivery_at")
    private LocalDateTime arrivalForDeliveryAt;

    @Column(name = "arrival_for_pickup_at")
    private LocalDateTime arrivalForPickupAt;

    @Column(name = "cancel_reason")
    private String cancelReason;

    // Could be Boolean, but Integer is more common and safe
    @Column(name = "cancelled")
    private Integer cancelled;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "customer_id")
    private Long customerId;

    @Column(name = "in_delivery_at")
    private LocalDateTime inDeliveryAt;

    @Column(name = "last_updated_at")
    private LocalDateTime lastUpdatedAt;

    @Column(name = "eta")
    private Integer eta;

    @Column(name = "status")
    private String status;

    @Column(name = "store_id")
    private Long storeId;

    @Column(name = "origin_address_id")
    private Long originAddressId;

    @Column(name = "type")
    private String type;

    @Column(name = "waiting_for_assignment_at")
    private LocalDateTime waitingForAssignmentAt;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "collected")
    private Integer collected;

    @Column(name = "collected_at")
    private LocalDateTime collectedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "picked_up_at")
    private LocalDateTime pickedUpAt;

    @Column(name = "reassigned")
    private Integer reassigned;

    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "delivery_date")
    private String deliveryDate;

    public boolean isCancelled() {
        return cancelled != null && cancelled == 1;
    }

    public boolean isCompleted() {
        return "COMPLETED".equalsIgnoreCase(status);
    }
}