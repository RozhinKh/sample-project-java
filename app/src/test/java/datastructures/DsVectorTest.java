package datastructures;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Vector;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("DsVector Data Structure Tests")
public class DsVectorTest {

  // ============================================================================
  // modifyVector Tests
  // ============================================================================

  @Test
  @DisplayName("modifyVector: 10 elements - all incremented by 1")
  public void testModifyVector10Elements() {
    Vector<Integer> v = new Vector<>();
    for (int i = 0; i < 10; i++) {
      v.add(i);
    }
    Vector<Integer> result = DsVector.modifyVector(v);
    assertEquals(10, result.size());
    for (int i = 0; i < 10; i++) {
      assertEquals(i + 1, result.get(i));
    }
  }

  @Test
  @DisplayName("modifyVector: 100 elements - all incremented by 1")
  public void testModifyVector100Elements() {
    Vector<Integer> v = new Vector<>();
    for (int i = 0; i < 100; i++) {
      v.add(i);
    }
    Vector<Integer> result = DsVector.modifyVector(v);
    assertEquals(100, result.size());
    for (int i = 0; i < 100; i++) {
      assertEquals(i + 1, result.get(i));
    }
  }

  @Test
  @DisplayName("modifyVector: 1000 elements - all incremented by 1")
  public void testModifyVector1000Elements() {
    Vector<Integer> v = new Vector<>();
    for (int i = 0; i < 1000; i++) {
      v.add(i);
    }
    Vector<Integer> result = DsVector.modifyVector(v);
    assertEquals(1000, result.size());
    for (int i = 0; i < 1000; i++) {
      assertEquals(i + 1, result.get(i));
    }
  }

  @Test
  @DisplayName("modifyVector: Single element")
  public void testModifyVectorSingleElement() {
    Vector<Integer> v = new Vector<>();
    v.add(5);
    Vector<Integer> result = DsVector.modifyVector(v);
    assertEquals(1, result.size());
    assertEquals(6, result.get(0));
  }

  @Test
  @DisplayName("modifyVector: Negative numbers incremented")
  public void testModifyVectorNegativeNumbers() {
    Vector<Integer> v = new Vector<>();
    v.add(-5);
    v.add(-2);
    v.add(0);
    v.add(3);
    Vector<Integer> result = DsVector.modifyVector(v);
    assertEquals(4, result.size());
    assertEquals(-4, result.get(0));
    assertEquals(-1, result.get(1));
    assertEquals(1, result.get(2));
    assertEquals(4, result.get(3));
  }

  // ============================================================================
  // searchVector Tests
  // ============================================================================

  @Test
  @DisplayName("searchVector: 10 elements - no matches")
  public void testSearchVector10NoMatches() {
    Vector<Integer> v = new Vector<>();
    for (int i = 0; i < 10; i++) {
      v.add(i);
    }
    Vector<Integer> result = DsVector.searchVector(v, 99);
    assertEquals(0, result.size());
  }

  @Test
  @DisplayName("searchVector: 10 elements - single match")
  public void testSearchVector10SingleMatch() {
    Vector<Integer> v = new Vector<>();
    for (int i = 0; i < 10; i++) {
      v.add(i);
    }
    Vector<Integer> result = DsVector.searchVector(v, 5);
    assertEquals(1, result.size());
    assertEquals(5, result.get(0));
  }

  @Test
  @DisplayName("searchVector: 10 elements - multiple matches")
  public void testSearchVector10MultipleMatches() {
    Vector<Integer> v = new Vector<>();
    int[] values = {1, 2, 3, 2, 4, 2, 5, 2, 6, 2};
    for (int val : values) {
      v.add(val);
    }
    Vector<Integer> result = DsVector.searchVector(v, 2);
    assertEquals(5, result.size());
    assertEquals(1, result.get(0));
    assertEquals(3, result.get(1));
    assertEquals(5, result.get(2));
    assertEquals(7, result.get(3));
    assertEquals(9, result.get(4));
  }

  @Test
  @DisplayName("searchVector: 100 elements - no matches")
  public void testSearchVector100NoMatches() {
    Vector<Integer> v = generateRandomVector(100, 50);
    Vector<Integer> result = DsVector.searchVector(v, 999);
    assertEquals(0, result.size());
  }

