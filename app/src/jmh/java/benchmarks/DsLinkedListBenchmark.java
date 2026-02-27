package benchmarks;

import java.util.LinkedList;
import java.util.Random;
import org.openjdk.jmh.annotations.*;
import datastructures.DsLinkedList;

/**
 * JMH benchmarks for the DsLinkedList module with linked list operations.
 * Tests shuffle and slice operations across three workload sizes (1K, 5K, 10K elements).
 * Demonstrates the reusable state class pattern for efficient benchmark data management.
 * 
 * STATE CLASS PATTERN:
 * - Use @State(Scope.Thread) for thread-local state: each thread manages independent state
 * - Implement @Setup(Level.Trial) to initialize data structures once per trial
 * - Trial-level setup runs once before all iterations (warmup + measurement)
 * - Generate all test linked lists in @Setup, not in @Benchmark methods
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
 * 
 * BENCHMARK COVERAGE:
 * - benchmarkShuffle: Tests randomized list shuffling (O(n))
 * - benchmarkSlice: Tests list slicing with varied start/end indices (O(n))
 */
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(java.util.concurrent.TimeUnit.MILLISECONDS)
@Fork(1)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
public class DsLinkedListBenchmark {

    // ============================================================================
    // STATE FIELDS: Test data in different sizes
    // ============================================================================
    
    // LinkedLists for shuffle benchmark
    private LinkedList<Integer> shuffleSmallList;
    private LinkedList<Integer> shuffleMediumList;
    private LinkedList<Integer> shuffleLargeList;
    
    // LinkedLists for slice benchmark
    private LinkedList<Integer> sliceSmallList;
    private LinkedList<Integer> sliceMediumList;
    private LinkedList<Integer> sliceLargeList;
    
    // Constants for slice benchmark
    private static final int SLICE_SMALL_START = 100;      // Start at 10% of 1K
    private static final int SLICE_SMALL_END = 900;        // End at 90% of 1K
    private static final int SLICE_MEDIUM_START = 500;     // Start at 10% of 5K
    private static final int SLICE_MEDIUM_END = 4500;      // End at 90% of 5K
    private static final int SLICE_LARGE_START = 1000;     // Start at 10% of 10K
    private static final int SLICE_LARGE_END = 9000;       // End at 90% of 10K

    // ============================================================================
    // SETUP LIFECYCLE
    // ============================================================================
    
    /**
     * Setup method to generate test linked lists once per trial.
     * Initializes linked lists of increasing size: 1K, 5K, and 10K elements.
     * Each list contains random integers in range [0, 10000).
     * Uses seeded Random for reproducible, deterministic test data generation.
     */
    @Setup(Level.Trial)
    public void setup() {
        Random random = new Random(12345L);  // Fixed seed for reproducibility
        
        // --- LinkedLists for shuffle benchmark ---
        shuffleSmallList = generateLinkedList(1000, 10000, random);
        shuffleMediumList = generateLinkedList(5000, 10000, random);
        shuffleLargeList = generateLinkedList(10000, 10000, random);
        
        // --- LinkedLists for slice benchmark ---
        sliceSmallList = generateLinkedList(1000, 10000, random);
        sliceMediumList = generateLinkedList(5000, 10000, random);
        sliceLargeList = generateLinkedList(10000, 10000, random);
    }
    
    // ============================================================================
    // HELPER METHODS: Data generation with seeded random
    // ============================================================================
    
    /**
     * Generate a random linked list with seeded Random.
     * 
     * @param size Number of elements
     * @param maxValue Maximum value (exclusive) for random integers
     * @param random Seeded Random instance
     * @return LinkedList<Integer> with random data
     */
    private LinkedList<Integer> generateLinkedList(int size, int maxValue, Random random) {
        LinkedList<Integer> result = new LinkedList<>();
        for (int i = 0; i < size; i++) {
            result.add(random.nextInt(maxValue));
        }
        return result;
    }
    
    // ============================================================================
    // BENCHMARK METHODS: benchmarkShuffle (randomized list shuffling)
    // ============================================================================
    
    /**
     * Benchmark shuffle with small dataset (1K elements).
     * Tests randomized shuffling of a linked list.
     * Verifies correctness: size is preserved and elements are present.
     */
    @Benchmark
    public LinkedList<Integer> benchmarkShuffleSmall() {
        LinkedList<Integer> result = DsLinkedList.shuffle(shuffleSmallList);
        
        // Correctness assertion: verify size is preserved
        assert result.size() == 1000 : 
            "Shuffled list should have 1000 elements, got " + result.size();
        
        // Verify some original elements are present (all should be)
        assert result.contains(shuffleSmallList.get(0)) : 
            "Shuffled list should contain elements from original list";
        
        return result;
    }
    
