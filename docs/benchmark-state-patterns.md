# Benchmark State Class Design Patterns and Data Generation Strategy

## Table of Contents

1. [Executive Summary](#executive-summary)
2. [JMH State Scope Selection](#jmh-state-scope-selection)
3. [JMH Lifecycle Management](#jmh-lifecycle-management)
4. [Reproducibility and Seeding Strategy](#reproducibility-and-seeding-strategy)
5. [Data Generation Patterns](#data-generation-patterns)
6. [Naming Conventions](#naming-conventions)
7. [Data Immutability Strategies](#data-immutability-strategies)
8. [Example State Class Implementations](#example-state-class-implementations)
9. [Best Practices and Checklist](#best-practices-and-checklist)

---

## Executive Summary

This document defines the design patterns for all JMH benchmark state classes across the 16 benchmarks in the Scenario B suite. The patterns ensure:

- **Reproducibility:** Deterministic data generation using seed=12345
- **Consistency:** Uniform approaches across all benchmarks
- **Performance:** Minimal overhead from state setup in timed operations
- **Fairness:** Identical data across all benchmark runs
- **Maintainability:** Clear naming conventions and lifecycle management

**Key Principles:**
- Use `@State(Scope.Benchmark)` for shared state (default)
- Use `@State(Scope.Thread)` only when thread-local data is required
- Generate data once per trial with `@Setup(Level.Trial)`
- Use seeded Random(12345) for all data generation
- Keep benchmark methods free of data preparation logic

---

## JMH State Scope Selection

### @State(Scope.Benchmark) - Shared Across All Threads

**Definition:** One state instance is shared among all benchmark threads executing the same benchmark.

**Use Cases:**
- **Recommended for all benchmarks in this suite** (single-threaded execution)
- Algorithms category (Sort, Primes) - single-thread workloads
- DataStructures category (Vector, LinkedList) - single-thread operations
- Control category - single and double control flows

**Advantages:**
- Minimal memory overhead (single data instance)
- Lower garbage collection pressure
- Predictable data state across all iterations
- Simpler correctness validation

**Disadvantages:**
- Not suitable for multi-threaded contention benchmarks
- Potential false sharing on CPU cache lines (not applicable here)

**Example Annotation:**
```java
@State(Scope.Benchmark)
public class SortBenchmark {
    private Vector<Integer> smallVector;
    private Vector<Integer> mediumVector;
    private Vector<Integer> largeVector;
    
    @Setup(Level.Trial)
    public void setupData() {
        // Initialize state once per trial
    }
}
```

### @State(Scope.Thread) - Per-Thread Instance

**Definition:** Each thread executing the benchmark gets its own state instance.

**Use Cases:**
- Not typically used in this benchmark suite
- Only if future thread-local or concurrent benchmarks are added
- Thread-local storage or thread-specific caching

**Advantages:**
- Thread safety without synchronization
- Isolated data per thread
- Suitable for concurrent benchmarks

**Disadvantages:**
- Higher memory consumption (N copies for N threads)
- More garbage collection overhead
- Complex data validation across threads
- Not needed for single-threaded workloads

**Note:** This suite uses single-threaded benchmarks, so `@State(Scope.Benchmark)` is the standard choice.

---

## JMH Lifecycle Management

### @Setup Phases

The `@Setup` annotation specifies when state initialization occurs. All data generation should happen in `@Setup(Level.Trial)` to avoid timing overhead.

#### @Setup(Level.Trial) - Per Trial Initialization

**Timing:** Executed once per trial, **before** timing measurements begin

**Purpose:** Generate test data that will be used across all iterations of a trial

**Characteristics:**
- **Not included in benchmark timing**
- Data is generated once and reused
- Ideal for deterministic data generation
- Eliminates per-iteration overhead

**Example:**
```java
@Setup(Level.Trial)
public void setupTrial() {
    // Generate data ONCE per trial
    smallVector = generateVector(1000, 10000);
    mediumVector = generateVector(5000, 10000);
    largeVector = generateVector(10000, 10000);
}
```

**When to Use:**
- ✅ Generating fresh data for each trial
- ✅ Initializing state that doesn't change during iterations
- ✅ All scenarios in this benchmark suite

#### @Setup(Level.Iteration) - Per Iteration Initialization

**Timing:** Executed before each iteration, **not included** in benchmark timing

**Characteristics:**
- Higher overhead (repeated per iteration)
- Use only if data must be reset between iterations
- Acceptable for single-iteration benchmarks

**When NOT to Use (for this suite):**
- ❌ Avoid for data generation (too expensive)
- ❌ Reserved for state reset only if benchmark modifies data

#### @Setup(Level.Invocation) - Per Method Invocation

**Timing:** Executed immediately before each benchmark method call

**Characteristics:**
- Extremely expensive (called thousands/millions of times)
- Minimal use cases

**When NOT to Use:**
- ❌ Never use for data generation
- ❌ Avoid unless absolutely necessary

### @TearDown Phases

The `@TearDown` annotation specifies cleanup operations. Use sparingly.

#### @TearDown(Level.Trial) - Per Trial Cleanup

**Timing:** Executed after all iterations complete, not included in timing

**Purpose:** Clean up resources allocated during trial setup

**Example:**
```java
@TearDown(Level.Trial)
public void teardownTrial() {
    // Optional: Clear large data structures for garbage collection
    smallVector = null;
    mediumVector = null;
    largeVector = null;
}
```

**When to Use:**
- ✅ Closing file handles or connections
- ✅ Releasing significant memory resources
- ❌ Not required for this suite (data is simple Java objects)

#### @TearDown(Level.Iteration) - Per Iteration Cleanup

**Characteristics:**
- Called after each iteration
- Minimal impact on timing

**Note:** Not typically needed for this suite.

---

## Reproducibility and Seeding Strategy

### Seeded Random for Deterministic Data Generation

**Problem:** `new Random()` generates different data on each run, making results non-reproducible.

**Solution:** Use `new Random(12345)` with a fixed seed.

**Why seed=12345?**
- Fixed, memorable seed value
- Arbitrary but consistent across all benchmarks
- Ensures identical data generation across multiple runs
- Enables fair performance comparisons

### Seeding Implementation Pattern

**Correct Implementation:**
```java
@Setup(Level.Trial)
public void setupTrial() {
    Random random = new Random(12345L); // Fixed seed for reproducibility
    
    smallVector = new Vector<>();
    for (int i = 0; i < 1000; i++) {
        smallVector.add(random.nextInt(10000));
    }
}
```

**Why This Works:**
1. Same seed → same sequence of random numbers
2. Every benchmark run generates identical data
3. Performance variations are due to system factors, not data differences
4. Results are comparable across machines and time

**Alternatives Considered:**
- ❌ `new Random()` - Non-deterministic, not reproducible
- ❌ `ThreadLocalRandom.current()` - Seeding not supported, non-deterministic
- ✅ `new Random(seed)` - Deterministic, reproducible, simple

### Seed Usage Across Benchmarks

**All benchmarks use the same seed (12345):**

```java
private Random random = new Random(12345L);

@Setup(Level.Trial)
public void setupTrial() {
    // All vector/list generation uses random with seed 12345
    Vector<Integer> data = generateVectorWithSeededRandom(1000, 10000);
}

private Vector<Integer> generateVectorWithSeededRandom(int size, int maxValue) {
    Vector<Integer> result = new Vector<>();
    for (int i = 0; i < size; i++) {
        result.add(random.nextInt(maxValue));
    }
    return result;
}
```

**Benefits of Uniform Seeding:**
- Consistent baseline data across all benchmarks
- Deterministic comparison between related benchmarks
- Example: Sort(1000) and DutchFlagPartition(1000) use identical data

---

## Data Generation Patterns

This section details how to generate test data for each data structure type, following the Scenario B workload specification.

### Vector Data Generation Pattern

**Data Structure:** `Vector<Integer>`
**Scope:** Algorithms.Sort, Algorithms.Primes, DataStructures.DsVector categories
**Sizes:** 1K, 5K, 10K elements

**Pattern:**
```java
@State(Scope.Benchmark)
public class VectorBenchmark {
    private Vector<Integer> smallVector;
    private Vector<Integer> mediumVector;
    private Vector<Integer> largeVector;
    
    @Setup(Level.Trial)
    public void setupTrial() {
        Random random = new Random(12345L);
        
        // Small: 1K elements, range [0, 10000)
        smallVector = new Vector<>();
        for (int i = 0; i < 1000; i++) {
            smallVector.add(random.nextInt(10000));
        }
        
        // Medium: 5K elements, range [0, 10000)
        mediumVector = new Vector<>();
        for (int i = 0; i < 5000; i++) {
            mediumVector.add(random.nextInt(10000));
        }
        
        // Large: 10K elements, range [0, 10000)
        largeVector = new Vector<>();
        for (int i = 0; i < 10000; i++) {
            largeVector.add(random.nextInt(10000));
        }
    }
}
```

**Characteristics:**
- Range: [0, 10000) for standard sorting/partitioning
- Alternative Range: [0, 100000) for algorithms requiring wider distribution (e.g., MaxN)
- All generated in `@Setup(Level.Trial)` - **not** in benchmark method
- Deterministic sequence ensured by seed=12345

**Memory Layout:**
- **1K Vector:** ~8 KB (32-bit integers + references)
- **5K Vector:** ~40 KB
- **10K Vector:** ~80 KB
- All fit comfortably in L2-L3 cache

### ArrayList Data Generation Pattern

**Note:** For performance reasons, generation uses ArrayList internally, then converts to Vector if required.

**Pattern:**
```java
private Vector<Integer> generateVector(int size, int maxValue) {
    Random random = new Random(12345L);
    ArrayList<Integer> list = new ArrayList<>(size);
    for (int i = 0; i < size; i++) {
        list.add(random.nextInt(maxValue));
    }
    return new Vector<>(list);
}
```

**Rationale:**
- ArrayList has better performance during generation
- Vector is the benchmark target data structure
- Conversion happens in setup, not in timed code

### LinkedList Data Generation Pattern

**Data Structure:** `LinkedList<Integer>`
**Scope:** DataStructures.DsLinkedList category
**Sizes:** 1K, 5K, 10K elements

**Pattern:**
```java
@State(Scope.Benchmark)
public class LinkedListBenchmark {
    private LinkedList<Integer> smallList;
    private LinkedList<Integer> mediumList;
    private LinkedList<Integer> largeList;
    
    @Setup(Level.Trial)
    public void setupTrial() {
        Random random = new Random(12345L);
        
        // Small: 1K elements
        smallList = new LinkedList<>();
        for (int i = 0; i < 1000; i++) {
            smallList.add(random.nextInt(10000));
        }
        
        // Medium: 5K elements
        mediumList = new LinkedList<>();
        for (int i = 0; i < 5000; i++) {
            mediumList.add(random.nextInt(10000));
        }
        
        // Large: 10K elements
        largeList = new LinkedList<>();
        for (int i = 0; i < 10000; i++) {
            largeList.add(random.nextInt(10000));
        }
    }
}
```

**Characteristics:**
- LinkedList uses node-based storage
- Memory overhead: ~2 references per node (~16-24 bytes per element)
- Cache-unfriendly for sequential access
- **Not** reused or modified during benchmark (data immutability)

### Primitive Array Data Generation Pattern

**Data Structure:** `int[]`
**Scope:** Control flow benchmarks
**Sizes:** 1K, 5K, 10K elements

**Pattern:**
```java
@State(Scope.Benchmark)
public class ControlBenchmark {
    private int[] smallArray;
    private int[] mediumArray;
    private int[] largeArray;
    
    @Setup(Level.Trial)
    public void setupTrial() {
        Random random = new Random(12345L);
        
        // Small: 1K elements
        smallArray = new int[1000];
        for (int i = 0; i < 1000; i++) {
            smallArray[i] = random.nextInt(10000);
        }
        
        // Medium: 5K elements
        mediumArray = new int[5000];
        for (int i = 0; i < 5000; i++) {
            mediumArray[i] = random.nextInt(10000);
        }
        
        // Large: 10K elements
        largeArray = new int[10000];
        for (int i = 0; i < 10000; i++) {
            largeArray[i] = random.nextInt(10000);
        }
    }
}
```

**Characteristics:**
- Most memory-efficient (no reference overhead)
- Cache-friendly (contiguous memory)
- **1K Array:** ~4 KB (4 bytes per int × 1000)
- **5K Array:** ~20 KB
- **10K Array:** ~40 KB

### Matrix/2D Array Data Generation Pattern

**Data Structure:** `int[][]` or `Vector<Vector<Integer>>`
**Scope:** Future advanced benchmarks (not in current suite)

**Pattern for 2D Array:**
```java
@State(Scope.Benchmark)
public class MatrixBenchmark {
    private int[][] smallMatrix;
    private int[][] mediumMatrix;
    
    @Setup(Level.Trial)
    public void setupTrial() {
        Random random = new Random(12345L);
        
        // Small: 100×100 matrix
        smallMatrix = new int[100][100];
        for (int i = 0; i < 100; i++) {
            for (int j = 0; j < 100; j++) {
                smallMatrix[i][j] = random.nextInt(1000);
            }
        }
        
        // Medium: 500×500 matrix
        mediumMatrix = new int[500][500];
        for (int i = 0; i < 500; i++) {
            for (int j = 0; j < 500; j++) {
                mediumMatrix[i][j] = random.nextInt(1000);
            }
        }
    }
}
```

**Note:** 2D arrays require careful iteration order for cache efficiency (row-major order in Java).

### Specialized Distribution Patterns

#### Seeded Random with Restricted Ranges

**For algorithms requiring specific distributions:**

```java
// MaxN requires wider range: [0, 100000)
private Vector<Integer> generateWideRangeVector(int size) {
    Random random = new Random(12345L);
    Vector<Integer> result = new Vector<>();
    for (int i = 0; i < size; i++) {
        result.add(random.nextInt(100000)); // Wider range
    }
    return result;
}

// Standard range for sort/partition: [0, 10000)
private Vector<Integer> generateStandardRangeVector(int size) {
    Random random = new Random(12345L);
    Vector<Integer> result = new Vector<>();
    for (int i = 0; i < size; i++) {
        result.add(random.nextInt(10000)); // Standard range
    }
    return result;
}
```

**Rationale:**
- Different algorithms have different optimal ranges
- Wider ranges (100K) reduce duplicate probability for algorithms like MaxN
- Standard ranges (10K) are sufficient for sorting and searching
- See benchmark-design.md for specific algorithm requirements

---

## Naming Conventions

Consistent naming enables quick identification of data size and type across all benchmarks.

### Variable Naming Pattern: `{size}{DataType}`

**Format:** `{size prefix}{CamelCaseType}`

**Size Prefixes:**
- `small` - 1K elements (1,000)
- `medium` - 5K elements (5,000)
- `large` - 10K elements (10,000)

**Type Suffixes:**
- `Vector` - `Vector<Integer>`
- `List` - `LinkedList<Integer>`
- `Array` - `int[]` or `Integer[]`
- `Matrix` - `int[][]`

### Examples

**Vector Data:**
```java
@State(Scope.Benchmark)
public class SortBenchmark {
    private Vector<Integer> smallVector;    // 1K Vector<Integer>
    private Vector<Integer> mediumVector;   // 5K Vector<Integer>
    private Vector<Integer> largeVector;    // 10K Vector<Integer>
}
```

**Array Data:**
```java
@State(Scope.Benchmark)
public class ArrayBenchmark {
    private int[] smallArray;               // 1K int[]
    private int[] mediumArray;              // 5K int[]
    private int[] largeArray;               // 10K int[]
}
```

**LinkedList Data:**
```java
@State(Scope.Benchmark)
public class LinkedListBenchmark {
    private LinkedList<Integer> smallList;  // 1K LinkedList<Integer>
    private LinkedList<Integer> mediumList; // 5K LinkedList<Integer>
    private LinkedList<Integer> largeList;  // 10K LinkedList<Integer>
}
```

**Multiple Variant Data:**
```java
@State(Scope.Benchmark)
public class MaxNBenchmark {
    // Size variants
    private Vector<Integer> smallVector;
    private Vector<Integer> mediumVector;
    private Vector<Integer> largeVector;
    
    // No need for separate 10, 100, 1000 variants (single vector with multiple n values)
}
```

### Field Ordering Conventions

**Best Practice:** Order fields by category and size:
```java
@State(Scope.Benchmark)
public class ComprehensiveBenchmark {
    // Vector data (by size)
    private Vector<Integer> smallVector;
    private Vector<Integer> mediumVector;
    private Vector<Integer> largeVector;
    
    // List data (by size)
    private LinkedList<Integer> smallList;
    private LinkedList<Integer> mediumList;
    private LinkedList<Integer> largeList;
    
    // Array data (by size)
    private int[] smallArray;
    private int[] mediumArray;
    private int[] largeArray;
}
```

---

## Data Immutability Strategies

Data immutability is critical to ensure benchmark fairness and correctness. Each benchmark iteration should operate on identical data.

### Strategy 1: Read-Only State

**Best Practice for most benchmarks:** Generate data once, read-only access in benchmark methods.

**Example:**
```java
@State(Scope.Benchmark)
public class ReadOnlyBenchmark {
    private Vector<Integer> dataVector;
    
    @Setup(Level.Trial)
    public void setupTrial() {
        Random random = new Random(12345L);
        dataVector = new Vector<>();
        for (int i = 0; i < 5000; i++) {
            dataVector.add(random.nextInt(10000));
        }
    }
    
    @Benchmark
    public int benchmark() {
        // GOOD: Read-only access
        int sum = 0;
        for (Integer value : dataVector) {
            sum += value;
        }
        return sum;
    }
}
```

**Advantages:**
- No reset logic needed
- Data remains consistent across iterations
- Minimal memory overhead
- Predictable garbage collection

### Strategy 2: Copy-on-Benchmark for Destructive Operations

**Use when:** Benchmark modifies data (e.g., sorting mutates vector in-place)

**Pattern:**
```java
@State(Scope.Benchmark)
public class CopyBenchmark {
    private Vector<Integer> originalData;
    private Vector<Integer> workingCopy;
    
    @Setup(Level.Trial)
    public void setupTrial() {
        Random random = new Random(12345L);
        originalData = new Vector<>();
        for (int i = 0; i < 5000; i++) {
            originalData.add(random.nextInt(10000));
        }
    }
    
    @Setup(Level.Iteration)
    public void setupIteration() {
        // Create fresh copy before each iteration
        workingCopy = new Vector<>(originalData);
    }
    
    @Benchmark
    public void benchmark() {
        // GOOD: Modifies copy, not original
        Collections.sort(workingCopy);
    }
}
```

**Characteristics:**
- Original data preserved across iterations
- Copy created fresh per iteration (@Setup(Level.Iteration))
- Slightly higher overhead but ensures correctness
- Used for in-place algorithms (Sort, Partition, etc.)

**Cost Analysis:**
- Copy overhead: O(n) time, O(n) space
- Acceptable when benchmark algorithm is also O(n) or worse
- For sorting (O(n log n)): copy is ~10% overhead → acceptable trade-off

### Strategy 3: Immutable Wrapper Pattern

**Use when:** Prevent accidental modification while maintaining performance

**Example:**
```java
@State(Scope.Benchmark)
public class ImmutableBenchmark {
    private final Vector<Integer> dataVector;
    
    public ImmutableBenchmark() {
        // Initialize once (not in @Setup, but can be)
        Random random = new Random(12345L);
        dataVector = new Vector<>();
        for (int i = 0; i < 5000; i++) {
            dataVector.add(random.nextInt(10000));
        }
    }
    
    public Vector<Integer> getData() {
        // Prevent modification by returning unmodifiable view
        return Collections.unmodifiableVector(dataVector);
    }
    
    @Benchmark
    public int benchmark() {
        // Can't accidentally modify protected view
        Vector<Integer> data = getData();
        return data.stream().mapToInt(Integer::intValue).sum();
    }
}
```

**Advantages:**
- Compile-time enforcement (if data is final)
- Runtime protection (Collections.unmodifiableX)
- No performance penalty

### Strategy 4: Regeneration Pattern (Not Recommended)

**When to use:** Only if memory constraints are severe (rare)

```java
@State(Scope.Benchmark)
public class RegenerationBenchmark {
    private Random random;
    private int dataSize;
    
    @Setup(Level.Trial)
    public void setupTrial() {
        random = new Random(12345L);
        dataSize = 5000;
    }
    
    @Setup(Level.Iteration)
    public void setupIteration() {
        // Regenerate data before each iteration
        // Re-initialize random to same seed for reproducibility
        random = new Random(12345L);
    }
    
    @Benchmark
    public int benchmark() {
        // AVOID: Generating data in benchmark
        Vector<Integer> data = new Vector<>();
        for (int i = 0; i < dataSize; i++) {
            data.add(random.nextInt(10000));
        }
        return data.size();
    }
}
```

**Disadvantages:**
- ❌ Higher overhead (generation every iteration)
- ❌ Mixes setup with timing (unclear cost attribution)
- ❌ Violates best practice: benchmark methods should contain only tested code
- Only acceptable if data set is very small and memory is critical

### Immutability Anti-Patterns (What NOT to Do)

**❌ Anti-Pattern 1: Generating Data in Benchmark Method**
```java
@Benchmark
public Vector<Integer> bad_benchmark() {
    // WRONG: Data generation in timed code
    Vector<Integer> data = new Vector<>();
    for (int i = 0; i < 5000; i++) {
        data.add(random.nextInt(10000));
    }
    // Time includes data generation, not target algorithm
    return data;
}
```

**❌ Anti-Pattern 2: Modifying Shared Data Without Copies**
```java
@Benchmark
public void bad_sort_benchmark() {
    // WRONG: Sorting shared data mutates it
    Collections.sort(sharedVector);
    // Second iteration gets pre-sorted data → unfair comparison
}
```

**❌ Anti-Pattern 3: Non-Deterministic Setup**
```java
@Setup(Level.Trial)
public void bad_setup() {
    // WRONG: No seed, different data every run
    Random random = new Random(); // No seed!
    data = generateData(random);
    // Each run has different data → non-reproducible results
}
```

**❌ Anti-Pattern 4: Excessive @Setup(Level.Iteration)**
```java
@Setup(Level.Iteration)
public void bad_setup_every_iteration() {
    // WRONG: Heavy work before each benchmark call
    // This adds overhead to every measurement
    data = expensiveDataGeneration();
}
```

---

## Example State Class Implementations

This section provides complete, copy-paste-ready implementations for each benchmark category.

### Example 1: Sort Benchmark State

**File:** `SortBenchmark.java`

```java
package benchmarks;

import java.util.*;
import org.openjdk.jmh.annotations.*;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(value = 1, jvmArgs = "-Xmx2g")
public class SortBenchmark {
    
    // ============================================================================
    // STATE FIELDS: Data containers for different sizes
    // ============================================================================
    
    // Small size: 1,000 elements
    private Vector<Integer> smallVector;
    
    // Medium size: 5,000 elements
    private Vector<Integer> mediumVector;
    
    // Large size: 10,000 elements
    private Vector<Integer> largeVector;
    
    // Working copies for destructive operations
    private Vector<Integer> smallVectorCopy;
    private Vector<Integer> mediumVectorCopy;
    private Vector<Integer> largeVectorCopy;
    
    // ============================================================================
    // SETUP LIFECYCLE: @Setup(Level.Trial) - ONE TIME PER TRIAL
    // ============================================================================
    
    /**
     * Initialize all test data once per trial.
     * 
     * This method is called BEFORE benchmark timing begins.
     * Setup cost is NOT included in benchmark measurements.
     * 
     * Design Rationale:
     * - Generate all data once, reuse across iterations
     * - Use fixed seed (12345) for reproducible data
     * - Create working copies for in-place algorithms
     */
    @Setup(Level.Trial)
    public void setupTrial() {
        // Use fixed seed for reproducible data generation
        Random random = new Random(12345L);
        
        // ---- Small Vector: 1,000 elements, range [0, 10000) ----
        smallVector = new Vector<>();
        for (int i = 0; i < 1000; i++) {
            smallVector.add(random.nextInt(10000));
        }
        smallVectorCopy = new Vector<>(smallVector);
        
        // ---- Medium Vector: 5,000 elements, range [0, 10000) ----
        mediumVector = new Vector<>();
        for (int i = 0; i < 5000; i++) {
            mediumVector.add(random.nextInt(10000));
        }
        mediumVectorCopy = new Vector<>(mediumVector);
        
        // ---- Large Vector: 10,000 elements, range [0, 10000) ----
        largeVector = new Vector<>();
        for (int i = 0; i < 10000; i++) {
            largeVector.add(random.nextInt(10000));
        }
        largeVectorCopy = new Vector<>(largeVector);
    }
    
    // ============================================================================
    // SETUP LIFECYCLE: @Setup(Level.Iteration) - PER ITERATION
    // ============================================================================
    
    /**
     * Reset working copies before each iteration.
     * 
     * This is necessary because sort() modifies vectors in-place.
     * By resetting from the original data, each iteration operates on
     * identical input → fair performance measurements.
     * 
     * Cost: ~5% overhead per iteration (copying is O(n), sort is O(n log n))
     */
    @Setup(Level.Iteration)
    public void setupIteration() {
        // Reset copies to original state
        smallVectorCopy.clear();
        smallVectorCopy.addAll(smallVector);
        
        mediumVectorCopy.clear();
        mediumVectorCopy.addAll(mediumVector);
        
        largeVectorCopy.clear();
        largeVectorCopy.addAll(largeVector);
    }
    
    // ============================================================================
    // TEARDOWN LIFECYCLE: Optional cleanup
    // ============================================================================
    
    /**
     * Optional cleanup after all iterations complete.
     * Not strictly necessary for this benchmark (simple Java objects).
     * Include for completeness and future resource management.
     */
    @TearDown(Level.Trial)
    public void teardownTrial() {
        // Clear references for garbage collection
        smallVector = null;
        mediumVector = null;
        largeVector = null;
        smallVectorCopy = null;
        mediumVectorCopy = null;
        largeVectorCopy = null;
    }
    
    // ============================================================================
    // BENCHMARK METHODS: Only the target operation - NO setup code
    // ============================================================================
    
    /**
     * Benchmark: Sort small vector (1,000 elements)
     * Expected: 0.04-0.08 ms
     */
    @Benchmark
    public void sortSmall() {
        Collections.sort(smallVectorCopy);
    }
    
    /**
     * Benchmark: Sort medium vector (5,000 elements)
     * Expected: 0.25-0.45 ms
     */
    @Benchmark
    public void sortMedium() {
        Collections.sort(mediumVectorCopy);
    }
    
    /**
     * Benchmark: Sort large vector (10,000 elements)
     * Expected: 0.55-0.95 ms
     */
    @Benchmark
    public void sortLarge() {
        Collections.sort(largeVectorCopy);
    }
}
```

**Key Design Points:**
1. **State Scope:** `@State(Scope.Benchmark)` - shared across all threads
2. **Setup Level:** `@Setup(Level.Trial)` - data generated once per trial
3. **Seeding:** `new Random(12345L)` - identical data every run
4. **Naming:** `{size}{Type}` pattern (smallVector, mediumVector, largeVector)
5. **Copies:** Original data preserved; copies reset per iteration
6. **Benchmark Methods:** Only the operation being tested (sort)

---

### Example 2: Prime Number Benchmark State

**File:** `PrimesBenchmark.java`

```java
package benchmarks;

import java.util.*;
import org.openjdk.jmh.annotations.*;
import algorithms.Primes;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(value = 1, jvmArgs = "-Xmx2g")
public class PrimesBenchmark {
    
    // ============================================================================
    // STATE FIELDS: Data for prime algorithms (read-only, no copies needed)
    // ============================================================================
    
    // No mutable data needed - algorithms take int/Vector as parameters
    // Parameters are generated per-benchmark as needed
    private int[] testNumbers;
    private int[] largeNumbers;
    
    // ============================================================================
    // SETUP LIFECYCLE
    // ============================================================================
    
    @Setup(Level.Trial)
    public void setupTrial() {
        Random random = new Random(12345L);
        
        // Test numbers for IsPrime: [1, 10000]
        testNumbers = new int[100];
        for (int i = 0; i < 100; i++) {
            testNumbers[i] = random.nextInt(10000);
        }
        
        // Large numbers: [10000, 100000]
        largeNumbers = new int[50];
        for (int i = 0; i < 50; i++) {
            largeNumbers[i] = 10000 + random.nextInt(90000);
        }
    }
    
    @TearDown(Level.Trial)
    public void teardownTrial() {
        testNumbers = null;
        largeNumbers = null;
    }
    
    // ============================================================================
    // BENCHMARK METHODS: Read-only state access
    // ============================================================================
    
    /**
     * Benchmark: Check if 1000 is prime
     * Time Complexity: O(sqrt(n)) trial division
     */
    @Benchmark
    public boolean isPrime1000() {
        return Primes.isPrime(1000);
    }
    
    /**
     * Benchmark: Sum of all primes up to 1000
     * Time Complexity: O(n) with optimized sieve
     */
    @Benchmark
    public int sumPrimes1000() {
        return Primes.sumPrimes(1000);
    }
    
    /**
     * Benchmark: Prime factors of 24
     * Time Complexity: O(sqrt(n))
     */
    @Benchmark
    public Vector<Integer> primeFactors24() {
        return Primes.primeFactors(24);
    }
}
```

**Key Design Points:**
1. **Read-Only Pattern:** No data modification, no copies needed
2. **Minimal State:** Only test numbers for algorithms that need them
3. **Seed Usage:** Fixed seed for test number generation
4. **Benchmark Methods:** Pure algorithm invocations

---

### Example 3: DataStructures.DsVector Benchmark State

**File:** `DsVectorBenchmark.java`

```java
package benchmarks;

import java.util.*;
import org.openjdk.jmh.annotations.*;
import datastructures.DsVector;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(value = 1, jvmArgs = "-Xmx2g")
public class DsVectorBenchmark {
    
    // ============================================================================
    // STATE FIELDS: Test data in different sizes
    // ============================================================================
    
    private Vector<Integer> smallVector;
    private Vector<Integer> mediumVector;
    private Vector<Integer> largeVector;
    
    // For search: need data with specific values at known positions
    private Vector<Integer> searchSmallVector;
    private Vector<Integer> searchMediumVector;
    private Vector<Integer> searchLargeVector;
    
    // Values to search for (must exist in vectors)
    private static final int SEARCH_VALUE = 5000;
    
    // ============================================================================
    // SETUP LIFECYCLE
    // ============================================================================
    
    @Setup(Level.Trial)
    public void setupTrial() {
        Random random = new Random(12345L);
        
        // --- Standard vectors for modification benchmark ---
        smallVector = generateVector(1000, 10000, random);
        mediumVector = generateVector(5000, 10000, random);
        largeVector = generateVector(10000, 10000, random);
        
        // --- Vectors for search benchmark (ensure search value exists) ---
        searchSmallVector = generateVectorWithSearchValue(1000, 10000, random);
        searchMediumVector = generateVectorWithSearchValue(5000, 10000, random);
        searchLargeVector = generateVectorWithSearchValue(10000, 10000, random);
    }
    
    @TearDown(Level.Trial)
    public void teardownTrial() {
        smallVector = null;
        mediumVector = null;
        largeVector = null;
        searchSmallVector = null;
        searchMediumVector = null;
        searchLargeVector = null;
    }
    
    // ============================================================================
    // HELPER METHODS: Data generation with seeded random
    // ============================================================================
    
    /**
     * Generate a random vector with seeded Random.
     * 
     * @param size Number of elements
     * @param maxValue Maximum value (exclusive) for random integers
     * @param random Seeded Random instance
     * @return Vector<Integer> with random data
     */
    private Vector<Integer> generateVector(int size, int maxValue, Random random) {
        Vector<Integer> result = new Vector<>();
        for (int i = 0; i < size; i++) {
            result.add(random.nextInt(maxValue));
        }
        return result;
    }
    
    /**
     * Generate vector that contains SEARCH_VALUE (for correctness).
     * 
     * Ensures search benchmark has at least one match.
     */
    private Vector<Integer> generateVectorWithSearchValue(int size, int maxValue, Random random) {
        Vector<Integer> result = new Vector<>();
        for (int i = 0; i < size; i++) {
            result.add(random.nextInt(maxValue));
        }
        // Ensure search value exists at multiple positions
        result.set(0, SEARCH_VALUE);
        result.set(size / 2, SEARCH_VALUE);
        result.set(size - 1, SEARCH_VALUE);
        return result;
    }
    
    // ============================================================================
    // BENCHMARK METHODS
    // ============================================================================
    
    @Benchmark
    public Vector<Integer> modifySmall() {
        return DsVector.modifyVector(smallVector);
    }
    
    @Benchmark
    public Vector<Integer> modifyMedium() {
        return DsVector.modifyVector(mediumVector);
    }
    
    @Benchmark
    public Vector<Integer> modifyLarge() {
        return DsVector.modifyVector(largeVector);
    }
    
    @Benchmark
    public Vector<Integer> searchSmall() {
        return DsVector.searchVector(searchSmallVector, SEARCH_VALUE);
    }
    
    @Benchmark
    public Vector<Integer> searchMedium() {
        return DsVector.searchVector(searchMediumVector, SEARCH_VALUE);
    }
    
    @Benchmark
    public Vector<Integer> searchLarge() {
        return DsVector.searchVector(searchLargeVector, SEARCH_VALUE);
    }
}
```

**Key Design Points:**
1. **Data Specialization:** Different data for modify vs. search
2. **Seed Consistency:** All vectors use seed=12345L
3. **Search Value Guarantee:** Vectors contain SEARCH_VALUE at known positions
4. **Helper Methods:** Encapsulate data generation logic

---

## Best Practices and Checklist

### Pre-Implementation Checklist

Before writing benchmark state classes:

- [ ] **Identify Data Structures:** What data types does the algorithm operate on?
  - [ ] Vector<T>?
  - [ ] LinkedList<T>?
  - [ ] Primitive arrays (int[], double[])?
  - [ ] Custom objects?

- [ ] **Determine Sizes:** What input sizes should be tested?
  - [ ] Small (1K)?
  - [ ] Medium (5K)?
  - [ ] Large (10K)?
  - [ ] Other?

- [ ] **Check for Mutation:** Does the benchmark algorithm modify input data?
  - [ ] If YES: Use copies and @Setup(Level.Iteration)
  - [ ] If NO: Use read-only state, no copies needed

- [ ] **Select Scope:** Which JMH scope is appropriate?
  - [ ] Benchmark scope (shared, default): For single-threaded workloads
  - [ ] Thread scope: For concurrent benchmarks (rare)

- [ ] **Plan Seeding:** How to ensure reproducibility?
  - [ ] Use Random(12345L) for all data generation
  - [ ] Document seed value in state class

### Implementation Checklist

While implementing state classes:

- [ ] **Add State Annotation:** `@State(Scope.Benchmark)`

- [ ] **Declare Fields:** Use naming convention `{size}{Type}`
  - [ ] smallVector, mediumVector, largeVector
  - [ ] smallArray, mediumArray, largeArray
  - [ ] smallList, mediumList, largeList

- [ ] **Implement @Setup(Level.Trial):**
  - [ ] Create Random with seed 12345L
  - [ ] Generate all required data structures
  - [ ] For mutable operations, create working copies

- [ ] **Implement @Setup(Level.Iteration) if needed:**
  - [ ] Reset working copies from originals
  - [ ] Ensure same initial state for each iteration

- [ ] **Implement @TearDown(Level.Trial):**
  - [ ] Clear references (optional but recommended)
  - [ ] Close resources if any

- [ ] **Write Benchmark Methods:**
  - [ ] No data generation code
  - [ ] No setup logic (that's in @Setup)
  - [ ] Only the target algorithm/operation
  - [ ] Named descriptively (e.g., sortLarge)

### Testing and Validation Checklist

After implementation:

- [ ] **Data Consistency:** Verify data is identical across runs
  - [ ] Run same benchmark twice, compare input data
  - [ ] Seed should produce identical sequences

- [ ] **Immutability:** Ensure data state is preserved
  - [ ] Run multiple iterations, verify same results
  - [ ] Check that working copies reset properly

- [ ] **Correctness:** Validate algorithm results
  - [ ] Spot-check results for correctness
  - [ ] Verify edge cases (empty, single element, etc.)

- [ ] **Performance:** Ensure benchmark is timed correctly
  - [ ] Setup time should NOT be in measurements
  - [ ] Warm-up iterations show typical behavior
  - [ ] Results are consistent (low variance)

- [ ] **Memory:** Check for excessive memory usage
  - [ ] Monitor heap size during execution
  - [ ] No unbounded growth across iterations

- [ ] **Documentation:** Document state class design
  - [ ] Why State.Benchmark vs Thread?
  - [ ] Why these specific data sizes?
  - [ ] How are working copies managed?
  - [ ] What seed is used and why?

### Common Pitfalls and How to Avoid Them

**Pitfall 1: Generating Data in Benchmark Method**
```java
// ❌ WRONG
@Benchmark
public Vector<Integer> bad() {
    Vector<Integer> data = generateData(); // Timed!
    return algorithm(data);
}

// ✅ CORRECT
@Benchmark
public Vector<Integer> good() {
    return algorithm(stateVector); // Only algorithm is timed
}
```

**Pitfall 2: Modifying Shared Data Without Copies**
```java
// ❌ WRONG
@Benchmark
public void bad_sort() {
    Collections.sort(sharedVector); // Mutates shared state!
}

// ✅ CORRECT
@Setup(Level.Iteration)
public void setup() {
    workingCopy = new Vector<>(sharedVector); // Reset before each iteration
}

@Benchmark
public void good_sort() {
    Collections.sort(workingCopy); // Mutates copy, preserves original
}
```

**Pitfall 3: Non-Deterministic Seed**
```java
// ❌ WRONG
private Random random = new Random(); // No seed, non-reproducible

// ✅ CORRECT
private Random random = new Random(12345L); // Fixed seed, reproducible
```

**Pitfall 4: Excessive Setup Work**
```java
// ❌ WRONG
@Setup(Level.Iteration)
public void bad_setup() {
    data = expensiveAlgorithm(); // Runs before every iteration!
}

// ✅ CORRECT
@Setup(Level.Trial)
public void good_setup() {
    data = expensiveAlgorithm(); // Runs once per trial
}
```

**Pitfall 5: Forgetting @TearDown**
```java
// Not wrong, but suboptimal
@Setup(Level.Trial)
public void setup() {
    largeData = new byte[100_000_000];
}
// Memory not released until JVM exit

// ✅ BETTER
@TearDown(Level.Trial)
public void teardown() {
    largeData = null; // Hint for GC
}
```

---

## Summary

This document establishes the design patterns for all benchmark state classes in the Scenario B suite:

**Key Takeaways:**

1. **Use `@State(Scope.Benchmark)`** for shared, single-threaded state
2. **Generate data in `@Setup(Level.Trial)`** - once per trial, outside timing
3. **Use `new Random(12345L)`** for deterministic, reproducible data
4. **Name variables as `{size}{Type}`:** smallVector, mediumArray, largeList
5. **Create working copies** for algorithms that mutate data
6. **Keep benchmark methods clean** - only the target operation
7. **Document the design** - why these choices for each benchmark

**Benefits:**
- ✅ Reproducible results across runs
- ✅ Fair performance measurements
- ✅ Minimal overhead in timed code
- ✅ Clear, maintainable code
- ✅ Consistent practices across all 16 benchmarks

---

**Document Version:** 1.0  
**Last Updated:** 2024  
**Status:** Comprehensive design guide for JMH state classes and data generation strategy
