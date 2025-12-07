package com.ekasikci.courierdatasimulator.kafka.strategy;

import java.util.List;

import com.ekasikci.courierdatasimulator.kafka.entitiy.Package;

/**
 * Strategy pattern for different ingestion approaches
 * Implementations: Batch, MicroBatch, Streaming
 */
public interface IngestionStrategy {

    /**
     * Process and ingest packages according to strategy
     * 
     * @param packages List of packages to process
     */
    void ingest(List<Package> packages);

    /**
     * Get strategy name for logging/metrics
     */
    String getStrategyName();

    /**
     * Check if this strategy is currently active
     */
    boolean isActive();
}