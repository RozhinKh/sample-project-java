package datastructures;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedList;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("DsLinkedList Data Structure Tests")
public class DsLinkedListTest {

  // ============================================================================
  // shuffle Tests
  // ============================================================================

  @Test
  @DisplayName("shuffle: 100 elements - size unchanged")
  public void testShuffle100SizeUnchanged() {
    LinkedList<Integer> list = createSequentialList(100);
    LinkedList<Integer> result = DsLinkedList.shuffle(list);
    assertEquals(100, result.size(), "Shuffled list should have same size as original");
  }

  @Test
  @DisplayName("shuffle: 1000 elements - size unchanged")
  public void testShuffle1000SizeUnchanged() {
    LinkedList<Integer> list = createSequentialList(1000);
    LinkedList<Integer> result = DsLinkedList.shuffle(list);
    assertEquals(1000, result.size(), "Shuffled list should have same size as original");
  }

  @Test
  @DisplayName("shuffle: 5000 elements - size unchanged")
  public void testShuffle5000SizeUnchanged() {
    LinkedList<Integer> list = createSequentialList(5000);
    LinkedList<Integer> result = DsLinkedList.shuffle(list);
    assertEquals(5000, result.size(), "Shuffled list should have same size as original");
  }

  @Test
  @DisplayName("shuffle: All elements present after shuffle (100 elements)")
  public void testShuffleAllElementsPresent100() {
    LinkedList<Integer> list = createSequentialList(100);
    LinkedList<Integer> result = DsLinkedList.shuffle(list);
    verifyAllElementsPresent(result, 0, 99);
  }

  @Test
  @DisplayName("shuffle: All elements present after shuffle (1000 elements)")
  public void testShuffleAllElementsPresent1000() {
    LinkedList<Integer> list = createSequentialList(1000);
    LinkedList<Integer> result = DsLinkedList.shuffle(list);
    verifyAllElementsPresent(result, 0, 999);
  }

  @Test
  @DisplayName("shuffle: Randomization verified (multiple shuffles differ)")
  public void testShuffleRandomization() {
    LinkedList<Integer> list = createSequentialList(100);
    LinkedList<Integer> shuffle1 = DsLinkedList.shuffle(list);
    LinkedList<Integer> shuffle2 = DsLinkedList.shuffle(list);
    
    // With high probability, two shuffles of same list will differ
    // If they're identical, try again
    int attempts = 0;
    while (listsEqual(shuffle1, shuffle2) && attempts < 5) {
      shuffle2 = DsLinkedList.shuffle(list);
      attempts++;
    }
    
    assertTrue(!listsEqual(shuffle1, shuffle2),
        "Multiple shuffles should produce different orderings (with very high probability)");
  }

  @Test
  @DisplayName("shuffle: Empty list")
  public void testShuffleEmpty() {
    LinkedList<Integer> list = new LinkedList<>();
    LinkedList<Integer> result = DsLinkedList.shuffle(list);
    assertEquals(0, result.size(), "Shuffled empty list should remain empty");
  }

  @Test
  @DisplayName("shuffle: Single element")
  public void testShuffleSingleElement() {
    LinkedList<Integer> list = new LinkedList<>();
    list.add(42);
    LinkedList<Integer> result = DsLinkedList.shuffle(list);
    assertEquals(1, result.size(), "Shuffled single element list should have size 1");
    assertEquals(42, result.get(0), "Single element should remain unchanged");
  }

  @Test
  @DisplayName("shuffle: Two elements - both present")
  public void testShuffleTwoElements() {
    LinkedList<Integer> list = new LinkedList<>();
    list.add(1);
    list.add(2);
    LinkedList<Integer> result = DsLinkedList.shuffle(list);
    assertEquals(2, result.size());
    verifyAllElementsPresent(result, 1, 2);
  }

  // ============================================================================
  // slice Tests
  // ============================================================================

  @Test
  @DisplayName("slice: Full list slice (start=0, end=size)")
  public void testSliceFullList() {
    LinkedList<Integer> list = createSequentialList(10);
    LinkedList<Integer> result = DsLinkedList.slice(list, 0, 10);
    assertEquals(10, result.size(), "Full slice should have same size as original");
    for (int i = 0; i < 10; i++) {
      assertEquals(i, result.get(i), "Full slice should contain same elements in same order");
    }
  }

  @Test
  @DisplayName("slice: Slice from middle (5 elements)")
  public void testSliceMiddle() {
    LinkedList<Integer> list = createSequentialList(10);
    LinkedList<Integer> result = DsLinkedList.slice(list, 2, 7);
    assertEquals(5, result.size(), "Slice from 2 to 7 should have size 5");
    for (int i = 0; i < 5; i++) {
      assertEquals(i + 2, result.get(i), "Slice should preserve order starting from index 2");
    }
  }

  @Test
  @DisplayName("slice: Slice first half")
  public void testSliceFirstHalf() {
    LinkedList<Integer> list = createSequentialList(100);
    LinkedList<Integer> result = DsLinkedList.slice(list, 0, 50);
    assertEquals(50, result.size());
    for (int i = 0; i < 50; i++) {
      assertEquals(i, result.get(i), "First half slice should contain elements 0-49");
    }
  }

