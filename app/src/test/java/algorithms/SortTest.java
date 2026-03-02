package algorithms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Vector;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Sort Algorithm Tests")
public class SortTest {

  // ============================================================================
  // SortVector Tests
  // ============================================================================

  @Test
  @DisplayName("SortVector: Empty vector")
  public void testSortVectorEmpty() {
    Vector<Integer> v = new Vector<>();
    Sort.SortVector(v);
    assertEquals(0, v.size());
  }

  @Test
  @DisplayName("SortVector: Single element")
  public void testSortVectorSingleElement() {
    Vector<Integer> v = new Vector<>();
    v.add(42);
    Sort.SortVector(v);
    assertEquals(1, v.size());
    assertEquals(42, v.get(0));
  }

  @Test
  @DisplayName("SortVector: Two elements - unsorted")
  public void testSortVectorTwoElements() {
    Vector<Integer> v = new Vector<>();
    v.add(5);
    v.add(3);
    Sort.SortVector(v);
    assertEquals(2, v.size());
    assertEquals(3, v.get(0));
    assertEquals(5, v.get(1));
    assertTrue(isSorted(v));
  }

  @Test
  @DisplayName("SortVector: 10 elements - random order")
  public void testSortVector10Random() {
    Vector<Integer> v = new Vector<>();
    int[] values = {7, 2, 8, 1, 9, 3, 6, 4, 5, 0};
    for (int val : values) {
      v.add(val);
    }
    Sort.SortVector(v);
    assertEquals(10, v.size());
    assertTrue(isSorted(v));
    verifyContainsExactly(v, values);
  }

  @Test
  @DisplayName("SortVector: 10 elements - already sorted")
  public void testSortVector10Sorted() {
    Vector<Integer> v = new Vector<>();
    for (int i = 0; i < 10; i++) {
      v.add(i);
    }
    Sort.SortVector(v);
    assertEquals(10, v.size());
    assertTrue(isSorted(v));
    for (int i = 0; i < 10; i++) {
      assertEquals(i, v.get(i));
    }
  }

  @Test
  @DisplayName("SortVector: 10 elements - reverse sorted")
  public void testSortVector10ReverseSorted() {
    Vector<Integer> v = new Vector<>();
    for (int i = 9; i >= 0; i--) {
      v.add(i);
    }
    Sort.SortVector(v);
    assertEquals(10, v.size());
    assertTrue(isSorted(v));
    for (int i = 0; i < 10; i++) {
      assertEquals(i, v.get(i));
    }
  }

  @Test
  @DisplayName("SortVector: 100 elements - random order")
  public void testSortVector100Random() {
    Vector<Integer> v = generateRandomVector(100, 500);
    Sort.SortVector(v);
    assertEquals(100, v.size());
    assertTrue(isSorted(v));
  }

  @Test
  @DisplayName("SortVector: 100 elements - already sorted")
  public void testSortVector100Sorted() {
    Vector<Integer> v = new Vector<>();
    for (int i = 0; i < 100; i++) {
      v.add(i);
    }
    Sort.SortVector(v);
    assertEquals(100, v.size());
    assertTrue(isSorted(v));
  }

  @Test
  @DisplayName("SortVector: 100 elements - reverse sorted")
  public void testSortVector100ReverseSorted() {
    Vector<Integer> v = new Vector<>();
    for (int i = 99; i >= 0; i--) {
      v.add(i);
    }
    Sort.SortVector(v);
    assertEquals(100, v.size());
    assertTrue(isSorted(v));
  }

  @Test
  @DisplayName("SortVector: 1000 elements - random order")
  public void testSortVector1000Random() {
    Vector<Integer> v = generateRandomVector(1000, 5000);
    Sort.SortVector(v);
    assertEquals(1000, v.size());
    assertTrue(isSorted(v));
  }

  @Test
  @DisplayName("SortVector: 1000 elements - already sorted")
  public void testSortVector1000Sorted() {
    Vector<Integer> v = new Vector<>();
    for (int i = 0; i < 1000; i++) {
      v.add(i);
    }
    Sort.SortVector(v);
    assertEquals(1000, v.size());
    assertTrue(isSorted(v));
  }

  @Test
  @DisplayName("SortVector: 1000 elements - reverse sorted")
  public void testSortVector1000ReverseSorted() {
    Vector<Integer> v = new Vector<>();
    for (int i = 999; i >= 0; i--) {
      v.add(i);
    }
    Sort.SortVector(v);
    assertEquals(1000, v.size());
    assertTrue(isSorted(v));
  }

