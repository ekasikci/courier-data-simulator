# Kafka Package System - Performance Benchmark Report

Generated: 2025-12-07 21:24:43

## Test Environment
- Java Version: 21.0.9
- OS: Linux
- CPU Cores: 16
- Max Memory: 7844.00 MB

## Benchmark Results

### Concurrent Processing (10 threads)

- **Total Packages**: 1000
- **Total Time**: 671.70 ms
- **Throughput**: 1488.75 packages/sec
- **Average Latency**: 645.32 ms
- **Min Latency**: 0 ns
- **Max Latency**: 0 ns
- **P50 Latency**: 0 ns
- **P95 Latency**: 0 ns
- **P99 Latency**: 0 ns

### Concurrent Processing (50 threads)

- **Total Packages**: 5000
- **Total Time**: 165.60 ms
- **Throughput**: 30193.05 packages/sec
- **Average Latency**: 124.81 ms
- **Min Latency**: 0 ns
- **Max Latency**: 0 ns
- **P50 Latency**: 0 ns
- **P95 Latency**: 0 ns
- **P99 Latency**: 0 ns
- **Memory Used**: 90.26 MB

### Transformation Latency

- **Total Packages**: 1000
- **Average Latency**: 32.42 μs
- **Min Latency**: 17.42 μs
- **Max Latency**: 404.74 μs
- **P50 Latency**: 26.54 μs
- **P95 Latency**: 59.66 μs
- **P99 Latency**: 98.70 μs

### End-to-End Latency (DB -> Kafka)

- **Total Packages**: 500
- **Average Latency**: 65.94 μs
- **Min Latency**: 30.07 μs
- **Max Latency**: 7.54 ms
- **P50 Latency**: 41.77 μs
- **P95 Latency**: 85.84 μs
- **P99 Latency**: 186.15 μs

### Database Query Latency

- **Total Packages**: 1000
- **Average Latency**: 9.70 μs
- **Min Latency**: 7.57 μs
- **Max Latency**: 318.95 μs
- **P50 Latency**: 8.54 μs
- **P95 Latency**: 12.87 μs
- **P99 Latency**: 25.12 μs

### Kafka Publishing Latency

- **Total Packages**: 500
- **Average Latency**: 5.47 μs
- **Min Latency**: 3.23 μs
- **Max Latency**: 335.13 μs
- **P50 Latency**: 3.52 μs
- **P95 Latency**: 9.43 μs
- **P99 Latency**: 28.86 μs

### Single Package Processing

- **Total Packages**: 1000
- **Total Time**: 47.52 ms
- **Throughput**: 21042.49 packages/sec
- **Average Latency**: 47.52 μs
- **Min Latency**: 20.69 μs
- **Max Latency**: 8.71 ms
- **P50 Latency**: 31.17 μs
- **P95 Latency**: 74.24 μs
- **P99 Latency**: 115.70 μs
- **Memory Used**: 12.24 KB

### Batch Processing (100 packages)

- **Total Packages**: 2000
- **Total Time**: 81.30 ms
- **Throughput**: 24600.47 packages/sec
- **Average Latency**: 4.06 ms
- **Min Latency**: 1.38 ms
- **Max Latency**: 41.56 ms
- **P50 Latency**: 1.98 ms
- **P95 Latency**: 3.46 ms
- **P99 Latency**: 41.56 ms
- **Memory Used**: 852.92 KB

### Batch Processing (500 packages)

- **Total Packages**: 5000
- **Total Time**: 56.15 ms
- **Throughput**: 89041.86 packages/sec
- **Average Latency**: 5.62 ms
- **Min Latency**: 4.24 ms
- **Max Latency**: 7.14 ms
- **P50 Latency**: 5.40 ms
- **P95 Latency**: 7.14 ms
- **P99 Latency**: 7.14 ms

### Batch Processing (10 packages)

- **Total Packages**: 1000
- **Total Time**: 30.22 ms
- **Throughput**: 33093.40 packages/sec
- **Average Latency**: 302.18 μs
- **Min Latency**: 148.90 μs
- **Max Latency**: 1.05 ms
- **P50 Latency**: 266.51 μs
- **P95 Latency**: 539.85 μs
- **P99 Latency**: 1.02 ms
- **Memory Used**: 71.90 KB

### Batch Processing (50 packages)

- **Total Packages**: 2500
- **Total Time**: 23.08 ms
- **Throughput**: 108305.61 packages/sec
- **Average Latency**: 461.66 μs
- **Min Latency**: 248.24 μs
- **Max Latency**: 1.29 ms
- **P50 Latency**: 428.66 μs
- **P95 Latency**: 774.46 μs
- **P99 Latency**: 1.29 ms
- **Memory Used**: 253.20 KB

### Memory Usage Patterns


**Memory Profile by Batch Size**:
| Batch Size | Memory Used | Per Package |
|------------|-------------|-------------|
| 10 | 3.91 MB | 400.12 KB |
| 50 | 1.62 MB | 33.09 KB |
| 100 | 1.62 MB | 16.55 KB |
| 500 | 8.92 MB | 18.26 KB |
| 1000 | 15.61 MB | 15.98 KB |