  @Test
  @DisplayName("slice: Slice second half")
  public void testSliceSecondHalf() {
    LinkedList<Integer> list = createSequentialList(100);
    LinkedList<Integer> result = DsLinkedList.slice(list, 50, 100);
    assertEquals(50, result.size());
    for (int i = 0; i < 50; i++) {
      assertEquals(i + 50, result.get(i), "Second half slice should contain elements 50-99");
    }
  }

  @Test
  @DisplayName("slice: Empty slice (start=end)")
  public void testSliceEmpty() {
    LinkedList<Integer> list = createSequentialList(10);
    LinkedList<Integer> result = DsLinkedList.slice(list, 5, 5);
    assertEquals(0, result.size(), "Slice with start=end should be empty");
  }

  @Test
  @DisplayName("slice: Single element slice")
  public void testSliceSingleElement() {
    LinkedList<Integer> list = createSequentialList(10);
    LinkedList<Integer> result = DsLinkedList.slice(list, 5, 6);
    assertEquals(1, result.size());
    assertEquals(5, result.get(0), "Single element slice should contain correct element");
  }

  @Test
  @DisplayName("slice: Slice at beginning")
  public void testSliceAtBeginning() {
    LinkedList<Integer> list = createSequentialList(10);
    LinkedList<Integer> result = DsLinkedList.slice(list, 0, 3);
    assertEquals(3, result.size());
    for (int i = 0; i < 3; i++) {
      assertEquals(i, result.get(i));
    }
  }

  @Test
  @DisplayName("slice: Slice at end")
  public void testSliceAtEnd() {
    LinkedList<Integer> list = createSequentialList(10);
    LinkedList<Integer> result = DsLinkedList.slice(list, 7, 10);
    assertEquals(3, result.size());
    for (int i = 0; i < 3; i++) {
      assertEquals(i + 7, result.get(i));
    }
  }

  @Test
  @DisplayName("slice: Large list slice (1000 elements, 500 element slice)")
  public void testSliceLargeList() {
    LinkedList<Integer> list = createSequentialList(1000);
    LinkedList<Integer> result = DsLinkedList.slice(list, 250, 750);
    assertEquals(500, result.size());
    for (int i = 0; i < 500; i++) {
      assertEquals(i + 250, result.get(i), "Slice should contain correct elements");
    }
  }

  @Test
  @DisplayName("slice: Very large list (5000 elements)")
  public void testSliceVeryLargeList() {
    LinkedList<Integer> list = createSequentialList(5000);
    LinkedList<Integer> result = DsLinkedList.slice(list, 1000, 4000);
    assertEquals(3000, result.size());
    for (int i = 0; i < 3000; i++) {
      assertEquals(i + 1000, result.get(i), "Slice should contain correct elements");
    }
  }

  @Test
  @DisplayName("slice: Empty list slice")
  public void testSliceEmptyList() {
    LinkedList<Integer> list = new LinkedList<>();
    LinkedList<Integer> result = DsLinkedList.slice(list, 0, 0);
    assertEquals(0, result.size(), "Slice of empty list should be empty");
  }

  @Test
  @DisplayName("slice: Invalid range - start > end (throws exception)")
  public void testSliceInvalidRange() {
    LinkedList<Integer> list = createSequentialList(10);
    assertThrows(IllegalArgumentException.class, () -> {
      DsLinkedList.slice(list, 7, 3);
    }, "Slice with start > end should throw exception");
  }

  @Test
  @DisplayName("slice: Out of bounds - start > list size (throws exception)")
  public void testSliceOutOfBoundsStart() {
    LinkedList<Integer> list = createSequentialList(10);
    assertThrows(IndexOutOfBoundsException.class, () -> {
      DsLinkedList.slice(list, 15, 20);
    }, "Slice with start >= size should throw exception");
  }

  @Test
  @DisplayName("slice: Out of bounds - end > list size (throws exception)")
  public void testSliceOutOfBoundsEnd() {
    LinkedList<Integer> list = createSequentialList(10);
    assertThrows(IndexOutOfBoundsException.class, () -> {
      DsLinkedList.slice(list, 5, 15);
    }, "Slice with end > size should throw exception");
  }

  // ============================================================================
  // Helper Methods
  // ============================================================================

  /**
   * Creates a sequential linked list with elements 0 to size-1
   */
  private LinkedList<Integer> createSequentialList(int size) {
    LinkedList<Integer> list = new LinkedList<>();
    for (int i = 0; i < size; i++) {
      list.add(i);
    }
    return list;
  }

  /**
   * Verifies that a list contains all elements from min to max (inclusive)
   */
  private void verifyAllElementsPresent(LinkedList<Integer> list, int min, int max) {
    LinkedList<Integer> temp = new LinkedList<>(list);
    int expectedCount = max - min + 1;
    assertEquals(expectedCount, list.size(),
        "List should contain exactly " + expectedCount + " elements");
    
    for (int i = min; i <= max; i++) {
      assertTrue(temp.remove(Integer.valueOf(i)),
          "List should contain element: " + i);
    }
  }

  /**
   * Compares two lists for equality
   */
  private boolean listsEqual(LinkedList<Integer> list1, LinkedList<Integer> list2) {
    if (list1.size() != list2.size()) {
      return false;
    }
    for (int i = 0; i < list1.size(); i++) {
      if (!list1.get(i).equals(list2.get(i))) {
        return false;
      }
    }
    return true;
  }
}
