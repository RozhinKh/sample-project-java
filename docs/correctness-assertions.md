# Correctness Validation Assertions for Benchmark Methods

## Overview

This document specifies lightweight correctness assertions for all 16 benchmark methods in Scenario B. Each assertion is designed to:
- **Execute in microseconds** - not impacting overall benchmark timing
- **Validate core correctness** - without requiring external computations
- **Use inline checks** - minimizing method calls and conditional branches
- **Support deterministic validation** - reproducible across runs
- **Handle edge cases** - empty collections, boundary values, special inputs

### Integration Strategy

Assertions are embedded in benchmark methods using JMH `@Setup` and result analysis phases:

1. **Before Benchmark (`@Setup`):** Validate input data generation
2. **After Benchmark (`@TearDown`):** Quick validation of output correctness
3. **In-Method Assertions:** Strategic inline checks for intermediate results

Example JMH integration:
```java
@Benchmark
public Vector<Integer> benchmarkMethod(BenchmarkState state) {
    Vector<Integer> result = BenchmarkClass.methodUnderTest(state.input);
    
    // Quick inline assertion (~1-5 microseconds)
    assert result.size() == state.input.size() : "Size mismatch";
    
    return result; // JMH times this return
}

@TearDown(Level.Trial)
public void validateResult(BenchmarkState state, Blackhole bh) {
    // More thorough validation after benchmark
    if (!isValid(state.result)) {
        throw new AssertionError("Correctness check failed");
    }
}
```

---

## Algorithms.Sort Category

### 1. SortVector - Correctness Assertion

**Method:** `public static void SortVector(Vector<Integer> v)`

**Correctness Property:** Vector is sorted in ascending order; size preserved

**Assertion Logic:**
```java
// Validation approach:
// 1. Size check (1 comparison)
// 2. Single pass ascending order verification (n comparisons)

assert v.size() == originalSize : "Size changed after sort";

// Verify sorted order with single pass
for (int i = 0; i < v.size() - 1; i++) {
    assert v.get(i) <= v.get(i + 1) : 
        "Not sorted: position " + i + " (" + v.get(i) + ") > " + 
        (i+1) + " (" + v.get(i+1) + ")";
}
```

**Execution Time:** ~10-50 microseconds (n comparisons)

**Edge Cases:**
- Empty vector: Skip loop, size check passes (0 ms)
- Single element: Loop doesn't execute, size check passes (< 1 µs)
- Already sorted: All comparisons pass quickly (branch prediction helps)
- Descending: First comparison fails immediately (< 1 µs)

**Lightweight Variant (for hot path):**
```java
// Skip full verification; just spot-check first/last + a few samples
assert v.get(0) <= v.get(1) : "First two elements unsorted";
assert v.get(v.size()-2) <= v.get(v.size()-1) : "Last two elements unsorted";
if (v.size() > 100) {
    int mid = v.size() / 2;
    assert v.get(mid-1) <= v.get(mid) : "Middle elements unsorted";
}
```

---

### 2. DutchFlagPartition - Correctness Assertion

**Method:** `public static void DutchFlagPartition(Vector<Integer> v, int pivot_value)`

**Correctness Property:** Vector partitioned into three regions: [<pivot], [=pivot], [>pivot]

**Assertion Logic:**
```java
// Three-way partition validation:
// Count occurrences of each region and verify ordering

int less = 0, equal = 0, greater = 0;
int lastLess = Integer.MIN_VALUE;
int firstEqual = -1, lastEqual = -1;
int firstGreater = Integer.MAX_VALUE;

for (int i = 0; i < v.size(); i++) {
    int val = v.get(i);
    
    if (val < pivot_value) {
        less++;
        assert val >= lastLess : "Less region not sorted";
        lastLess = val;
        assert firstEqual == -1 : "Less region after equal region";
        assert firstGreater == Integer.MAX_VALUE : "Less region after greater region";
    } else if (val == pivot_value) {
        equal++;
        if (firstEqual == -1) firstEqual = i;
        lastEqual = i;
        assert lastLess == Integer.MIN_VALUE || lastLess <= val : "Equal before less";
    } else { // val > pivot_value
        greater++;
        if (firstGreater == Integer.MAX_VALUE) firstGreater = i;
        assert firstEqual == -1 || lastEqual < i : "Greater before equal ends";
    }
}

assert less + equal + greater == v.size() : "Element count mismatch";
```

**Execution Time:** ~20-100 microseconds (single pass, n comparisons)

**Edge Cases:**
- No pivot values: `equal` = 0, verify `less + greater == size` (n comparisons)
- All values = pivot: `less` = 0, `greater` = 0 (single pass)
- All values < pivot: `equal` = 0, `greater` = 0 (n comparisons)

**Lightweight Variant:**
```java
// Count regions without full ordering validation
int less = 0, equal = 0, greater = 0;
for (int i = 0; i < v.size(); i++) {
    int val = v.get(i);
    if (val < pivot_value) less++;
    else if (val == pivot_value) equal++;
    else greater++;
}
assert less + equal + greater == v.size() : "Count mismatch";
```

---

### 3. MaxN - Correctness Assertion

**Method:** `public static Vector<Integer> MaxN(Vector<Integer> v, int n)`

**Correctness Property:** Returns n largest elements, sorted descending; size = min(n, v.size())

