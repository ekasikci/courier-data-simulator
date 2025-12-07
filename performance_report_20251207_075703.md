# Kafka Package System - Performance Benchmark Report

Generated: 2025-12-07 07:57:03

## Test Environment
- Java Version: 21.0.9
- OS: Linux
- CPU Cores: 16
- Max Memory: 7844.00 MB

## Benchmark Results

### Concurrent Processing (10 threads)

- **Total Packages**: 1000
- **Total Time**: 564.91 ms
- **Throughput**: 1770.19 packages/sec
- **Average Latency**: 557.52 ms
- **Min Latency**: 0 ns
- **Max Latency**: 0 ns
- **P50 Latency**: 0 ns
- **P95 Latency**: 0 ns
- **P99 Latency**: 0 ns
- **Memory Used**: 19.13 MB

### Concurrent Processing (50 threads)

- **Total Packages**: 5000
- **Total Time**: 190.58 ms
- **Throughput**: 26236.17 packages/sec
- **Average Latency**: 158.84 ms
- **Min Latency**: 0 ns
- **Max Latency**: 0 ns
- **P50 Latency**: 0 ns
- **P95 Latency**: 0 ns
- **P99 Latency**: 0 ns

### Database Query Latency

- **Total Packages**: 1000
- **Average Latency**: 49.03 μs
- **Min Latency**: 19.91 μs
- **Max Latency**: 482.90 μs
- **P50 Latency**: 45.26 μs
- **P95 Latency**: 83.24 μs
- **P99 Latency**: 122.79 μs

### Transformation Latency

- **Total Packages**: 1000
- **Average Latency**: 29.12 μs
- **Min Latency**: 17.58 μs
- **Max Latency**: 385.44 μs
- **P50 Latency**: 23.21 μs
- **P95 Latency**: 49.87 μs
- **P99 Latency**: 91.04 μs

### Kafka Publishing Latency

- **Total Packages**: 500
- **Average Latency**: 25.21 μs
- **Min Latency**: 3.73 μs
- **Max Latency**: 8.86 ms
- **P50 Latency**: 4.72 μs
- **P95 Latency**: 12.99 μs
- **P99 Latency**: 45.65 μs

### End-to-End Latency (DB -> Kafka)

- **Total Packages**: 500
- **Average Latency**: 78.27 μs
- **Min Latency**: 32.14 μs
- **Max Latency**: 569.16 μs
- **P50 Latency**: 74.05 μs
- **P95 Latency**: 127.50 μs
- **P99 Latency**: 217.89 μs

### Single Package Processing

- **Total Packages**: 1000
- **Total Time**: 60.55 ms
- **Throughput**: 16516.06 packages/sec
- **Average Latency**: 60.55 μs
- **Min Latency**: 23.56 μs
- **Max Latency**: 9.36 ms
- **P50 Latency**: 43.89 μs
- **P95 Latency**: 95.86 μs
- **P99 Latency**: 162.27 μs
- **Memory Used**: 32.45 KB

### Batch Processing (10 packages)

- **Total Packages**: 1000
- **Total Time**: 131.13 ms
- **Throughput**: 7625.99 packages/sec
- **Average Latency**: 1.31 ms
- **Min Latency**: 539.42 μs
- **Max Latency**: 36.97 ms
- **P50 Latency**: 832.39 μs
- **P95 Latency**: 1.45 ms
- **P99 Latency**: 5.08 ms
- **Memory Used**: 99.40 KB

### Batch Processing (50 packages)

- **Total Packages**: 2500
- **Total Time**: 63.46 ms
- **Throughput**: 39392.96 packages/sec
- **Average Latency**: 1.27 ms
- **Min Latency**: 570.78 μs
- **Max Latency**: 2.68 ms
- **P50 Latency**: 1.17 ms
- **P95 Latency**: 2.06 ms
- **P99 Latency**: 2.68 ms
- **Memory Used**: 344.43 KB

### Batch Processing (100 packages)

- **Total Packages**: 2000
- **Total Time**: 19.88 ms
- **Throughput**: 100606.82 packages/sec
- **Average Latency**: 993.97 μs
- **Min Latency**: 745.02 μs
- **Max Latency**: 1.61 ms
- **P50 Latency**: 930.97 μs
- **P95 Latency**: 1.18 ms
- **P99 Latency**: 1.61 ms
- **Memory Used**: 307.75 KB

### Batch Processing (500 packages)

- **Total Packages**: 5000
- **Total Time**: 27.55 ms
- **Throughput**: 181481.35 packages/sec
- **Average Latency**: 2.76 ms
- **Min Latency**: 2.02 ms
- **Max Latency**: 4.07 ms
- **P50 Latency**: 2.68 ms
- **P95 Latency**: 4.07 ms
- **P99 Latency**: 4.07 ms
- **Memory Used**: 1.85 MB

### Memory Usage Patterns


**Memory Profile by Batch Size**:
| Batch Size | Memory Used | Per Package |
|------------|-------------|-------------|
| 10 | 4.92 MB | 504.05 KB |
| 50 | 2.00 MB | 40.96 KB |
| 100 | 5.15 MB | 52.76 KB |
| 500 | 19.48 MB | 39.90 KB |
| 1000 | 37.00 MB | 37.89 KB |

