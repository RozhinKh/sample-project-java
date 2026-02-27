package benchmarks;

import java.util.Vector;
import org.openjdk.jmh.annotations.*;
import generator.GenVector;
import algorithms.Sort;

/**
 * JMH benchmarks for the Sort module with various data sizes.
 * Demonstrates the reusable state class pattern for efficient benchmark data management.
 * 
 * STATE CLASS PATTERN:
 * - Use @State(Scope.Thread) for thread-local state: each thread manages independent state
 * - Implement @Setup(Level.Trial) to initialize data structures once per trial
 * - Trial-level setup runs once before all iterations (warmup + measurement)
 * - Generate all test vectors in @Setup, not in @Benchmark methods
 * - Benchmark methods only receive pre-generated, immutable data
 * - This pattern prevents allocation and generation costs from contaminating measurements
 * 
 * SCOPE.THREAD vs SCOPE.BENCHMARK:
 * - Scope.Thread: Each thread has independent state (used here for better parallelism)
 * - Scope.Benchmark: Shared state across all threads (not used here; less optimal)
 * 
 * SETUP LEVELS:
 * - Level.Trial: Runs once per trial (before warmup + measurement)
 * - Level.Iteration: Runs once per iteration (uncommon for performance benchmarks)
 * - Level.Invocation: Runs before each method call (too expensive, not recommended)
 */
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(java.util.concurrent.TimeUnit.MILLISECONDS)
public class SortBenchmarks {

    private Vector<Integer> small_vector;
    private Vector<Integer> medium_vector;
    private Vector<Integer> large_vector;

    /**
     * Setup method to generate test vectors once per trial.
     * Initializes three vectors of increasing size: 1K, 5K, and 10K elements.
     * Each vector contains random integers in range [0, 10000).
     * Uses GenVector utility for consistent, efficient vector generation.
     */
    @Setup(Level.Trial)
    public void setup() {
        small_vector = GenVector.generateVector(1000, 10000);
        medium_vector = GenVector.generateVector(5000, 10000);
        large_vector = GenVector.generateVector(10000, 10000);
    }

    @Benchmark
    public void sortVectorSmall() {
        Vector<Integer> v = new Vector<>(small_vector);
        Sort.SortVector(v);
    }

    @Benchmark
    public void sortVectorMedium() {
        Vector<Integer> v = new Vector<>(medium_vector);
        Sort.SortVector(v);
    }

    @Benchmark
    public void sortVectorLarge() {
        Vector<Integer> v = new Vector<>(large_vector);
        Sort.SortVector(v);
    }

    @Benchmark
    public void dutchFlagPartitionSmall() {
        Vector<Integer> v = new Vector<>(small_vector);
        Sort.DutchFlagPartition(v, 5000);
    }

    @Benchmark
    public void dutchFlagPartitionMedium() {
        Vector<Integer> v = new Vector<>(medium_vector);
        Sort.DutchFlagPartition(v, 5000);
    }

    @Benchmark
    public Vector<Integer> maxNMedium() {
        return Sort.MaxN(medium_vector, 100);
    }

    @Benchmark
    public Vector<Integer> maxNLarge() {
        return Sort.MaxN(large_vector, 100);
    }
}