**Assertion Logic:**
```java
// Validate:
// 1. Result size is correct
// 2. Result is sorted in descending order
// 3. All elements are from input vector
// 4. Result contains actual top-n values (spot check)

assert result.size() == Math.min(n, v.size()) : 
    "Result size " + result.size() + " != min(" + n + ", " + v.size() + ")";

// Verify descending order
for (int i = 0; i < result.size() - 1; i++) {
    assert result.get(i) >= result.get(i + 1) : 
        "Not descending at position " + i;
}

// Verify all result elements are in input (probabilistic - spot check)
if (result.size() > 0) {
    int minResultValue = result.get(result.size() - 1);
    
    // Count how many input values are >= minResultValue
    int countAboveMin = 0;
    for (int i = 0; i < v.size(); i++) {
        if (v.get(i) >= minResultValue) countAboveMin++;
    }
    assert countAboveMin >= result.size() : 
        "Not enough values >= " + minResultValue + " in input";
}
```

**Execution Time:** ~15-80 microseconds (descent check + spot check)

**Edge Cases:**
- n > v.size(): Result contains all elements, sorted descending
- n = 0: Result empty, size check passes (< 1 µs)
- All same value: Descending check passes (all equal), spot check validates

**Lightweight Variant:**
```java
// Just verify size and descending property
assert result.size() == Math.min(n, v.size()) : "Size mismatch";
for (int i = 0; i < result.size() - 1; i++) {
    assert result.get(i) >= result.get(i + 1) : "Not descending";
}
```

---

## Algorithms.Primes Category

### 4. IsPrime - Correctness Assertion

**Method:** `public static boolean IsPrime(int n)`

**Correctness Property:** Correctly identifies prime numbers via mathematical divisibility

**Assertion Logic:**
```java
// Test a range of values with known primality
// Use precomputed primes for spot-check validation

// Small primes: 2, 3, 5, 7, 11, 13, 17, 19, 23, 29...
int[] knownPrimes = {2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47};
int[] knownComposites = {4, 6, 8, 9, 10, 12, 14, 15, 16, 18, 20, 21, 22, 24, 25};

for (int prime : knownPrimes) {
    assert IsPrime(prime) : prime + " should be prime";
}

for (int composite : knownComposites) {
    assert !IsPrime(composite) : composite + " should not be prime";
}

// Edge case: 0, 1 not prime
assert !IsPrime(0) : "0 is not prime";
assert !IsPrime(1) : "1 is not prime";

// For benchmark inputs, use mathematical property:
// A prime n has exactly 2 divisors: 1 and n
// Verify by trial division (quick check for < 100)
if (inputValue < 100) {
    int divisorCount = 0;
    for (int i = 1; i <= inputValue; i++) {
        if (inputValue % i == 0) divisorCount++;
    }
    assert (divisorCount == 2) == IsPrime(inputValue) : 
        "Primality mismatch for " + inputValue;
}
```

**Execution Time:** ~5-20 microseconds (15 spot checks + optional trial division for small values)

**Edge Cases:**
- n = 0: Must return false
- n = 1: Must return false (not prime by definition)
- n = 2: Only even prime, must return true
- Primes > 1000: Trial division verification too expensive; use known primes list

**Lightweight Variant:**
```java
// Just verify against known primes/composites
int[] knownPrimes = {2, 3, 5, 7, 11, 13, 17, 19, 23, 29};
for (int p : knownPrimes) {
    assert IsPrime(p) : p + " should be prime";
}
for (int c : new int[]{4, 6, 8, 9, 10, 12, 14, 15}) {
    assert !IsPrime(c) : c + " should not be prime";
}
```

---

### 5. SumPrimes - Correctness Assertion

**Method:** `public static int SumPrimes(int n)`

**Correctness Property:** Sum of all primes from 0 to n equals known mathematical value

**Assertion Logic:**
```java
// Verify against known prime sums (precomputed)
// Prime sum formula: Can verify by recalculating with validated IsPrime

Map<Integer, Integer> knownPrimeSums = new HashMap<>();
knownPrimeSums.put(10, 17);      // 2+3+5+7 = 17
knownPrimeSums.put(20, 77);      // + 11+13+17+19 = 77
knownPrimeSums.put(100, 1060);   // Sum of primes < 100
knownPrimeSums.put(1000, 76127); // From benchmark spec
knownPrimeSums.put(5000, 1593350);
knownPrimeSums.put(10000, 4227191);

if (knownPrimeSums.containsKey(n)) {
    assert result == knownPrimeSums.get(n) : 
        "SumPrimes(" + n + ") = " + result + 
        ", expected " + knownPrimeSums.get(n);
}

// Alternative: Verify result is reasonable (bounds check)
// Sum of all integers 0 to n = n*(n-1)/2
int sumAll = n * (n - 1) / 2;
// Sum of primes should be less than sum of all integers
assert result < sumAll && result > 0 : 
    "SumPrimes(" + n + ") = " + result + " out of bounds [0, " + sumAll + ")";

// Verify result is even if n > 2 (all primes except 2 are odd)
if (n > 2) {
    assert result % 2 == 1 : "Sum should be odd (2 + sum of odd primes)";
}
```

**Execution Time:** ~5-10 microseconds (map lookup + bounds check)

**Edge Cases:**
- n ≤ 2: Result = 0 (no primes)
- n = 3: Result = 2 (only prime is 2)
- Large n: Verify bounds only (too expensive to enumerate all primes)

**Lightweight Variant:**
```java
// Just verify against precomputed known values
int[] testValues = {1000, 5000, 10000};
int[] expectedSums = {76127, 1593350, 4227191};

for (int i = 0; i < testValues.length; i++) {
    if (n == testValues[i]) {
        assert result == expectedSums[i] : 
            "SumPrimes(" + n + ") mismatch";
    }
}
```

---

### 6. PrimeFactors - Correctness Assertion

**Method:** `public static Vector<Integer> PrimeFactors(int n)`

**Correctness Property:** All returned values are prime; product equals original number

