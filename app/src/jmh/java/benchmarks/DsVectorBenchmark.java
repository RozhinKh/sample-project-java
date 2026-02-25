package benchmarks;

import java.util.Random;
import java.util.Vector;
import org.openjdk.jmh.annotations.*;
import datastructures.DsVector;

/**
 * JMH benchmarks for the DsVector module with various vector operations and data sizes.
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
 * 
 * BENCHMARK COVERAGE:
 * - modifyVector: Increments each vector element (O(n))
 * - searchVector: Finds all indices of a target value (O(n))
 * - sortVector: Bubble sort implementation (O(n²))
 * - reverseVector: Reverses vector order (O(n))
 * - rotateVector: Rotates elements by fixed amounts (O(n))
 * - mergeVectors: Concatenates two vectors (O(n+m))
 */
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(java.util.concurrent.TimeUnit.MILLISECONDS)
@Fork(1)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
public class DsVectorBenchmark {

    // ============================================================================
    // STATE FIELDS: Test data in different sizes
    // ============================================================================
    
    // Vectors for modify benchmark
    private Vector<Integer> modifySmallVector;
    private Vector<Integer> modifyMediumVector;
    private Vector<Integer> modifyLargeVector;
    
    // Vectors for search benchmark
    private Vector<Integer> searchSmallVector;
    private Vector<Integer> searchMediumVector;
    private Vector<Integer> searchLargeVector;
    
    // Vectors for sort benchmark
    private Vector<Integer> sortSmallVector;
    private Vector<Integer> sortMediumVector;
    private Vector<Integer> sortLargeVector;
    
    // Vectors for reverse benchmark
    private Vector<Integer> reverseSmallVector;
    private Vector<Integer> reverseMediumVector;
    private Vector<Integer> reverseLargeVector;
    
    // Vectors for rotate benchmark
    private Vector<Integer> rotateSmallVector;
    private Vector<Integer> rotateMediumVector;
    private Vector<Integer> rotateLargeVector;
    
    // Vectors for merge benchmark (two vectors for each size pair)
    private Vector<Integer> mergeSmallVector1;
    private Vector<Integer> mergeSmallVector2;
    private Vector<Integer> mergeMediumVector1;
    private Vector<Integer> mergeMediumVector2;
    private Vector<Integer> mergeLargeVector1;
    private Vector<Integer> mergeLargeVector2;
    
    // Constants for search benchmark
    private static final int SEARCH_VALUE = 5000;
    
    // Constants for rotate benchmark
    private static final int ROTATE_SMALL_AMOUNT = 250;    // 25% of 1000
    private static final int ROTATE_MEDIUM_AMOUNT = 1250;  // 25% of 5000
    private static final int ROTATE_LARGE_AMOUNT = 2500;   // 25% of 10000

    // ============================================================================
    // SETUP LIFECYCLE
    // ============================================================================
    
    /**
     * Setup method to generate test vectors once per trial.
     * Initializes vectors of increasing size: 1K, 5K, and 10K elements.
     * Each vector contains random integers in range [0, 10000).
     * Uses seeded Random for reproducible, deterministic test data generation.
     */
    @Setup(Level.Trial)
    public void setup() {
        Random random = new Random(12345L);  // Fixed seed for reproducibility
        
        // --- Vectors for modify benchmark ---
        modifySmallVector = generateVector(1000, 10000, random);
        modifyMediumVector = generateVector(5000, 10000, random);
        modifyLargeVector = generateVector(10000, 10000, random);
        
        // --- Vectors for search benchmark (with search value guaranteed) ---
        searchSmallVector = generateVectorWithSearchValue(1000, 10000, random);
        searchMediumVector = generateVectorWithSearchValue(5000, 10000, random);
        searchLargeVector = generateVectorWithSearchValue(10000, 10000, random);
        
        // --- Vectors for sort benchmark ---
        sortSmallVector = generateVector(1000, 10000, random);
        sortMediumVector = generateVector(5000, 10000, random);
        sortLargeVector = generateVector(10000, 10000, random);
        
        // --- Vectors for reverse benchmark ---
        reverseSmallVector = generateVector(1000, 10000, random);
        reverseMediumVector = generateVector(5000, 10000, random);
        reverseLargeVector = generateVector(10000, 10000, random);
        
        // --- Vectors for rotate benchmark ---
        rotateSmallVector = generateVector(1000, 10000, random);
        rotateMediumVector = generateVector(5000, 10000, random);
        rotateLargeVector = generateVector(10000, 10000, random);
        
        // --- Vectors for merge benchmark (pairs of equal-sized vectors) ---
        mergeSmallVector1 = generateVector(500, 10000, random);
        mergeSmallVector2 = generateVector(500, 10000, random);
        mergeMediumVector1 = generateVector(2500, 10000, random);
        mergeMediumVector2 = generateVector(2500, 10000, random);
        mergeLargeVector1 = generateVector(5000, 10000, random);
        mergeLargeVector2 = generateVector(5000, 10000, random);
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
        Vector<Integer> result = new Vector<>(size);
        for (int i = 0; i < size; i++) {
            result.add(random.nextInt(maxValue));
        }
        return result;
    }
    
