# Kafka Package System - Performance Benchmark Report

Generated: 2025-12-07 06:56:06

## Test Environment
- Java Version: 21.0.9
- OS: Linux
- CPU Cores: 16
- Max Memory: 7844.00 MB

## Benchmark Results

### Concurrent Processing (10 threads)

- **Total Packages**: 1000
- **Total Time**: 666.21 ms
- **Throughput**: 1501.02 packages/sec
- **Average Latency**: 643.59 ms
- **Min Latency**: 0 ns
- **Max Latency**: 0 ns
- **P50 Latency**: 0 ns
- **P95 Latency**: 0 ns
- **P99 Latency**: 0 ns
- **Memory Used**: 11.14 MB

### Concurrent Processing (50 threads)

- **Total Packages**: 5000
- **Total Time**: 177.95 ms
- **Throughput**: 28098.10 packages/sec
- **Average Latency**: 112.01 ms
- **Min Latency**: 0 ns
- **Max Latency**: 0 ns
- **P50 Latency**: 0 ns
- **P95 Latency**: 0 ns
- **P99 Latency**: 0 ns

### Database Query Latency

- **Total Packages**: 1000
- **Average Latency**: 33.58 μs
- **Min Latency**: 19.04 μs
- **Max Latency**: 381.64 μs
- **P50 Latency**: 25.08 μs
- **P95 Latency**: 62.26 μs
- **P99 Latency**: 91.32 μs

### Transformation Latency

- **Total Packages**: 1000
- **Average Latency**: 23.83 μs
- **Min Latency**: 14.27 μs
- **Max Latency**: 390.97 μs
- **P50 Latency**: 17.63 μs
- **P95 Latency**: 43.93 μs
- **P99 Latency**: 93.05 μs

### Kafka Publishing Latency

- **Total Packages**: 500
- **Average Latency**: 24.59 μs
- **Min Latency**: 3.28 μs
- **Max Latency**: 8.88 ms
- **P50 Latency**: 4.37 μs
- **P95 Latency**: 10.31 μs
- **P99 Latency**: 30.44 μs

### End-to-End Latency (DB -> Kafka)

- **Total Packages**: 500
- **Average Latency**: 64.51 μs
- **Min Latency**: 31.75 μs
- **Max Latency**: 714.69 μs
- **P50 Latency**: 49.65 μs
- **P95 Latency**: 119.54 μs
- **P99 Latency**: 216.70 μs

### Single Package Processing

- **Total Packages**: 1000
- **Total Time**: 56.98 ms
- **Throughput**: 17549.88 packages/sec
- **Average Latency**: 56.98 μs
- **Min Latency**: 25.84 μs
- **Max Latency**: 9.23 ms
- **P50 Latency**: 37.62 μs
- **P95 Latency**: 89.13 μs
- **P99 Latency**: 218.09 μs
- **Memory Used**: 26.33 KB

### Batch Processing (10 packages)

- **Total Packages**: 1000
- **Total Time**: 123.23 ms
- **Throughput**: 8115.22 packages/sec
- **Average Latency**: 1.23 ms
- **Min Latency**: 468.17 μs
- **Max Latency**: 34.58 ms
- **P50 Latency**: 828.25 μs
- **P95 Latency**: 1.28 ms
- **P99 Latency**: 3.49 ms
- **Memory Used**: 94.01 KB

### Batch Processing (50 packages)

- **Total Packages**: 2500
- **Total Time**: 54.18 ms
- **Throughput**: 46140.83 packages/sec
- **Average Latency**: 1.08 ms
- **Min Latency**: 555.53 μs
- **Max Latency**: 2.22 ms
- **P50 Latency**: 1.02 ms
- **P95 Latency**: 1.95 ms
- **P99 Latency**: 2.22 ms
- **Memory Used**: 282.80 KB

### Batch Processing (100 packages)

- **Total Packages**: 2000
- **Total Time**: 17.93 ms
- **Throughput**: 111570.22 packages/sec
- **Average Latency**: 896.30 μs
- **Min Latency**: 733.73 μs
- **Max Latency**: 1.51 ms
- **P50 Latency**: 799.37 μs
- **P95 Latency**: 1.29 ms
- **P99 Latency**: 1.51 ms
- **Memory Used**: 405.34 KB

### Batch Processing (500 packages)

- **Total Packages**: 5000
- **Total Time**: 27.75 ms
- **Throughput**: 180178.23 packages/sec
- **Average Latency**: 2.78 ms
- **Min Latency**: 2.04 ms
- **Max Latency**: 4.26 ms
- **P50 Latency**: 2.28 ms
- **P95 Latency**: 4.26 ms
- **P99 Latency**: 4.26 ms
- **Memory Used**: 1.42 MB

### Memory Usage Patterns


**Memory Profile by Batch Size**:
| Batch Size | Memory Used | Per Package |
|------------|-------------|-------------|
| 10 | 4.44 MB | 454.64 KB |
| 50 | 2.00 MB | 40.96 KB |
| 100 | 5.15 MB | 52.74 KB |
| 500 | 17.48 MB | 35.80 KB |
| 1000 | 35.48 MB | 36.33 KB |

