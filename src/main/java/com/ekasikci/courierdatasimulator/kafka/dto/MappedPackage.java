package com.ekasikci.courierdatasimulator.kafka.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MappedPackage {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("created_at")
    private String createdAt;

    @JsonProperty("last_updated_at")
    private String lastUpdatedAt;

    @JsonProperty("collection_duration")
    private Integer collectionDuration;

    @JsonProperty("delivery_duration")
    private Integer deliveryDuration;

    @JsonProperty("eta")
    private Integer eta;

    @JsonProperty("lead_time")
    private Integer leadTime;

    @JsonProperty("order_in_time")
    private Boolean orderInTime;
}