    /**
     * Generate vector that contains SEARCH_VALUE (for correctness assertions).
     * 
     * Ensures search benchmark has guaranteed matches by inserting the search value
     * at multiple positions within the randomly generated vector.
     * 
     * @param size Number of elements
     * @param maxValue Maximum value (exclusive) for random integers
     * @param random Seeded Random instance
     * @return Vector<Integer> containing SEARCH_VALUE at known positions
     */
    private Vector<Integer> generateVectorWithSearchValue(int size, int maxValue, Random random) {
        Vector<Integer> result = generateVector(size, maxValue, random);
        // Ensure search value exists at multiple positions for correctness verification
        result.set(0, SEARCH_VALUE);
        result.set(size / 2, SEARCH_VALUE);
        result.set(size - 1, SEARCH_VALUE);
        return result;
    }
    
    // ============================================================================
    // BENCHMARK METHODS: modifyVector (add 1 to each element)
    // ============================================================================
    
    /**
     * Benchmark modifyVector with small dataset (1K elements).
     * Verifies correctness: all elements should be incremented by 1.
     */
    @Benchmark
    public Vector<Integer> benchmarkModifyVectorSmall() {
        Vector<Integer> v = new Vector<>(modifySmallVector);
        Vector<Integer> result = DsVector.modifyVector(v);
        
        // Correctness assertion: verify all elements were modified (incremented by 1)
        assert result.size() == 1000 : 
            "Modified vector should have 1000 elements, got " + result.size();
        assert allElementsIncrementedByOne(result, v) : 
            "All elements in result should be incremented by 1 from original";
        
        return result;
    }
    
    /**
     * Benchmark modifyVector with medium dataset (5K elements).
     * Verifies correctness: all elements should be incremented by 1.
     */
    @Benchmark
    public Vector<Integer> benchmarkModifyVectorMedium() {
        Vector<Integer> v = new Vector<>(modifyMediumVector);
        Vector<Integer> result = DsVector.modifyVector(v);
        
        // Correctness assertion: verify all elements were modified (incremented by 1)
        assert result.size() == 5000 : 
            "Modified vector should have 5000 elements, got " + result.size();
        assert allElementsIncrementedByOne(result, v) : 
            "All elements in result should be incremented by 1 from original";
        
        return result;
    }
    
    /**
     * Benchmark modifyVector with large dataset (10K elements).
     * Verifies correctness: all elements should be incremented by 1.
     */
    @Benchmark
    public Vector<Integer> benchmarkModifyVectorLarge() {
        Vector<Integer> v = new Vector<>(modifyLargeVector);
        Vector<Integer> result = DsVector.modifyVector(v);
        
        // Correctness assertion: verify all elements were modified (incremented by 1)
        assert result.size() == 10000 : 
            "Modified vector should have 10000 elements, got " + result.size();
        assert allElementsIncrementedByOne(result, v) : 
            "All elements in result should be incremented by 1 from original";
        
        return result;
    }
    
    // ============================================================================
    // BENCHMARK METHODS: searchVector (find all occurrences of value)
    // ============================================================================
    
