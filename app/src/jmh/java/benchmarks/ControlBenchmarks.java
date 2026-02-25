package benchmarks;

import org.openjdk.jmh.annotations.*;
import control.Single;
import control.Double;

/**
 * JMH benchmarks for control flow operations (Single and Double).
 * Demonstrates the state class pattern for efficient benchmark data management.
 * 
 * STATE CLASS PATTERN:
 * - Use @State(Scope.Thread) for thread-local state managed independently per thread
 * - Implement @Setup(Level.Trial) to initialize data once per trial run
 * - Trial-level setup executes before warmup and measurement phases
 * - Initialize all large data structures (arrays, matrices) in @Setup(Level.Trial)
 * - This ensures data generation doesn't contaminate benchmark measurements
 * - Each @Benchmark method operates on pre-generated data without allocation overhead
 */
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(java.util.concurrent.TimeUnit.MILLISECONDS)
public class ControlBenchmarks {

    private int[] arr_small;
    private int[][] matrix;
    private int expectedSumRangeSmall;
    private int expectedSumRangeLarge;
    private int expectedMaxArraySmall;
    private int expectedSumSquareSmall;

    /**
     * Setup method to initialize test data structures once per trial.
     * Uses @Setup(Level.Trial) to ensure data generation occurs exactly once,
     * outside the measurement window, preventing timing contamination.
     */
    @Setup(Level.Trial)
    public void setup() {
        // Initialize 100-element array with sequential values [0, 1, 2, ..., 99]
        arr_small = new int[100];
        for (int i = 0; i < arr_small.length; i++) {
            arr_small[i] = i;
        }
        
        // Initialize 50x50 matrix with product values [i*j]
        matrix = new int[50][50];
        for (int i = 0; i < 50; i++) {
            for (int j = 0; j < 50; j++) {
                matrix[i][j] = i * j;
            }
        }
        
        // Pre-compute expected values for assertions
        // sumRange(100) = sum of 0 to 99 = 99 * 100 / 2
        expectedSumRangeSmall = 99 * 100 / 2;
        
        // sumRange(10000) = sum of 0 to 9999 = 9999 * 10000 / 2
        expectedSumRangeLarge = 9999 * 10000 / 2;
        
        // maxArray(arr_small) = maximum of [0, 1, ..., 99] = 99
        expectedMaxArraySmall = 99;
        
        // sumSquare(50) = sum of i^2 for i=0 to 49 = n(n-1)(2n-1)/6
        // For n=50: 50*49*99/6 = 40425
        expectedSumSquareSmall = 50 * 49 * 99 / 6;
    }

    /**
     * Benchmark for Single.sumRange(100).
     * Asserts result equals expected sum of 0 to 99.
     */
    @Benchmark
    public int sumRangeSmall() {
        int result = Single.sumRange(100);
        assert result == expectedSumRangeSmall : 
            "Sum of range [0, 100) should be " + expectedSumRangeSmall + 
            " but got " + result;
        return result;
    }

    /**
     * Benchmark for Single.sumRange(10000).
     * Asserts result equals expected sum of 0 to 9999.
     */
    @Benchmark
    public int sumRangeLarge() {
        int result = Single.sumRange(10000);
        assert result == expectedSumRangeLarge : 
            "Sum of range [0, 10000) should be " + expectedSumRangeLarge + 
            " but got " + result;
        return result;
    }

    /**
     * Benchmark for Single.maxArray on 100-element array.
     * Asserts result equals expected maximum value 99.
     */
    @Benchmark
    public int maxArraySmall() {
        int result = Single.maxArray(arr_small);
        assert result == expectedMaxArraySmall : 
            "Maximum of arr_small should be " + expectedMaxArraySmall + 
            " but got " + result;
        return result;
    }

    /**
     * Benchmark for Double.sumSquare(50).
     * Asserts result equals expected sum of squares from 0 to 49.
     */
    @Benchmark
    public int sumSquareSmall() {
        int result = Double.sumSquare(50);
        assert result == expectedSumSquareSmall : 
            "Sum of squares [0, 50) should be " + expectedSumSquareSmall + 
            " but got " + result;
        return result;
    }

}