  @Test
  @DisplayName("SortVector: Handles duplicates correctly")
  public void testSortVectorWithDuplicates() {
    Vector<Integer> v = new Vector<>();
    int[] values = {3, 1, 4, 1, 5, 9, 2, 6, 5, 3};
    for (int val : values) {
      v.add(val);
    }
    Sort.SortVector(v);
    assertTrue(isSorted(v));
    assertEquals(10, v.size());
  }

  @Test
  @DisplayName("SortVector: Handles negative numbers")
  public void testSortVectorNegativeNumbers() {
    Vector<Integer> v = new Vector<>();
    int[] values = {-5, 3, -2, 0, 7, -10};
    for (int val : values) {
      v.add(val);
    }
    Sort.SortVector(v);
    assertTrue(isSorted(v));
    assertEquals(-10, v.get(0));
    assertEquals(7, v.get(v.size() - 1));
  }

  // ============================================================================
  // DutchFlagPartition Tests
  // ============================================================================

  @Test
  @DisplayName("DutchFlagPartition: Simple partition with pivot 5")
  public void testDutchFlagPartitionSimple() {
    Vector<Integer> v = new Vector<>();
    int[] values = {3, 5, 7, 5, 1, 9, 5, 2};
    for (int val : values) {
      v.add(val);
    }
    int pivotValue = 5;
    Sort.DutchFlagPartition(v, pivotValue);

    // Verify partition: all < pivot on left, all == pivot in middle, all > pivot
    // on right
    int lessCount = 0;
    int equalCount = 0;
    for (int i = 0; i < v.size(); i++) {
      if (v.get(i) < pivotValue) {
        lessCount++;
      } else if (v.get(i) == pivotValue) {
        equalCount++;
      }
    }

    // Check that all elements < pivot are at the beginning
    for (int i = 0; i < lessCount; i++) {
      assertTrue(v.get(i) < pivotValue, "Element at " + i + " should be < pivot");
    }

    // Check that all elements == pivot come after the smaller elements
    for (int i = lessCount; i < lessCount + equalCount; i++) {
      assertEquals(pivotValue, v.get(i), "Element at " + i + " should equal pivot");
    }

    // Check that all remaining elements > pivot
    for (int i = lessCount + equalCount; i < v.size(); i++) {
      assertTrue(v.get(i) > pivotValue, "Element at " + i + " should be > pivot");
    }
  }

  @Test
  @DisplayName("DutchFlagPartition: All elements equal to pivot")
  public void testDutchFlagPartitionAllEqual() {
    Vector<Integer> v = new Vector<>();
    for (int i = 0; i < 5; i++) {
      v.add(5);
    }
    Sort.DutchFlagPartition(v, 5);
    assertEquals(5, v.size());
    for (int i = 0; i < 5; i++) {
      assertEquals(5, v.get(i));
    }
  }

  @Test
  @DisplayName("DutchFlagPartition: All elements less than pivot")
  public void testDutchFlagPartitionAllLess() {
    Vector<Integer> v = new Vector<>();
    int[] values = {1, 2, 3, 4, 5};
    for (int val : values) {
      v.add(val);
    }
    Sort.DutchFlagPartition(v, 10);
    assertEquals(5, v.size());
    for (int i = 0; i < 5; i++) {
      assertTrue(v.get(i) < 10);
    }
  }

  @Test
  @DisplayName("DutchFlagPartition: All elements greater than pivot")
  public void testDutchFlagPartitionAllGreater() {
    Vector<Integer> v = new Vector<>();
    int[] values = {6, 7, 8, 9, 10};
    for (int val : values) {
      v.add(val);
    }
    Sort.DutchFlagPartition(v, 5);
    assertEquals(5, v.size());
    for (int i = 0; i < 5; i++) {
      assertTrue(v.get(i) > 5);
    }
  }

  @Test
  @DisplayName("DutchFlagPartition: Pivot value 0")
  public void testDutchFlagPartitionPivotZero() {
    Vector<Integer> v = new Vector<>();
    int[] values = {-2, 0, 3, 0, -1, 5, 0};
    for (int val : values) {
      v.add(val);
    }
    Sort.DutchFlagPartition(v, 0);

    // All negative should be at start
    int lessCount = 0;
    for (int i = 0; i < v.size(); i++) {
      if (v.get(i) < 0) {
        lessCount++;
      }
    }
    for (int i = 0; i < lessCount; i++) {
      assertTrue(v.get(i) < 0);
    }

    // Zeros should come next
    int zeroCount = 0;
    for (int i = lessCount; i < v.size(); i++) {
      if (v.get(i) == 0) {
        zeroCount++;
      }
    }
    for (int i = lessCount; i < lessCount + zeroCount; i++) {
      assertEquals(0, v.get(i));
    }

    // Positives should be at end
    for (int i = lessCount + zeroCount; i < v.size(); i++) {
      assertTrue(v.get(i) > 0);
    }
  }