  @Test
  @DisplayName("searchVector: 100 elements - single match")
  public void testSearchVector100SingleMatch() {
    Vector<Integer> v = new Vector<>();
    for (int i = 0; i < 100; i++) {
      v.add(i);
    }
    Vector<Integer> result = DsVector.searchVector(v, 50);
    assertEquals(1, result.size());
    assertEquals(50, result.get(0));
  }

  @Test
  @DisplayName("searchVector: 1000 elements - no matches")
  public void testSearchVector1000NoMatches() {
    Vector<Integer> v = generateRandomVector(1000, 500);
    Vector<Integer> result = DsVector.searchVector(v, 9999);
    assertEquals(0, result.size());
  }

  @Test
  @DisplayName("searchVector: 1000 elements - multiple matches")
  public void testSearchVector1000MultipleMatches() {
    Vector<Integer> v = new Vector<>();
    // Add target value 42 at specific indices
    for (int i = 0; i < 1000; i++) {
      if (i % 100 == 0) {
        v.add(42);
      } else {
        v.add(i);
      }
    }
    Vector<Integer> result = DsVector.searchVector(v, 42);
    assertEquals(10, result.size());
    for (int i = 0; i < 10; i++) {
      assertEquals(i * 100, result.get(i));
    }
  }

  @Test
  @DisplayName("searchVector: Match at beginning")
  public void testSearchVectorMatchAtBeginning() {
    Vector<Integer> v = new Vector<>();
    v.add(5);
    v.add(1);
    v.add(2);
    v.add(3);
    Vector<Integer> result = DsVector.searchVector(v, 5);
    assertEquals(1, result.size());
    assertEquals(0, result.get(0));
  }

  @Test
  @DisplayName("searchVector: Match at end")
  public void testSearchVectorMatchAtEnd() {
    Vector<Integer> v = new Vector<>();
    v.add(1);
    v.add(2);
    v.add(3);
    v.add(5);
    Vector<Integer> result = DsVector.searchVector(v, 5);
    assertEquals(1, result.size());
    assertEquals(3, result.get(0));
  }

  // ============================================================================
  // sortVector Tests
  // ============================================================================

  @Test
  @DisplayName("sortVector: 10 elements - random order")
  public void testSortVector10Random() {
    Vector<Integer> v = new Vector<>();
    int[] values = {7, 2, 8, 1, 9, 3, 6, 4, 5, 0};
    for (int val : values) {
      v.add(val);
    }
    Vector<Integer> result = DsVector.sortVector(v);
    assertEquals(10, result.size());
    assertTrue(isSorted(result));
    verifyContainsElements(result, values);
  }

  @Test
  @DisplayName("sortVector: 10 elements - already sorted")
  public void testSortVector10Sorted() {
    Vector<Integer> v = new Vector<>();
    for (int i = 0; i < 10; i++) {
      v.add(i);
    }
    Vector<Integer> result = DsVector.sortVector(v);
    assertEquals(10, result.size());
    assertTrue(isSorted(result));
    for (int i = 0; i < 10; i++) {
      assertEquals(i, result.get(i));
    }
  }

  @Test
  @DisplayName("sortVector: 10 elements - reverse sorted")
  public void testSortVector10ReverseSorted() {
    Vector<Integer> v = new Vector<>();
    for (int i = 9; i >= 0; i--) {
      v.add(i);
    }
    Vector<Integer> result = DsVector.sortVector(v);
    assertEquals(10, result.size());
    assertTrue(isSorted(result));
    for (int i = 0; i < 10; i++) {
      assertEquals(i, result.get(i));
    }
  }

  @Test
  @DisplayName("sortVector: 100 elements - random order")
  public void testSortVector100Random() {
    Vector<Integer> v = generateRandomVector(100, 500);
    Vector<Integer> result = DsVector.sortVector(v);
    assertEquals(100, result.size());
    assertTrue(isSorted(result));
  }

  @Test
  @DisplayName("sortVector: 100 elements - already sorted")
  public void testSortVector100Sorted() {
    Vector<Integer> v = new Vector<>();
    for (int i = 0; i < 100; i++) {
      v.add(i);
    }
    Vector<Integer> result = DsVector.sortVector(v);
    assertEquals(100, result.size());
    assertTrue(isSorted(result));
  }