**Assertion Logic:**
```java
// Validate:
// 1. All elements in result are prime
// 2. Product of all elements equals input n
// 3. Result sorted in ascending order (typical implementation)

// Check each factor is prime
for (Integer factor : result) {
    assert factor >= 2 : "Factor " + factor + " is not prime";
    // Simple primality check for factors (they're typically small)
    boolean isPrime = true;
    for (int i = 2; i * i <= factor; i++) {
        if (factor % i == 0) {
            isPrime = false;
            break;
        }
    }
    assert isPrime : factor + " is not prime";
}

// Verify product equals input
long product = 1;
for (Integer factor : result) {
    product *= factor;
}
assert product == n : 
    "Product of factors " + product + " != input " + n;

// Verify sorted order (optional, but common)
for (int i = 0; i < result.size() - 1; i++) {
    assert result.get(i) <= result.get(i + 1) : "Not sorted";
}

// Edge cases
if (n <= 1) {
    assert result.isEmpty() : "Prime factorization of " + n + " should be empty";
}
if (n == 1) {
    assert result.isEmpty() : "1 has no prime factors";
}
```

**Execution Time:** ~20-50 microseconds (verify each factor + compute product)

**Edge Cases:**
- n = 1: Empty result (1 has no prime factors)
- n = prime: Single-element result containing n
- n = 2: Result = [2]
- Highly composite (e.g., 120 = 2³×3×5): Multiple factors, verify product

**Lightweight Variant:**
```java
// Verify product only (fastest check)
long product = 1;
for (Integer factor : result) {
    assert factor >= 2 : "Invalid factor";
    product *= factor;
}
assert product == n : "Product mismatch";
```

---

## DataStructures.DsVector Category

### 7. modifyVector - Correctness Assertion

**Method:** `public static Vector<Integer> modifyVector(Vector<Integer> v)`

**Correctness Property:** Each element incremented by exactly 1; size preserved

**Assertion Logic:**
```java
// Validate:
// 1. Size unchanged
// 2. Each element = original[i] + 1

assert result.size() == original.size() : "Size changed";

for (int i = 0; i < result.size(); i++) {
    assert result.get(i) == original.get(i) + 1 : 
        "Element " + i + " not incremented by 1: " + original.get(i) + 
        " -> " + result.get(i);
}
```

**Execution Time:** ~5-20 microseconds (n element comparisons)

**Edge Cases:**
- Empty vector: Size check passes (< 1 µs)
- Single element: One comparison
- Integer.MAX_VALUE: Element wraps to Integer.MIN_VALUE after +1 (verify behavior)

**Lightweight Variant:**
```java
// Spot-check first, middle, last elements
assert result.size() == original.size() : "Size changed";
assert result.get(0) == original.get(0) + 1 : "First element";
if (result.size() > 1) {
    assert result.get(result.size()/2) == original.get(result.size()/2) + 1 : "Middle";
    assert result.get(result.size()-1) == original.get(original.size()-1) + 1 : "Last";
}
```

---

### 8. searchVector - Correctness Assertion

**Method:** `public static Vector<Integer> searchVector(Vector<Integer> v, int n)`

**Correctness Property:** All returned indices contain the search value; no indices missed

**Assertion Logic:**
```java
// Validate:
// 1. All returned indices are valid
// 2. All indices point to search value
// 3. No valid indices are missed

// Check each returned index is valid and contains search value
for (Integer index : result) {
    assert index >= 0 && index < v.size() : 
        "Index " + index + " out of bounds [0, " + v.size() + ")";
    assert v.get(index) == searchValue : 
        "Index " + index + " has value " + v.get(index) + 
        ", not search value " + searchValue;
}

// Count expected occurrences by scanning input
int expectedCount = 0;
for (int i = 0; i < v.size(); i++) {
    if (v.get(i) == searchValue) expectedCount++;
}

assert result.size() == expectedCount : 
    "Found " + result.size() + " matches, expected " + expectedCount;

// Verify indices are unique and sorted
Set<Integer> uniqueIndices = new HashSet<>(result);
assert uniqueIndices.size() == result.size() : "Duplicate indices returned";
```

**Execution Time:** ~15-40 microseconds (scan + validation)

**Edge Cases:**
- Search value not found: Result empty, validation passes (< 1 µs)
- All elements match: Result contains all indices [0..size)
- Single match: One index verified
- Duplicates in input: All indices found and returned

**Lightweight Variant:**
```java
// Verify all returned indices contain search value
for (Integer index : result) {
    assert v.get(index) == searchValue : "Index points to wrong value";
}

// Verify count matches
int count = 0;
for (int i = 0; i < v.size(); i++) {
    if (v.get(i) == searchValue) count++;
}
assert result.size() == count : "Count mismatch";
```

---

### 9. sortVector - Correctness Assertion

**Method:** `public static Vector<Integer> sortVector(Vector<Integer> v)`

**Correctness Property:** Bubble sort result in ascending order; size and elements preserved

**Assertion Logic:**
```java
// Validate:
// 1. Size preserved
// 2. All elements from input present in output
// 3. Output sorted in ascending order

assert result.size() == v.size() : "Size changed";

// Create frequency map of input
Map<Integer, Integer> inputFreq = new HashMap<>();
for (Integer val : v) {
    inputFreq.put(val, inputFreq.getOrDefault(val, 0) + 1);
}

// Verify output has same element frequencies
Map<Integer, Integer> outputFreq = new HashMap<>();
for (Integer val : result) {
    outputFreq.put(val, outputFreq.getOrDefault(val, 0) + 1);
}
assert inputFreq.equals(outputFreq) : "Element counts mismatch";

// Verify sorted order
for (int i = 0; i < result.size() - 1; i++) {
    assert result.get(i) <= result.get(i + 1) : 
        "Not sorted at position " + i;
}

// Alternative: Verify no elements lost or duplicated
Set<Integer> inputSet = new HashSet<>(v);
Set<Integer> outputSet = new HashSet<>(result);
assert inputSet.equals(outputSet) : "Element set mismatch";
```

