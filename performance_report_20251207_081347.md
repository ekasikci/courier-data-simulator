# Kafka Package System - Performance Benchmark Report

Generated: 2025-12-07 08:13:47

## Test Environment
- Java Version: 21.0.9
- OS: Linux
- CPU Cores: 16
- Max Memory: 7844.00 MB

## Benchmark Results

### Concurrent Processing (10 threads)

- **Total Packages**: 1000
- **Total Time**: 602.62 ms
- **Throughput**: 1659.43 packages/sec
- **Average Latency**: 591.97 ms
- **Min Latency**: 0 ns
- **Max Latency**: 0 ns
- **P50 Latency**: 0 ns
- **P95 Latency**: 0 ns
- **P99 Latency**: 0 ns
- **Memory Used**: 11.46 MB

### Concurrent Processing (50 threads)

- **Total Packages**: 5000
- **Total Time**: 174.97 ms
- **Throughput**: 28576.79 packages/sec
- **Average Latency**: 137.60 ms
- **Min Latency**: 0 ns
- **Max Latency**: 0 ns
- **P50 Latency**: 0 ns
- **P95 Latency**: 0 ns
- **P99 Latency**: 0 ns

### Database Query Latency

- **Total Packages**: 1000
- **Average Latency**: 36.92 μs
- **Min Latency**: 20.97 μs
- **Max Latency**: 399.11 μs
- **P50 Latency**: 25.73 μs
- **P95 Latency**: 62.80 μs
- **P99 Latency**: 113.94 μs

### Transformation Latency

- **Total Packages**: 1000
- **Average Latency**: 37.64 μs
- **Min Latency**: 22.03 μs
- **Max Latency**: 596.94 μs
- **P50 Latency**: 26.85 μs
- **P95 Latency**: 64.35 μs
- **P99 Latency**: 189.06 μs

### Kafka Publishing Latency

- **Total Packages**: 500
- **Average Latency**: 24.85 μs
- **Min Latency**: 3.56 μs
- **Max Latency**: 9.21 ms
- **P50 Latency**: 4.58 μs
- **P95 Latency**: 13.78 μs
- **P99 Latency**: 54.96 μs

### End-to-End Latency (DB -> Kafka)

- **Total Packages**: 500
- **Average Latency**: 71.11 μs
- **Min Latency**: 38.66 μs
- **Max Latency**: 671.75 μs
- **P50 Latency**: 59.91 μs
- **P95 Latency**: 125.11 μs
- **P99 Latency**: 203.41 μs

### Single Package Processing

- **Total Packages**: 1000
- **Total Time**: 68.64 ms
- **Throughput**: 14569.77 packages/sec
- **Average Latency**: 68.64 μs
- **Min Latency**: 32.61 μs
- **Max Latency**: 9.34 ms
- **P50 Latency**: 49.57 μs
- **P95 Latency**: 108.67 μs
- **P99 Latency**: 161.50 μs
- **Memory Used**: 24.83 KB

### Batch Processing (10 packages)

- **Total Packages**: 1000
- **Total Time**: 113.69 ms
- **Throughput**: 8795.87 packages/sec
- **Average Latency**: 1.14 ms
- **Min Latency**: 488.49 μs
- **Max Latency**: 33.07 ms
- **P50 Latency**: 727.11 μs
- **P95 Latency**: 1.31 ms
- **P99 Latency**: 2.19 ms
- **Memory Used**: 133.85 KB

### Batch Processing (50 packages)

- **Total Packages**: 2500
- **Total Time**: 72.59 ms
- **Throughput**: 34437.76 packages/sec
- **Average Latency**: 1.45 ms
- **Min Latency**: 681.31 μs
- **Max Latency**: 7.48 ms
- **P50 Latency**: 1.02 ms
- **P95 Latency**: 3.68 ms
- **P99 Latency**: 7.48 ms
- **Memory Used**: 199.78 KB

### Batch Processing (100 packages)

- **Total Packages**: 2000
- **Total Time**: 21.47 ms
- **Throughput**: 93157.38 packages/sec
- **Average Latency**: 1.07 ms
- **Min Latency**: 692.10 μs
- **Max Latency**: 1.75 ms
- **P50 Latency**: 1.03 ms
- **P95 Latency**: 1.59 ms
- **P99 Latency**: 1.75 ms
- **Memory Used**: 611.49 KB

### Batch Processing (500 packages)

- **Total Packages**: 5000
- **Total Time**: 25.94 ms
- **Throughput**: 192747.14 packages/sec
- **Average Latency**: 2.59 ms
- **Min Latency**: 1.94 ms
- **Max Latency**: 4.36 ms
- **P50 Latency**: 2.11 ms
- **P95 Latency**: 4.36 ms
- **P99 Latency**: 4.36 ms
- **Memory Used**: 1.82 MB

### Memory Usage Patterns


**Memory Profile by Batch Size**:
| Batch Size | Memory Used | Per Package |
|------------|-------------|-------------|
| 10 | 4.44 MB | 454.90 KB |
| 50 | 2.00 MB | 40.96 KB |
| 100 | 5.15 MB | 52.76 KB |
| 500 | 17.48 MB | 35.80 KB |
| 1000 | 35.48 MB | 36.33 KB |