  @Test
  @DisplayName("sortVector: 100 elements - reverse sorted")
  public void testSortVector100ReverseSorted() {
    Vector<Integer> v = new Vector<>();
    for (int i = 99; i >= 0; i--) {
      v.add(i);
    }
    Vector<Integer> result = DsVector.sortVector(v);
    assertEquals(100, result.size());
    assertTrue(isSorted(result));
  }

  @Test
  @DisplayName("sortVector: 1000 elements - random order")
  public void testSortVector1000Random() {
    Vector<Integer> v = generateRandomVector(1000, 5000);
    Vector<Integer> result = DsVector.sortVector(v);
    assertEquals(1000, result.size());
    assertTrue(isSorted(result));
  }

  @Test
  @DisplayName("sortVector: 1000 elements - already sorted")
  public void testSortVector1000Sorted() {
    Vector<Integer> v = new Vector<>();
    for (int i = 0; i < 1000; i++) {
      v.add(i);
    }
    Vector<Integer> result = DsVector.sortVector(v);
    assertEquals(1000, result.size());
    assertTrue(isSorted(result));
  }

  @Test
  @DisplayName("sortVector: 1000 elements - reverse sorted")
  public void testSortVector1000ReverseSorted() {
    Vector<Integer> v = new Vector<>();
    for (int i = 999; i >= 0; i--) {
      v.add(i);
    }
    Vector<Integer> result = DsVector.sortVector(v);
    assertEquals(1000, result.size());
    assertTrue(isSorted(result));
  }

  @Test
  @DisplayName("sortVector: With duplicates")
  public void testSortVectorWithDuplicates() {
    Vector<Integer> v = new Vector<>();
    int[] values = {3, 1, 4, 1, 5, 9, 2, 6, 5, 3};
    for (int val : values) {
      v.add(val);
    }
    Vector<Integer> result = DsVector.sortVector(v);
    assertEquals(10, result.size());
    assertTrue(isSorted(result));
  }

  @Test
  @DisplayName("sortVector: With negative numbers")
  public void testSortVectorNegativeNumbers() {
    Vector<Integer> v = new Vector<>();
    int[] values = {-5, 3, -2, 0, 7, -10};
    for (int val : values) {
      v.add(val);
    }
    Vector<Integer> result = DsVector.sortVector(v);
    assertEquals(6, result.size());
    assertTrue(isSorted(result));
    assertEquals(-10, result.get(0));
    assertEquals(7, result.get(result.size() - 1));
  }

  // ============================================================================
  // reverseVector Tests
  // ============================================================================

  @Test
  @DisplayName("reverseVector: 10 elements (even size)")
  public void testReverseVector10Elements() {
    Vector<Integer> v = new Vector<>();
    for (int i = 0; i < 10; i++) {
      v.add(i);
    }
    Vector<Integer> result = DsVector.reverseVector(v);
    assertEquals(10, result.size());
    for (int i = 0; i < 10; i++) {
      assertEquals(9 - i, result.get(i));
    }
  }

  @Test
  @DisplayName("reverseVector: 11 elements (odd size)")
  public void testReverseVector11Elements() {
    Vector<Integer> v = new Vector<>();
    for (int i = 0; i < 11; i++) {
      v.add(i);
    }
    Vector<Integer> result = DsVector.reverseVector(v);
    assertEquals(11, result.size());
    for (int i = 0; i < 11; i++) {
      assertEquals(10 - i, result.get(i));
    }
  }

  @Test
  @DisplayName("reverseVector: 100 elements (even size)")
  public void testReverseVector100Elements() {
    Vector<Integer> v = new Vector<>();
    for (int i = 0; i < 100; i++) {
      v.add(i);
    }
    Vector<Integer> result = DsVector.reverseVector(v);
    assertEquals(100, result.size());
    for (int i = 0; i < 100; i++) {
      assertEquals(99 - i, result.get(i));
    }
  }

  @Test
  @DisplayName("reverseVector: 101 elements (odd size)")
  public void testReverseVector101Elements() {
    Vector<Integer> v = new Vector<>();
    for (int i = 0; i < 101; i++) {
      v.add(i);
    }
    Vector<Integer> result = DsVector.reverseVector(v);
    assertEquals(101, result.size());
    for (int i = 0; i < 101; i++) {
      assertEquals(100 - i, result.get(i));
    }
  }

