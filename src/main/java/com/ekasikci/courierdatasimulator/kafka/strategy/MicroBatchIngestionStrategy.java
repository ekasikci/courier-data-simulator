package com.ekasikci.courierdatasimulator.kafka.strategy;

import java.util.List;

import org.springframework.stereotype.Component;

import com.ekasikci.courierdatasimulator.kafka.config.IngestionStrategyConfig;
import com.ekasikci.courierdatasimulator.kafka.entitiy.Package;
import com.ekasikci.courierdatasimulator.kafka.service.KafkaProducerService;
import com.ekasikci.courierdatasimulator.kafka.service.PackageService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class MicroBatchIngestionStrategy implements IngestionStrategy {

    private final PackageService packageService;
    private final KafkaProducerService kafkaProducerService;
    private final IngestionStrategyConfig config;

    @Override
    public void ingest(List<Package> packages) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'ingest'");
    }

    @Override
    public String getStrategyName() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getStrategyName'");
    }

    @Override
    public boolean isActive() {
        return "BATCH".equalsIgnoreCase(config.getStrategy());
    }

}
