# Kafka Package System - Performance Benchmark Report

Generated: 2025-12-07 07:48:19

## Test Environment
- Java Version: 21.0.9
- OS: Linux
- CPU Cores: 16
- Max Memory: 7844.00 MB

## Benchmark Results

### Concurrent Processing (10 threads)

- **Total Packages**: 1000
- **Total Time**: 617.33 ms
- **Throughput**: 1619.87 packages/sec
- **Average Latency**: 599.79 ms
- **Min Latency**: 0 ns
- **Max Latency**: 0 ns
- **P50 Latency**: 0 ns
- **P95 Latency**: 0 ns
- **P99 Latency**: 0 ns

### Concurrent Processing (50 threads)

- **Total Packages**: 5000
- **Total Time**: 253.01 ms
- **Throughput**: 19762.05 packages/sec
- **Average Latency**: 170.37 ms
- **Min Latency**: 0 ns
- **Max Latency**: 0 ns
- **P50 Latency**: 0 ns
- **P95 Latency**: 0 ns
- **P99 Latency**: 0 ns

### Database Query Latency

- **Total Packages**: 1000
- **Average Latency**: 48.16 μs
- **Min Latency**: 23.77 μs
- **Max Latency**: 447.27 μs
- **P50 Latency**: 39.16 μs
- **P95 Latency**: 87.31 μs
- **P99 Latency**: 155.57 μs

### Transformation Latency

- **Total Packages**: 1000
- **Average Latency**: 31.46 μs
- **Min Latency**: 17.77 μs
- **Max Latency**: 473.24 μs
- **P50 Latency**: 20.14 μs
- **P95 Latency**: 83.05 μs
- **P99 Latency**: 186.21 μs

### Kafka Publishing Latency

- **Total Packages**: 500
- **Average Latency**: 22.64 μs
- **Min Latency**: 3.66 μs
- **Max Latency**: 8.85 ms
- **P50 Latency**: 3.95 μs
- **P95 Latency**: 7.40 μs
- **P99 Latency**: 21.84 μs

### End-to-End Latency (DB -> Kafka)

- **Total Packages**: 500
- **Average Latency**: 81.03 μs
- **Min Latency**: 42.91 μs
- **Max Latency**: 861.88 μs
- **P50 Latency**: 74.56 μs
- **P95 Latency**: 128.18 μs
- **P99 Latency**: 191.27 μs

### Single Package Processing

- **Total Packages**: 1000
- **Total Time**: 77.15 ms
- **Throughput**: 12961.04 packages/sec
- **Average Latency**: 77.15 μs
- **Min Latency**: 26.78 μs
- **Max Latency**: 8.93 ms
- **P50 Latency**: 58.32 μs
- **P95 Latency**: 131.97 μs
- **P99 Latency**: 206.70 μs
- **Memory Used**: 29.36 KB

### Batch Processing (10 packages)

- **Total Packages**: 1000
- **Total Time**: 128.14 ms
- **Throughput**: 7803.76 packages/sec
- **Average Latency**: 1.28 ms
- **Min Latency**: 466.44 μs
- **Max Latency**: 35.65 ms
- **P50 Latency**: 872.98 μs
- **P95 Latency**: 1.52 ms
- **P99 Latency**: 2.33 ms
- **Memory Used**: 141.34 KB

### Batch Processing (50 packages)

- **Total Packages**: 2500
- **Total Time**: 60.14 ms
- **Throughput**: 41566.96 packages/sec
- **Average Latency**: 1.20 ms
- **Min Latency**: 817.86 μs
- **Max Latency**: 2.54 ms
- **P50 Latency**: 1.10 ms
- **P95 Latency**: 1.64 ms
- **P99 Latency**: 2.54 ms
- **Memory Used**: 278.37 KB

### Batch Processing (100 packages)

- **Total Packages**: 2000
- **Total Time**: 23.17 ms
- **Throughput**: 86332.37 packages/sec
- **Average Latency**: 1.16 ms
- **Min Latency**: 799.52 μs
- **Max Latency**: 1.67 ms
- **P50 Latency**: 1.04 ms
- **P95 Latency**: 1.60 ms
- **P99 Latency**: 1.67 ms
- **Memory Used**: 596.65 KB

### Batch Processing (500 packages)

- **Total Packages**: 5000
- **Total Time**: 28.75 ms
- **Throughput**: 173909.23 packages/sec
- **Average Latency**: 2.88 ms
- **Min Latency**: 2.42 ms
- **Max Latency**: 3.76 ms
- **P50 Latency**: 2.71 ms
- **P95 Latency**: 3.76 ms
- **P99 Latency**: 3.76 ms

### Memory Usage Patterns


**Memory Profile by Batch Size**:
| Batch Size | Memory Used | Per Package |
|------------|-------------|-------------|
| 10 | 4.39 MB | 449.09 KB |
| 50 | 2.00 MB | 40.96 KB |
| 100 | 5.18 MB | 53.07 KB |
| 500 | 17.53 MB | 35.91 KB |
| 1000 | 35.53 MB | 36.39 KB |

