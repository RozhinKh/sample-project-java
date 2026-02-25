package algorithms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.Vector;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Primes Algorithm Tests")
public class PrimesTest {

  // ============================================================================
  // IsPrime Tests
  // ============================================================================

  @Test
  @DisplayName("IsPrime: 2 is prime")
  public void testIsPrimeTwoIsTrue() {
    assertTrue(Primes.IsPrime(2));
  }

  @Test
  @DisplayName("IsPrime: 3 is prime")
  public void testIsPrimeThreeIsTrue() {
    assertTrue(Primes.IsPrime(3));
  }

  @Test
  @DisplayName("IsPrime: 5 is prime")
  public void testIsPrimeFiveIsTrue() {
    assertTrue(Primes.IsPrime(5));
  }

  @Test
  @DisplayName("IsPrime: 7 is prime")
  public void testIsPrimeSevenIsTrue() {
    assertTrue(Primes.IsPrime(7));
  }

  @Test
  @DisplayName("IsPrime: 11 is prime")
  public void testIsPrimeElevenIsTrue() {
    assertTrue(Primes.IsPrime(11));
  }

  @Test
  @DisplayName("IsPrime: 13 is prime")
  public void testIsPrimeThirteenIsTrue() {
    assertTrue(Primes.IsPrime(13));
  }

  @Test
  @DisplayName("IsPrime: 17 is prime")
  public void testIsPrimeSeventeenIsTrue() {
    assertTrue(Primes.IsPrime(17));
  }

  @Test
  @DisplayName("IsPrime: 19 is prime")
  public void testIsPrimeNineteenIsTrue() {
    assertTrue(Primes.IsPrime(19));
  }

  @Test
  @DisplayName("IsPrime: 23 is prime")
  public void testIsPrimeTwentythreeIsTrue() {
    assertTrue(Primes.IsPrime(23));
  }

  @Test
  @DisplayName("IsPrime: 29 is prime")
  public void testIsPrimeTwentynineIsTrue() {
    assertTrue(Primes.IsPrime(29));
  }

  @Test
  @DisplayName("IsPrime: 97 is prime")
  public void testIsPrimeNinetyseven() {
    assertTrue(Primes.IsPrime(97));
  }

  @Test
  @DisplayName("IsPrime: 4 is composite (2*2)")
  public void testIsPrimeFourIsFalse() {
    assertFalse(Primes.IsPrime(4));
  }

  @Test
  @DisplayName("IsPrime: 6 is composite (2*3)")
  public void testIsPrimeSixIsFalse() {
    assertFalse(Primes.IsPrime(6));
  }

  @Test
  @DisplayName("IsPrime: 8 is composite (2*4)")
  public void testIsPrimeEightIsFalse() {
    assertFalse(Primes.IsPrime(8));
  }

  @Test
  @DisplayName("IsPrime: 9 is composite (3*3)")
  public void testIsPrimeNineIsFalse() {
    assertFalse(Primes.IsPrime(9));
  }

  @Test
  @DisplayName("IsPrime: 10 is composite (2*5)")
  public void testIsPrimeTenIsFalse() {
    assertFalse(Primes.IsPrime(10));
  }

  @Test
  @DisplayName("IsPrime: 12 is composite (3*4)")
  public void testIsPrimeTwelveIsFalse() {
    assertFalse(Primes.IsPrime(12));
  }

  @Test
  @DisplayName("IsPrime: 15 is composite (3*5)")
  public void testIsPrimeFifteenIsFalse() {
    assertFalse(Primes.IsPrime(15));
  }

  @Test
  @DisplayName("IsPrime: 21 is composite (3*7)")
  public void testIsPrimeTwentyone() {
    assertFalse(Primes.IsPrime(21));
  }

  @Test
  @DisplayName("IsPrime: 25 is composite (5*5)")
  public void testIsPrimeTwentyfive() {
    assertFalse(Primes.IsPrime(25));
  }

  @Test
  @DisplayName("IsPrime: 0 is not prime")
  public void testIsPrimeZeroIsFalse() {
    assertFalse(Primes.IsPrime(0));
  }

  @Test
  @DisplayName("IsPrime: 1 is not prime")
  public void testIsPrimeOneIsFalse() {
    assertFalse(Primes.IsPrime(1));
  }

  @Test
  @DisplayName("IsPrime: negative number -1 is not prime")
  public void testIsPrimeNegativeOneIsFalse() {
    assertFalse(Primes.IsPrime(-1));
  }

  @Test
  @DisplayName("IsPrime: negative number -5 is not prime")
  public void testIsPrimeNegativeFiveIsFalse() {
    assertFalse(Primes.IsPrime(-5));
  }

  @Test
  @DisplayName("IsPrime: negative number -10 is not prime")
  public void testIsPrimeNegativeTenIsFalse() {
    assertFalse(Primes.IsPrime(-10));
  }

  // ============================================================================
  // SumPrimes Tests
  // ============================================================================