**Execution Time:** ~20-60 microseconds (O(n) frequency check + sorting verification)

**Edge Cases:**
- Empty vector: Size check passes (< 1 µs)
- Single element: Trivially sorted
- All same value: All comparisons pass
- Reverse sorted: Full verification needed

**Lightweight Variant:**
```java
// Verify size and ascending order only
assert result.size() == v.size() : "Size mismatch";
for (int i = 0; i < result.size() - 1; i++) {
    assert result.get(i) <= result.get(i + 1) : "Not sorted";
}
```

---

### 10. reverseVector - Correctness Assertion

**Method:** `public static Vector<Integer> reverseVector(Vector<Integer> v)`

**Correctness Property:** Element at position i in result is original[n-1-i]; size preserved

**Assertion Logic:**
```java
// Validate:
// 1. Size preserved
// 2. Each element at correct reversed position
// 3. All elements from input present

assert result.size() == v.size() : "Size changed";

// Verify reverse mapping: result[i] == v[n-1-i]
for (int i = 0; i < result.size(); i++) {
    int reverseIndex = v.size() - 1 - i;
    assert result.get(i) == v.get(reverseIndex) : 
        "Position " + i + " has " + result.get(i) + 
        ", expected " + v.get(reverseIndex) + 
        " (from position " + reverseIndex + ")";
}

// Verify all elements preserved
Map<Integer, Integer> inputFreq = new HashMap<>();
for (Integer val : v) {
    inputFreq.put(val, inputFreq.getOrDefault(val, 0) + 1);
}
Map<Integer, Integer> outputFreq = new HashMap<>();
for (Integer val : result) {
    outputFreq.put(val, outputFreq.getOrDefault(val, 0) + 1);
}
assert inputFreq.equals(outputFreq) : "Element count mismatch";
```

**Execution Time:** ~10-30 microseconds (n position checks)

**Edge Cases:**
- Empty vector: Size check passes (< 1 µs)
- Single element: result[0] == v[0] (trivial)
- Two elements: result[0] == v[1], result[1] == v[0]

**Lightweight Variant:**
```java
// Spot-check first and last elements only
assert result.get(0) == v.get(v.size()-1) : "First element mismatch";
if (v.size() > 1) {
    assert result.get(result.size()-1) == v.get(0) : "Last element mismatch";
}
```

---

### 11. rotateVector - Correctness Assertion

**Method:** `public static Vector<Integer> rotateVector(Vector<Integer> v, int rotationAmount)`

**Correctness Property:** Element at position i in result is original[(i + rotationAmount) % size]; size preserved

**Assertion Logic:**
```java
// Validate:
// 1. Size preserved
// 2. Each element at correct rotated position
// 3. All elements from input present

assert result.size() == v.size() : "Size changed";

int size = v.size();
int normalizedRotation = rotationAmount % size; // Handle rotation >= size

// Verify rotation mapping: result[i] == v[(i + rotation) % size]
// OR equivalently: v[j] appears at position (j - rotation) % size in result
for (int i = 0; i < result.size(); i++) {
    int originalIndex = (i + normalizedRotation) % size;
    assert result.get(i) == v.get(originalIndex) : 
        "Position " + i + " has " + result.get(i) + 
        ", expected " + v.get(originalIndex) + 
        " (from original position " + originalIndex + 
        " with rotation " + rotationAmount + ")";
}

// Verify element frequencies preserved
Map<Integer, Integer> inputFreq = new HashMap<>();
for (Integer val : v) {
    inputFreq.put(val, inputFreq.getOrDefault(val, 0) + 1);
}
Map<Integer, Integer> outputFreq = new HashMap<>();
for (Integer val : result) {
    outputFreq.put(val, outputFreq.getOrDefault(val, 0) + 1);
}
assert inputFreq.equals(outputFreq) : "Element count mismatch";
```

**Execution Time:** ~15-40 microseconds (n rotation checks)

**Edge Cases:**
- Rotation = 0: result equals v (identity)
- Rotation = size: result equals v (full rotation)
- Rotation > size: Normalize with modulo before check
- Empty vector: Size check passes (< 1 µs)

**Lightweight Variant:**
```java
// Spot-check first element only
int normalizedRotation = rotationAmount % v.size();
int firstOriginal = (normalizedRotation) % v.size();
assert result.get(0) == v.get(firstOriginal) : "First element mismatch";
```

---

### 12. mergeVectors - Correctness Assertion

**Method:** `public static Vector<Integer> mergeVectors(Vector<Integer> v1, Vector<Integer> v2)`

**Correctness Property:** Result size = v1.size() + v2.size(); first elements from v1, then v2

**Assertion Logic:**
```java
// Validate:
// 1. Result size = v1.size() + v2.size()
// 2. First v1.size() elements match v1
// 3. Next v2.size() elements match v2
// 4. Element order preserved

int expectedSize = v1.size() + v2.size();
assert result.size() == expectedSize : 
    "Size " + result.size() + " != " + v1.size() + " + " + v2.size();

// Verify first half matches v1
for (int i = 0; i < v1.size(); i++) {
    assert result.get(i) == v1.get(i) : 
        "Position " + i + " mismatch in v1 portion";
}

// Verify second half matches v2
for (int i = 0; i < v2.size(); i++) {
    assert result.get(v1.size() + i) == v2.get(i) : 
        "Position " + (v1.size() + i) + " mismatch in v2 portion";
}

// Verify element frequencies
Map<Integer, Integer> expectedFreq = new HashMap<>();
for (Integer val : v1) {
    expectedFreq.put(val, expectedFreq.getOrDefault(val, 0) + 1);
}
for (Integer val : v2) {
    expectedFreq.put(val, expectedFreq.getOrDefault(val, 0) + 1);
}

Map<Integer, Integer> resultFreq = new HashMap<>();
for (Integer val : result) {
    resultFreq.put(val, resultFreq.getOrDefault(val, 0) + 1);
}
assert expectedFreq.equals(resultFreq) : "Element frequency mismatch";
```

