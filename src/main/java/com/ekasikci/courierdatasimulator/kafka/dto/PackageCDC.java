package com.ekasikci.courierdatasimulator.kafka.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing the raw package data from Debezium CDC
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PackageCDC {

    private Long id;

    @JsonProperty("arrival_for_delivery_at")
    private String arrivalForDeliveryAt;

    @JsonProperty("arrival_for_pickup_at")
    private String arrivalForPickupAt;

    @JsonProperty("cancel_reason")
    private String cancelReason;

    private Integer cancelled;

    @JsonProperty("completed_at")
    private String completedAt;

    @JsonProperty("created_at")
    private String createdAt;

    @JsonProperty("customer_id")
    private Long customerId;

    @JsonProperty("in_delivery_at")
    private String inDeliveryAt;

    @JsonProperty("last_updated_at")
    private String lastUpdatedAt;

    private Integer eta;

    private String status;

    @JsonProperty("store_id")
    private Long storeId;

    @JsonProperty("origin_address_id")
    private Long originAddressId;

    private String type;

    @JsonProperty("waiting_for_assignment_at")
    private String waitingForAssignmentAt;

    @JsonProperty("user_id")
    private Long userId;

    private Integer collected;

    @JsonProperty("collected_at")
    private String collectedAt;

    @JsonProperty("cancelled_at")
    private String cancelledAt;

    @JsonProperty("picked_up_at")
    private String pickedUpAt;

    private Integer reassigned;

    @JsonProperty("order_id")
    private Long orderId;

    @JsonProperty("delivery_date")
    private String deliveryDate;

    /**
     * Check if package is cancelled
     */
    public boolean isCancelled() {
        return cancelled != null && cancelled == 1;
    }

    /**
     * Check if package is completed
     */
    public boolean isCompleted() {
        return completedAt != null && !completedAt.isEmpty();
    }
}