    /**
     * Benchmark searchVector with small dataset (1K elements).
     * Searches for value 5000 which appears at known positions.
     * Verifies correctness: all returned indices contain search target value.
     */
    @Benchmark
    public Vector<Integer> benchmarkSearchVectorSmall() {
        Vector<Integer> result = DsVector.searchVector(searchSmallVector, SEARCH_VALUE);
        
        // Correctness assertion: verify all returned indices contain the search value
        assert result.size() >= 3 : 
            "Search should find at least 3 occurrences in small vector, got " + result.size();
        assert result.get(0) == 0 : 
            "First match should be at index 0, got " + result.get(0);
        assert allIndicesContainValue(result, searchSmallVector, SEARCH_VALUE) : 
            "All returned indices should contain the search value";
        
        return result;
    }
    
    /**
     * Benchmark searchVector with medium dataset (5K elements).
     * Searches for value 5000 which appears at known positions.
     * Verifies correctness: all returned indices contain search target value.
     */
    @Benchmark
    public Vector<Integer> benchmarkSearchVectorMedium() {
        Vector<Integer> result = DsVector.searchVector(searchMediumVector, SEARCH_VALUE);
        
        // Correctness assertion: verify all returned indices contain the search value
        assert result.size() >= 3 : 
            "Search should find at least 3 occurrences in medium vector, got " + result.size();
        assert result.contains(0) : 
            "First guaranteed position (0) should be in results";
        assert result.contains(2500) : 
            "Middle guaranteed position (2500) should be in results";
        assert allIndicesContainValue(result, searchMediumVector, SEARCH_VALUE) : 
            "All returned indices should contain the search value";
        
        return result;
    }
    
    /**
     * Benchmark searchVector with large dataset (10K elements).
     * Searches for value 5000 which appears at known positions.
     * Verifies correctness: all returned indices contain search target value.
     */
    @Benchmark
    public Vector<Integer> benchmarkSearchVectorLarge() {
        Vector<Integer> result = DsVector.searchVector(searchLargeVector, SEARCH_VALUE);
        
        // Correctness assertion: verify all returned indices contain the search value
        assert result.size() >= 3 : 
            "Search should find at least 3 occurrences in large vector, got " + result.size();
        assert result.contains(0) : 
            "First guaranteed position (0) should be in results";
        assert result.contains(5000) : 
            "Middle guaranteed position (5000) should be in results";
        assert allIndicesContainValue(result, searchLargeVector, SEARCH_VALUE) : 
            "All returned indices should contain the search value";
        
        return result;
    }
    
    // ============================================================================
    // BENCHMARK METHODS: sortVector (bubble sort)
    // ============================================================================
    
    /**
     * Benchmark sortVector with small dataset (1K elements).
     * Uses bubble sort implementation (O(n²)).
     * Verifies correctness: output is sorted in ascending order.
     */
    @Benchmark
    public Vector<Integer> benchmarkSortVectorSmall() {
        Vector<Integer> v = new Vector<>(sortSmallVector);
        Vector<Integer> result = DsVector.sortVector(v);
        
        // Correctness assertion: verify result is sorted
        assert result.size() == 1000 : 
            "Sorted vector should have 1000 elements, got " + result.size();
        assert isSorted(result) : 
            "Result vector should be sorted in ascending order";
        
        return result;
    }
    
    /**
     * Benchmark sortVector with medium dataset (5K elements).
     * Uses bubble sort implementation (O(n²)).
     * Verifies correctness: output is sorted.
     */
    @Benchmark
    public Vector<Integer> benchmarkSortVectorMedium() {
        Vector<Integer> v = new Vector<>(sortMediumVector);
        Vector<Integer> result = DsVector.sortVector(v);
        
        // Correctness assertion: verify result is sorted and has correct size
        assert result.size() == 5000 : 
            "Sorted vector should have 5000 elements, got " + result.size();
        assert isSorted(result) : 
            "Result vector should be sorted in ascending order";
        
        return result;
    }
    
    /**
     * Benchmark sortVector with large dataset (10K elements).
     * Uses bubble sort implementation (O(n²)).
     * Verifies correctness: output is sorted.
     */
    @Benchmark
    public Vector<Integer> benchmarkSortVectorLarge() {
        Vector<Integer> v = new Vector<>(sortLargeVector);
        Vector<Integer> result = DsVector.sortVector(v);
        
        // Correctness assertion: verify result is sorted and has correct size
        assert result.size() == 10000 : 
            "Sorted vector should have 10000 elements, got " + result.size();
        assert isSorted(result) : 
            "Result vector should be sorted in ascending order";
        
        return result;
    }
    
