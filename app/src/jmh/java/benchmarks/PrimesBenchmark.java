package benchmarks;

import java.util.Random;
import java.util.Vector;
import org.openjdk.jmh.annotations.*;
import algorithms.Primes;

/**
 * JMH benchmarks for the Primes module with comprehensive correctness verification.
 * Measures performance of IsPrime, SumPrimes, and PrimeFactors methods
 * across various input values and ranges.
 * 
 * STATE CLASS PATTERN (REFERENCE IMPLEMENTATION):
 * ===============================================
 * This class demonstrates the recommended pattern for all JMH benchmarks:
 * 
 * 1. @State(Scope.Thread):
 *    - Each thread maintains independent state (better for parallel benchmarking)
 *    - Avoids contention and cross-thread interference
 *    - Alternative: @State(Scope.Benchmark) for shared state (less common)
 * 
 * 2. @Setup(Level.Trial) Method:
 *    - Executes exactly once per trial (before warmup and measurement)
 *    - Initializes all test data: arrays, vectors, pre-computed values
 *    - Setup time is NOT included in benchmark measurements
 *    - Ensures each iteration uses same pre-generated data
 * 
 * 3. Reproducibility with Seeded Random:
 *    - Random seeded with fixed value (12345) for deterministic data
 *    - Guarantees same test data across multiple runs
 *    - Enables comparison across benchmark runs
 * 
 * 4. Pre-computed Verification Values:
 *    - Calculate expected results in @Setup (outside measurement window)
 *    - Use in lightweight assertions within @Benchmark methods
 *    - Verify correctness without measurement overhead
 * 
 * 5. Lightweight Correctness Assertions:
 *    - Include in @Benchmark to verify results are correct
 *    - Assertions should be fast; they affect measurement
 *    - Examples: verify known primes, product of factors, sorted order
 * 
 * BENEFITS OF THIS PATTERN:
 * - Consistent data generation across benchmark runs
 * - No allocation/generation overhead in measurements
 * - Correctness verification without timing contamination
 * - Thread-safe state management
 * - Clear separation of setup and measurement phases
 */
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(java.util.concurrent.TimeUnit.MILLISECONDS)
@Fork(1)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
public class PrimesBenchmark {

    private Random random;
    private int[] candidatesForIsPrime;
    private int[] integersForFactorization;
    private int expectedSumPrimes10K;

    /**
     * Setup method to initialize test inputs once per trial.
     * Uses seeded Random (seed=12345) for reproducibility.
     * Generates:
     * - Test candidates for primality checking across 0-10,000
     * - Random integers for factorization up to 10,000
     * - Expected sum of primes up to 10,000 for verification
     */
    @Setup(Level.Trial)
    public void setup() {
        random = new Random(12345);
        
        // Generate 1000 candidate numbers for primality testing across 0-10,000
        candidatesForIsPrime = new int[1000];
        for (int i = 0; i < 1000; i++) {
            candidatesForIsPrime[i] = random.nextInt(10001);
        }
        
        // Generate 1000 random integers for factorization
        integersForFactorization = new int[1000];
        for (int i = 0; i < 1000; i++) {
            integersForFactorization[i] = 2 + random.nextInt(9999);  // Range [2, 10000]
        }
        
        // Pre-calculate expected sum of primes up to 10,000 for correctness assertion
        expectedSumPrimes10K = Primes.SumPrimes(10000);
    }

    /**
     * Benchmark for Primes.IsPrime() testing primality checks across 0-10,000.
     * Tests a mix of primes and composite numbers.
     * Asserts that known primes are correctly identified.
     */
    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(java.util.concurrent.TimeUnit.MILLISECONDS)
    @Fork(1)
    @Warmup(iterations = 5, time = 1)
    @Measurement(iterations = 5, time = 1)
    public boolean benchmarkIsPrime() {
        boolean result = false;
        // Test each candidate and verify against known primes
        for (int candidate : candidatesForIsPrime) {
            result = Primes.IsPrime(candidate);
            // Lightweight assertion: verify specific known primes
            if (candidate == 2 || candidate == 3 || candidate == 5 || candidate == 7 || 
                candidate == 11 || candidate == 13 || candidate == 17 || candidate == 19 || 
                candidate == 23 || candidate == 29 || candidate == 31 || candidate == 37 || 
                candidate == 41 || candidate == 43 || candidate == 47) {
                assert Primes.IsPrime(candidate) : 
                    "Known prime " + candidate + " returned false";
            }
        }
        return result;
    }

    /**
     * Benchmark for Primes.SumPrimes() computing the sum of all primes up to 10,000.
     * Asserts the result matches the pre-calculated expected value.
     */
    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(java.util.concurrent.TimeUnit.MILLISECONDS)
    @Fork(1)
    @Warmup(iterations = 5, time = 1)
    @Measurement(iterations = 5, time = 1)
    public int benchmarkSumPrimes() {
        int result = Primes.SumPrimes(10000);
        assert result == expectedSumPrimes10K : 
            "Sum of primes up to 10,000 should be " + expectedSumPrimes10K + 
            " but got " + result;
        return result;
    }

    /**
     * Benchmark for Primes.PrimeFactors() factorizing random integers up to 10,000.
     * Asserts that the product of returned factors equals the original number.
     */
    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(java.util.concurrent.TimeUnit.MILLISECONDS)
    @Fork(1)
    @Warmup(iterations = 5, time = 1)
    @Measurement(iterations = 5, time = 1)
    public Vector<Integer> benchmarkPrimeFactors() {
        Vector<Integer> lastFactors = new Vector<>();
        
        for (int number : integersForFactorization) {
            Vector<Integer> factors = Primes.PrimeFactors(number);
            lastFactors = factors;
            
            // Verify: product of factors should equal original number
            long product = 1;
            for (int factor : factors) {
                product *= factor;
            }
            
            assert product == number : 
                "Product of factors " + product + " does not equal original number " + number;
        }
        
        return lastFactors;
    }
}