  @Test
  @DisplayName("SumPrimes: Sum of primes < 0 equals 0")
  public void testSumPrimesZero() {
    assertEquals(0, Primes.SumPrimes(0));
  }

  @Test
  @DisplayName("SumPrimes: Sum of primes < 1 equals 0")
  public void testSumPrimesOne() {
    assertEquals(0, Primes.SumPrimes(1));
  }

  @Test
  @DisplayName("SumPrimes: Sum of primes < 2 equals 0")
  public void testSumPrimesTwo() {
    assertEquals(0, Primes.SumPrimes(2));
  }

  @Test
  @DisplayName("SumPrimes: Sum of primes < 3 equals 2")
  public void testSumPrimesThree() {
    assertEquals(2, Primes.SumPrimes(3));
  }

  @Test
  @DisplayName("SumPrimes: Sum of primes < 5 equals 5 (2+3)")
  public void testSumPrimesFive() {
    assertEquals(5, Primes.SumPrimes(5));
  }

  @Test
  @DisplayName("SumPrimes: Sum of primes < 8 equals 10 (2+3+5)")
  public void testSumPrimesEight() {
    assertEquals(10, Primes.SumPrimes(8));
  }

  @Test
  @DisplayName("SumPrimes: Sum of primes < 10 equals 17 (2+3+5+7)")
  public void testSumPrimesTen() {
    assertEquals(17, Primes.SumPrimes(10));
  }

  @Test
  @DisplayName("SumPrimes: Sum of primes < 20 equals 77")
  public void testSumPrimesTwenty() {
    // 2+3+5+7+11+13+17+19 = 77
    assertEquals(77, Primes.SumPrimes(20));
  }

  @Test
  @DisplayName("SumPrimes: Sum of primes < 50 equals 328")
  public void testSumPrimesFifty() {
    // 2+3+5+7+11+13+17+19+23+29+31+37+41+43+47 = 328
    assertEquals(328, Primes.SumPrimes(50));
  }

  @Test
  @DisplayName("SumPrimes: Sum of primes < 100 equals 1060")
  public void testSumPrimesHundred() {
    // Sum of all primes less than 100
    assertEquals(1060, Primes.SumPrimes(100));
  }

  @Test
  @DisplayName("SumPrimes: Sum of primes < 101 equals 1060")
  public void testSumPrimesHundredOne() {
    // 1060 (primes less than 100)
    assertEquals(1060, Primes.SumPrimes(101));
  }

  @Test
  @DisplayName("SumPrimes: Sum of primes < 200 greater than 1060")
  public void testSumPrimesHundredHalf() {
    // This verifies the function works for larger ranges
    int result = Primes.SumPrimes(200);
    assertTrue(result > 1060, "Sum should be greater than sum of primes < 100");
  }

  // ============================================================================
  // PrimeFactors Tests
  // ============================================================================

  @Test
  @DisplayName("PrimeFactors: Prime number 2 returns [2]")
  public void testPrimeFactorsTwo() {
    Vector<Integer> factors = Primes.PrimeFactors(2);
    assertEquals(1, factors.size());
    assertEquals(2, factors.get(0));
    verifyProductEqualsOriginal(factors, 2);
  }

  @Test
  @DisplayName("PrimeFactors: Prime number 3 returns [3]")
  public void testPrimeFactorsThree() {
    Vector<Integer> factors = Primes.PrimeFactors(3);
    assertEquals(1, factors.size());
    assertEquals(3, factors.get(0));
    verifyProductEqualsOriginal(factors, 3);
  }

  @Test
  @DisplayName("PrimeFactors: Prime number 5 returns [5]")
  public void testPrimeFactorsFive() {
    Vector<Integer> factors = Primes.PrimeFactors(5);
    assertEquals(1, factors.size());
    assertEquals(5, factors.get(0));
    verifyProductEqualsOriginal(factors, 5);
  }

  @Test
  @DisplayName("PrimeFactors: Prime number 7 returns [7]")
  public void testPrimeFactorsSeven() {
    Vector<Integer> factors = Primes.PrimeFactors(7);
    assertEquals(1, factors.size());
    assertEquals(7, factors.get(0));
    verifyProductEqualsOriginal(factors, 7);
  }

  @Test
  @DisplayName("PrimeFactors: 4 (2*2) returns [2, 2]")
  public void testPrimeFactorsFour() {
    Vector<Integer> factors = Primes.PrimeFactors(4);
    assertEquals(2, factors.size());
    assertEquals(2, factors.get(0));
    assertEquals(2, factors.get(1));
    verifyProductEqualsOriginal(factors, 4);
  }

  @Test
  @DisplayName("PrimeFactors: 6 (2*3) returns [2, 3]")
  public void testPrimeFactorsSix() {
    Vector<Integer> factors = Primes.PrimeFactors(6);
    assertEquals(2, factors.size());
    verifyContainsFactors(factors, 2, 3);
    verifyProductEqualsOriginal(factors, 6);
  }