  @Test
  @DisplayName("reverseVector: 1000 elements (even size)")
  public void testReverseVector1000Elements() {
    Vector<Integer> v = new Vector<>();
    for (int i = 0; i < 1000; i++) {
      v.add(i);
    }
    Vector<Integer> result = DsVector.reverseVector(v);
    assertEquals(1000, result.size());
    for (int i = 0; i < 1000; i++) {
      assertEquals(999 - i, result.get(i));
    }
  }

  @Test
  @DisplayName("reverseVector: Single element")
  public void testReverseVectorSingleElement() {
    Vector<Integer> v = new Vector<>();
    v.add(42);
    Vector<Integer> result = DsVector.reverseVector(v);
    assertEquals(1, result.size());
    assertEquals(42, result.get(0));
  }

  @Test
  @DisplayName("reverseVector: Two elements")
  public void testReverseVectorTwoElements() {
    Vector<Integer> v = new Vector<>();
    v.add(1);
    v.add(2);
    Vector<Integer> result = DsVector.reverseVector(v);
    assertEquals(2, result.size());
    assertEquals(2, result.get(0));
    assertEquals(1, result.get(1));
  }

  // ============================================================================
  // rotateVector Tests
  // ============================================================================

  @Test
  @DisplayName("rotateVector: 10 elements - rotate by 0")
  public void testRotateVector10RotateBy0() {
    Vector<Integer> v = new Vector<>();
    for (int i = 0; i < 10; i++) {
      v.add(i);
    }
    Vector<Integer> result = DsVector.rotateVector(v, 0);
    assertEquals(10, result.size());
    for (int i = 0; i < 10; i++) {
      assertEquals(i, result.get(i));
    }
  }

  @Test
  @DisplayName("rotateVector: 10 elements - rotate by 1")
  public void testRotateVector10RotateBy1() {
    Vector<Integer> v = new Vector<>();
    for (int i = 0; i < 10; i++) {
      v.add(i);
    }
    Vector<Integer> result = DsVector.rotateVector(v, 1);
    assertEquals(10, result.size());
    for (int i = 0; i < 9; i++) {
      assertEquals(i + 1, result.get(i));
    }
    assertEquals(0, result.get(9));
  }

  @Test
  @DisplayName("rotateVector: 10 elements - rotate by 5 (half size)")
  public void testRotateVector10RotateByHalf() {
    Vector<Integer> v = new Vector<>();
    for (int i = 0; i < 10; i++) {
      v.add(i);
    }
    Vector<Integer> result = DsVector.rotateVector(v, 5);
    assertEquals(10, result.size());
    for (int i = 0; i < 5; i++) {
      assertEquals(i + 5, result.get(i));
    }
    for (int i = 5; i < 10; i++) {
      assertEquals(i - 5, result.get(i));
    }
  }

  @Test
  @DisplayName("rotateVector: 10 elements - rotate by 10 (full size)")
  public void testRotateVector10RotateBySize() {
    Vector<Integer> v = new Vector<>();
    for (int i = 0; i < 10; i++) {
      v.add(i);
    }
    Vector<Integer> result = DsVector.rotateVector(v, 10);
    assertEquals(10, result.size());
    for (int i = 0; i < 10; i++) {
      assertEquals(i, result.get(i));
    }
  }

  @Test
  @DisplayName("rotateVector: 100 elements - rotate by 1")
  public void testRotateVector100RotateBy1() {
    Vector<Integer> v = new Vector<>();
    for (int i = 0; i < 100; i++) {
      v.add(i);
    }
    Vector<Integer> result = DsVector.rotateVector(v, 1);
    assertEquals(100, result.size());
    for (int i = 0; i < 99; i++) {
      assertEquals(i + 1, result.get(i));
    }
    assertEquals(0, result.get(99));
  }

