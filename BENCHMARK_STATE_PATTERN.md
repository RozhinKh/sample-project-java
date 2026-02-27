# Reusable JMH Benchmark State Class Pattern

## Overview

This document establishes a consistent, reusable pattern for JMH benchmark state classes that ensures safe, efficient, and reproducible test data management across all benchmark classes in the project.

## Pattern Components

### 1. Class-Level Annotations

```java
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(java.util.concurrent.TimeUnit.MILLISECONDS)
@Fork(1)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
public class MyBenchmark {
    // ...
}
```

**Explanation:**
- **@State(Scope.Thread)**: Each thread maintains independent state (better for parallel benchmarking, avoids contention)
- **@BenchmarkMode(Mode.AverageTime)**: Measures average execution time per operation
- **@OutputTimeUnit(TimeUnit.MILLISECONDS)**: Reports results in milliseconds
- **@Fork(1)**: Runs in separate JVM to isolate results from JVM warmup effects
- **@Warmup(iterations = 5, time = 1)**: 5 warmup iterations, 1 second each
- **@Measurement(iterations = 5, time = 1)**: 5 measurement iterations, 1 second each

### 2. State Fields Declaration

```java
private Random random;
private Vector<Integer> vector1K;
private Vector<Integer> vector5K;
private Vector<Integer> vector10K;
private int expectedSumPrimes100;
```

**Best Practices:**
- Declare all test data as private fields
- Include pre-computed expected values for correctness verification
- Use seeded Random for reproducible data generation
- All initialization occurs in @Setup, never in benchmark methods

### 3. Setup Method with Trial-Level Initialization

```java
@Setup(Level.Trial)
public void setup() {
    random = new Random(12345);  // Fixed seed for reproducibility
    vector1K = generateVector(1000, 10000);
    vector5K = generateVector(5000, 10000);
    vector10K = generateVector(10000, 10000);
}
```

**Key Points:**
- Use **@Setup(Level.Trial)**: Executes exactly once per trial (before warmup and measurement)
- **Seeded Random (seed=12345)**: Guarantees deterministic, reproducible test data
- **Outside Measurement Window**: Setup overhead is not included in benchmark times
- **Pre-allocate Data**: Ensure all vectors, arrays, matrices are created in setup
- **Keep Lightweight**: Avoid expensive computations; focus on data allocation

### 4. Benchmark Methods

```java
@Benchmark
public void benchmarkSortVector1K() {
    Vector<Integer> v = new Vector<>(vector1K);  // Copy for mutation
    Sort.SortVector(v);
    assert isSorted(v) : "Vector is not sorted after SortVector()";
}
```

**Best Practices:**
- Only invoke methods under test
- Use data from state fields (pre-generated in @Setup)
- Make copies of mutable data structures if the method mutates them
- Include lightweight correctness assertions
- Avoid allocation/generation (unless testing allocation itself)
- Each iteration operates on the same pre-generated data

### 5. Correctness Assertions

```java
@Benchmark
public int benchmarkSumPrimes() {
    int result = Primes.SumPrimes(100);
    assert result == expectedSumPrimes100 : 
        "Sum of primes up to 100 should be " + expectedSumPrimes100 + 
        " but got " + result;
    return result;
}
```

**Guidelines:**
- Add lightweight assertions to verify correctness
- Pre-compute expected results in @Setup (outside measurement)
- Use assertions to validate:
  - Sorted output
  - Expected sums/products
  - Factor products match original numbers
  - Array sizes
- Assertions ensure measurements are for correct implementations

## Scope Options

### Scope.Thread (Recommended)
```java
@State(Scope.Thread)
```
- Each thread gets independent state
- Better for parallel benchmarking
- No contention or cross-thread interference
- Slightly higher memory usage
- **Used in all project benchmarks**

### Scope.Benchmark
```java
@State(Scope.Benchmark)
```
- Shared state across all threads
- Only one instance per benchmark method
- Less optimal for parallel benchmarks
- Lower memory usage but potential contention
- **Not recommended for this project**

### Scope.Group
```java
@State(Scope.Group)
```
- Shared state within thread groups
- Rarely used
- Complex synchronization required
- **Not used in this project**

## Setup Levels

### Level.Trial (Recommended)
```java
@Setup(Level.Trial)
public void setup() { ... }
```
- Executes **once per trial** (before warmup + measurement)
- Ideal for static data that doesn't change across iterations
- Overhead is NOT included in measurements
- **Most common for data generation**

### Level.Iteration
```java
@Setup(Level.Iteration)
public void setup() { ... }
```
- Executes once per iteration
- Expensive option
- Use only if data must change every iteration
- Can significantly impact measurements
- **Rarely used**

### Level.Invocation
```java
@Setup(Level.Invocation)
public void setup() { ... }
```
- Executes before each benchmark method call
- Very expensive
- **Not recommended for benchmarking**
- Only for specialized profiling

## Helper Methods