  @Test
  @DisplayName("DutchFlagPartition: Single element")
  public void testDutchFlagPartitionSingleElement() {
    Vector<Integer> v = new Vector<>();
    v.add(5);
    Sort.DutchFlagPartition(v, 5);
    assertEquals(1, v.size());
    assertEquals(5, v.get(0));
  }

  @Test
  @DisplayName("DutchFlagPartition: Two elements")
  public void testDutchFlagPartitionTwoElements() {
    Vector<Integer> v = new Vector<>();
    v.add(7);
    v.add(3);
    Sort.DutchFlagPartition(v, 5);
    assertEquals(2, v.size());
    assertTrue(v.get(0) < 5);
    assertTrue(v.get(1) > 5);
  }

  @Test
  @DisplayName("DutchFlagPartition: Large pivot value")
  public void testDutchFlagPartitionLargePivot() {
    Vector<Integer> v = new Vector<>();
    for (int i = 0; i < 20; i++) {
      v.add(i);
    }
    Sort.DutchFlagPartition(v, 100);
    // All elements should be < pivot
    for (int i = 0; i < v.size(); i++) {
      assertTrue(v.get(i) < 100);
    }
  }

  @Test
  @DisplayName("DutchFlagPartition: Negative pivot")
  public void testDutchFlagPartitionNegativePivot() {
    Vector<Integer> v = new Vector<>();
    int[] values = {-5, 0, 5, -2, 3, -8, 1};
    for (int val : values) {
      v.add(val);
    }
    Sort.DutchFlagPartition(v, -3);

    // Count elements
    int lessCount = 0;
    int equalCount = 0;
    for (int i = 0; i < v.size(); i++) {
      if (v.get(i) < -3) {
        lessCount++;
      } else if (v.get(i) == -3) {
        equalCount++;
      }
    }

    // Verify partition
    for (int i = 0; i < lessCount; i++) {
      assertTrue(v.get(i) < -3);
    }
    for (int i = lessCount; i < lessCount + equalCount; i++) {
      assertEquals(-3, v.get(i));
    }
    for (int i = lessCount + equalCount; i < v.size(); i++) {
      assertTrue(v.get(i) > -3);
    }
  }

  // ============================================================================
  // MaxN Tests
  // ============================================================================

  @Test
  @DisplayName("MaxN: N=1 from small vector")
  public void testMaxNOne() {
    Vector<Integer> v = new Vector<>();
    int[] values = {3, 1, 4, 1, 5};
    for (int val : values) {
      v.add(val);
    }
    Vector<Integer> result = Sort.MaxN(v, 1);
    assertEquals(1, result.size());
    assertEquals(5, result.get(0));
  }

  @Test
  @DisplayName("MaxN: N=5 from 10 elements")
  public void testMaxNFive() {
    Vector<Integer> v = new Vector<>();
    for (int i = 0; i < 10; i++) {
      v.add(i);
    }
    Vector<Integer> result = Sort.MaxN(v, 5);
    assertEquals(5, result.size());
    // Results should be in descending order: 9, 8, 7, 6, 5
    assertEquals(9, result.get(0));
    assertEquals(8, result.get(1));
    assertEquals(7, result.get(2));
    assertEquals(6, result.get(3));
    assertEquals(5, result.get(4));
    assertTrue(isDescendingOrder(result));
  }

  @Test
  @DisplayName("MaxN: N=10 from 10 elements")
  public void testMaxNAll() {
    Vector<Integer> v = new Vector<>();
    for (int i = 0; i < 10; i++) {
      v.add(i);
    }
    Vector<Integer> result = Sort.MaxN(v, 10);
    assertEquals(10, v.size());
    assertTrue(isDescendingOrder(result));
  }

  @Test
  @DisplayName("MaxN: N=0 should return empty")
  public void testMaxNZero() {
    Vector<Integer> v = new Vector<>();
    for (int i = 0; i < 10; i++) {
      v.add(i);
    }
    Vector<Integer> result = Sort.MaxN(v, 0);
    assertEquals(0, result.size());
  }

  @Test
  @DisplayName("MaxN: N > vector size should return empty")
  public void testMaxNGreaterThanSize() {
    Vector<Integer> v = new Vector<>();
    for (int i = 0; i < 5; i++) {
      v.add(i);
    }
    Vector<Integer> result = Sort.MaxN(v, 10);
    assertEquals(0, result.size());
  }