  @Test
  @DisplayName("rotateVector: 100 elements - rotate by 50 (half size)")
  public void testRotateVector100RotateByHalf() {
    Vector<Integer> v = new Vector<>();
    for (int i = 0; i < 100; i++) {
      v.add(i);
    }
    Vector<Integer> result = DsVector.rotateVector(v, 50);
    assertEquals(100, result.size());
    for (int i = 0; i < 50; i++) {
      assertEquals(i + 50, result.get(i));
    }
    for (int i = 50; i < 100; i++) {
      assertEquals(i - 50, result.get(i));
    }
  }

  @Test
  @DisplayName("rotateVector: 1000 elements - rotate by 1")
  public void testRotateVector1000RotateBy1() {
    Vector<Integer> v = new Vector<>();
    for (int i = 0; i < 1000; i++) {
      v.add(i);
    }
    Vector<Integer> result = DsVector.rotateVector(v, 1);
    assertEquals(1000, result.size());
    for (int i = 0; i < 999; i++) {
      assertEquals(i + 1, result.get(i));
    }
    assertEquals(0, result.get(999));
  }

  @Test
  @DisplayName("rotateVector: 1000 elements - rotate by 500 (half size)")
  public void testRotateVector1000RotateByHalf() {
    Vector<Integer> v = new Vector<>();
    for (int i = 0; i < 1000; i++) {
      v.add(i);
    }
    Vector<Integer> result = DsVector.rotateVector(v, 500);
    assertEquals(1000, result.size());
    for (int i = 0; i < 500; i++) {
      assertEquals(i + 500, result.get(i));
    }
    for (int i = 500; i < 1000; i++) {
      assertEquals(i - 500, result.get(i));
    }
  }

  @Test
  @DisplayName("rotateVector: Single element - rotate by 1")
  public void testRotateVectorSingleElement() {
    Vector<Integer> v = new Vector<>();
    v.add(42);
    Vector<Integer> result = DsVector.rotateVector(v, 1);
    assertEquals(1, result.size());
    assertEquals(42, result.get(0));
  }

  // ============================================================================
  // mergeVectors Tests
  // ============================================================================

  @Test
  @DisplayName("mergeVectors: Both empty vectors")
  public void testMergeVectorsBothEmpty() {
    Vector<Integer> v1 = new Vector<>();
    Vector<Integer> v2 = new Vector<>();
    Vector<Integer> result = DsVector.mergeVectors(v1, v2);
    assertEquals(0, result.size());
  }

  @Test
  @DisplayName("mergeVectors: First empty, second with 10 elements")
  public void testMergeVectorsFirstEmpty() {
    Vector<Integer> v1 = new Vector<>();
    Vector<Integer> v2 = new Vector<>();
    for (int i = 0; i < 10; i++) {
      v2.add(i);
    }
    Vector<Integer> result = DsVector.mergeVectors(v1, v2);
    assertEquals(10, result.size());
    for (int i = 0; i < 10; i++) {
      assertEquals(i, result.get(i));
    }
  }

  @Test
  @DisplayName("mergeVectors: First with 10, second empty")
  public void testMergeVectorsSecondEmpty() {
    Vector<Integer> v1 = new Vector<>();
    Vector<Integer> v2 = new Vector<>();
    for (int i = 0; i < 10; i++) {
      v1.add(i);
    }
    Vector<Integer> result = DsVector.mergeVectors(v1, v2);
    assertEquals(10, result.size());
    for (int i = 0; i < 10; i++) {
      assertEquals(i, result.get(i));
    }
  }

  @Test
  @DisplayName("mergeVectors: 10 + 10 elements")
  public void testMergeVectors10Plus10() {
    Vector<Integer> v1 = new Vector<>();
    Vector<Integer> v2 = new Vector<>();
    for (int i = 0; i < 10; i++) {
      v1.add(i);
    }
    for (int i = 10; i < 20; i++) {
      v2.add(i);
    }
    Vector<Integer> result = DsVector.mergeVectors(v1, v2);
    assertEquals(20, result.size());
    for (int i = 0; i < 20; i++) {
      assertEquals(i, result.get(i));
    }
  }

  @Test
  @DisplayName("mergeVectors: 50 + 50 elements")
  public void testMergeVectors50Plus50() {
    Vector<Integer> v1 = new Vector<>();
    Vector<Integer> v2 = new Vector<>();
    for (int i = 0; i < 50; i++) {
      v1.add(i);
    }
    for (int i = 50; i < 100; i++) {
      v2.add(i);
    }
    Vector<Integer> result = DsVector.mergeVectors(v1, v2);
    assertEquals(100, result.size());
    for (int i = 0; i < 100; i++) {
      assertEquals(i, result.get(i));
    }
  }

