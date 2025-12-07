# Kafka Package System - Performance Benchmark Report

Generated: 2025-12-07 21:18:56

## Test Environment
- Java Version: 21.0.9
- OS: Linux
- CPU Cores: 16
- Max Memory: 7844.00 MB

## Benchmark Results

### Concurrent Processing (10 threads)

- **Total Packages**: 1000
- **Total Time**: 564.30 ms
- **Throughput**: 1772.11 packages/sec
- **Average Latency**: 536.32 ms
- **Min Latency**: 0 ns
- **Max Latency**: 0 ns
- **P50 Latency**: 0 ns
- **P95 Latency**: 0 ns
- **P99 Latency**: 0 ns
- **Memory Used**: 70.54 MB

### Concurrent Processing (50 threads)

- **Total Packages**: 5000
- **Total Time**: 200.55 ms
- **Throughput**: 24931.44 packages/sec
- **Average Latency**: 152.27 ms
- **Min Latency**: 0 ns
- **Max Latency**: 0 ns
- **P50 Latency**: 0 ns
- **P95 Latency**: 0 ns
- **P99 Latency**: 0 ns
- **Memory Used**: 99.81 MB

### Transformation Latency

- **Total Packages**: 1000
- **Average Latency**: 21.14 μs
- **Min Latency**: 14.06 μs
- **Max Latency**: 350.64 μs
- **P50 Latency**: 17.26 μs
- **P95 Latency**: 35.65 μs
- **P99 Latency**: 66.41 μs

### End-to-End Latency (DB -> Kafka)

- **Total Packages**: 500
- **Average Latency**: 72.98 μs
- **Min Latency**: 29.75 μs
- **Max Latency**: 9.95 ms
- **P50 Latency**: 42.53 μs
- **P95 Latency**: 90.20 μs
- **P99 Latency**: 252.42 μs

### Database Query Latency

- **Total Packages**: 1000
- **Average Latency**: 10.39 μs
- **Min Latency**: 7.28 μs
- **Max Latency**: 290.33 μs
- **P50 Latency**: 8.12 μs
- **P95 Latency**: 16.93 μs
- **P99 Latency**: 26.83 μs

### Kafka Publishing Latency

- **Total Packages**: 500
- **Average Latency**: 6.81 μs
- **Min Latency**: 3.14 μs
- **Max Latency**: 385.69 μs
- **P50 Latency**: 3.68 μs
- **P95 Latency**: 13.18 μs
- **P99 Latency**: 61.96 μs

### Single Package Processing

- **Total Packages**: 1000
- **Total Time**: 47.04 ms
- **Throughput**: 21259.80 packages/sec
- **Average Latency**: 47.04 μs
- **Min Latency**: 19.37 μs
- **Max Latency**: 8.60 ms
- **P50 Latency**: 30.13 μs
- **P95 Latency**: 66.87 μs
- **P99 Latency**: 111.59 μs
- **Memory Used**: 17.45 KB

### Batch Processing (100 packages)

- **Total Packages**: 2000
- **Total Time**: 67.57 ms
- **Throughput**: 29598.11 packages/sec
- **Average Latency**: 3.38 ms
- **Min Latency**: 1.35 ms
- **Max Latency**: 31.59 ms
- **P50 Latency**: 1.78 ms
- **P95 Latency**: 3.39 ms
- **P99 Latency**: 31.59 ms
- **Memory Used**: 1012.57 KB

### Batch Processing (500 packages)

- **Total Packages**: 5000
- **Total Time**: 72.02 ms
- **Throughput**: 69429.35 packages/sec
- **Average Latency**: 7.20 ms
- **Min Latency**: 5.61 ms
- **Max Latency**: 10.20 ms
- **P50 Latency**: 6.67 ms
- **P95 Latency**: 10.20 ms
- **P99 Latency**: 10.20 ms
- **Memory Used**: 2.15 MB

### Batch Processing (10 packages)

- **Total Packages**: 1000
- **Total Time**: 45.60 ms
- **Throughput**: 21931.27 packages/sec
- **Average Latency**: 455.97 μs
- **Min Latency**: 169.02 μs
- **Max Latency**: 2.53 ms
- **P50 Latency**: 409.46 μs
- **P95 Latency**: 715.92 μs
- **P99 Latency**: 1.94 ms
- **Memory Used**: 54.57 KB

### Batch Processing (50 packages)

- **Total Packages**: 2500
- **Total Time**: 28.05 ms
- **Throughput**: 89128.85 packages/sec
- **Average Latency**: 560.99 μs
- **Min Latency**: 348.30 μs
- **Max Latency**: 1.79 ms
- **P50 Latency**: 541.20 μs
- **P95 Latency**: 756.74 μs
- **P99 Latency**: 1.79 ms
- **Memory Used**: 204.94 KB

### Memory Usage Patterns


**Memory Profile by Batch Size**:
| Batch Size | Memory Used | Per Package |
|------------|-------------|-------------|
| 10 | 3.04 MB | 311.45 KB |
| 50 | 1.93 MB | 39.63 KB |
| 100 | 2.08 MB | 21.27 KB |
| 500 | 9.11 MB | 18.66 KB |
| 1000 | 17.11 MB | 17.52 KB |