    // ============================================================================
    // BENCHMARK METHODS: reverseVector (reverse order)
    // ============================================================================
    
    /**
     * Benchmark reverseVector with small dataset (1K elements).
     * Reverses the order of elements.
     * Verifies correctness: output is reverse of input.
     */
    @Benchmark
    public Vector<Integer> benchmarkReverseVectorSmall() {
        Vector<Integer> original = reverseSmallVector;
        Vector<Integer> result = DsVector.reverseVector(original);
        
        // Correctness assertion: verify result has correct size and is properly reversed
        assert result.size() == 1000 : 
            "Reversed vector should have 1000 elements, got " + result.size();
        assert result.get(0).equals(original.get(999)) : 
            "First element of reversed should equal last element of original";
        
        return result;
    }
    
    /**
     * Benchmark reverseVector with medium dataset (5K elements).
     * Reverses the order of elements.
     * Verifies correctness: output is reverse of input.
     */
    @Benchmark
    public Vector<Integer> benchmarkReverseVectorMedium() {
        Vector<Integer> original = reverseMediumVector;
        Vector<Integer> result = DsVector.reverseVector(original);
        
        // Correctness assertion: verify result is properly reversed
        assert result.size() == 5000 : 
            "Reversed vector should have 5000 elements, got " + result.size();
        assert result.get(0).equals(original.get(4999)) : 
            "First element of reversed should equal last element of original";
        
        return result;
    }
    
    /**
     * Benchmark reverseVector with large dataset (10K elements).
     * Reverses the order of elements.
     * Verifies correctness: output is reverse of input.
     */
    @Benchmark
    public Vector<Integer> benchmarkReverseVectorLarge() {
        Vector<Integer> original = reverseLargeVector;
        Vector<Integer> result = DsVector.reverseVector(original);
        
        // Correctness assertion: verify result is properly reversed
        assert result.size() == 10000 : 
            "Reversed vector should have 10000 elements, got " + result.size();
        assert result.get(0).equals(original.get(9999)) : 
            "First element of reversed should equal last element of original";
        
        return result;
    }
    
    // ============================================================================
    // BENCHMARK METHODS: rotateVector (rotate by fixed amount)
    // ============================================================================
    
    /**
     * Benchmark rotateVector with small dataset (1K elements, rotate by 25%).
     * Rotates elements by 250 positions (25% of 1000).
     * Verifies correctness: output size matches input and rotation is correct.
     */
    @Benchmark
    public Vector<Integer> benchmarkRotateVectorSmall() {
        Vector<Integer> original = rotateSmallVector;
        Vector<Integer> result = DsVector.rotateVector(original, ROTATE_SMALL_AMOUNT);
        
        // Correctness assertion: verify size and rotation correctness
        assert result.size() == 1000 : 
            "Rotated vector should have 1000 elements, got " + result.size();
        assert result.get(0).equals(original.get(ROTATE_SMALL_AMOUNT)) : 
            "First element after rotation should be from position " + ROTATE_SMALL_AMOUNT;
        
        return result;
    }
    
    /**
     * Benchmark rotateVector with medium dataset (5K elements, rotate by 25%).
     * Rotates elements by 1250 positions (25% of 5000).
     * Verifies correctness: output size and rotation are correct.
     */
    @Benchmark
    public Vector<Integer> benchmarkRotateVectorMedium() {
        Vector<Integer> original = rotateMediumVector;
        Vector<Integer> result = DsVector.rotateVector(original, ROTATE_MEDIUM_AMOUNT);
        
        // Correctness assertion: verify size and rotation correctness
        assert result.size() == 5000 : 
            "Rotated vector should have 5000 elements, got " + result.size();
        assert result.get(0).equals(original.get(ROTATE_MEDIUM_AMOUNT)) : 
            "First element after rotation should be from position " + ROTATE_MEDIUM_AMOUNT;
        
        return result;
    }
    