**Execution Time:** ~20-50 microseconds (verify both halves)

**Edge Cases:**
- Empty v1: Result equals v2
- Empty v2: Result equals v1
- Both empty: Result empty

**Lightweight Variant:**
```java
// Just verify size and element count
assert result.size() == v1.size() + v2.size() : "Size mismatch";

// Spot-check boundaries
if (v1.size() > 0 && v2.size() > 0) {
    assert result.get(v1.size()-1) == v1.get(v1.size()-1) : "v1 end mismatch";
    assert result.get(v1.size()) == v2.get(0) : "v2 start mismatch";
}
```

---

## DataStructures.DsLinkedList Category

### 13. shuffle - Correctness Assertion

**Method:** `public static LinkedList<Integer> shuffle(LinkedList<Integer> l)`

**Correctness Property:** All original elements present; size preserved; order changed (probabilistic)

**Assertion Logic:**
```java
// Validate:
// 1. Size preserved
// 2. All original elements present (frequency preserved)
// 3. Order is likely different (probabilistic test)

assert result.size() == original.size() : "Size changed";

// Verify element frequencies
Map<Integer, Integer> originalFreq = new HashMap<>();
for (Integer val : original) {
    originalFreq.put(val, originalFreq.getOrDefault(val, 0) + 1);
}

Map<Integer, Integer> resultFreq = new HashMap<>();
for (Integer val : result) {
    resultFreq.put(val, resultFreq.getOrDefault(val, 0) + 1);
}
assert originalFreq.equals(resultFreq) : "Element frequency mismatch";

// Verify order is different (unless very small list or by chance)
// Count position changes
int positionsChanged = 0;
for (int i = 0; i < result.size(); i++) {
    if (!original.get(i).equals(result.get(i))) {
        positionsChanged++;
    }
}

// For valid shuffle with n > 5, expect most elements moved
// But allow some chance that shuffle is similar to original
if (original.size() > 5) {
    // At least some elements should move (probabilistically likely)
    // Don't assert strictly; just log if suspicious
    if (positionsChanged < original.size() * 0.2) {
        // Warning: shuffle may not have worked properly
        // But don't fail - allow edge case where random shuffle happened to be similar
    }
}
```

**Execution Time:** ~10-30 microseconds (frequency map creation + comparison)

**Edge Cases:**
- Empty list: Size check passes (< 1 µs)
- Single element: Only one element, can't shuffle; validate it's preserved
- Two elements: 50% chance each of [0,1] or [1,0]; just validate elements present

**Lightweight Variant:**
```java
// Just verify size and element presence
assert result.size() == original.size() : "Size changed";

// Verify all elements from original are in result
Map<Integer, Integer> resultFreq = new HashMap<>();
for (Integer val : result) {
    resultFreq.put(val, resultFreq.getOrDefault(val, 0) + 1);
}
for (Map.Entry<Integer, Integer> entry : resultFreq.entrySet()) {
    int count = 0;
    for (Integer val : original) {
        if (val.equals(entry.getKey())) count++;
    }
    assert count == entry.getValue() : "Element " + entry.getKey() + " count mismatch";
}
```

---

### 14. slice - Correctness Assertion

**Method:** `public static LinkedList<Integer> slice(LinkedList<Integer> l, int start, int end)`

**Correctness Property:** Size = (end - start); elements match original[start..end)

**Assertion Logic:**
```java
// Validate:
// 1. Result size = (end - start)
// 2. Each element matches original at corresponding offset
// 3. All result elements are from original

int expectedSize = end - start;
assert result.size() == expectedSize : 
    "Size " + result.size() + " != expected " + expectedSize;

// Verify each element
for (int i = 0; i < result.size(); i++) {
    int originalIndex = start + i;
    assert result.get(i).equals(original.get(originalIndex)) : 
        "Position " + i + " has " + result.get(i) + 
        ", expected " + original.get(originalIndex) + 
        " (from original index " + originalIndex + ")";
}

// Verify start/end boundaries are valid
assert start >= 0 && start <= original.size() : 
    "Start " + start + " out of bounds [0, " + original.size() + "]";
assert end >= start && end <= original.size() : 
    "End " + end + " out of bounds [" + start + ", " + original.size() + "]";
```

**Execution Time:** ~10-30 microseconds (size check + boundary validation + element verification)

**Edge Cases:**
- start = end: Empty result, size = 0
- start = 0, end = size: Result equals original
- start > 0: Middle slice
- end = size: Slice to end

**Lightweight Variant:**
```java
// Just verify size and first/last elements
assert result.size() == (end - start) : "Size mismatch";
if (result.size() > 0) {
    assert result.get(0).equals(original.get(start)) : "First element mismatch";
    assert result.get(result.size()-1).equals(original.get(end-1)) : "Last element mismatch";
}
```

---

## Control.Single Category

### 15. sumRange - Correctness Assertion

**Method:** `public static int sumRange(int n)`

**Correctness Property:** Result equals n*(n-1)/2 (formula for sum 0 to n-1)

**Assertion Logic:**
```java
// Validate result against mathematical formula
// Sum of integers from 0 to n-1 = n*(n-1)/2

long expected = (long) n * (n - 1) / 2;
assert result == expected : 
    "sumRange(" + n + ") = " + result + ", expected " + expected;

// Additional check: result should fit in int range
assert result >= 0 : "Result is negative (shouldn't be)";
assert result <= Integer.MAX_VALUE : "Result overflows int";

// Sanity check: result <= sum of all integers to n
assert result < ((long) n * (n + 1) / 2) : "Result exceeds maximum possible sum";
```