  @Test
  @DisplayName("MaxN: N=100 from 100 random elements")
  public void testMaxN100From100() {
    Vector<Integer> v = generateRandomVector(100, 1000);
    Vector<Integer> result = Sort.MaxN(v, 100);
    assertEquals(100, result.size());
    assertTrue(isDescendingOrder(result));
  }

  @Test
  @DisplayName("MaxN: N=10 from 100 elements")
  public void testMaxN10From100() {
    Vector<Integer> v = new Vector<>();
    for (int i = 0; i < 100; i++) {
      v.add(i);
    }
    Vector<Integer> result = Sort.MaxN(v, 10);
    assertEquals(10, result.size());
    // Should be: 99, 98, 97, ..., 90
    assertEquals(99, result.get(0));
    assertEquals(90, result.get(9));
    assertTrue(isDescendingOrder(result));
    // Verify all elements are largest in original vector
    for (int val : result) {
      assertTrue(val >= 90);
    }
  }

  @Test
  @DisplayName("MaxN: N=1 from 1000 elements")
  public void testMaxN1From1000() {
    Vector<Integer> v = generateRandomVector(1000, 10000);
    Vector<Integer> result = Sort.MaxN(v, 1);
    assertEquals(1, result.size());
    // Verify it's the maximum
    int max = Integer.MIN_VALUE;
    for (int val : v) {
      max = Math.max(max, val);
    }
    assertEquals(max, result.get(0));
  }

  @Test
  @DisplayName("MaxN: N=10 from 1000 elements")
  public void testMaxN10From1000() {
    Vector<Integer> v = generateRandomVector(1000, 10000);
    Vector<Integer> result = Sort.MaxN(v, 10);
    assertEquals(10, result.size());
    assertTrue(isDescendingOrder(result));
    // All results should be >= the 10th largest
    int minInResult = result.get(result.size() - 1);
    int countLarger = 0;
    for (int val : v) {
      if (val > result.get(result.size() - 1)) {
        countLarger++;
      }
    }
    assertTrue(countLarger == 0 || countLarger < 10);
  }

  @Test
  @DisplayName("MaxN: N=5 from vector with duplicates")
  public void testMaxNWithDuplicates() {
    Vector<Integer> v = new Vector<>();
    int[] values = {5, 5, 5, 3, 3, 1, 7, 7, 7, 7};
    for (int val : values) {
      v.add(val);
    }
    Vector<Integer> result = Sort.MaxN(v, 5);
    assertEquals(5, result.size());
    assertTrue(isDescendingOrder(result));
    // All results should be >= 5
    for (int val : result) {
      assertTrue(val >= 5);
    }
  }

  @Test
  @DisplayName("MaxN: N=5 from vector with negative numbers")
  public void testMaxNWithNegatives() {
    Vector<Integer> v = new Vector<>();
    int[] values = {-5, 3, -2, 0, 7, -10, 5, -1};
    for (int val : values) {
      v.add(val);
    }
    Vector<Integer> result = Sort.MaxN(v, 5);
    assertEquals(5, result.size());
    assertTrue(isDescendingOrder(result));
    // Should be: 7, 5, 3, 0, -1
    assertEquals(7, result.get(0));
    assertEquals(-1, result.get(4));
  }

  @Test
  @DisplayName("MaxN: Single element vector")
  public void testMaxNSingleElement() {
    Vector<Integer> v = new Vector<>();
    v.add(42);
    Vector<Integer> result = Sort.MaxN(v, 1);
    assertEquals(1, result.size());
    assertEquals(42, result.get(0));
  }

  // ============================================================================
  // Helper Methods
  // ============================================================================

  /**
   * Checks if a vector is sorted in ascending order
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
   * Checks if a vector is sorted in descending order
   */
  private boolean isDescendingOrder(Vector<Integer> v) {
    for (int i = 0; i < v.size() - 1; i++) {
      if (v.get(i) < v.get(i + 1)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Verifies that a vector contains exactly the specified values (in any order)
   */
  private void verifyContainsExactly(Vector<Integer> v, int[] expected) {
    Vector<Integer> remaining = new Vector<>();
    for (int val : expected) {
      remaining.add(val);
    }
    for (int val : v) {
      assertTrue(remaining.remove((Integer) val),
          "Vector contains unexpected value: " + val);
    }
    assertEquals(0, remaining.size(), "Vector missing expected values");
  }

  /**
   * Generates a random vector with specified size and max value
   */
  private Vector<Integer> generateRandomVector(int size, int maxValue) {
    Vector<Integer> v = new Vector<>();
    for (int i = 0; i < size; i++) {
      v.add((int) (Math.random() * maxValue));
    }
    return v;
  }
}
