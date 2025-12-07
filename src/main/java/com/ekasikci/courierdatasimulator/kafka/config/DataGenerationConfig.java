package com.ekasikci.courierdatasimulator.kafka.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

/**
 * Configuration for data generation and simulation
 * All values can be changed in application.properties
 */
@Configuration
@ConfigurationProperties(prefix = "data.generation")
@Data
public class DataGenerationConfig {

    /**
     * Enable/disable automatic data generation
     */
    private boolean enabled = false;

    /**
     * Traffic pattern to simulate
     * Options: CONSTANT, MORNING_RUSH, SPIKE, REALISTIC, BLACK_FRIDAY
     */
    private String pattern = "CONSTANT";

    /**
     * Base rate of package generation (packages per second)
     */
    private int baseRate = 10;

    /**
     * Probability of generating duplicate packages (0.0 to 1.0)
     */
    private double duplicateRate = 0.05;

    /**
     * Probability of generating packages with errors (0.0 to 1.0)
     */
    private double errorRate = 0.02;

    /**
     * Probability of network/system delay (0.0 to 1.0)
     */
    private double delayProbability = 0.10;

    /**
     * Maximum delay in seconds when delay occurs
     */
    private int maxDelaySeconds = 5;

    /**
     * Probability of package cancellation (0.0 to 1.0)
     */
    private double cancellationRate = 0.15;

    /**
     * Probability of package completion (0.0 to 1.0)
     */
    private double completionRate = 0.80;
}