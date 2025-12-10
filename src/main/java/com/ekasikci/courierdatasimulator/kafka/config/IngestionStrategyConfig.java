package com.ekasikci.courierdatasimulator.kafka.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

/**
 * Configuration for ingestion strategy (Batch/MicroBatch/Streaming)
 */
@Configuration
@ConfigurationProperties(prefix = "ingestion")
@Data
public class IngestionStrategyConfig {

    /**
     * Ingestion strategy: BATCH, MICROBATCH, or STREAMING
     */
    private String strategy = "BATCH";

    /**
     * Batch configuration
     */
    private BatchConfig batch = new BatchConfig();

    /**
     * Micro-batch configuration
     */
    private MicrobatchConfig microbatch = new MicrobatchConfig();

    /**
     * Streaming configuration
     */
    private StreamingConfig streaming = new StreamingConfig();

    @Data
    public static class BatchConfig {
        /**
         * Interval between batch runs (in minutes)
         */
        private int intervalMinutes = 5;

        /**
         * Maximum batch size (number of packages)
         */
        private int maxSize = 1000;
    }

    @Data
    public static class MicrobatchConfig {
        /**
         * Interval between micro-batch runs (in seconds)
         */
        private int intervalSeconds = 1;

        /**
         * Maximum micro-batch size (number of packages)
         */
        private int maxSize = 100;
    }

    @Data
    public static class StreamingConfig {
        /**
         * Enable streaming mode
         */
        private boolean enabled = true;

        /**
         * Use async processing for streaming
         */
        private boolean async = true;
    }
}