```java
private Vector<Integer> generateVector(int size, int maxValue) {
    Vector<Integer> vector = new Vector<>(size);
    for (int i = 0; i < size; i++) {
        vector.add(random.nextInt(maxValue));
    }
    return vector;
}

private boolean isSorted(Vector<Integer> v) {
    for (int i = 0; i < v.size() - 1; i++) {
        if (v.get(i) > v.get(i + 1)) {
            return false;
        }
    }
    return true;
}
```

**Benefits:**
- Encapsulate validation logic
- Reusable across multiple benchmarks
- Cleaner benchmark method implementation
- Easier to maintain

## Benefits of This Pattern

1. **Reproducibility**: Seeded Random ensures identical data across runs
2. **No Measurement Contamination**: All data generation in @Setup, outside measurements
3. **Correctness Verification**: Assertions validate that measurements are for correct code
4. **Performance Isolation**: Thread-scoped state prevents cross-thread interference
5. **Memory Efficiency**: Data generated once per trial, reused across iterations
6. **Consistency**: All benchmark classes follow the same pattern
7. **Maintainability**: Clear separation of concerns

## Data Generation Best Practices

### DO
✓ Use seeded Random(12345) for reproducibility
✓ Generate all data in @Setup(Level.Trial)
✓ Pre-compute expected results for verification
✓ Use immutable data where possible (e.g., Collections.unmodifiableList())
✓ Make copies of mutable data for methods that mutate

### DON'T
✗ Generate data inside @Benchmark methods
✗ Use unseeded Random (varies each run)
✗ Allocate large structures inside benchmark loops
✗ Perform expensive computations in @Benchmark methods
✗ Mutate shared state between iterations without copying

## Timing Impact

```
Trial
├── Setup (Level.Trial) ← NOT MEASURED
├── Warmup Phase
│   ├── Warmup Iteration 1
│   ├── Warmup Iteration 2
│   └── ...
└── Measurement Phase
    ├── Measurement Iteration 1 ← MEASURED
    ├── Measurement Iteration 2 ← MEASURED
    └── ...
```

- **Setup overhead**: Not included in measurements
- **Data generation**: Overhead from @Setup doesn't contaminate results
- **Warmup iterations**: Warm up JIT compiler and caches
- **Measurement iterations**: Only these iterations are measured

## Implementation Checklist

For each benchmark class:

- [ ] Add @State(Scope.Thread) annotation
- [ ] Add @Setup(Level.Trial) method
- [ ] Seed Random with 12345
- [ ] Generate all test data in @Setup
- [ ] Declare private fields for test data
- [ ] Pre-compute expected values in @Setup
- [ ] Add correctness assertions in @Benchmark methods
- [ ] Copy mutable data before passing to methods that mutate
- [ ] Document the pattern in class-level comment
- [ ] Use consistent naming conventions

## Applied to Project Benchmarks

This pattern is implemented in:
- **PrimesBenchmark.java** (Reference implementation)
- **SortBenchmark.java** (Reference implementation with detailed docs)
- **PrimesBenchmarks.java** (Enhanced with @Setup)
- **SortBenchmarks.java** (Documented pattern)
- **ControlBenchmarks.java** (Documented pattern)

## Example: Complete Benchmark Class

```java
/**
 * JMH benchmarks for the Sort module.
 * Demonstrates the state class pattern for efficient data management.
 * 
 * STATE CLASS PATTERN:
 * - @State(Scope.Thread): Each thread maintains independent state
 * - @Setup(Level.Trial): Initialize data once per trial
 * - Seeded Random(12345): Reproducible, deterministic test data
 * - Pre-computed values: For correctness verification
 * - Lightweight assertions: Verify results are correct
 */
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(java.util.concurrent.TimeUnit.MILLISECONDS)
@Fork(1)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
public class SortBenchmark {

    private Random random;
    private Vector<Integer> vector1K;
    private Vector<Integer> vector5K;

    /**
     * Setup method to generate test vectors once per trial.
     * Uses @Setup(Level.Trial) to initialize data exactly once.
     * Overhead is NOT included in benchmark measurements.
     */
    @Setup(Level.Trial)
    public void setup() {
        random = new Random(12345);  // Fixed seed for reproducibility
        vector1K = generateVector(1000, 10000);
        vector5K = generateVector(5000, 10000);
    }

    private Vector<Integer> generateVector(int size, int maxValue) {
        Vector<Integer> vector = new Vector<>(size);
        for (int i = 0; i < size; i++) {
            vector.add(random.nextInt(maxValue));
        }
        return vector;
    }

    @Benchmark
    public void benchmarkSortVector1K() {
        Vector<Integer> v = new Vector<>(vector1K);  // Copy for mutation
        Sort.SortVector(v);
        assert isSorted(v) : "Vector is not sorted";
    }

    private boolean isSorted(Vector<Integer> v) {
        for (int i = 0; i < v.size() - 1; i++) {
            if (v.get(i) > v.get(i + 1)) {
                return false;
            }
        }
        return true;
    }
}
```

## Conclusion

This reusable pattern ensures:
- **Consistency**: All benchmark classes follow the same structure
- **Correctness**: Assertions verify implementations are correct
- **Reproducibility**: Seeded Random guarantees identical data
- **Accuracy**: No measurement contamination from data generation
- **Maintainability**: Clear, documented pattern for future benchmarks