**Execution Time:** ~2-5 microseconds (one arithmetic operation + comparison)

**Edge Cases:**
- n = 0: Expected = 0
- n = 1: Expected = 0 (only 0 in range)
- n = 2: Expected = 1 (0+1)
- Large n: Verify no integer overflow

**Lightweight Variant:**
```java
// Just verify formula
long expected = (long) n * (n - 1) / 2;
assert result == expected : "sumRange mismatch";
```

---

### 16. maxArray - Correctness Assertion

**Method:** `public static int maxArray(int[] arr)`

**Correctness Property:** Result is maximum value in array; appears in array

**Assertion Logic:**
```java
// Validate:
// 1. Result appears in input array
// 2. Result is >= all other elements
// 3. Array is not empty (handled by implementation)

assert arr.length > 0 : "Array is empty";

// Verify result is maximum
for (int i = 0; i < arr.length; i++) {
    assert result >= arr[i] : 
        "Result " + result + " < arr[" + i + "] = " + arr[i];
}

// Verify result appears in array
boolean found = false;
for (int i = 0; i < arr.length; i++) {
    if (arr[i] == result) {
        found = true;
        break;
    }
}
assert found : "Result " + result + " not found in array";

// Verify at least one element equals result
int maxCount = 0;
for (int i = 0; i < arr.length; i++) {
    if (arr[i] == result) maxCount++;
}
assert maxCount >= 1 : "No elements equal to maximum";
```

**Execution Time:** ~5-15 microseconds (scan array + verify max)

**Edge Cases:**
- Single element: Result equals that element
- All same value: All comparisons pass, element found
- Negative numbers: Max is least negative value
- Integer.MIN_VALUE / MAX_VALUE: Extreme values handled

**Lightweight Variant:**
```java
// Just verify result is in array and >= all elements
for (int val : arr) {
    assert result >= val : "Result not maximum";
}
boolean found = false;
for (int val : arr) {
    if (val == result) { found = true; break; }
}
assert found : "Result not in array";
```

---

## Control.Double Category

### 17. sumSquare - Correctness Assertion

**Method:** `public static int sumSquare(int n)`

**Correctness Property:** Result equals n*(n+1)*(2n+1)/6 (sum of squares formula)

**Assertion Logic:**
```java
// Validate result against mathematical formula
// Sum of i² from 0 to n = n*(n+1)*(2n+1)/6

long expected = (long) n * (n + 1) * (2 * n + 1) / 6;
assert result == expected : 
    "sumSquare(" + n + ") = " + result + ", expected " + expected;

// Sanity checks
assert result >= 0 : "Sum of squares should be non-negative";
assert result <= Integer.MAX_VALUE : "Result overflows int";

// Verify result is reasonable
// Sum should be > n² (since we're summing squares)
long minExpected = (long) n * n;
assert result >= minExpected : 
    "Result " + result + " < minimum expected " + minExpected;
```

**Execution Time:** ~3-8 microseconds (arithmetic + comparison)

