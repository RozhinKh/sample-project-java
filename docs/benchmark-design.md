# Benchmark Design Document: Scenario B Workloads

## Table of Contents
1. [Executive Summary](#executive-summary)
2. [Overview](#overview)
3. [Algorithms.Sort Category](#algorithmssort-category)
4. [Algorithms.Primes Category](#algorithmsprimes-category)
5. [DataStructures.DsVector Category](#datastructuresDsvector-category)
6. [DataStructures.DsLinkedList Category](#datastructuresDslinkedlist-category)
7. [Control.Single Category](#controlsingle-category)
8. [Control.Double Category](#controldouble-category)
9. [Summary and Total Runtime Estimate](#summary-and-total-runtime-estimate)
10. [Test Data Generation Guidelines](#test-data-generation-guidelines)

---

## Executive Summary

This document specifies the complete benchmark suite for Scenario B, comprising **16 benchmark methods** across 6 categories. Each method is thoroughly documented with specific workload parameters, expected execution times, input distributions, and test data characteristics. The benchmark suite is designed to execute within a 5-10 minute window while comprehensively testing performance across varying complexity levels.

**Key Metrics:**
- **Total Methods:** 16
- **Total Test Cases:** 33 distinct workloads
- **Estimated Total Runtime:** 6-8 minutes (single execution)
- **Complexity Range:** O(n) to O(n²) operations
- **Workload Range:** 1K to 10K elements (vectors/arrays), 100-500 numeric iterations

---

## Overview

### Scenario B Definition

Scenario B is designed to measure real-world performance with medium-sized workloads that are representative of practical applications. Unlike smaller unit tests, these workloads are large enough to produce measurable execution times while remaining tractable for frequent execution.

### Workload Sizing Strategy

Workload sizes are selected to:
1. **Show clear scaling behavior** - Multiple sizes demonstrate O(n) vs O(n²) differences
2. **Remain within reasonable execution time** - No single test exceeds a few seconds
3. **Test practical data volumes** - 1K-10K elements represent typical application data sizes
4. **Provide reproducibility** - Fixed sizes ensure consistent results across runs

### Time Estimation Methodology

Execution time estimates are calculated based on:
- **Algorithm Complexity:** Theoretical Big-O analysis
- **Operation Costs:** Typical latencies for Java operations on modern hardware (2-4 GHz CPUs)
- **Memory Effects:** Cache behavior for different data sizes
- **JVM Overhead:** Class loading, garbage collection, and JIT compilation warmup

All estimates assume:
- Warm JVM (methods have been compiled to native code)
- System idle (minimal background interference)
- Standard Java optimization flags
- No unusual memory pressure

---

## Algorithms.Sort Category

The Sort category contains 3 benchmark methods testing various sorting and selection algorithms with vector operations.

### 1. SortVector

**Method Signature:**
```java
public static void SortVector(Vector<Integer> v)
```

**Algorithm Details:**
- Uses `Collections.sort()` which employs TimSort (hybrid merge/insertion sort)
- Time Complexity: **O(n log n)** average and worst case
- Space Complexity: **O(n)** for temporary arrays

**Workload Specification:**

| Workload | Vector Size | Description |
|----------|-------------|-------------|
| Small | 1,000 | Small dataset for baseline measurement |
| Medium | 5,000 | Mid-range workload |
| Large | 10,000 | Large dataset to demonstrate scaling |

**Input Characteristics:**
- **Value Distribution:** Random integers in range [0, 10000)
- **Generation Method:** `GenVector.generateVector(size, 10000)`
- **Pre-sort State:** Unsorted/randomized
- **Duplicates:** ~10-15% probability (natural distribution)

**Expected Execution Times:**
- 1K elements: **0.04-0.08 ms** (0.00004-0.00008 seconds)
  - Justification: TimSort works efficiently on small datasets with minimal overhead
- 5K elements: **0.25-0.45 ms** (0.00025-0.00045 seconds)
  - Justification: Scales logarithmically; 5x data size → ~1.15x time
- 10K elements: **0.55-0.95 ms** (0.00055-0.00095 seconds)
  - Justification: 2x data size → ~1.35x time due to log(n) scaling

**Total Expected Time: ~1.5-2 ms**

**Test Data Generation:**
```
For each size in [1K, 5K, 10K]:
  - Generate random vector using GenVector.generateVector(size, 10000)
  - Record execution time for sorting
  - Verify result is sorted (correctness validation)
```

**Special Considerations:**
- TimSort benefits from partially sorted data; ensure random generation
- JVM warm-up is critical; run multiple iterations before timing
- Vector class has synchronized methods; ArrayList alternative may be faster
- Cache effects become noticeable at 10K due to L3 cache capacity (~20MB typical)

---

### 2. DutchFlagPartition

**Method Signature:**
```java
public static void DutchFlagPartition(Vector<Integer> v, int pivot_value)
```

**Algorithm Details:**
- Three-way partitioning: values < pivot, = pivot, > pivot
- Time Complexity: **O(n)** - single pass with 2 passes for partitioning
- Space Complexity: **O(1)** - in-place partitioning

**Workload Specification:**

| Workload | Vector Size | Pivot Value | Description |
|----------|-------------|------------|-------------|
| Small | 1,000 | 5000 (median) | Small vector with mid-range pivot |
| Medium | 5,000 | 5000 (median) | Medium vector, tests scaling |
| Large | 10,000 | 5000 (median) | Large vector, demonstrates efficiency |

**Input Characteristics:**
- **Value Distribution:** Random integers in range [0, 10000)
- **Generation Method:** `GenVector.generateVector(size, 10000)`
- **Pivot Selection:** 5000 = median value (statistically balanced)
- **Expected Pivot Count:** ~100-200 values equal to pivot per 1K elements

**Expected Execution Times:**
- 1K elements: **0.008-0.015 ms** (8-15 microseconds)
  - Justification: Simple comparison and swap operations, minimal overhead
- 5K elements: **0.04-0.08 ms** (40-80 microseconds)
  - Justification: Linear scaling, 5x elements → ~5x time
- 10K elements: **0.08-0.16 ms** (80-160 microseconds)
  - Justification: Linear scaling, 10x elements → ~10x time

**Total Expected Time: ~0.15 ms**

**Test Data Generation:**
```
For each size in [1K, 5K, 10K]:
  - Generate random vector: GenVector.generateVector(size, 10000)
  - Apply DutchFlagPartition with pivot=5000
  - Verify partitioning: count elements < 5000, == 5000, > 5000
  - Validate ordering within partitions (not required, just verify counts)
```

**Special Considerations:**
- Pivot value at median (5000) ensures balanced partitioning
- Vector swap operations have overhead; count all swaps
- Element comparisons are fast (integer), bottleneck is memory access
- Verify the method actually does 2-pass partitioning as specified

---

### 3. MaxN

**Method Signature:**
```java
public static Vector<Integer> MaxN(Vector<Integer> v, int n)
```

**Algorithm Details:**
- Finds the n largest elements using a min-heap of size n
- Time Complexity: **O(n log k)** where k=n (typically k << vector size)
- Space Complexity: **O(n)** for the heap and result vector
- Result is sorted in descending order

**Workload Specification:**

| Workload | Vector Size | N Value | Description |
|----------|-------------|---------|-------------|
| Small-10 | 1,000 | 10 | Find top 10 from small set |
| Medium-10 | 5,000 | 10 | Find top 10 from medium set |
| Large-10 | 10,000 | 10 | Find top 10 from large set |
| Small-100 | 1,000 | 100 | Find top 100 from small set |
| Medium-100 | 5,000 | 100 | Find top 100 from medium set |
| Large-100 | 10,000 | 100 | Find top 100 from large set |
| Small-1000 | 1,000 | 1,000 | Find top 1000 from 1K set |
| Medium-1000 | 5,000 | 1,000 | Find top 1000 from 5K set |
| Large-1000 | 10,000 | 1,000 | Find top 1000 from 10K set |

**Input Characteristics:**
- **Value Distribution:** Random integers in range [0, 100000) for better spread
- **Generation Method:** `GenVector.generateVector(size, 100000)`
- **Heap Size:** Varies (n=10, 100, 1000)
- **Duplicate Probability:** ~1-5% (sparse duplicates in 100K range)

**Expected Execution Times:**

*For n=10:*
- 1K elements: **0.025 ms** (small heap operations)
  - Justification: 1K iterations, heap ops are O(log 10) ≈ 3.3
- 5K elements: **0.12 ms** (linear scaling with 5x elements)
  - Justification: 5K iterations × log(10) operations
- 10K elements: **0.24 ms** (linear scaling with 10x elements)
  - Justification: 10K iterations × log(10) operations

*For n=100:*
- 1K elements: **0.04 ms** (moderate heap size)
  - Justification: 1K iterations × log(100) ≈ 6.6 ops
- 5K elements: **0.22 ms** (linear scaling)
  - Justification: 5K iterations × log(100) ops
- 10K elements: **0.44 ms** (linear scaling)
  - Justification: 10K iterations × log(100) ops

*For n=1000:*
- 1K elements: **0.15 ms** (heap includes 1K elements)
  - Justification: 1K iterations × log(1000) ≈ 10 ops + initial heap building
- 5K elements: **1.2 ms** (larger heap overhead)
  - Justification: 5K iterations × log(1000) ops + vector construction
- 10K elements: **2.5 ms** (significant heap operations)
  - Justification: 10K iterations × log(1000) ops + final sort

**Total Expected Time: ~5-6 ms** (across all 9 workloads)

**Test Data Generation:**
```
For each (size, n) pair:
  - Generate random vector: GenVector.generateVector(size, 100000)
  - Execute MaxN(vector, n)
  - Verify result size = min(n, vector.size())
  - Verify all elements in result are actually in top n
  - Verify result is sorted in descending order
```

**Special Considerations:**
- Result vector is sorted in descending order; verify this in tests
- When n >= vector size, should return all elements
- Heap operations dominate time when n is large (1000)
- Vector initialization and sorting add overhead for larger n values
- Test with various n values to show heap size impact on scaling

---

## Algorithms.Primes Category

The Primes category contains 3 benchmark methods testing prime number algorithms with varying input ranges.

### 4. IsPrime

**Method Signature:**
```java
public static boolean IsPrime(int n)
```

**Algorithm Details:**
- Trial division up to √n
- Time Complexity: **O(√n)** per invocation
- Space Complexity: **O(1)**
- Tests primality by checking divisibility by integers up to square root

**Workload Specification:**

The method is called individually with various inputs to test across the 0-10K range.

| Workload | Input Range | Count | Description |
|----------|-------------|-------|-------------|
| Small Primes | 0-100 | 101 calls | Baseline for small numbers |
| Small-Med Primes | 100-1000 | 100 calls | Sample from 100-1000 |
| Medium Primes | 1000-5000 | 100 calls | Sample from 1000-5000 |
| Large Primes | 5000-10000 | 100 calls | Sample from 5000-10000 |

**Input Characteristics:**
- **Value Distribution:** Evenly spaced samples across each range
- **Prime Density:** 
  - [0-100]: 25 primes (25%)
  - [100-1000]: ~143 primes (14.3%)
  - [1000-5000]: ~587 primes (14.7%)
  - [5000-10000]: ~608 primes (12.2%)
- **Generation Method:** Sample every 10th number in range, plus specific primes

**Expected Execution Times:**

*Aggregated across all calls in each range:*
- 0-100 range (101 calls): **0.3-0.5 ms**
  - Justification: Small numbers have small √n, ~6.3 max iterations
  - Average iterations: ~4-5 per number
- 100-1000 range (100 calls): **1.5-2.5 ms**
  - Justification: √1000 ≈ 31.6, average iterations ~15-20
- 1000-5000 range (100 calls): **4-6 ms**
  - Justification: √5000 ≈ 70.7, average iterations ~35-40
- 5000-10000 range (100 calls): **6-9 ms**
  - Justification: √10000 = 100, average iterations ~50-60

**Total Expected Time: ~12-22 ms**

**Test Data Generation:**
```
For each range [start, end]:
  - Generate sample: every 10th number from start to end
  - For each sample, record primality result
  - Verify correctness against known primes
  - Measure time for batch of ~100 calls
```

**Special Considerations:**
- Prime checking is inherently computational; expect wider time variance
- Composite numbers may be eliminated faster (small divisor check)
- Large primes need full iteration up to √n
- Batch timing includes method call overhead (should be minimal)
- Consider warm-up iterations before timing critical calls

---

### 5. SumPrimes

**Method Signature:**
```java
public static int SumPrimes(int n)
```

**Algorithm Details:**
- Sums all prime numbers from 0 to n (exclusive)
- Internally calls `IsPrime()` for each integer
- Time Complexity: **O(n√n)** - n numbers checked, each O(√n)
- Space Complexity: **O(1)** - only maintains running sum

**Workload Specification:**

| Workload | Upper Limit (n) | Approx Primes | Description |
|----------|-----------------|---------------|-------------|
| Small | 1,000 | 168 primes | Quick baseline measurement |
| Medium | 5,000 | 669 primes | Moderate computational load |
| Large | 10,000 | 1,229 primes | Substantial computation |

**Input Characteristics:**
- **Range:** [0, n) where n ∈ {1000, 5000, 10000}
- **Prime Count Accuracy:**
  - Sum of primes < 1000 = 76,127
  - Sum of primes < 5000 = 1,593,350
  - Sum of primes < 10000 = 4,227,191
- **Distribution:** Natural density of primes decreases with larger n

**Expected Execution Times:**
- SumPrimes(1000): **8-12 ms**
  - Justification: 1000 numbers × average 5-7 iterations per IsPrime
  - Formula: 1000 × (√1000 / 3) ≈ 1000 × 10.5 ≈ 10.5 ops total
- SumPrimes(5000): **60-100 ms**
  - Justification: 5000 numbers × average 15-20 iterations per IsPrime
  - Formula: 5000 × (√5000 / 3) ≈ 5000 × 23.6 ≈ 118 ops total
- SumPrimes(10000): **150-250 ms**
  - Justification: 10000 numbers × average 25-35 iterations per IsPrime
  - Formula: 10000 × (√10000 / 4) ≈ 10000 × 25 ≈ 250 ops total

**Total Expected Time: ~220-360 ms**

**Test Data Generation:**
```
For each n in [1000, 5000, 10000]:
  - Execute SumPrimes(n)
  - Record result value
  - Verify against known prime sums (if available)
  - Measure execution time
```

**Special Considerations:**
- This is the heaviest computation in Primes category
- O(n√n) complexity shows dramatic time increase with input size
- Expected to be one of the slowest individual benchmarks
- JVM compilation/warm-up critical; first execution much slower
- Memory allocation minimal; CPU-bound operation

---

### 6. PrimeFactors

**Method Signature:**
```java
public static Vector<Integer> PrimeFactors(int n)
```

**Algorithm Details:**
- Finds all prime factors of n (with repetition)
- Trial division up to √n
- Time Complexity: **O(√n)** - varies by number of factors
- Space Complexity: **O(log n)** - stores prime factors

**Workload Specification:**

| Workload | Input Range | Count | Description |
|----------|------------|-------|-------------|
| Small | 100-1000 | 50 numbers | Small numbers with few factors |
| Medium | 1000-5000 | 50 numbers | Medium range |
| Large | 5000-10000 | 50 numbers | Large range numbers |

**Input Characteristics:**
- **Value Distribution:** Random selection from each range
- **Factor Count Variation:**
  - Most numbers: 1-10 factors (highly composite: up to 50+)
  - Primes: 0 factors (return empty)
  - Highly composite: 120 factors (e.g., 5040)
- **Generation Method:** Random integers in range, varied sizes

**Expected Execution Times:**

*Aggregated across 50 numbers in each range:*
- 100-1000 range: **0.8-1.5 ms**
  - Justification: Average √500 ≈ 22.4 iterations per number
  - 50 numbers × 15-20 avg iterations ≈ 0.75-1.0 ms
- 1000-5000 range: **1.5-3 ms**
  - Justification: Average √3000 ≈ 54.8 iterations per number
  - 50 numbers × 30-40 avg iterations ≈ 1.5-2.0 ms
- 5000-10000 range: **2.5-5 ms**
  - Justification: Average √7500 ≈ 86.6 iterations per number
  - 50 numbers × 50-70 avg iterations ≈ 2.5-3.5 ms

**Total Expected Time: ~5-10 ms**

**Test Data Generation:**
```
For each range [start, end]:
  - Generate 50 random numbers in range
  - For each number, execute PrimeFactors(n)
  - Verify each factor is prime
  - Verify product of factors equals original number
  - Measure total execution time
```

**Special Considerations:**
- Numbers with many small prime factors execute faster (early exit)
- Prime numbers return empty vector (minimal work)
- Highly composite numbers (lots of factors) need full iteration
- Memory allocation for result vector negligible
- Time variance high due to factor diversity

---

## DataStructures.DsVector Category

The DsVector category contains 6 benchmark methods testing various vector operations with fixed element counts.

### 7. modifyVector

**Method Signature:**
```java
public static Vector<Integer> modifyVector(Vector<Integer> v)
```

**Algorithm Details:**
- Adds 1 to each element in the vector
- Time Complexity: **O(n)** - single pass
- Space Complexity: **O(1)** - in-place modification
- Modifies vector and returns reference

**Workload Specification:**

| Workload | Vector Size | Description |
|----------|-------------|-------------|
| Small | 1,000 | Small dataset baseline |
| Medium | 5,000 | Mid-size workload |
| Large | 10,000 | Large dataset |

**Input Characteristics:**
- **Value Distribution:** Random integers in range [0, 10000)
- **Generation Method:** `GenVector.generateVector(size, 10000)`
- **State:** No specific pre-state required

**Expected Execution Times:**
- 1K elements: **0.08-0.15 ms**
  - Justification: 1000 get/set operations on Vector (synchronized)
  - Each operation: ~80-150 nanoseconds (includes synchronization overhead)
- 5K elements: **0.4-0.75 ms**
  - Justification: 5000 get/set operations, linear scaling
- 10K elements: **0.8-1.5 ms**
  - Justification: 10000 get/set operations, linear scaling

**Total Expected Time: ~1.3-2.4 ms**

**Test Data Generation:**
```
For each size in [1K, 5K, 10K]:
  - Generate random vector: GenVector.generateVector(size, 10000)
  - Execute modifyVector(vector)
  - Verify all elements incremented by 1
  - Measure execution time
```

**Special Considerations:**
- Vector class is synchronized; adds overhead vs ArrayList
- Each get/set involves bounds checking
- Integer boxing/unboxing minimal overhead
- Cache-friendly access pattern (sequential memory)
- Method returns modified reference; no copy operation

---

### 8. searchVector

**Method Signature:**
```java
public static Vector<Integer> searchVector(Vector<Integer> v, int n)
```

**Algorithm Details:**
- Finds all indices where value n appears
- Time Complexity: **O(n)** - linear search
- Space Complexity: **O(k)** where k=occurrences of n
- Returns vector of all matching indices

**Workload Specification:**

| Workload | Vector Size | Search Value | Expected Matches | Description |
|----------|-------------|--------------|------------------|-------------|
| Small | 1,000 | 5000 | ~10-15 | Small vector |
| Medium | 5,000 | 5000 | ~50-75 | Medium vector |
| Large | 10,000 | 5000 | ~100-150 | Large vector |

**Input Characteristics:**
- **Value Distribution:** Random integers in range [0, 10000)
- **Generation Method:** `GenVector.generateVector(size, 10000)`
- **Search Value:** 5000 (mid-range value with expected ~10% frequency)
- **Match Count:** Approximately 10% of elements (probabilistic)

**Expected Execution Times:**
- 1K elements: **0.1-0.2 ms**
  - Justification: 1000 comparisons + 10-15 vector adds
  - Comparison: ~100 ns, Add: ~150 ns
- 5K elements: **0.5-1.0 ms**
  - Justification: 5000 comparisons + 50-75 vector adds
- 10K elements: **1.0-2.0 ms**
  - Justification: 10000 comparisons + 100-150 vector adds

**Total Expected Time: ~1.6-3.2 ms**

**Test Data Generation:**
```
For each size in [1K, 5K, 10K]:
  - Generate random vector: GenVector.generateVector(size, 10000)
  - Calculate expected match count (size / 10)
  - Execute searchVector(vector, 5000)
  - Verify all returned indices contain value 5000
  - Verify no indices are missed
  - Measure execution time
```

**Special Considerations:**
- Result vector grows dynamically; accounts for allocation overhead
- Search value choice (5000) gives ~10% hit rate
- Memory allocation for result vector adds latency (proportional to match count)
- Exact match count varies; use probabilistic validation
- Comparison overhead dominates total time

---

### 9. sortVector

**Method Signature:**
```java
public static Vector<Integer> sortVector(Vector<Integer> v)
```

**Algorithm Details:**
- Bubble sort implementation
- Time Complexity: **O(n²)** - nested loops over all elements
- Space Complexity: **O(n)** - creates new vector copy
- Performs unnecessary comparisons even when partially sorted

**Workload Specification:**

| Workload | Vector Size | Description |
|----------|-------------|-------------|
| Small | 1,000 | Small dataset (quick) |
| Medium | 5,000 | Medium dataset (noticeable time) |
| Large | 10,000 | Large dataset (substantial time) |

**Input Characteristics:**
- **Value Distribution:** Random integers in range [0, 10000)
- **Generation Method:** `GenVector.generateVector(size, 10000)`
- **Initial State:** Unsorted/randomized
- **Duplicates:** ~10% probability

**Expected Execution Times:**

The bubble sort implementation performs n(n-1)/2 comparisons.

- 1K elements: **50-100 ms**
  - Justification: 1000×999/2 ≈ 500K comparisons
  - Plus 1000 set operations per pass
  - Total ops: ~500K comparisons
  - Modern CPU: 500K ops ÷ 5M ops/ms ≈ 100 ms
- 5K elements: **1.2-1.8 seconds** (1200-1800 ms)
  - Justification: 5000×4999/2 ≈ 12.5M comparisons
  - Total ops: ~12.5M 
  - Modern CPU: 12.5M ops ÷ 5M ops/ms ≈ 2500 ms
- 10K elements: **5-8 seconds** (5000-8000 ms)
  - Justification: 10000×9999/2 ≈ 50M comparisons
  - Total ops: ~50M
  - Modern CPU: 50M ops ÷ 5M ops/ms ≈ 10000 ms

**Total Expected Time: ~6-10 seconds**

**Test Data Generation:**
```
For each size in [1K, 5K, 10K]:
  - Generate random vector: GenVector.generateVector(size, 10000)
  - Execute sortVector(vector)
  - Verify result is sorted (ascending order)
  - Verify no elements lost or duplicated
  - Measure execution time
```

**Special Considerations:**
- **This is the single slowest benchmark method**
- Bubble sort is O(n²) - dramatic time increase with data size
- 10K test will take several seconds alone
- Pure comparison-based algorithm; no shortcuts
- Test data must be unsorted to hit worst-case
- JVM warm-up and compilation critical for accurate timing
- Consider measuring separately due to long execution time
- This method dominates total suite runtime

---

### 10. reverseVector

**Method Signature:**
```java
public static Vector<Integer> reverseVector(Vector<Integer> v)
```

**Algorithm Details:**
- Reverses order of vector elements
- Time Complexity: **O(n)** - single pass in reverse
- Space Complexity: **O(n)** - creates new vector
- Iterates from end to start, adding elements

**Workload Specification:**

| Workload | Vector Size | Description |
|----------|-------------|-------------|
| Small | 1,000 | Small dataset |
| Medium | 5,000 | Medium dataset |
| Large | 10,000 | Large dataset |

**Input Characteristics:**
- **Value Distribution:** Random integers in range [0, 10000)
- **Generation Method:** `GenVector.generateVector(size, 10000)`
- **State:** Any (order doesn't matter for reversal)

**Expected Execution Times:**
- 1K elements: **0.12-0.25 ms**
  - Justification: 1000 get + 1000 add operations
  - Sequential access pattern
- 5K elements: **0.6-1.25 ms**
  - Justification: 5000 get + 5000 add operations
- 10K elements: **1.2-2.5 ms**
  - Justification: 10000 get + 10000 add operations

**Total Expected Time: ~2-4 ms**

**Test Data Generation:**
```
For each size in [1K, 5K, 10K]:
  - Generate random vector: GenVector.generateVector(size, 10000)
  - Store original as reference
  - Execute reverseVector(vector)
  - Verify result[i] == original[n-1-i] for all i
  - Measure execution time
```

**Special Considerations:**
- Creates new vector; memory allocation overhead
- Sequential access pattern (good cache behavior)
- No sorting or complex operations (fast)
- Vector additions may trigger internal resizing
- Very predictable performance (no variance)

---

### 11. rotateVector

**Method Signature:**
```java
public static Vector<Integer> rotateVector(Vector<Integer> v, int n)
```

**Algorithm Details:**
- Rotates vector elements by n positions
- Time Complexity: **O(size)** - rebuilds entire vector
- Space Complexity: **O(size)** - creates new vector
- Elements at position i move to (i+n) % size

**Workload Specification:**

| Workload | Vector Size | Rotation Amount | Description |
|----------|-------------|-----------------|-------------|
| Small-25% | 1,000 | 250 | Rotate 25% |
| Small-50% | 1,000 | 500 | Rotate 50% (half) |
| Small-75% | 1,000 | 750 | Rotate 75% |
| Medium-25% | 5,000 | 1,250 | Rotate 25% |
| Medium-50% | 5,000 | 2,500 | Rotate 50% |
| Medium-75% | 5,000 | 3,750 | Rotate 75% |
| Large-25% | 10,000 | 2,500 | Rotate 25% |
| Large-50% | 10,000 | 5,000 | Rotate 50% |
| Large-75% | 10,000 | 7,500 | Rotate 75% |

**Input Characteristics:**
- **Value Distribution:** Random integers in range [0, 10000)
- **Generation Method:** `GenVector.generateVector(size, 10000)`
- **Rotation Amounts:** 25%, 50%, 75% of size (demonstrates rotation depth)
- **Note:** Rotation amount doesn't affect execution time (always O(n))

**Expected Execution Times:**

*Execution time is independent of rotation amount (always O(n))*

- 1K elements (any rotation): **0.12-0.25 ms**
  - Justification: Build new vector with 2x loops (1000 + 250/500/750)
- 5K elements (any rotation): **0.6-1.25 ms**
  - Justification: Build new vector with 2x loops (5000 + 1250/2500/3750)
- 10K elements (any rotation): **1.2-2.5 ms**
  - Justification: Build new vector with 2x loops (10000 + 2500/5000/7500)

**Total Expected Time: ~9x individual = 11-23 ms** (across all 9 rotation configs)

**Test Data Generation:**
```
For each (size, rotation) pair:
  - Generate random vector: GenVector.generateVector(size, 10000)
  - Store original as reference
  - Execute rotateVector(vector, rotation)
  - Verify result.size() == vector.size()
  - Verify result[i] == original[(i + rotation) % size]
  - Measure execution time
```

**Special Considerations:**
- Rotation amount affects validation but not execution time
- Multiple rotations (25%, 50%, 75%) test consistency
- Edge case: rotation = 0 or rotation >= size (test these too)
- Sequential memory access (good cache behavior)
- Vector allocation and population dominates time
- Time is independent of rotation amount

---

### 12. mergeVectors

**Method Signature:**
```java
public static Vector<Integer> mergeVectors(Vector<Integer> v1, Vector<Integer> v2)
```

**Algorithm Details:**
- Concatenates two vectors
- Time Complexity: **O(n+m)** where n,m are vector sizes
- Space Complexity: **O(n+m)** for result vector
- Sequential addition of all elements

**Workload Specification:**

| Workload | Size 1 | Size 2 | Total | Description |
|----------|--------|--------|-------|-------------|
| Small-Equal | 500 | 500 | 1K | Two equal small vectors |
| Medium-Equal | 2,500 | 2,500 | 5K | Two equal medium vectors |
| Large-Equal | 5,000 | 5,000 | 10K | Two equal large vectors |

**Input Characteristics:**
- **Value Distribution:** Random integers in range [0, 10000) for each vector
- **Generation Method:** `GenVector.generateVector(size1, 10000)` and `GenVector.generateVector(size2, 10000)`
- **Asymmetry:** Test with equal sizes (balanced merges)
- **Overlap:** No sorting; values can appear in both vectors

**Expected Execution Times:**
- 500+500 merge: **0.08-0.15 ms**
  - Justification: 500 + 500 = 1000 add operations
- 2500+2500 merge: **0.4-0.75 ms**
  - Justification: 5000 add operations
- 5000+5000 merge: **0.8-1.5 ms**
  - Justification: 10000 add operations

**Total Expected Time: ~1.3-2.4 ms**

**Test Data Generation:**
```
For each (size1, size2) pair:
  - Generate v1: GenVector.generateVector(size1, 10000)
  - Generate v2: GenVector.generateVector(size2, 10000)
  - Execute mergeVectors(v1, v2)
  - Verify result.size() == size1 + size2
  - Verify first size1 elements match v1
  - Verify next size2 elements match v2
  - Measure execution time
```

**Special Considerations:**
- Both vectors created independently (different random values)
- Result preserves element order (v1 then v2)
- Test equal-sized vectors (balanced workloads)
- Vector allocation grows dynamically; may resize multiple times
- Memory copying adds latency
- No sorting or comparison operations

---

## DataStructures.DsLinkedList Category

The DsLinkedList category contains 2 benchmark methods testing linked list operations.

### 13. shuffle

**Method Signature:**
```java
public static LinkedList<Integer> shuffle(LinkedList<Integer> l)
```

**Algorithm Details:**
- Randomly shuffles linked list elements
- Uses Fisher-Yates algorithm via `Collections.shuffle()`
- Time Complexity: **O(n)** - single pass randomization
- Space Complexity: **O(n)** - creates ArrayList intermediate

**Workload Specification:**

| Workload | List Size | Description |
|----------|-----------|-------------|
| Small | 1,000 | Small list |
| Medium | 5,000 | Medium list |
| Large | 10,000 | Large list |

**Input Characteristics:**
- **Value Distribution:** Sequential integers [0, n)
- **Generation Method:** Create LinkedList with integers 0 to n-1
- **Initial State:** Ordered (perfect for detecting shuffling)

**Expected Execution Times:**
- 1K elements: **0.5-1.2 ms**
  - Justification: Convert to ArrayList (1K add ops) + shuffle (1K random swaps)
  - Then convert back to LinkedList (1K add ops)
- 5K elements: **2.5-6 ms**
  - Justification: 5K add ops + 5K swaps + 5K add ops = 15K operations
- 10K elements: **5-12 ms**
  - Justification: 10K add ops + 10K swaps + 10K add ops = 30K operations

**Total Expected Time: ~8-19 ms**

**Test Data Generation:**
```
For each size in [1K, 5K, 10K]:
  - Create LinkedList with integers [0, size)
  - Store original list as reference
  - Execute shuffle(linkedList)
  - Verify result contains all original elements (same count)
  - Verify result is different from original (statistically)
  - Verify no elements lost or duplicated
  - Measure execution time
```

**Special Considerations:**
- Implementation converts to ArrayList (O(n) overhead)
- Shuffle itself is O(n) random swaps
- Convert back to LinkedList adds another O(n)
- Total overhead: 3x traversal of data
- Randomness quality depends on JVM's Random implementation
- Verify shuffling actually occurred (changed order)

---

### 14. slice

**Method Signature:**
```java
public static LinkedList<Integer> slice(LinkedList<Integer> l, int start, int end)
```

**Algorithm Details:**
- Extracts subsequence from linked list [start, end)
- Uses `subList()` then wraps in new LinkedList
- Time Complexity: **O(k)** where k = end - start (slice length)
- Space Complexity: **O(k)** for result list

**Workload Specification:**

| Workload | List Size | Start | End | Slice Size | Description |
|----------|-----------|-------|-----|-----------|-------------|
| Small-25% | 1,000 | 250 | 500 | 250 | First quarter |
| Small-50% | 1,000 | 0 | 500 | 500 | First half |
| Small-75% | 1,000 | 0 | 750 | 750 | First three-quarters |
| Medium-25% | 5,000 | 1,250 | 2,500 | 1,250 | Middle quarter |
| Medium-50% | 5,000 | 0 | 2,500 | 2,500 | First half |
| Medium-75% | 5,000 | 0 | 3,750 | 3,750 | First three-quarters |
| Large-25% | 10,000 | 2,500 | 5,000 | 2,500 | Quarter slice |
| Large-50% | 10,000 | 0 | 5,000 | 5,000 | First half |
| Large-75% | 10,000 | 0 | 7,500 | 7,500 | Three-quarters |

**Input Characteristics:**
- **Value Distribution:** Sequential integers [0, size)
- **Generation Method:** Create LinkedList with integers 0 to size-1
- **Slice Positions:** Various start/end to test different workload sizes
- **Slice Depth:** Tests different slice percentages

**Expected Execution Times:**

*Execution time depends on slice length k (not list size)*

- 250-element slice: **0.15-0.3 ms** (subList + copy overhead)
- 500-element slice: **0.3-0.6 ms** (500 get operations)
- 1,250-element slice: **0.75-1.5 ms** (1250 get operations)
- 2,500-element slice: **1.5-3 ms** (2500 get operations)
- 3,750-element slice: **2.25-4.5 ms** (3750 get operations)
- 5,000-element slice: **3-6 ms** (5000 get operations)
- 7,500-element slice: **4.5-9 ms** (7500 get operations)

**Total Expected Time: ~13-32 ms** (across all 9 slice configurations)

**Test Data Generation:**
```
For each (size, start, end) tuple:
  - Create LinkedList with integers [0, size)
  - Execute slice(linkedList, start, end)
  - Verify result.size() == (end - start)
  - Verify result[i] == original[start + i]
  - Measure execution time
```

**Special Considerations:**
- Slice length (not list size) determines execution time
- LinkedList.subList() is efficient (view-based)
- Converting to new LinkedList requires O(k) copy
- Various positions test different latency patterns
- Start position can affect cache behavior
- Time linear in slice length

---

## Control.Single Category

The Control.Single category contains 3 benchmark methods testing single-loop computations.

### 15. sumRange

**Method Signature:**
```java
public static int sumRange(int n)
```

**Algorithm Details:**
- Sums all integers from 0 to n (exclusive)
- Internally: creates array, populates, then sums
- Time Complexity: **O(n)** - array creation + population + summation
- Space Complexity: **O(n)** - array of size n

**Workload Specification:**

| Workload | N Value | Description |
|----------|---------|-------------|
| Small | 1,000 | Quick baseline |
| Medium | 2,500 | Mid-size |
| Large | 5,000 | Larger dataset |

**Input Characteristics:**
- **Range:** [0, n) with n ∈ {1000, 2500, 5000}
- **Sum Values:**
  - sumRange(1000) = 0+1+2+...+999 = 499,500
  - sumRange(2500) = 0+1+2+...+2499 = 3,122,500
  - sumRange(5000) = 0+1+2+...+4999 = 12,497,500
- **Data Type:** All fit in int (< 2^31 - 1)

**Expected Execution Times:**
- sumRange(1000): **0.05-0.1 ms**
  - Justification: Array creation (1000 ints) + loop (1000) + sum loop (1000)
  - Total ops: ~3000 operations
- sumRange(2500): **0.12-0.25 ms**
  - Justification: Array creation + loop + sum loop = ~7500 operations
- sumRange(5000): **0.25-0.5 ms**
  - Justification: Array creation + loop + sum loop = ~15000 operations

**Total Expected Time: ~0.4-0.8 ms**

**Test Data Generation:**
```
For each n in [1000, 2500, 5000]:
  - Execute sumRange(n)
  - Verify result == n*(n-1)/2
  - Measure execution time
```

**Special Considerations:**
- Three passes over data: array creation, population, summation
- Array allocation dominates time for small n
- Very predictable performance (no variance)
- Integer arithmetic is fast
- No complex logic (pure computation)
- Sum value may approach integer limits at larger n

---

### 16. maxArray

**Method Signature:**
```java
public static int maxArray(int[] arr)
```

**Algorithm Details:**
- Finds maximum value in array
- Time Complexity: **O(n)** - single pass
- Space Complexity: **O(1)** - only tracks maximum
- Returns first maximum encountered

**Workload Specification:**

| Workload | Array Size | Max Value | Description |
|----------|------------|-----------|-------------|
| Small | 1,000 | ~1000 | Small array |
| Medium | 2,500 | ~2500 | Medium array |
| Large | 5,000 | ~5000 | Large array |

**Input Characteristics:**
- **Value Distribution:** Random integers in range [0, size) for variance
- **Generation Method:** Create array of size n with random values [0, n)
- **Max Value:** Roughly equal to array size (natural distribution)
- **Duplicates:** High probability (values in [0, size) range)

**Expected Execution Times:**
- 1K elements: **0.01-0.05 ms**
  - Justification: Single comparison per element = 1000 comparisons
  - Comparison overhead ~10-50 nanoseconds
- 2.5K elements: **0.025-0.125 ms**
  - Justification: 2500 comparisons
- 5K elements: **0.05-0.25 ms**
  - Justification: 5000 comparisons

**Total Expected Time: ~0.1-0.4 ms**

**Test Data Generation:**
```
For each size in [1K, 2.5K, 5K]:
  - Create array of integers [0, size)
  - Execute maxArray(array)
  - Verify result is in range [0, size)
  - Verify result appears in array
  - Measure execution time
```

**Special Considerations:**
- Very fast method (minimal overhead)
- Single variable tracks state (cache-friendly)
- Integer comparison is primitive operation
- No memory allocation (array passed in)
- Linear scan with no early exit
- Baseline overhead dominates for small arrays

---

## Control.Double Category

The Control.Double category contains 5 benchmark methods testing nested-loop computations.

### 17. sumSquare

**Method Signature:**
```java
public static int sumSquare(int n)
```

**Algorithm Details:**
- Sums values where i²(diagonal only) from 0 to n
- Two nested loops but only adds when i==j
- Time Complexity: **O(n²)** - full nested loop regardless of diagonal check
- Space Complexity: **O(1)** - only maintains sum

**Workload Specification:**

| Workload | N Value | Expected Sum | Description |
|----------|---------|--------------|-------------|
| Small | 100 | 328,350 | Quick baseline |
| Medium | 200 | 2,626,800 | Moderate workload |
| Large | 500 | 41,458,750 | Substantial workload |

**Input Characteristics:**
- **Range:** n ∈ {100, 200, 500}
- **Computation:** For each (i,j) where i==j, add i*j = i²
- **Sum Calculation:** Sum of i² from 0 to n = n(n+1)(2n+1)/6
  - sumSquare(100) = 100×101×201/6 = 338,350
  - sumSquare(200) = 200×201×401/6 = 2,686,700
  - sumSquare(500) = 500×501×1001/6 = 41,791,750

**Expected Execution Times:**
- sumSquare(100): **0.5-1 ms**
  - Justification: 100×100 = 10K comparisons + ~100 additions
  - Pure comparison overhead dominates
- sumSquare(200): **2-4 ms**
  - Justification: 200×200 = 40K comparisons + ~200 additions
  - 4x workload → ~4x time
- sumSquare(500): **12-25 ms**
  - Justification: 500×500 = 250K comparisons + ~500 additions
  - 25x workload → ~20x time (cache effects)

**Total Expected Time: ~15-30 ms**

**Test Data Generation:**
```
For each n in [100, 200, 500]:
  - Execute sumSquare(n)
  - Calculate expected sum = n*(n+1)*(2*n+1)/6
  - Verify result matches expected (or close)
  - Measure execution time
```

**Special Considerations:**
- Nested loops are O(n²) - significant computation
- Inner loop only executes comparisons (i==j mostly false)
- Sum additions are sparse (only at diagonal)
- Loop structure is inefficient (could be O(n))
- Tests performance of redundant comparisons
- Time increases quadratically

---

### 18. sumTriangle

**Method Signature:**
```java
public static int sumTriangle(int n)
```

**Algorithm Details:**
- Sums triangular numbers T(i) = sum of 0 to i
- Nested loops where inner loop size varies
- Time Complexity: **O(n²)** - total iterations = n(n+1)/2
- Space Complexity: **O(1)** - only maintains sum

**Workload Specification:**

| Workload | N Value | Inner Loop Total | Description |
|----------|---------|------------------|-------------|
| Small | 100 | 5,050 | Quick baseline |
| Medium | 200 | 20,100 | Moderate workload |
| Large | 500 | 125,250 | Substantial workload |

**Input Characteristics:**
- **Range:** n ∈ {100, 200, 500}
- **Computation:** For each i from 0 to n, sum j from 0 to i
- **Triangular Structure:** First i terms = i(i+1)/2
- **Total Operations:** Sum of triangular numbers = Σ T(i) = sum of i(i+1)/2

**Expected Execution Times:**
- sumTriangle(100): **0.3-0.6 ms**
  - Justification: 100×101/2 = 5,050 inner loop iterations
  - Inner loop small (1 to 100)
- sumTriangle(200): **1.2-2.4 ms**
  - Justification: 200×201/2 = 20,100 inner loop iterations
  - 4x workload → ~4x time
- sumTriangle(500): **7.5-15 ms**
  - Justification: 500×501/2 = 125,250 inner loop iterations
  - 25x workload → ~25x time

**Total Expected Time: ~9-18 ms**

**Test Data Generation:**
```
For each n in [100, 200, 500]:
  - Execute sumTriangle(n)
  - Calculate expected sum (sum of T(i) from 0 to n)
  - Verify result is correct
  - Measure execution time
```

**Special Considerations:**
- Variable inner loop size (i increases from 0 to n)
- More iterations than sumSquare (n(n+1)/2 vs n²)
- Inner loop additions are always executed (no conditional skip)
- Pure summation (no comparisons)
- Time increases quadratically with n

---

### 19. countPairs

**Method Signature:**
```java
public static int countPairs(int[] arr)
```

**Algorithm Details:**
- Counts values that appear exactly twice
- Nested loops to count duplicates for each element
- Time Complexity: **O(n²)** - for each element, count occurrences
- Space Complexity: **O(1)** - only tracks count

**Workload Specification:**

| Workload | Array Size | Duplicates | Description |
|----------|------------|-----------|-------------|
| Small | 1,000 | ~100-150 | Few duplicates expected |
| Medium | 2,500 | ~250-375 | Moderate duplicates |
| Large | 5,000 | ~500-750 | More duplicates |

**Input Characteristics:**
- **Value Distribution:** Random integers in range [0, 100)
- **Generation Method:** Random array with values [0, 100)
- **Expected Duplicates:** With 1K elements in [0, 100), expect ~10-15% exact pairs
- **Pairs vs Triples:** Also counts elements appearing 3+ times (handled in division)

**Expected Execution Times:**
- 1K elements: **10-20 ms**
  - Justification: 1000×1000 = 1M comparisons (counting duplicates)
  - Each element checked against all others
- 2.5K elements: **60-120 ms**
  - Justification: 2500×2500 = 6.25M comparisons
  - 6.25x workload → ~6x time
- 5K elements: **250-500 ms**
  - Justification: 5000×5000 = 25M comparisons
  - 25x workload → ~25x time

**Total Expected Time: ~320-640 ms**

**Test Data Generation:**
```
For each size in [1K, 2.5K, 5K]:
  - Create random array with values [0, 100)
  - Execute countPairs(array)
  - Verify result is non-negative
  - Verify result ≤ size/2 (can't have more pairs than elements)
  - Measure execution time
```

**Special Considerations:**
- O(n²) nested loops dominate execution time
- For each element, count how many times it appears
- Only count if exactly 2 occurrences (divide by 2 at end)
- With small value range [0, 100), duplicates are guaranteed
- Memory access patterns vary (full matrix traversal)
- One of the slower benchmarks

---

### 20. countDuplicates

**Method Signature:**
```java
public static int countDuplicates(int[] arr0, int[] arr1)
```

**Algorithm Details:**
- Counts positions where two arrays have same value
- Nested loops comparing same indices
- Time Complexity: **O(n²)** - both arrays same size, full matrix check
- Space Complexity: **O(1)** - only tracks count

**Workload Specification:**

| Workload | Array Size | Matches | Description |
|----------|------------|---------|-------------|
| Small | 1,000 | ~100-150 | Expected matches |
| Medium | 2,500 | ~250-375 | Moderate matches |
| Large | 5,000 | ~500-750 | More matches |

**Input Characteristics:**
- **Value Distribution (arr0):** Random integers in range [0, 10000)
- **Value Distribution (arr1):** Random integers in range [0, 10000)
- **Generation:** Independent random arrays
- **Expected Match Probability:** ~1% (since values [0, 10000), collision rare)

**Expected Execution Times:**
- 1K elements: **10-20 ms**
  - Justification: 1000×1000 = 1M comparisons
  - Two nested loops checking all i,j pairs where i==j
- 2.5K elements: **60-120 ms**
  - Justification: 2500×2500 = 6.25M comparisons
  - Note: Only counts when i==j, so effectively just index comparison
- 5K elements: **250-500 ms**
  - Justification: 5000×5000 = 25M comparisons

**Total Expected Time: ~320-640 ms**

**Test Data Generation:**
```
For each size in [1K, 2.5K, 5K]:
  - Create arr0: random array [0, 10000)
  - Create arr1: random array [0, 10000)
  - Execute countDuplicates(arr0, arr1)
  - Verify result ≥ 0 and ≤ size
  - Manually verify some matches (spot check)
  - Measure execution time
```

**Special Considerations:**
- Inner loop checks both i and j for equality condition
- Effectively only counts matches where i==j
- With independent random values, matches are rare (~1%)
- Nested loop structure is O(n²) despite sparse matches
- Memory access patterns complex (two arrays)
- Similar execution time to countPairs

---

### 21. sumMatrix

**Method Signature:**
```java
public static int sumMatrix(int[][] arr)
```

**Algorithm Details:**
- Sums all values in n×n matrix
- Double nested loops (i and j both from 0 to n)
- Time Complexity: **O(n²)** - visits each cell once
- Space Complexity: **O(1)** - only maintains sum

**Workload Specification:**

| Workload | Matrix Size | Total Cells | Sum Range | Description |
|----------|------------|------------|-----------|-------------|
| Small | 10×10 | 100 | 0-1K | Tiny matrix |
| Medium | 50×50 | 2,500 | 0-25K | Medium matrix |
| Large | 100×100 | 10,000 | 0-100K | Large matrix |

**Input Characteristics:**
- **Value Distribution:** Random integers in range [0, 100)
- **Generation Method:** Create n×n matrix filled with random [0, 100)
- **Matrix Sizes:** 10×10, 50×50, 100×100 (small due to quadratic memory)
- **Sum Range:** ~5000 average for 10×10, ~125K for 50×50, ~500K for 100×100

**Expected Execution Times:**
- 10×10 matrix: **0.01-0.03 ms**
  - Justification: 100 get operations + 100 additions
  - Very fast (tiny dataset)
- 50×50 matrix: **0.15-0.3 ms**
  - Justification: 2,500 get operations + 2,500 additions
  - Small overhead per cell
- 100×100 matrix: **0.6-1.2 ms**
  - Justification: 10,000 get operations + 10,000 additions
  - Linear with cell count (despite O(n²) complexity)

**Total Expected Time: ~0.8-1.5 ms**

**Test Data Generation:**
```
For each size in [10, 50, 100]:
  - Create n×n matrix with random values [0, 100)
  - Execute sumMatrix(matrix)
  - Verify result ≥ 0
  - Verify result ≤ size² × 100
  - Manually verify partial sum (spot check)
  - Measure execution time
```

**Special Considerations:**
- Fastest "Double" category method (smallest workloads)
- Matrix size constraint: 10K cells max (memory bounded)
- Linear access pattern through matrix (good cache)
- int overflow possible at 100×100 (sum ~500K < 2^31)
- No complex operations (pure addition)
- Very predictable performance

---

## Summary and Total Runtime Estimate

### Method Count by Category

| Category | Method Count | Methods |
|----------|-------------|---------|
| Algorithms.Sort | 3 | SortVector, DutchFlagPartition, MaxN |
| Algorithms.Primes | 3 | IsPrime, SumPrimes, PrimeFactors |
| DataStructures.DsVector | 6 | modifyVector, searchVector, sortVector, reverseVector, rotateVector, mergeVectors |
| DataStructures.DsLinkedList | 2 | shuffle, slice |
| Control.Single | 3 | sumRange, maxArray, sumModulus |
| Control.Double | 5 | sumSquare, sumTriangle, countPairs, countDuplicates, sumMatrix |
| **TOTAL** | **16** | **21 individual workloads** |

### Execution Time Summary by Category

| Category | Estimated Time | Primary Driver |
|----------|---|---|
| Algorithms.Sort | 5-6 ms | SortVector (TimSort) and MaxN with n=1000 |
| Algorithms.Primes | 220-360 ms | SumPrimes(10000) dominates |
| DataStructures.DsVector | 6,100-10,150 ms | sortVector() is O(n²) bubble sort killer |
| DataStructures.DsLinkedList | 21-51 ms | shuffle() involves multiple passes |
| Control.Single | 0.4-0.8 ms | sumRange dominates (3 passes) |
| Control.Double | 640-1,280 ms | countPairs and countDuplicates are O(n²) |
| **TOTAL ESTIMATED** | **~6,987-11,877 ms** | **~7-12 seconds** |

### Detailed Time Breakdown

**Fastest Methods (< 1 ms):**
- maxArray: 0.1-0.4 ms
- sumRange: 0.4-0.8 ms
- DutchFlagPartition: 0.15 ms
- mergeVectors: 1.3-2.4 ms
- sumMatrix: 0.8-1.5 ms

**Medium Methods (1-100 ms):**
- SortVector: 1.5-2 ms (all sizes)
- MaxN: 5-6 ms (all workloads)
- DsVector.modifyVector: 1.3-2.4 ms
- DsVector.reverseVector: 2-4 ms
- DsVector.searchVector: 1.6-3.2 ms
- DsVector.rotateVector: 11-23 ms
- DsLinkedList.shuffle: 8-19 ms
- DsLinkedList.slice: 13-32 ms
- Primes.IsPrime: 12-22 ms (batch)
- Primes.PrimeFactors: 5-10 ms
- sumSquare: 15-30 ms
- sumTriangle: 9-18 ms

**Slowest Methods (> 100 ms):**
- **DsVector.sortVector: 6,100-10,150 ms** ← DOMINANT
- Primes.SumPrimes: 220-360 ms
- countPairs: 320-640 ms
- countDuplicates: 320-640 ms

### Expected Total Execution Time

**Conservative Estimate:** 6,987 ms ≈ **7 seconds**
**Typical Estimate:** 9,000 ms ≈ **9 seconds**
**Worst Case:** 11,877 ms ≈ **12 seconds**

**Target Range:** 5-10 minutes per execution ✓ (well within bounds)

The benchmark suite easily fits within the specified 5-10 minute window, with ~7-12 seconds of pure computation time. With test setup, validation, and other overhead, full suite should complete in 1-3 minutes comfortably.

### Performance Scaling Analysis

**Linear (O(n)) Methods:**
- 1K→5K (5x): Time increases ~5x ✓
- 5K→10K (2x): Time increases ~2x ✓
- Very predictable scaling

**Quadratic (O(n²)) Methods:**
- sumSquare (100→200): 4x workload → ~4x time
- sumSquare (200→500): 6.25x workload → ~6x time
- countPairs/countDuplicates: 1K→5K = 25x workload → ~25x time
- Unpredictable due to cache effects

**Logarithmic (O(n log n)) Methods:**
- SortVector: 1K→5K (5x) → ~1.15x time
- MaxN with n=1000: scales linearly with workload size

---

## Test Data Generation Guidelines

### Deterministic vs. Random

**Random Generation (Preferred for Performance):**
- Use `GenVector.generateVector(size, max)` for most workloads
- Provides unbiased, realistic data distribution
- Ensures cache miss patterns are typical
- Reproduces across runs with same seed

**Deterministic Generation (For Validation):**
- Sequential arrays [0, n): Use for DsLinkedList tests
- Sorted arrays: Pre-sort after random generation
- Specific patterns: Primes, factors (mathematically defined)

### Value Range Selection

**Small Range [0, 100):**
- Used for countPairs, countDuplicates
- High duplicate probability (ensures test coverage)
- Good for collision testing

**Medium Range [0, 10,000):**
- Standard for most vector/array tests
- Provides ~10% hit rate for mid-value search
- Typical application data magnitude

**Large Range [0, 100,000):**
- Used for MaxN (spread out for heap efficiency)
- Low collision probability (~1%)
- Distributes across broader value space

### Array Initialization Strategies

**For Sort/Search/Comparison Methods:**
```
Generate random: GenVector.generateVector(size, range)
No pre-conditioning needed (tests average case)
```

**For DsLinkedList Tests:**
```
Create LinkedList with sequential values [0, size)
Enables verification of shuffle/slice correctness
```

**For Primes Tests:**
```
Generate sample integers from range
Include known primes and composites
Test actual prime-checking correctness
```

### Memory Considerations

**1K Elements:**
- ~4 KB for int array (32-bit integers)
- ~8 KB for Integer vector (includes references)
- Fits easily in L1 cache (32 KB typical)

**5K Elements:**
- ~20 KB for int array
- ~40 KB for Integer vector
- Fits in L2 cache (256 KB typical)

**10K Elements:**
- ~40 KB for int array
- ~80 KB for Integer vector
- May exceed L3 cache (20 MB), triggers memory access overhead

### Validation Approaches

**Correctness Checks (Post-Execution):**
- Verify result matches expected output
- Use known values (sums, max values) for validation
- Spot-check random results against sequential validation

**Timing Validation:**
- Multiple runs should show consistent results (±5% variance)
- Cache warm-up: run method 2-3 times before timing
- JVM warm-up: execute 10-100 iterations before timing measurements

**Scaling Validation:**
- O(n) methods: 5x data → ~5x time
- O(n²) methods: 5x data → ~25x time
- Verify observed scaling matches theoretical expectations

---

## Implementation Notes for Benchmark Harness

### Method Invocation Pattern

```java
// Warm-up phase (not timed)
for (int i = 0; i < 3; i++) {
    benchmarkMethod(testData);
}

// Measurement phase (timed)
long startTime = System.nanoTime();
benchmarkMethod(testData);
long endTime = System.nanoTime();
long duration = (endTime - startTime) / 1_000_000; // Convert to ms
```

### Data Setup

- Generate all test data before timing measurements
- Ensure data independence between runs
- Cache results for validation (don't regenerate)

### Result Collection

- Record individual method execution times
- Aggregate by category
- Calculate total suite runtime
- Compare against expected estimates

---

## Related Documentation

This benchmark design document is part of a comprehensive performance analysis framework. Refer to the following documents for additional guidance:

### JFR Profiling Strategy

**File:** `docs/jfr-profiling-guide.md`

Provides detailed guidance on:

- Java Flight Recorder (JFR) event configuration and usage
- Hotspot identification criteria with specific thresholds (≥5% runtime, >1% lock overhead, >100ms GC pauses)
- Analysis workflow using `jcmd` and Java Mission Control (JMC)
- Flame graph interpretation and correlation with benchmark results
- Expected hotspots for each benchmark category
- Troubleshooting and best practices for profiling sessions

**When to use:** During baseline and candidate profiling runs to identify performance bottlenecks and correlate findings with timing measurements.

### Performance Reporting and Comparison Framework

**File:** `docs/performance-reporting.md`

Provides detailed guidance on:

- Report formats and specifications for three artifacts (report.json, profile.txt, test-health.txt)
- Primary metrics (average execution time in milliseconds) and secondary metrics (GC allocation rate in bytes/iteration)
- Comparison thresholds and calculations for regression (>5% degradation) and improvement (>5% gain) detection
- JSON schema definition with concrete examples and field descriptions
- Human-readable profile.txt format with tables and summary statistics
- JUnit test health report format (test-health.txt) with pass/fail status
- Metadata consistency verification requirements (JVM version, CPU cores, system specs, timestamps)
- Complete worked examples of regression and improvement analysis with root cause investigation procedures
- Comparison output format for automated analysis pipelines

**When to use:** After benchmark execution to analyze baseline vs candidate performance, detect statistically significant changes, and generate reports for engineering review and deployment decisions.

---

**Document Version:** 1.0
**Last Updated:** 2024
**Status:** Comprehensive specification for Scenario B benchmark suite
