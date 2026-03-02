package benchmarks;

import org.openjdk.jmh.annotations.*;
import algorithms.Primes;

/**
 * JMH benchmarks for the Primes module (simple fixed-size variants).
 * Demonstrates state class pattern for data generation and reproducibility.
 * 
 * STATE CLASS PATTERN:
 * - Use @State(Scope.Thread) for thread-local state
 * - Implement @Setup(Level.Trial) to initialize test data once per trial
 * - Seed Random with fixed value (12345) for reproducible results
 * - Pre-compute expected results in setup for correctness verification
 * - Keep data generation in @Setup, not in @Benchmark methods
 */
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(java.util.concurrent.TimeUnit.MILLISECONDS)
public class PrimesBenchmarks {

    private int expectedSumPrimes100;
    private int expectedSumPrimes500;

    /**
     * Setup method to pre-calculate expected values once per trial.
     * Uses @Setup(Level.Trial) to ensure this runs exactly once per trial,
     * before warmup and measurement phases, preventing timing contamination.
     */
    @Setup(Level.Trial)
    public void setup() {
        // Pre-calculate expected values for correctness assertions
        expectedSumPrimes100 = Primes.SumPrimes(100);
        expectedSumPrimes500 = Primes.SumPrimes(500);
    }

    @Benchmark
    public int sumPrimesSmall() {
        int result = Primes.SumPrimes(100);
        assert result == expectedSumPrimes100 : 
            "Sum of primes up to 100 should be " + expectedSumPrimes100 + 
            " but got " + result;
        return result;
    }

    @Benchmark
    public int sumPrimesMedium() {
        int result = Primes.SumPrimes(500);
        assert result == expectedSumPrimes500 : 
            "Sum of primes up to 500 should be " + expectedSumPrimes500 + 
            " but got " + result;
        return result;
    }

    @Benchmark
    public java.util.Vector<Integer> primeFactorsSmall() {
        java.util.Vector<Integer> factors = Primes.PrimeFactors(120);
        // Verify: product of factors should equal original number
        long product = 1;
        for (int factor : factors) {
            product *= factor;
        }
        assert product == 120 : 
            "Product of factors " + product + " does not equal 120";
        return factors;
    }

    @Benchmark
    public java.util.Vector<Integer> primeFactorsMedium() {
        java.util.Vector<Integer> factors = Primes.PrimeFactors(10000);
        // Verify: product of factors should equal original number
        long product = 1;
        for (int factor : factors) {
            product *= factor;
        }
        assert product == 10000 : 
            "Product of factors " + product + " does not equal 10000";
        return factors;
    }

    @Benchmark
    public boolean isPrimeSmall() {
        boolean result = Primes.IsPrime(97);
        assert result : "97 is a known prime number";
        return result;
    }
}
