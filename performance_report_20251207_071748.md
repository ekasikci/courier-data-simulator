# Kafka Package System - Performance Benchmark Report

Generated: 2025-12-07 07:17:48

## Test Environment
- Java Version: 21.0.9
- OS: Linux
- CPU Cores: 16
- Max Memory: 7844.00 MB

## Benchmark Results

### Concurrent Processing (10 threads)

- **Total Packages**: 1000
- **Total Time**: 570.63 ms
- **Throughput**: 1752.45 packages/sec
- **Average Latency**: 559.31 ms
- **Min Latency**: 0 ns
- **Max Latency**: 0 ns
- **P50 Latency**: 0 ns
- **P95 Latency**: 0 ns
- **P99 Latency**: 0 ns
- **Memory Used**: 11.19 MB

### Concurrent Processing (50 threads)

- **Total Packages**: 5000
- **Total Time**: 191.89 ms
- **Throughput**: 26057.12 packages/sec
- **Average Latency**: 152.60 ms
- **Min Latency**: 0 ns
- **Max Latency**: 0 ns
- **P50 Latency**: 0 ns
- **P95 Latency**: 0 ns
- **P99 Latency**: 0 ns

### Database Query Latency

- **Total Packages**: 1000
- **Average Latency**: 35.62 μs
- **Min Latency**: 18.65 μs
- **Max Latency**: 522.38 μs
- **P50 Latency**: 26.49 μs
- **P95 Latency**: 62.81 μs
- **P99 Latency**: 102.62 μs

### Transformation Latency

- **Total Packages**: 1000
- **Average Latency**: 24.10 μs
- **Min Latency**: 14.30 μs
- **Max Latency**: 403.13 μs
- **P50 Latency**: 17.63 μs
- **P95 Latency**: 47.95 μs
- **P99 Latency**: 91.11 μs

### Kafka Publishing Latency

- **Total Packages**: 500
- **Average Latency**: 25.70 μs
- **Min Latency**: 3.80 μs
- **Max Latency**: 9.23 ms
- **P50 Latency**: 4.72 μs
- **P95 Latency**: 12.22 μs
- **P99 Latency**: 52.31 μs

### End-to-End Latency (DB -> Kafka)

- **Total Packages**: 500
- **Average Latency**: 55.81 μs
- **Min Latency**: 28.78 μs
- **Max Latency**: 632.55 μs
- **P50 Latency**: 45.74 μs
- **P95 Latency**: 92.87 μs
- **P99 Latency**: 203.07 μs

### Single Package Processing

- **Total Packages**: 1000
- **Total Time**: 58.78 ms
- **Throughput**: 17011.86 packages/sec
- **Average Latency**: 58.78 μs
- **Min Latency**: 23.84 μs
- **Max Latency**: 9.02 ms
- **P50 Latency**: 39.59 μs
- **P95 Latency**: 95.03 μs
- **P99 Latency**: 189.99 μs
- **Memory Used**: 23.57 KB

### Batch Processing (10 packages)

- **Total Packages**: 1000
- **Total Time**: 111.25 ms
- **Throughput**: 8988.42 packages/sec
- **Average Latency**: 1.11 ms
- **Min Latency**: 442.73 μs
- **Max Latency**: 36.54 ms
- **P50 Latency**: 701.00 μs
- **P95 Latency**: 1.20 ms
- **P99 Latency**: 1.41 ms
- **Memory Used**: 129.78 KB

### Batch Processing (50 packages)

- **Total Packages**: 2500
- **Total Time**: 63.90 ms
- **Throughput**: 39122.41 packages/sec
- **Average Latency**: 1.28 ms
- **Min Latency**: 737.06 μs
- **Max Latency**: 3.19 ms
- **P50 Latency**: 1.13 ms
- **P95 Latency**: 2.00 ms
- **P99 Latency**: 3.19 ms

### Batch Processing (100 packages)

- **Total Packages**: 2000
- **Total Time**: 20.17 ms
- **Throughput**: 99159.53 packages/sec
- **Average Latency**: 1.01 ms
- **Min Latency**: 712.00 μs
- **Max Latency**: 1.89 ms
- **P50 Latency**: 959.99 μs
- **P95 Latency**: 1.35 ms
- **P99 Latency**: 1.89 ms
- **Memory Used**: 342.33 KB

### Batch Processing (500 packages)

- **Total Packages**: 5000
- **Total Time**: 28.56 ms
- **Throughput**: 175069.94 packages/sec
- **Average Latency**: 2.86 ms
- **Min Latency**: 1.94 ms
- **Max Latency**: 3.66 ms
- **P50 Latency**: 2.83 ms
- **P95 Latency**: 3.66 ms
- **P99 Latency**: 3.66 ms
- **Memory Used**: 2.07 MB

### Memory Usage Patterns


**Memory Profile by Batch Size**:
| Batch Size | Memory Used | Per Package |
|------------|-------------|-------------|
| 10 | 4.50 MB | 460.73 KB |
| 50 | 2.00 MB | 40.96 KB |
| 100 | 5.10 MB | 52.19 KB |
| 500 | 17.45 MB | 35.73 KB |
| 1000 | 35.96 MB | 36.82 KB |