    /**
     * Benchmark shuffle with medium dataset (5K elements).
     * Tests randomized shuffling of a linked list.
     * Verifies correctness: size is preserved and elements are present.
     */
    @Benchmark
    public LinkedList<Integer> benchmarkShuffleMedium() {
        LinkedList<Integer> result = DsLinkedList.shuffle(shuffleMediumList);
        
        // Correctness assertion: verify size is preserved
        assert result.size() == 5000 : 
            "Shuffled list should have 5000 elements, got " + result.size();
        
        // Verify some original elements are present (all should be)
        assert result.contains(shuffleMediumList.get(0)) : 
            "Shuffled list should contain elements from original list";
        assert result.contains(shuffleMediumList.get(2500)) : 
            "Shuffled list should contain all elements from original list";
        
        return result;
    }
    
    /**
     * Benchmark shuffle with large dataset (10K elements).
     * Tests randomized shuffling of a linked list.
     * Verifies correctness: size is preserved and elements are present.
     */
    @Benchmark
    public LinkedList<Integer> benchmarkShuffleLarge() {
        LinkedList<Integer> result = DsLinkedList.shuffle(shuffleLargeList);
        
        // Correctness assertion: verify size is preserved
        assert result.size() == 10000 : 
            "Shuffled list should have 10000 elements, got " + result.size();
        
        // Verify some original elements are present (all should be)
        assert result.contains(shuffleLargeList.get(0)) : 
            "Shuffled list should contain elements from original list";
        assert result.contains(shuffleLargeList.get(5000)) : 
            "Shuffled list should contain all elements from original list";
        
        return result;
    }
    
    // ============================================================================
    // BENCHMARK METHODS: benchmarkSlice (slice with varied indices)
    // ============================================================================
    
    /**
     * Benchmark slice with small dataset (1K elements).
     * Tests slicing from index 100 to 900 (800 elements).
     * Verifies correctness: sliced list has expected size and contains correct elements.
     */
    @Benchmark
    public LinkedList<Integer> benchmarkSliceSmall() {
        LinkedList<Integer> result = DsLinkedList.slice(sliceSmallList, SLICE_SMALL_START, SLICE_SMALL_END);
        
        // Correctness assertion: verify sliced size (900-100 = 800)
        assert result.size() == 800 : 
            "Sliced list should have 800 elements, got " + result.size();
        
        // Verify that sliced elements match original list
        assert result.get(0).equals(sliceSmallList.get(SLICE_SMALL_START)) : 
            "First element of slice should match original at start index";
        assert result.get(799).equals(sliceSmallList.get(SLICE_SMALL_END - 1)) : 
            "Last element of slice should match original at end-1 index";
        
        return result;
    }
    
    /**
     * Benchmark slice with medium dataset (5K elements).
     * Tests slicing from index 500 to 4500 (4000 elements).
     * Verifies correctness: sliced list has expected size and contains correct elements.
     */
    @Benchmark
    public LinkedList<Integer> benchmarkSliceMedium() {
        LinkedList<Integer> result = DsLinkedList.slice(sliceMediumList, SLICE_MEDIUM_START, SLICE_MEDIUM_END);
        
        // Correctness assertion: verify sliced size (4500-500 = 4000)
        assert result.size() == 4000 : 
            "Sliced list should have 4000 elements, got " + result.size();
        
        // Verify that sliced elements match original list
        assert result.get(0).equals(sliceMediumList.get(SLICE_MEDIUM_START)) : 
            "First element of slice should match original at start index";
        assert result.get(3999).equals(sliceMediumList.get(SLICE_MEDIUM_END - 1)) : 
            "Last element of slice should match original at end-1 index";
        
        return result;
    }
    
    /**
     * Benchmark slice with large dataset (10K elements).
     * Tests slicing from index 1000 to 9000 (8000 elements).
     * Verifies correctness: sliced list has expected size and contains correct elements.
     */
    @Benchmark
    public LinkedList<Integer> benchmarkSliceLarge() {
        LinkedList<Integer> result = DsLinkedList.slice(sliceLargeList, SLICE_LARGE_START, SLICE_LARGE_END);
        
        // Correctness assertion: verify sliced size (9000-1000 = 8000)
        assert result.size() == 8000 : 
            "Sliced list should have 8000 elements, got " + result.size();
        
        // Verify that sliced elements match original list
        assert result.get(0).equals(sliceLargeList.get(SLICE_LARGE_START)) : 
            "First element of slice should match original at start index";
        assert result.get(7999).equals(sliceLargeList.get(SLICE_LARGE_END - 1)) : 
            "Last element of slice should match original at end-1 index";
        
        return result;
    }
}