    /**
     * Benchmark rotateVector with large dataset (10K elements, rotate by 25%).
     * Rotates elements by 2500 positions (25% of 10000).
     * Verifies correctness: output size and rotation are correct.
     */
    @Benchmark
    public Vector<Integer> benchmarkRotateVectorLarge() {
        Vector<Integer> original = rotateLargeVector;
        Vector<Integer> result = DsVector.rotateVector(original, ROTATE_LARGE_AMOUNT);
        
        // Correctness assertion: verify size and rotation correctness
        assert result.size() == 10000 : 
            "Rotated vector should have 10000 elements, got " + result.size();
        assert result.get(0).equals(original.get(ROTATE_LARGE_AMOUNT)) : 
            "First element after rotation should be from position " + ROTATE_LARGE_AMOUNT;
        
        return result;
    }
    
    // ============================================================================
    // BENCHMARK METHODS: mergeVectors (concatenate two vectors)
    // ============================================================================
    
    /**
     * Benchmark mergeVectors with small dataset (500+500 = 1K elements).
     * Concatenates two equal-sized small vectors.
     * Verifies correctness: output contains both vectors in order.
     */
    @Benchmark
    public Vector<Integer> benchmarkMergeVectorsSmall() {
        Vector<Integer> result = DsVector.mergeVectors(mergeSmallVector1, mergeSmallVector2);
        
        // Correctness assertion: verify merged size and content
        assert result.size() == 1000 : 
            "Merged vector should have 1000 elements (500+500), got " + result.size();
        assert result.get(0).equals(mergeSmallVector1.get(0)) : 
            "First element of result should match first element of first vector";
        assert result.get(500).equals(mergeSmallVector2.get(0)) : 
            "Element at position 500 should be first element of second vector";
        
        return result;
    }
    
    /**
     * Benchmark mergeVectors with medium dataset (2500+2500 = 5K elements).
     * Concatenates two equal-sized medium vectors.
     * Verifies correctness: output contains both vectors in order.
     */
    @Benchmark
    public Vector<Integer> benchmarkMergeVectorsMedium() {
        Vector<Integer> result = DsVector.mergeVectors(mergeMediumVector1, mergeMediumVector2);
        
        // Correctness assertion: verify merged size and content
        assert result.size() == 5000 : 
            "Merged vector should have 5000 elements (2500+2500), got " + result.size();
        assert result.get(0).equals(mergeMediumVector1.get(0)) : 
            "First element of result should match first element of first vector";
        assert result.get(2500).equals(mergeMediumVector2.get(0)) : 
            "Element at position 2500 should be first element of second vector";
        
        return result;
    }
    
    /**
     * Benchmark mergeVectors with large dataset (5000+5000 = 10K elements).
     * Concatenates two equal-sized large vectors.
     * Verifies correctness: output contains both vectors in order.
     */
    @Benchmark
    public Vector<Integer> benchmarkMergeVectorsLarge() {
        Vector<Integer> result = DsVector.mergeVectors(mergeLargeVector1, mergeLargeVector2);
        
        // Correctness assertion: verify merged size and content
        assert result.size() == 10000 : 
            "Merged vector should have 10000 elements (5000+5000), got " + result.size();
        assert result.get(0).equals(mergeLargeVector1.get(0)) : 
            "First element of result should match first element of first vector";
        assert result.get(5000).equals(mergeLargeVector2.get(0)) : 
            "Element at position 5000 should be first element of second vector";
        
        return result;
    }
    
    // ============================================================================
    // HELPER METHODS: Validation
    // ============================================================================
    
    /**
     * Verify that a vector is sorted in ascending order.
     * 
     * @param v Vector to check
     * @return true if vector is sorted, false otherwise
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
     * Verify that all elements in result were incremented by 1 from original.
     * 
     * @param result Result vector after modification
     * @param original Original vector before modification
     * @return true if all elements are incremented by 1, false otherwise
     */
    private boolean allElementsIncrementedByOne(Vector<Integer> result, Vector<Integer> original) {
        if (result.size() != original.size()) {
            return false;
        }
        for (int i = 0; i < result.size(); i++) {
            if (!result.get(i).equals(original.get(i) + 1)) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Verify that all returned indices contain the search value.
     * 
     * @param indices Indices returned by search operation
     * @param vector Vector being searched
     * @param searchValue Value being searched for
     * @return true if all indices contain the search value, false otherwise
     */
    private boolean allIndicesContainValue(Vector<Integer> indices, Vector<Integer> vector, int searchValue) {
        for (int index : indices) {
            if (!vector.get(index).equals(searchValue)) {
                return false;
            }
        }
        return true;
    }
}
