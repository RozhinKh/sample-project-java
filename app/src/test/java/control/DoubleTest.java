package control;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class DoubleTest {
  @Test
  @DisplayName("sumSquare: basic cases")
  public void testSumSquare() {
    assertEquals(0, Double.sumSquare(1), "sumSquare(1) should be 0 (only 0^2)");
    assertEquals(1, Double.sumSquare(2), "sumSquare(2) should be 1 (0^2 + 1^2)");
    assertEquals(5, Double.sumSquare(3), "sumSquare(3) should be 5 (0^2 + 1^2 + 2^2)");
    assertEquals(285, Double.sumSquare(10), "sumSquare(10) should be 285");
  }

  @Test
  @DisplayName("sumSquare: formula verification (sum of squares)")
  public void testSumSquareFormula() {
    // sumSquare(n) sums i^2 for i from 0 to n-1
    // Formula: 0^2 + 1^2 + 2^2 + ... + (n-1)^2 = (n-1)*n*(2n-1)/6
    
    // For n=4: 0^2 + 1^2 + 2^2 + 3^2 = 0+1+4+9 = 14
    assertEquals(14, Double.sumSquare(4), 
        "sumSquare(4) should be 0^2 + 1^2 + 2^2 + 3^2 = 14");
    
    // For n=5: 0^2 + 1^2 + 2^2 + 3^2 + 4^2 = 0+1+4+9+16 = 30
    assertEquals(30, Double.sumSquare(5), 
        "sumSquare(5) should be 0^2 + 1^2 + ... + 4^2 = 30");
    
    // For n=6: 0^2 + 1^2 + 2^2 + 3^2 + 4^2 + 5^2 = 55
    assertEquals(55, Double.sumSquare(6), 
        "sumSquare(6) should be 55");
    
    // For n=7: 0^2 + 1^2 + 2^2 + 3^2 + 4^2 + 5^2 + 6^2 = 91
    assertEquals(91, Double.sumSquare(7), 
        "sumSquare(7) should be 91");
    
    // For n=100: using formula (99)*100*(199)/6 = 328350
    assertEquals(328350, Double.sumSquare(100), 
        "sumSquare(100) should be 328350");
  }

  @Test
  @DisplayName("sumTriangle: basic cases")
  public void testSumTriangle() {
    assertEquals(0, Double.sumTriangle(1), "sumTriangle(1) should be 0");
    assertEquals(1, Double.sumTriangle(2), "sumTriangle(2) should be 1");
    assertEquals(4, Double.sumTriangle(3), "sumTriangle(3) should be 4");
    assertEquals(165, Double.sumTriangle(10), "sumTriangle(10) should be 165");
  }

  @Test
  @DisplayName("sumTriangle: triangular number formula verification")
  public void testSumTriangleFormula() {
    // T(n) = n*(n+1)/2 is the nth triangular number
    // sumTriangle(n) sums T(1) + T(2) + ... + T(n)
    // Formula: sum of T(k) from k=1 to n = n*(n+1)*(n+2)/6
    
    // For n=4: T(1)+T(2)+T(3)+T(4) = 1+3+6+10 = 20
    assertEquals(20, Double.sumTriangle(4), 
        "sumTriangle(4) should be T(1)+T(2)+T(3)+T(4) = 1+3+6+10 = 20");
    
    // For n=5: sum of triangular numbers = 35
    assertEquals(35, Double.sumTriangle(5), 
        "sumTriangle(5) should be 35");
    
    // For n=6: sum of triangular numbers = 56
    assertEquals(56, Double.sumTriangle(6), 
        "sumTriangle(6) should be 56");
    
    // For n=15: using formula 15*16*17/6 = 680
    assertEquals(680, Double.sumTriangle(15), 
        "sumTriangle(15) should be 680");
    
    // For n=20: using formula 20*21*22/6 = 1540
    assertEquals(1540, Double.sumTriangle(20), 
        "sumTriangle(20) should be 1540");
  }

  @Test
  @DisplayName("countPairs: basic cases")
  public void testCountPairs() {
    assertEquals(0, Double.countPairs(new int[] { 0 }), 
        "Single element should have 0 pairs");
    assertEquals(0, Double.countPairs(new int[] { 1, 2, 3 }), 
        "No duplicates should have 0 pairs");
    assertEquals(0, Double.countPairs(new int[] { 1, 1, 1 }), 
        "Triple values count as 0 pairs");
    assertEquals(1, Double.countPairs(new int[] { 1, 1, 2 }), 
        "One pair should be counted");
    assertEquals(2, Double.countPairs(new int[] { 1, 1, 2, 2 }), 
        "Two pairs should be counted");
    assertEquals(3, Double.countPairs(new int[] { 0, 0, 1, 1, 2, 2 }), 
        "Three pairs should be counted");
    assertEquals(3, Double.countPairs(new int[] { 0, 0, 1, 1, 2, 2, 3 }), 
        "Three pairs with extra element");
  }

  @Test
  @DisplayName("countPairs: edge cases (no duplicates, all same, mixed frequencies)")
  public void testCountPairsEdgeCases() {
    // Edge case: empty behavior (handled by no elements)
    assertEquals(0, Double.countPairs(new int[] {}), 
        "Empty array should have 0 pairs");
    
    // Edge case: all same value (count as 0 because frequency != 2)
    assertEquals(0, Double.countPairs(new int[] { 5, 5, 5, 5 }), 
        "All same value (frequency 4) should have 0 pairs");
    
    // Edge case: mixed with some pairs and some singles
    assertEquals(2, Double.countPairs(new int[] { 1, 1, 2, 2, 3, 4, 5 }), 
        "Mixed: 1 appears 2x, 2 appears 2x = 2 pairs");
    
    // Edge case: large array with many pairs
    assertEquals(5, Double.countPairs(new int[] { 1, 1, 2, 2, 3, 3, 4, 4, 5, 5 }), 
        "Array with 5 pairs");
    
    // Edge case: negative numbers
    assertEquals(2, Double.countPairs(new int[] { -1, -1, 0, 0, 1, 1 }), 
        "Pairs with negative numbers");
  }

  @Test
  @DisplayName("countDuplicates: basic cases")
  public void testCountDuplicates() {
    assertEquals(1, Double.countDuplicates(new int[] { 0 }, new int[] { 0 }), 
        "Single matching index");
    assertEquals(0, Double.countDuplicates(new int[] { 1, 2, 3 }, new int[] { 2, 3, 1 }), 
        "No matching values at same index");
    assertEquals(1, Double.countDuplicates(new int[] { 1, 1, 1 }, new int[] { 1, 2, 3 }), 
        "Match only at index 0");
    assertEquals(2, Double.countDuplicates(new int[] { 1, 1, 2 }, new int[] { 1, 2, 2 }), 
        "Matches at indices 0 and 1");
    assertEquals(4, Double.countDuplicates(new int[] { 1, 1, 2, 2 }, 
        new int[] { 1, 1, 2, 2 }), 
        "All indices match");
  }

  @Test
  @DisplayName("countDuplicates: edge cases (empty arrays, different sizes)")
  public void testCountDuplicatesEdgeCases() {
    // Edge case: both empty arrays
    assertEquals(0, Double.countDuplicates(new int[] {}, new int[] {}), 
        "Both empty arrays should return 0");
    
    // Edge case: one empty, one with elements (will cause index out of bounds in original)
    // Testing the behavior with arrays of different lengths
    int[] arr1 = new int[] { 1, 2, 3, 4, 5 };
    int[] arr2 = new int[] { 1, 2, 3 };
    // Note: original code may have issues with different sizes, test what it does
    // It will only compare up to arr2.length due to loop condition
    assertEquals(3, Double.countDuplicates(arr1, arr2), 
        "Should compare only common indices");
    
    // Edge case: all zeros
    assertEquals(5, Double.countDuplicates(
        new int[] { 0, 0, 0, 0, 0 }, 
        new int[] { 0, 0, 0, 0, 0 }), 
        "All zeros at same indices");
    
    // Edge case: negative and positive numbers
    assertEquals(2, Double.countDuplicates(
        new int[] { -1, 2, -3, 4, -5 }, 
        new int[] { -1, 2, 5, 6, 7 }), 
        "Matches with mixed positive/negative");
    
    // Edge case: single element arrays
    assertEquals(1, Double.countDuplicates(
        new int[] { 42 }, 
        new int[] { 42 }), 
        "Single matching element");
  }

  @Test
  @DisplayName("sumMatrix: basic cases")
  public void testSumMatrix() {
    assertEquals(0, Double.sumMatrix(new int[][] { { 0 } }), 
        "1x1 matrix with 0");
    assertEquals(6, Double.sumMatrix(new int[][] { { 0, 1 }, { 2, 3 } }), 
        "2x2 matrix: 0+1+2+3 = 6");
    assertEquals(36, Double.sumMatrix(new int[][] { { 0, 1, 2 }, { 3, 4, 5 }, { 6, 7, 8 } }), 
        "3x3 matrix: sum of 0-8 = 36");
  }

  @Test
  @DisplayName("sumMatrix: various matrix sizes (2x2, 5x5, 10x10)")
  public void testSumMatrixVariousSizes() {
    // 2x2 matrix with specific values
    assertEquals(10, Double.sumMatrix(new int[][] { { 1, 2 }, { 3, 4 } }), 
        "2x2 matrix: 1+2+3+4 = 10");
    
    // 4x4 matrix with sequential values 0-15
    int[][] matrix4x4 = new int[4][4];
    int value = 0;
    for (int i = 0; i < 4; i++) {
      for (int j = 0; j < 4; j++) {
        matrix4x4[i][j] = value++;
      }
    }
    assertEquals(120, Double.sumMatrix(matrix4x4), 
        "4x4 matrix: sum of 0-15 = 120");
    
    // 5x5 matrix with sequential values 0-24
    int[][] matrix5x5 = new int[5][5];
    value = 0;
    for (int i = 0; i < 5; i++) {
      for (int j = 0; j < 5; j++) {
        matrix5x5[i][j] = value++;
      }
    }
    assertEquals(300, Double.sumMatrix(matrix5x5), 
        "5x5 matrix: sum of 0-24 = 300");
    
    // 10x10 matrix with sequential values 0-99
    int[][] matrix10x10 = new int[10][10];
    value = 0;
    for (int i = 0; i < 10; i++) {
      for (int j = 0; j < 10; j++) {
        matrix10x10[i][j] = value++;
      }
    }
    assertEquals(4950, Double.sumMatrix(matrix10x10), 
        "10x10 matrix: sum of 0-99 = 4950");
  }

  @Test
  @DisplayName("sumMatrix: edge cases (1x1, all zeros, all negative values)")
  public void testSumMatrixEdgeCases() {
    // Edge case: 1x1 with value 1
    assertEquals(1, Double.sumMatrix(new int[][] { { 1 } }), 
        "1x1 matrix with value 1");
    
    // Edge case: 1x1 with negative value
    assertEquals(-5, Double.sumMatrix(new int[][] { { -5 } }), 
        "1x1 matrix with negative value");
    
    // Edge case: all zeros
    assertEquals(0, Double.sumMatrix(new int[][] { 
        { 0, 0, 0 }, 
        { 0, 0, 0 }, 
        { 0, 0, 0 } 
    }), "3x3 matrix of all zeros");
    
    // Edge case: all negative values
    assertEquals(-45, Double.sumMatrix(new int[][] { 
        { -1, -2, -3 }, 
        { -4, -5, -6 }, 
        { -7, -8, -9 } 
    }), "3x3 matrix of all negative values: sum = -45");
    
    // Edge case: mixed positive and negative
    assertEquals(0, Double.sumMatrix(new int[][] { 
        { 1, -1 }, 
        { -1, 1 } 
    }), "2x2 matrix with sum of 0");
    
    // Edge case: large values in 2x2
    assertEquals(1000000, Double.sumMatrix(new int[][] { 
        { 250000, 250000 }, 
        { 250000, 250000 } 
    }), "2x2 matrix with large values");
  }
}