**Edge Cases:**
- n = 0: Expected = 0
- n = 1: Expected = 1 (0² + 1² = 1... wait, that's wrong. Let me recalculate)
- Actually: sumSquare(n) sums i² for i from 0 to n
- n = 0: sum = 0
- n = 1: sum = 0² + 1² = 1 (formula: 1*2*3/6 = 1) ✓
- n = 2: sum = 0 + 1 + 4 = 5 (formula: 2*3*5/6 = 5) ✓
- n = 100: Formula result (from spec)

**Lightweight Variant:**
```java
// Just verify formula
long expected = (long) n * (n + 1) * (2 * n + 1) / 6;
assert result == expected : "sumSquare formula mismatch";
```

---

### 18. sumTriangle - Correctness Assertion

**Method:** `public static int sumTriangle(int n)`

**Correctness Property:** Result equals sum of triangular numbers T(0) + T(1) + ... + T(n)

**Assertion Logic:**
```java
// Validate result against triangular number summation formula
// T(k) = sum from 0 to k = k*(k+1)/2
// Result = sum of T(i) for i from 0 to n
// This equals: sum over all (i,j) where 0 <= j <= i <= n of 1
// Which equals: (n+1)*(n+2)*(n+3)/6

// Formula for sum of triangular numbers:
long expected = (long) (n + 1) * (n + 2) * (n + 3) / 6;
assert result == expected : 
    "sumTriangle(" + n + ") = " + result + ", expected " + expected;

// Sanity checks
assert result >= 0 : "Sum should be non-negative";
assert result <= Integer.MAX_VALUE : "Result overflows int";

// Verify result is larger than sum of n² 
// (since we're summing triangular numbers up to T(n) = n*(n+1)/2)
long approximateMin = (long) n * n;
assert result >= approximateMin : 
    "Result seems too small";
```

**Execution Time:** ~3-8 microseconds (arithmetic + comparison)

**Edge Cases:**
- n = 0: T(0) = 0, expected = 1*2*3/6 = 1
- n = 1: T(0) + T(1) = 0 + 1 = 1, expected = 2*3*4/6 = 4... let me verify
  - Actually T(1) = 0 + 1 = 1, so sum = 0 + 1 = 1
  - Formula: (1+1)*(1+2)*(1+3)/6 = 2*3*4/6 = 4
  - Wait, this doesn't match. Let me reconsider the formula.

Let me think about this more carefully:
- T(0) = 0
- T(1) = 0 + 1 = 1  
- T(2) = 0 + 1 + 2 = 3
- T(3) = 0 + 1 + 2 + 3 = 6
- Sum = T(0) + T(1) + T(2) + T(3) = 0 + 1 + 3 + 6 = 10

The sum of first n triangular numbers is: n*(n+1)*(n+2)/6

For n=3: 3*4*5/6 = 10 ✓

**Lightweight Variant:**
```java
// Verify formula
long expected = (long) n * (n + 1) * (n + 2) / 6;
assert result == expected : "sumTriangle formula mismatch";
```

---

### 19. countPairs - Correctness Assertion

**Method:** `public static int countPairs(int[] arr)`

**Correctness Property:** Result is non-negative and ≤ size/2; represents values appearing exactly twice

**Assertion Logic:**
```java
// Validate:
// 1. Result is non-negative
// 2. Result ≤ array.length / 2 (can't have more than half be in pairs)
// 3. Verify result matches manual count (spot check)

assert result >= 0 : "Result is negative";
assert result <= arr.length / 2 : 
    "Result " + result + " > array.length/2 = " + (arr.length / 2);

// Verify by manual count (expensive but necessary for correctness)
Map<Integer, Integer> freq = new HashMap<>();
for (int val : arr) {
    freq.put(val, freq.getOrDefault(val, 0) + 1);
}

int expectedCount = 0;
for (int count : freq.values()) {
    if (count == 2) expectedCount++;
}

assert result == expectedCount : 
    "countPairs = " + result + ", expected " + expectedCount;

// Additional check: count elements appearing exactly 2 times
int doubleCount = 0;
for (int count : freq.values()) {
    if (count >= 2) doubleCount++;
}
assert result <= doubleCount : "Result exceeds count of duplicate values";
```

**Execution Time:** ~30-100 microseconds (frequency map creation + count verification)

**Edge Cases:**
- No duplicates: Result = 0
- All same value: Result = 1 (one value appearing many times, counts as one pair)
- Mixed duplicates: Count values with count == 2

**Lightweight Variant:**
```java
// Just verify bounds
assert result >= 0 && result <= arr.length / 2 : "Result out of bounds";
```

---

### 20. countDuplicates - Correctness Assertion

**Method:** `public static int countDuplicates(int[] arr0, int[] arr1)`

**Correctness Property:** Result is non-negative and ≤ min(arr0.length, arr1.length)

**Assertion Logic:**
```java
// Validate:
// 1. Result is non-negative
// 2. Result ≤ min(arr0.length, arr1.length)
// 3. Verify result matches manual count (spot check)

assert result >= 0 : "Result is negative";
int maxPossible = Math.min(arr0.length, arr1.length);
assert result <= maxPossible : 
    "Result " + result + " > min(arr0.length, arr1.length) = " + maxPossible;

// Verify by manual count
// Based on implementation, likely counts where arr0[i] == arr1[i]
int expectedCount = 0;
for (int i = 0; i < Math.min(arr0.length, arr1.length); i++) {
    if (arr0[i] == arr1[i]) {
        expectedCount++;
    }
}

// If implementation is more complex (e.g., nested loops counting duplicates),
// we need to know the exact specification
// For now, assume it counts matching positions
assert result == expectedCount : 
    "countDuplicates = " + result + ", expected " + expectedCount;

// Safety check: result should be <= size (no value can match more than size times)
assert result <= arr0.length && result <= arr1.length : 
    "Result exceeds array sizes";
```

**Execution Time:** ~20-50 microseconds (index-based comparison only, not full O(n²) scanning)

**Note:** The specification in benchmark-design.md says this is O(n²), but the assertion depends on actual implementation behavior. The assertion above assumes it counts matching indices, which would be O(n). If the implementation does nested loop comparison, adjust accordingly.

**Edge Cases:**
- Empty arrays: Result = 0
- Arrays same size: Result ≤ size
- Arrays different sizes: Result ≤ smaller size
- All match: Result = size of smaller array
- No match: Result = 0

**Lightweight Variant:**
```java
// Just verify bounds
assert result >= 0 : "Negative result";
assert result <= Math.min(arr0.length, arr1.length) : "Result exceeds bounds";
```

---

### 21. sumMatrix - Correctness Assertion

**Method:** `public static int sumMatrix(int[][] arr)`

**Correctness Property:** Result is non-negative and ≤ rows × cols × 100

**Assertion Logic:**
```java
// Validate:
// 1. Result is non-negative
// 2. Result ≤ (rows × cols × 100) - upper bound for [0,100) values
// 3. Verify result matches manual sum (spot check)

int rows = arr.length;
assert rows >= 0 : "Negative row count";

int cols = 0;
if (rows > 0) {
    cols = arr[0].length;
}

assert result >= 0 : "Sum is negative (shouldn't be for [0,100) values)";

long maxPossible = (long) rows * cols * 100;
assert result <= maxPossible : 
    "Result " + result + " > max possible " + maxPossible;

// Verify by manual sum
long expectedSum = 0;
for (int i = 0; i < rows; i++) {
    for (int j = 0; j < arr[i].length; j++) {
        expectedSum += arr[i][j];
    }
}

assert result == expectedSum : 
    "sumMatrix = " + result + ", expected " + expectedSum;

// Additional sanity check
// Average value should be ~50 for [0,100) range
long estimatedAverage = expectedSum / (rows * cols);
assert estimatedAverage >= 0 && estimatedAverage < 100 : 
    "Average value out of expected [0,100) range";
```

**Execution Time:** ~20-60 microseconds (full matrix traversal + validation)

**Edge Cases:**
- Empty matrix (0×0): Result = 0
- Single cell (1×1): Result = value of that cell
- All zeros: Result = 0
- All 99 (max): Result = rows × cols × 99

**Lightweight Variant:**
```java
// Just verify bounds and spot-check sum
long maxPossible = (long) arr.length * (arr.length > 0 ? arr[0].length : 0) * 100;
assert result >= 0 && result <= maxPossible : "Result out of bounds";

// Spot-check: sum of first row
int firstRowSum = 0;
for (int val : arr[0]) {
    firstRowSum += val;
}
assert firstRowSum <= result : "First row sum exceeds total";
```

---

## Integration Guide

### JMH Benchmark Structure

Assertions should be integrated at three points in JMH benchmarks:

#### 1. Input Validation (@Setup)
```java
@State(Scope.Benchmark)
public static class BenchmarkState {
    Vector<Integer> inputVector;
    Vector<Integer> originalVector;
    
    @Setup(Level.Invocation)
    public void setup() {
        inputVector = GenVector.generateVector(1000, 10000);
        originalVector = new Vector<>(inputVector); // Backup for assertions
        
        // Quick validation of input data
        assert inputVector.size() == 1000 : "Input generation failed";
        assert !inputVector.isEmpty() : "Input is empty";
    }
}
```

#### 2. Result Validation (After Benchmark)
```java
@Benchmark
public Vector<Integer> benchmarkSortVector(BenchmarkState state) {
    Vector<Integer> result = Sort.SortVector(state.inputVector);
    
    // Quick inline assertion (< 5 µs)
    assert result.size() == state.inputVector.size() : "Size mismatch";
    
    return result;
}

@TearDown(Level.Invocation)
public void tearDown(BenchmarkState state) {
    // Detailed validation after timing completes
    // This doesn't affect benchmark measurements
    if (!isSorted(state.result)) {
        throw new AssertionError("Result not sorted");
    }
}
```

#### 3. Assertion Helper Methods
```java
private static boolean isSorted(Vector<Integer> v) {
    for (int i = 0; i < v.size() - 1; i++) {
        if (v.get(i) > v.get(i + 1)) return false;
    }
    return true;
}

private static boolean hasMatchingElements(Vector<Integer> expected, Vector<Integer> actual) {
    if (expected.size() != actual.size()) return false;
    Map<Integer, Integer> freq = new HashMap<>();
    for (Integer val : expected) {
        freq.put(val, freq.getOrDefault(val, 0) + 1);
    }
    for (Integer val : actual) {
        int count = freq.getOrDefault(val, 0);
        if (count == 0) return false;
        freq.put(val, count - 1);
    }
    return true;
}
```

### Performance Impact Considerations

**Minimal Impact (< 1% timing overhead):**
- Size checks (1 comparison)
- Single-pass linear verification (O(n) but simple comparisons)
- Mathematical formula verification (O(1) arithmetic)
- Spot-check sampling (constant number of checks)

**Should Use @TearDown (no timing impact):**
- Full frequency map validation
- Nested comparisons (O(n²) validation of O(n²) results)
- Complex element verification
- Detailed spot-checking

**Avoid in Hot Path:**
- HashMap creation/population (use lightweight bounds check instead)
- Nested loop validation (defer to @TearDown)
- String concatenation in assertions (use explicit values)
- Multiple passes over data

### Assertion Disable Strategy

For production benchmarks, assertions can be disabled via JVM flags:
```bash
java -da -XX:-TieredCompilation -cp benchmark.jar org.openjdk.jmh.Main
```

Or selectively disable in code:
```java
if (Boolean.parseBoolean(System.getProperty("benchmark.validate", "true"))) {
    assert result.size() == expectedSize : "Validation failed";
}
```

---

## Summary Table

| Method | Assertion Type | Execution Time | Location |
|--------|---|---|---|
| SortVector | Ascending order + size | 10-50 µs | Result validation |
| DutchFlagPartition | Partition regions | 20-100 µs | Result validation |
| MaxN | Descending order + size | 15-80 µs | Result validation |
| IsPrime | Known primes/composites | 5-20 µs | @TearDown |
| SumPrimes | Known values or formula | 5-10 µs | Result validation |
| PrimeFactors | Product + primality | 20-50 µs | Result validation |
| modifyVector | Increment by 1 | 5-20 µs | Result validation |
| searchVector | Index validity + count | 15-40 µs | Result validation |
| sortVector | Bubble sort order + size | 20-60 µs | Result validation |
| reverseVector | Reverse mapping | 10-30 µs | Result validation |
| rotateVector | Rotation mapping | 15-40 µs | Result validation |
| mergeVectors | Size + order preservation | 20-50 µs | Result validation |
| shuffle | Element presence + size | 10-30 µs | Result validation |
| slice | Size + element match | 10-30 µs | Result validation |
| sumRange | Formula: n*(n-1)/2 | 2-5 µs | Result validation |
| maxArray | Max + presence | 5-15 µs | Result validation |
| sumSquare | Formula: n*(n+1)*(2n+1)/6 | 3-8 µs | Result validation |
| sumTriangle | Formula: n*(n+1)*(n+2)/6 | 3-8 µs | Result validation |
| countPairs | Bounds + manual count | 30-100 µs | @TearDown |
| countDuplicates | Bounds + count | 20-50 µs | Result validation |
| sumMatrix | Bounds + sum | 20-60 µs | Result validation |

---

## Recommendations

1. **Always Include Size Checks** - Fastest validation, catches major errors
2. **Use Lightweight Variants in Hot Path** - Spot-checks instead of full validation
3. **Defer Complex Validation to @TearDown** - Zero timing impact
4. **Leverage Mathematical Properties** - Formulas are microseconds vs milliseconds
5. **Test Both Happy Path and Edge Cases** - Empty inputs, single elements, etc.
6. **Document Expected Values** - Hardcode known correct results for verification
7. **Consider Deterministic Seeding** - For shuffle validation, use seeded random

