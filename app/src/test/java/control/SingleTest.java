package control;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class SingleTest {
  @Test
  @DisplayName("sumRange: basic cases and formula verification")
  public void testSumRange() {
    assertEquals(0, Single.sumRange(0), "sumRange(0) should be 0");
    assertEquals(0, Single.sumRange(1), "sumRange(1) should be 0");
    assertEquals(1, Single.sumRange(2), "sumRange(2) should be 1");
    assertEquals(3, Single.sumRange(3), "sumRange(3) should be 3");
    assertEquals(6, Single.sumRange(4), "sumRange(4) should be 6");
    assertEquals(45, Single.sumRange(10), "sumRange(10) should be 45");
  }

  @Test
  @DisplayName("sumRange: large values (n=100, 1000)")
  public void testSumRangeLarge() {
    // For n=100: sum = 0+1+2+...+99 = 100*99/2 = 4950
    assertEquals(4950, Single.sumRange(100), 
        "sumRange(100) should equal 100*99/2 = 4950");
    
    // For n=1000: sum = 0+1+2+...+999 = 1000*999/2 = 499500
    assertEquals(499500, Single.sumRange(1000), 
        "sumRange(1000) should equal 1000*999/2 = 499500");
  }

  @Test
  @DisplayName("sumRange: edge cases")
  public void testSumRangeEdgeCases() {
    // Testing boundary at n=5
    assertEquals(10, Single.sumRange(5), "sumRange(5) should be 10");
    
    // Testing n=50
    assertEquals(1225, Single.sumRange(50), 
        "sumRange(50) should equal 50*49/2 = 1225");
  }

  @Test
  @DisplayName("maxArray: basic cases")
  public void testMaxArray() {
    assertEquals(0, Single.maxArray(new int[] { 0 }), 
        "Single element array with 0 should return 0");
    assertEquals(5, Single.maxArray(new int[] { 1, 2, 3, 4, 5 }), 
        "Max of [1,2,3,4,5] should be 5");
    assertEquals(1, Single.maxArray(new int[] { 1, 1, 1, 1, 0 }), 
        "Max with repeated max values should be 1");
    assertEquals(0, Single.maxArray(new int[] { -1, -1, -1, -1, 0 }), 
        "Max with negatives and 0 should be 0");
  }

  @Test
  @DisplayName("maxArray: various array sizes (10, 100, 1000 elements)")
  public void testMaxArrayLargeSizes() {
    // Array of 10 elements
    int[] arr10 = new int[10];
    for (int i = 0; i < 10; i++) {
      arr10[i] = i;
    }
    assertEquals(9, Single.maxArray(arr10), 
        "Max of [0-9] should be 9");
    
    // Array of 100 elements
    int[] arr100 = new int[100];
    for (int i = 0; i < 100; i++) {
      arr100[i] = i - 50; // Range from -50 to 49
    }
    assertEquals(49, Single.maxArray(arr100), 
        "Max of [-50 to 49] should be 49");
    
    // Array of 1000 elements
    int[] arr1000 = new int[1000];
    for (int i = 0; i < 1000; i++) {
      arr1000[i] = i - 500; // Range from -500 to 499
    }
    assertEquals(499, Single.maxArray(arr1000), 
        "Max of [-500 to 499] should be 499");
  }

  @Test
  @DisplayName("maxArray: edge case with all negative numbers")
  public void testMaxArrayAllNegative() {
    assertEquals(-1, Single.maxArray(new int[] { -5, -3, -1, -10 }), 
        "Max of all negatives should be closest to zero: -1");
    assertEquals(-100, Single.maxArray(new int[] { -1000, -500, -100 }), 
        "Max of large negatives should be -100");
  }

  @Test
  @DisplayName("sumModulus: basic cases with moduli")
  public void testSumModulus() {
    assertEquals(0, Single.sumModulus(0, 1), "sumModulus(0, 1) should be 0");
    assertEquals(0, Single.sumModulus(1, 2), "sumModulus(1, 2) should be 0");
    assertEquals(0, Single.sumModulus(2, 2), "sumModulus(2, 2) should be 0");
    assertEquals(2, Single.sumModulus(3, 2), "sumModulus(3, 2) should be 2");
    assertEquals(2, Single.sumModulus(4, 2), "sumModulus(4, 2) should be 2");
    assertEquals(20, Single.sumModulus(10, 2), "sumModulus(10, 2) should be 20");
    assertEquals(18, Single.sumModulus(10, 3), "sumModulus(10, 3) should be 18");
    assertEquals(12, Single.sumModulus(10, 4), "sumModulus(10, 4) should be 12");
  }

  @Test
  @DisplayName("sumModulus: various moduli (5, 10, 100)")
  public void testSumModulusVariousModuli() {
    // Modulus 5: multiples of 5 from 0 to 19 are: 0, 5, 10, 15
    assertEquals(30, Single.sumModulus(20, 5), 
        "sumModulus(20, 5) multiples [0,5,10,15] sum = 30");
    
    // Modulus 10: multiples of 10 from 0 to 99 are: 0, 10, 20, ..., 90
    assertEquals(450, Single.sumModulus(100, 10), 
        "sumModulus(100, 10) multiples [0,10,20,...,90] sum = 450");
    
    // Modulus 100: multiples of 100 from 0 to 999 are: 0, 100, 200, ..., 900
    assertEquals(4500, Single.sumModulus(1000, 100), 
        "sumModulus(1000, 100) multiples [0,100,200,...,900] sum = 4500");
  }

  @Test
  @DisplayName("sumModulus: edge cases (m=1, m>n)")
  public void testSumModulusEdgeCases() {
    // Edge case: m=1 means all numbers are divisible by 1
    // sumModulus(5, 1) should sum: 0, 1, 2, 3, 4 = 10
    assertEquals(10, Single.sumModulus(5, 1), 
        "sumModulus(5, 1) should sum all: 0+1+2+3+4 = 10");
    
    // Edge case: m > n means only 0 is divisible by m
    assertEquals(0, Single.sumModulus(5, 10), 
        "sumModulus(5, 10) should only include 0");
    
    // Edge case: m = n
    assertEquals(0, Single.sumModulus(7, 7), 
        "sumModulus(7, 7) should only include 0");
  }

  @Test
  @DisplayName("sumModulus: divisibility verification")
  public void testSumModulusDivisibility() {
    // Verify that all summed values are divisible by m
    int result5 = Single.sumModulus(50, 5);
    // Multiples of 5: 0, 5, 10, 15, ..., 45 sum = 5*(0+1+2+...+9) = 5*45 = 225
    assertEquals(225, result5, "All values in result must be divisible by 5");
    assertEquals(0, result5 % 5, "Result should be divisible by 5");
    
    int result3 = Single.sumModulus(30, 3);
    // Multiples of 3: 0, 3, 6, 9, 12, 15, 18, 21, 24, 27 sum = 135
    assertEquals(135, result3, "sumModulus(30, 3) should be 135");
    assertEquals(0, result3 % 3, "Result should be divisible by 3");
  }
}
