package benchmarks;

import java.util.Collections;
import java.util.Random;
import java.util.Vector;
import org.openjdk.jmh.annotations.*;
import algorithms.Sort;

/**
 * JMH benchmarks for the Sort module with advanced correctness verification.
 * Measures performance of SortVector, DutchFlagPartition, and MaxN methods
 * across various input sizes and parameter values.
 * 
 * STATE CLASS PATTERN (Reusable across all benchmark classes):
 * ============================================================
 * 
 * 1. CLASS-LEVEL ANNOTATIONS:
 *    - @State(Scope.Thread): Each thread gets independent state (better for parallelism)
 *    - @BenchmarkMode(Mode.AverageTime): Measures average execution time
 *    - @OutputTimeUnit(TimeUnit.MILLISECONDS): Reports results in milliseconds
 *    - @Fork(1): Runs in separate JVM to isolate results
 *    - @Warmup and @Measurement: Controls warmup and measurement iterations
 * 
 * 2. STATE FIELDS:
 *    - Declare private fields for test data (vectors, arrays, pre-computed values)
 *    - All data structures initialized in @Setup, never in @Benchmark methods
 *    - Use seeded Random (seed=12345) for reproducible test data
 * 
 * 3. SETUP METHOD:
 *    - Use @Setup(Level.Trial): Executes once per trial (before warmup + measurement)
 *    - Initialize all data structures here: large vectors, arrays, pre-computed results
 *    - Seed Random with fixed value for deterministic, reproducible results
 *    - Keep complexity minimal; avoid expensive computations
 * 
 * 4. BENCHMARK METHODS:
 *    - Only invoke methods under test; avoid allocation/generation
 *    - Use data from state fields prepared in @Setup
 *    - Include lightweight correctness assertions to verify results
 *    - Each iteration operates on the same pre-generated data
 * 
 * 5. CORRECTNESS ASSERTIONS:
 *    - Add lightweight assertions in @Benchmark methods to verify correctness
 *    - Verify sorted output, expected sums, factor products, etc.
 *    - Assertions ensure measurements are for correct implementations
 * 
 * SCOPE OPTIONS:
 * - Scope.Thread (used here): Each thread has independent state, better parallelism
 * - Scope.Benchmark: Shared state across all threads, less optimal but simpler
 * - Scope.Group: Shared state within thread groups, rarely used
 * 
 * SETUP LEVELS:
 * - Level.Trial: Once per trial (preferred for static data setup)
 * - Level.Iteration: Once per iteration (expensive, rarely used)
 * - Level.Invocation: Before each call (too expensive for benchmarking)
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
    private Vector<Integer> vector10K;

    /**
     * Setup method to generate test vectors once per trial.
     * 
     * Key points:
     * - Uses @Setup(Level.Trial) to initialize data exactly once per trial
     * - Executes before warmup and measurement phases (outside measurement window)
     * - Seeds Random with fixed value 12345 for reproducible, deterministic results
     * - Generates three vectors of increasing size: 1K, 5K, and 10K elements
     * - Each vector contains random integers in range [0, 10000)
     * - Prevents allocation and generation costs from contaminating measurements
     * 
     * Timing Impact:
     * - Setup overhead is NOT included in benchmark measurements
     * - Each benchmark iteration reuses pre-generated data
     * - Ensures measurements reflect actual algorithm performance
     */
    @Setup(Level.Trial)
    public void setup() {
        random = new Random(12345);
        vector1K = generateVector(1000, 10000);
        vector5K = generateVector(5000, 10000);
        vector10K = generateVector(10000, 10000);
    }

    /**
     * Generates a vector of random integers with specified size and max value.
     * 
     * @param size The number of elements in the vector
     * @param maxValue The maximum value (exclusive) for random integers
     * @return A vector of random integers
     */
    private Vector<Integer> generateVector(int size, int maxValue) {
        Vector<Integer> vector = new Vector<>(size);
        for (int i = 0; i < size; i++) {
            vector.add(random.nextInt(maxValue));
        }
        return vector;
    }

    /**
     * Benchmark for SortVector with 1K elements.
     * Verifies output is sorted.
     */
    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(java.util.concurrent.TimeUnit.MILLISECONDS)
    @Fork(1)
    @Warmup(iterations = 5, time = 1)
    @Measurement(iterations = 5, time = 1)
    public void benchmarkSortVector1K() {
        Vector<Integer> v = new Vector<>(vector1K);
        Sort.SortVector(v);
        assert isSorted(v) : "Vector is not sorted after SortVector()";
    }

    /**
     * Benchmark for SortVector with 5K elements.
     * Verifies output is sorted.
     */
    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(java.util.concurrent.TimeUnit.MILLISECONDS)
    @Fork(1)
    @Warmup(iterations = 5, time = 1)
    @Measurement(iterations = 5, time = 1)
    public void benchmarkSortVector5K() {
        Vector<Integer> v = new Vector<>(vector5K);
        Sort.SortVector(v);
        assert isSorted(v) : "Vector is not sorted after SortVector()";
    }

    /**
     * Benchmark for SortVector with 10K elements.
     * Verifies output is sorted.
     */
    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(java.util.concurrent.TimeUnit.MILLISECONDS)
    @Fork(1)
    @Warmup(iterations = 5, time = 1)
    @Measurement(iterations = 5, time = 1)
    public void benchmarkSortVector10K() {
        Vector<Integer> v = new Vector<>(vector10K);
        Sort.SortVector(v);
        assert isSorted(v) : "Vector is not sorted after SortVector()";
    }

    /**
     * Benchmark for DutchFlagPartition with 1K elements and pivot 5000.
     * Verifies partitioning: all elements < pivot appear before pivot,
     * all elements > pivot appear after.
     */
    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(java.util.concurrent.TimeUnit.MILLISECONDS)
    @Fork(1)
    @Warmup(iterations = 5, time = 1)
    @Measurement(iterations = 5, time = 1)
    public void benchmarkDutchFlagPartition1K() {
        Vector<Integer> v = new Vector<>(vector1K);
        int pivotValue = 5000;
        Sort.DutchFlagPartition(v, pivotValue);
        assert isPartitioned(v, pivotValue) : 
            "Vector is not properly partitioned after DutchFlagPartition()";
    }

    /**
     * Benchmark for DutchFlagPartition with 5K elements and pivot 5000.
     * Verifies partitioning: all elements < pivot appear before pivot,
     * all elements > pivot appear after.
     */
    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(java.util.concurrent.TimeUnit.MILLISECONDS)
    @Fork(1)
    @Warmup(iterations = 5, time = 1)
    @Measurement(iterations = 5, time = 1)
    public void benchmarkDutchFlagPartition5K() {
        Vector<Integer> v = new Vector<>(vector5K);
        int pivotValue = 5000;
        Sort.DutchFlagPartition(v, pivotValue);
        assert isPartitioned(v, pivotValue) : 
            "Vector is not properly partitioned after DutchFlagPartition()";
    }

    /**
     * Benchmark for DutchFlagPartition with 10K elements and pivot 5000.
     * Verifies partitioning: all elements < pivot appear before pivot,
     * all elements > pivot appear after.
     */
    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(java.util.concurrent.TimeUnit.MILLISECONDS)
    @Fork(1)
    @Warmup(iterations = 5, time = 1)
    @Measurement(iterations = 5, time = 1)
    public void benchmarkDutchFlagPartition10K() {
        Vector<Integer> v = new Vector<>(vector10K);
        int pivotValue = 5000;
        Sort.DutchFlagPartition(v, pivotValue);
        assert isPartitioned(v, pivotValue) : 
            "Vector is not properly partitioned after DutchFlagPartition()";
    }

    /**
     * Benchmark for MaxN with 1K elements and n=10.
     * Verifies result contains exactly n elements.
     */
    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(java.util.concurrent.TimeUnit.MILLISECONDS)
    @Fork(1)
    @Warmup(iterations = 5, time = 1)
    @Measurement(iterations = 5, time = 1)
    public Vector<Integer> benchmarkMaxN1K_n10() {
        Vector<Integer> result = Sort.MaxN(vector1K, 10);
        assert result.size() == 10 : 
            "Result size should be 10 but got " + result.size();
        return result;
    }

    /**
     * Benchmark for MaxN with 5K elements and n=100.
     * Verifies result contains exactly n elements.
     */
    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(java.util.concurrent.TimeUnit.MILLISECONDS)
    @Fork(1)
    @Warmup(iterations = 5, time = 1)
    @Measurement(iterations = 5, time = 1)
    public Vector<Integer> benchmarkMaxN5K_n100() {
        Vector<Integer> result = Sort.MaxN(vector5K, 100);
        assert result.size() == 100 : 
            "Result size should be 100 but got " + result.size();
        return result;
    }

    /**
     * Benchmark for MaxN with 10K elements and n=1000.
     * Verifies result contains exactly n elements.
     */
    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(java.util.concurrent.TimeUnit.MILLISECONDS)
    @Fork(1)
    @Warmup(iterations = 5, time = 1)
    @Measurement(iterations = 5, time = 1)
    public Vector<Integer> benchmarkMaxN10K_n1000() {
        Vector<Integer> result = Sort.MaxN(vector10K, 1000);
        assert result.size() == 1000 : 
            "Result size should be 1000 but got " + result.size();
        return result;
    }

    /**
     * Helper method to check if a vector is sorted in ascending order.
     * 
     * @param v The vector to check
     * @return true if the vector is sorted, false otherwise
     */
    private boolean isSorted(Vector<Integer> v) {
        for (int i = 0; i < v.size() - 1; i++) {
            if (v.get(i) > v.get(i + 1)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Helper method to check if a vector is properly partitioned around a pivot.
     * All elements < pivot should appear before elements >= pivot.
     * 
     * @param v The vector to check
     * @param pivotValue The pivot value
     * @return true if the vector is properly partitioned, false otherwise
     */
    private boolean isPartitioned(Vector<Integer> v, int pivotValue) {
        int i = 0;
        
        // Find the first element >= pivotValue
        while (i < v.size() && v.get(i) < pivotValue) {
            i++;
        }
        
        // All remaining elements should be >= pivotValue
        while (i < v.size()) {
            if (v.get(i) < pivotValue) {
                return false;
            }
            i++;
        }
        
        return true;
    }
}