  @Test
  @DisplayName("PrimeFactors: 8 (2*2*2) returns [2, 2, 2]")
  public void testPrimeFactorsEight() {
    Vector<Integer> factors = Primes.PrimeFactors(8);
    assertEquals(3, factors.size());
    assertEquals(2, factors.get(0));
    assertEquals(2, factors.get(1));
    assertEquals(2, factors.get(2));
    verifyProductEqualsOriginal(factors, 8);
  }

  @Test
  @DisplayName("PrimeFactors: 9 (3*3) returns [3, 3]")
  public void testPrimeFactorsNine() {
    Vector<Integer> factors = Primes.PrimeFactors(9);
    assertEquals(2, factors.size());
    assertEquals(3, factors.get(0));
    assertEquals(3, factors.get(1));
    verifyProductEqualsOriginal(factors, 9);
  }

  @Test
  @DisplayName("PrimeFactors: 12 (2*2*3) returns [2, 2, 3]")
  public void testPrimeFactorsTwelve() {
    Vector<Integer> factors = Primes.PrimeFactors(12);
    assertEquals(3, factors.size());
    verifyContainsFactors(factors, 2, 2, 3);
    verifyProductEqualsOriginal(factors, 12);
  }

  @Test
  @DisplayName("PrimeFactors: 15 (3*5) returns [3, 5]")
  public void testPrimeFactorsFifteen() {
    Vector<Integer> factors = Primes.PrimeFactors(15);
    assertEquals(2, factors.size());
    verifyContainsFactors(factors, 3, 5);
    verifyProductEqualsOriginal(factors, 15);
  }

  @Test
  @DisplayName("PrimeFactors: 18 (2*3*3) returns [2, 3, 3]")
  public void testPrimeFactorsEighteen() {
    Vector<Integer> factors = Primes.PrimeFactors(18);
    assertEquals(3, factors.size());
    verifyContainsFactors(factors, 2, 3, 3);
    verifyProductEqualsOriginal(factors, 18);
  }

  @Test
  @DisplayName("PrimeFactors: 20 (2*2*5) returns [2, 2, 5]")
  public void testPrimeFactorsTwenty() {
    Vector<Integer> factors = Primes.PrimeFactors(20);
    assertEquals(3, factors.size());
    verifyContainsFactors(factors, 2, 2, 5);
    verifyProductEqualsOriginal(factors, 20);
  }

  @Test
  @DisplayName("PrimeFactors: 30 (2*3*5) returns [2, 3, 5]")
  public void testPrimeFactorsThirty() {
    Vector<Integer> factors = Primes.PrimeFactors(30);
    assertEquals(3, factors.size());
    verifyContainsFactors(factors, 2, 3, 5);
    verifyProductEqualsOriginal(factors, 30);
  }

  @Test
  @DisplayName("PrimeFactors: 36 (2*2*3*3) returns [2, 2, 3, 3]")
  public void testPrimeFactorsThirtysix() {
    Vector<Integer> factors = Primes.PrimeFactors(36);
    assertEquals(4, factors.size());
    verifyContainsFactors(factors, 2, 2, 3, 3);
    verifyProductEqualsOriginal(factors, 36);
  }

  @Test
  @DisplayName("PrimeFactors: 60 (2*2*3*5) returns [2, 2, 3, 5]")
  public void testPrimeFactorsSixty() {
    Vector<Integer> factors = Primes.PrimeFactors(60);
    assertEquals(4, factors.size());
    verifyProductEqualsOriginal(factors, 60);
  }

  @Test
  @DisplayName("PrimeFactors: 100 (2*2*5*5) returns [2, 2, 5, 5]")
  public void testPrimeFactorsHundred() {
    Vector<Integer> factors = Primes.PrimeFactors(100);
    assertEquals(4, factors.size());
    verifyContainsFactors(factors, 2, 2, 5, 5);
    verifyProductEqualsOriginal(factors, 100);
  }

  @Test
  @DisplayName("PrimeFactors: 0 returns empty vector")
  public void testPrimeFactorsZero() {
    Vector<Integer> factors = Primes.PrimeFactors(0);
    assertEquals(0, factors.size());
  }

  @Test
  @DisplayName("PrimeFactors: 1 returns empty vector")
  public void testPrimeFactorsOne() {
    Vector<Integer> factors = Primes.PrimeFactors(1);
    assertEquals(0, factors.size());
  }

  // ============================================================================
  // Helper Methods
  // ============================================================================

  /**
   * Verifies that the product of all factors equals the original number
   */
  private void verifyProductEqualsOriginal(Vector<Integer> factors, int original) {
    int product = 1;
    for (int factor : factors) {
      product *= factor;
    }
    assertEquals(original, product, "Product of factors should equal original number");
  }

  /**
   * Verifies that the vector contains all expected factors (in order)
   */
  private void verifyContainsFactors(Vector<Integer> factors, int... expected) {
    assertEquals(expected.length, factors.size(),
        "Expected " + expected.length + " factors but got " + factors.size());
    for (int i = 0; i < expected.length; i++) {
      assertEquals(expected[i], factors.get(i),
          "Factor at index " + i + " should be " + expected[i]);
    }
  }
}