  @Test
  @DisplayName("mergeVectors: 100 + 100 elements")
  public void testMergeVectors100Plus100() {
    Vector<Integer> v1 = new Vector<>();
    Vector<Integer> v2 = new Vector<>();
    for (int i = 0; i < 100; i++) {
      v1.add(i);
    }
    for (int i = 100; i < 200; i++) {
      v2.add(i);
    }
    Vector<Integer> result = DsVector.mergeVectors(v1, v2);
    assertEquals(200, result.size());
    for (int i = 0; i < 200; i++) {
      assertEquals(i, result.get(i));
    }
  }

  @Test
  @DisplayName("mergeVectors: 500 + 500 elements")
  public void testMergeVectors500Plus500() {
    Vector<Integer> v1 = new Vector<>();
    Vector<Integer> v2 = new Vector<>();
    for (int i = 0; i < 500; i++) {
      v1.add(i);
    }
    for (int i = 500; i < 1000; i++) {
      v2.add(i);
    }
    Vector<Integer> result = DsVector.mergeVectors(v1, v2);
    assertEquals(1000, result.size());
    for (int i = 0; i < 1000; i++) {
      assertEquals(i, result.get(i));
    }
  }

  @Test
  @DisplayName("mergeVectors: Unequal sizes - 10 + 20")
  public void testMergeVectorsUnequalSizes10Plus20() {
    Vector<Integer> v1 = new Vector<>();
    Vector<Integer> v2 = new Vector<>();
    for (int i = 0; i < 10; i++) {
      v1.add(i);
    }
    for (int i = 10; i < 30; i++) {
      v2.add(i);
    }
    Vector<Integer> result = DsVector.mergeVectors(v1, v2);
    assertEquals(30, result.size());
    for (int i = 0; i < 30; i++) {
      assertEquals(i, result.get(i));
    }
  }

  @Test
  @DisplayName("mergeVectors: Single element + single element")
  public void testMergeVectorsSinglePlusSingle() {
    Vector<Integer> v1 = new Vector<>();
    Vector<Integer> v2 = new Vector<>();
    v1.add(1);
    v2.add(2);
    Vector<Integer> result = DsVector.mergeVectors(v1, v2);
    assertEquals(2, result.size());
    assertEquals(1, result.get(0));
    assertEquals(2, result.get(1));
  }

  @Test
  @DisplayName("mergeVectors: Preserves element order")
  public void testMergeVectorsPreservesOrder() {
    Vector<Integer> v1 = new Vector<>();
    Vector<Integer> v2 = new Vector<>();
    int[] vals1 = {7, 2, 8, 1};
    int[] vals2 = {9, 3, 6, 4};
    for (int val : vals1) {
      v1.add(val);
    }
    for (int val : vals2) {
      v2.add(val);
    }
    Vector<Integer> result = DsVector.mergeVectors(v1, v2);
    assertEquals(8, result.size());
    for (int i = 0; i < 4; i++) {
      assertEquals(vals1[i], result.get(i));
    }
    for (int i = 0; i < 4; i++) {
      assertEquals(vals2[i], result.get(i + 4));
    }
  }

  // ============================================================================
  // Helper Methods
  // ============================================================================

  /**
   * Verifies that a vector is sorted in ascending order
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
   * Generates a random vector with specified size and max value
   */
  private Vector<Integer> generateRandomVector(int size, int maxValue) {
    Vector<Integer> v = new Vector<>();
    for (int i = 0; i < size; i++) {
      v.add((int) (Math.random() * maxValue));
    }
    return v;
  }

  /**
   * Verifies that a vector contains all elements from the provided array
   * (regardless of order)
   */
  private void verifyContainsElements(Vector<Integer> v, int[] elements) {
    assertEquals(v.size(), elements.length);
    Vector<Integer> temp = new Vector<>(v);
    for (int element : elements) {
      assertTrue(temp.remove(Integer.valueOf(element)),
          "Vector should contain element: " + element);
    }
  }
}
