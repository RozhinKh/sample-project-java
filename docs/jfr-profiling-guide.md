# Java Flight Recorder (JFR) Profiling Strategy

## Table of Contents

1. [Executive Summary](#executive-summary)
2. [JFR Event Configuration](#jfr-event-configuration)
3. [Hotspot Identification Criteria](#hotspot-identification-criteria)
4. [Analysis Tools and Workflow](#analysis-tools-and-workflow)
5. [JFR Recording Commands](#jfr-recording-commands)
6. [Java Mission Control (JMC) Analysis](#java-mission-control-jmc-analysis)
7. [Flame Graph Interpretation](#flame-graph-interpretation)
8. [Correlation with Benchmark Results](#correlation-with-benchmark-results)
9. [Expected Hotspots by Benchmark Category](#expected-hotspots-by-benchmark-category)
10. [Troubleshooting and Best Practices](#troubleshooting-and-best-practices)

---

## Executive Summary

This document specifies the Java Flight Recorder (JFR) profiling strategy for capturing and analyzing detailed runtime behavior during benchmark execution. JFR is a low-overhead profiling framework built into the JVM that provides comprehensive insights into:

- **Method execution time distribution** - Identify which methods consume the most CPU time
- **Garbage collection behavior** - Monitor pause times, frequency, and overhead
- **Lock contention** - Detect synchronization bottlenecks and thread contention
- **I/O operations** - Track disk and network activity if relevant
- **Memory allocation patterns** - Understand allocation hotspots

**Key Objectives:**
- Capture performance data during 5-10 minute baseline and candidate runs
- Identify methods consuming ≥5% of total runtime (major hotspots)
- Flag lock contention overhead >1% and GC pause times >100ms
- Correlate profiling findings with benchmark timing results
- Document expected performance bottlenecks for each benchmark category

---

## JFR Event Configuration

### 1. Method Sampling Events

**Event Type:** `jdk.ExecutionSample`

**Configuration:**
- **Sampling Interval:** 100ms (10 samples per second)
- **Stack Depth:** Full call stack (default 64 frames)
- **Thread Filter:** All threads

**Purpose:**
- Captures method execution at regular intervals
- Provides statistical distribution of where CPU time is spent
- Low overhead: ~1-2% performance impact

**Rationale:**
- 100ms intervals provide sufficient granularity for multi-second benchmarks
- Aligns with 5-10 minute baseline runs (3000-6000 samples per run)
- Avoids excessive data generation while capturing hotspots
- Full stack depth enables detailed flame graph analysis

**Example Data Output:**
```
Sample #1234 @ 1234.567 ms: thread=main
  com.example.algorithms.Sort.SortVector (line 45) ← hottest frame
  com.example.algorithms.Sort.executeBenchmark (line 100)
  java.lang.Thread.run (line 833)

Sample #1235 @ 1334.567 ms: thread=main
  com.example.datastructures.Vector.get (line 67) ← synchronized method
  com.example.algorithms.Sort.SortVector (line 48)
  ...
```

### 2. Garbage Collection Events

**Event Types:**
- `jdk.GarbageCollection` - GC pause events
- `jdk.GCPhaseParallel` - Detailed GC phase timing
- `jdk.ObjectAllocationInNewTLAB` - Young generation allocations
- `jdk.ObjectAllocationOutsideTLAB` - Large/direct allocations

**Configuration:**
- **Enabled:** Yes
- **Threshold:** Log all GC events (no minimum threshold)
- **Stack Traces:** Enabled for allocation events

**Purpose:**
- Track GC pause times and frequency
- Identify allocation hotspots
- Monitor generational collection behavior

**Analysis Points:**
- GC pause times >100ms during benchmark: potential issue
- Frequent young generation collections: high allocation rate
- GC pause frequency: should correlate with benchmark duration

### 3. Lock Contention Events

**Event Types:**
- `jdk.JavaMonitorEnter` - Lock acquisition attempts
- `jdk.JavaMonitorWait` - Monitor wait operations
- `jdk.ThreadPark` - LockSupport.park() calls

**Configuration:**
- **Enabled:** Yes
- **Threshold:** Log contention >10ms (optional, or log all)
- **Stack Traces:** Enabled

**Purpose:**
- Identify synchronized method bottlenecks
- Detect threads waiting on locks
- Measure lock contention overhead

**Analysis Points:**
- Lock contention >1% of total runtime: significant overhead
- Repeated locks on same monitor: potential bottleneck
- Park/wait operations: thread synchronization points

### 4. I/O Events

**Event Types:**
- `jdk.FileRead` - File read operations
- `jdk.FileWrite` - File write operations
- `jdk.SocketRead` - Network read operations
- `jdk.SocketWrite` - Network write operations

**Configuration:**
- **Enabled:** Yes (optional if benchmarks don't involve I/O)
- **Threshold:** ≥1ms (minimal threshold)
- **Stack Traces:** Enabled

**Purpose:**
- Track I/O operations that might block execution
- Identify unexpected file/network activity
- Measure I/O overhead in benchmark execution

**Note:** For Scenario B benchmarks (CPU-bound), I/O activity should be minimal.

### 5. Memory Events (Optional)

**Event Type:** `jdk.ObjectAllocationSample`

**Configuration:**
- **Enabled:** Yes (sample-based to reduce overhead)
- **Sample Rate:** Every 1/10000 allocation
- **Stack Traces:** Enabled

**Purpose:**
- Identify memory allocation hotspots
- Track object creation patterns
- Understand memory pressure during execution

---

## Hotspot Identification Criteria

### Criteria Definitions

#### 1. Method-Level Hotspots (≥5% Runtime)

**Definition:** Methods that consume ≥5% of total benchmark execution time

**Identification Method:**
1. Extract call stack samples from JFR recording
2. Count occurrences of each method across all samples
3. Calculate percentage: (method_sample_count / total_samples) × 100
4. Flag methods with percentage ≥ 5%

**Interpretation:**
- **Tier 1 Hotspot (≥10%):** Critical optimization targets
  - Example: `Sort.SortVector` at 45% suggests sorting is the main bottleneck
  - Justification: O(n log n) complexity, dominates total time
  
- **Tier 2 Hotspot (5-10%):** Significant contributors
  - Example: `Vector.get()` at 7% indicates synchronized method overhead
  - Justification: Frequent calls accumulate overhead
  
- **Secondary Operations (<5%):** Supporting code
  - Example: `Vector.set()` at 2% is overhead, not a bottleneck

**Example Calculation:**
```
Total samples collected: 6000 (10 samples/sec × 600 seconds)

Method rankings (sample count):
1. jdk.ExecutionSample { stack frame: Sort.SortVector }: 2700 samples
   → Percentage: 2700/6000 = 45.0% ✓ TIER 1 HOTSPOT

2. jdk.ExecutionSample { stack frame: Vector.get }: 420 samples
   → Percentage: 420/6000 = 7.0% ✓ TIER 2 HOTSPOT

3. jdk.ExecutionSample { stack frame: Vector.set }: 120 samples
   → Percentage: 120/6000 = 2.0% ✗ Below 5% threshold
```

#### 2. Lock Contention Hotspots (>1% Overhead)

**Definition:** Lock contention causing >1% overhead (>6 seconds per 10 minute run)

**Identification Method:**
1. Collect `JavaMonitorEnter` events from JFR
2. Sum lock wait times across all monitors
3. Calculate overhead: total_lock_wait_time / total_run_time
4. Flag locks with overhead >1%

**Interpretation:**
- **Critical (>5%):** Significant contention, consider synchronization redesign
  - Example: Vector synchronized methods causing 5% overhead suggests high contention
  - Action: Evaluate thread-safe alternatives (CopyOnWriteArrayList, AtomicReference)

- **Moderate (1-5%):** Noticeable but manageable
  - Example: ReentrantLock on shared resource at 2% overhead
  - Action: Monitor carefully, optimize if possible

- **Minimal (<1%):** Acceptable synchronization cost
  - Example: Short-lived synchronized section at 0.5% overhead
  - Action: No action required

**Example Calculation:**
```
Total run time: 600 seconds

Lock contention events:
- Monitor: Vector.modifyVector lock
  - Wait count: 15,000
  - Total wait time: 6.2 seconds
  - Overhead: 6.2 / 600 = 1.03% ✓ FLAG AS HOTSPOT (>1%)

- Monitor: HashMap.resize lock
  - Wait count: 50
  - Total wait time: 0.4 seconds
  - Overhead: 0.4 / 600 = 0.07% ✗ Below threshold
```

#### 3. GC Pause Hotspots (>100ms per Iteration)

**Definition:** GC pause times >100ms during any benchmark iteration

**Identification Method:**
1. Extract `GarbageCollection` events from JFR
2. Identify pause duration for each event
3. Flag pauses >100ms

**Interpretation:**
- **Critical (>500ms pause):** Severe GC issue, likely full GC
  - Example: Full GC pause of 800ms indicates insufficient heap
  - Action: Increase heap size, analyze allocation rate

- **Significant (100-500ms pause):** Noticeable pause
  - Example: Young generation GC pause of 150ms
  - Action: Monitor if frequent, optimize allocation patterns

- **Normal (<100ms pause):** Acceptable GC behavior
  - Example: Young generation GC pause of 35ms
  - Action: No action required

**Note:** For CPU-bound benchmarks in Scenario B, GC pauses should be minimal (<50ms typical).

### Combined Hotspot Scoring

For comprehensive hotspot identification, use a scoring system:

```
Hotspot Score = 
  (method_runtime_percent / 5) +           // 5% threshold unit
  (lock_overhead_percent / 1) +            // 1% threshold unit
  (gc_pause_ms / 100) +                    // 100ms threshold unit
  (allocation_rate_percent / 2)            // optional allocation scoring

Flags when Score > 2.0
```

---

## Analysis Tools and Workflow

### Tools Overview

#### 1. **jcmd** (Java Command Line)
- Built-in JVM tool for recording control
- Records JFR data to `.jfr` files
- Minimal overhead

#### 2. **Java Mission Control (JMC)**
- GUI application for JFR file analysis
- Flame graphs, timeline views, method profiler
- Included in JDK 7u40+

#### 3. **Async-profiler** (Optional)
- Alternative sampling profiler
- Superior flame graph output
- Can correlate with JFR data

#### 4. **Custom Scripts** (Optional)
- Parse JFR XML/binary format
- Extract specific metrics
- Generate correlation reports

---

## JFR Recording Commands

### Recording Baseline Run

**Start Recording Before Benchmark Execution:**

```bash
# Start JFR recording (continuous)
jcmd <PID> JFR.start \
  name=baseline_run \
  settings=profile \
  duration=10m \
  filename=baseline_run.jfr

# Notes:
# - name: Recording identifier
# - settings=profile: Built-in profile (method sampling, GC, locks)
# - duration=10m: Record for 10 minutes
# - filename: Output file path (absolute recommended)
```

**Start Recording with Custom Configuration:**

```bash
# Start with method sampling at 100ms intervals
jcmd <PID> JFR.start \
  name=baseline_custom \
  duration=10m \
  filename=baseline_custom.jfr \
  settings=profile \
  jdk.ExecutionSample.enabled=true \
  jdk.ExecutionSample.period=100ms \
  jdk.GarbageCollection.enabled=true \
  jdk.JavaMonitorEnter.enabled=true \
  jdk.JavaMonitorEnter.threshold=10ms
```

### Monitoring Active Recordings

```bash
# List all active recordings
jcmd <PID> JFR.check

# Example output:
# Recording 1: name=baseline_run duration=10m (running)
# Recording 2: name=candidate_run duration=10m (running)
```

### Stopping Recording

```bash
# Stop recording by name (data flushed to file)
jcmd <PID> JFR.stop name=baseline_run

# Stop all recordings
jcmd <PID> JFR.stop

# Recording data automatically written to filename specified at start
```

### Dumping Recording Without Stopping

```bash
# Dump current recording to file without stopping
jcmd <PID> JFR.dump \
  name=baseline_run \
  filename=baseline_run_interim.jfr

# Use case: Periodic snapshots during long-running benchmark
```

### Complete Recording Workflow Script

```bash
#!/bin/bash
# Script: run_benchmark_with_profiling.sh

BENCHMARK_PID=$1
BASELINE_DURATION="10m"
BASELINE_OUTPUT="./jfr-profiles/baseline_$(date +%s).jfr"

# Create output directory
mkdir -p ./jfr-profiles

# Start JFR recording
echo "Starting JFR recording (PID: $BENCHMARK_PID)..."
jcmd "$BENCHMARK_PID" JFR.start \
  name=benchmark_baseline \
  duration="$BASELINE_DURATION" \
  filename="$BASELINE_OUTPUT" \
  settings=profile

# Wait for benchmark execution
echo "Benchmark running for $BASELINE_DURATION..."
sleep 600  # 10 minutes

# Recording automatically stops and writes to file
echo "JFR recording complete: $BASELINE_OUTPUT"

# List file size
ls -lh "$BASELINE_OUTPUT"

# Note: Next step: Open in JMC for analysis
echo "Open in JMC: $BASELINE_OUTPUT"
```

### Expected Output

JFR files are binary format, approximately:
- **10 minute recording with default sampling:** 15-30 MB
- **Custom light profile (method sampling only):** 8-15 MB
- **Detailed profile with all events:** 50-100 MB

---

## Java Mission Control (JMC) Analysis

### JMC Overview

Java Mission Control is the primary GUI tool for analyzing JFR recordings. Features:

- **Method Profiler:** Shows CPU time by method
- **Timeline View:** Time-based visualization of events
- **Flame Graphs:** Stack-based call hierarchy visualization
- **Memory Analysis:** Allocation hotspots and GC details
- **Lock Contention:** Monitor wait times and blocked threads

### Opening JFR File in JMC

**Step 1: Launch JMC**
```bash
# From JDK installation
$JAVA_HOME/bin/jmc

# Or if in PATH
jmc
```

**Step 2: Open File**
- Menu: `File → Open...`
- Navigate to `.jfr` file
- JMC auto-detects and parses format

**Step 3: Initial View**
- **Summary Tab:** Overview of run duration, GC events, threads
- **Memory Tab:** Heap usage, GC frequency, allocation rates
- **Threads Tab:** Thread activity and contention events
- **Events Tab:** Raw event browser with detailed information

### Method Profiler Analysis

**Access:** Tabs in main window → Method Profiler

**Interpretation:**
1. **Sample Count Column:** Number of samples collected for method
   - Directly correlates to runtime percentage
   - Example: 2700 samples out of 6000 = 45% runtime

2. **Package Filter:** Filter by package to focus analysis
   - `com.example.algorithms.*` - Algorithm implementations
   - `java.util.*` - Standard library methods
   - `java.lang.*` - JVM internals

3. **Stack View:** Click method to see call stack
   - Shows which callers invoke the hot method
   - Helps determine optimization strategy

**Example Analysis Flow:**
```
1. Open baseline_run.jfr in JMC
2. Navigate to Method Profiler tab
3. Sort by "Sample Count" (descending)
4. Top result: Sort.SortVector (2700 samples, 45%)
   → This is a Tier 1 hotspot
   → Expected: O(n log n) sorting dominates time
   → No optimization needed (efficient algorithm)

5. Second result: Vector.get() (420 samples, 7%)
   → This is a Tier 2 hotspot
   → Unexpected: Method should be quick
   → Root cause: Synchronized method overhead
   → Action: Evaluate thread-safe alternatives
```

### Flame Graph Analysis

**Access:** Menus → Windows → Flame Graph (if available in version)
Or use "Stack Trace View" with visualization

**How to Interpret Flame Graphs:**

1. **Horizontal Axis:** Represents time spent in each method
   - Wider box = more time spent
   - Narrow box = quick operation

2. **Vertical Axis:** Call stack depth
   - Bottom frame = bottom of stack
   - Top frame = currently executing method
   - Left-to-right: Call hierarchy

3. **Color Coding:** Typically by package
   - Red/orange: User code (com.example.*)
   - Blue: Standard library (java.*)
   - Gray: JVM internals

**Example Flame Graph (Text Representation):**
```
benchmark_main;Sort.SortVector;Collections.sort;Arrays.sort [45%]
├─ benchmark_main;Sort.SortVector;Collections.sort;TimSort.sort [43%]
│  ├─ TimSort.mergeAt [25%]
│  ├─ TimSort.binarySort [12%]
│  └─ TimSort.gallopLeft [6%]
└─ Other sort operations [2%]

benchmark_main;Vector.get;Monitor.Enter [7%]
benchmark_main;GC overhead;ParallelGC.gc [3%]
```

### Timeline View

**Access:** Timeline tab in JMC

**Interpretation:**
1. **Method Timeline:** Execution profile over time
   - Identify if hotspots are constant or changing
   - Example: Sort.SortVector flat at 45% indicates consistent load

2. **GC Timeline:** Pause frequency and duration
   - Example: GC spikes visible if >100ms pauses occur
   - Monitor heap level rise/fall pattern

3. **Thread Activity:** Thread utilization
   - Identify if single-threaded or parallel
   - Detect thread blocking/waiting

4. **Lock Timeline:** Lock contention over time
   - Peaks indicate contention events
   - Duration of bars shows lock wait time

---

## Flame Graph Interpretation

### Flame Graph Structure

A flame graph displays execution stack traces in graphical form:

```
                    [All Samples (100%)]
                           |
            _______________|_______________
           |               |               |
        [Main (60%)]    [GC (15%)]    [Locks (7%)]
           |
      [Sort (45%)]
           |
      [TimSort (43%)]
         / | \
      [mergeAt] [binarySort] [gallopLeft]
      (25%)     (12%)         (6%)
```

### Key Interpretation Principles

#### 1. Wider = Hotter
- **Horizontal width directly correlates to execution time**
- Wider stack frame = method consumed more CPU time

#### 2. Stack Depth Indicates Complexity
- Shallow stack (few nested calls): Direct computation
  - Example: Prime checking with few helper methods
- Deep stack (many nested calls): Complex call chains
  - Example: Collections.sort → TimSort → multiple helper methods

#### 3. Repeated Patterns = Loops
- Same stack sequence repeating horizontally suggests loop
- Example: Sort.SortVector → TimSort → mergeAt repeated 100x
  - Indicates 100 merge operations in sorting

#### 4. Sawtooth Patterns = Recursion
- Repeated stack names with decreasing width indicates recursion
- Example: quickSort → partition → quickSort → ...
  - Recursion depth visible in stack height

### Finding Hotspots in Flame Graphs

**Method 1: Look for Wide Bars at Top Level**
```
If bar at top > 10% width → Tier 1 hotspot
If bar at top 5-10% width → Tier 2 hotspot
If bar at top < 5% width → Supporting code
```

**Method 2: Follow Call Chain Downward**
```
Wide bar at top:
  ↓ (click/expand)
Which sub-methods make it wide?
  ↓
Are those sub-methods expensive by algorithm or by calling frequency?
  ↓
Determine optimization approach
```

**Method 3: Compare Expected vs Actual**
```
Benchmark design says:
  Sort.SortVector should be ~45% (O(n log n) on 10K elements)
  Vector.get should be <5% (quick synchronized method)

Actual JFR shows:
  Sort.SortVector = 45% ✓ Matches expectation
  Vector.get = 7% ✗ Higher than expected (lock overhead)

Conclusion: Lock contention is higher than expected
```

### Converting Flame Graphs to Actionable Insights

**Example 1: Hotspot = Expected Algorithm Time**
```
Observation: Sort.SortVector takes 45% of runtime
Analysis: O(n log n) TimSort on 10K elements is expected to dominate
Implication: This is not a bottleneck; algorithm is efficient
Action: No optimization needed
```

**Example 2: Hotspot = Synchronization Overhead**
```
Observation: Vector.get takes 7% but expected <5%
Analysis: Vector.get is synchronized; high call frequency causes overhead
Implication: Synchronization overhead exceeds algorithm cost
Action: Replace Vector with ArrayList (not thread-safe but faster)
        Or use CopyOnWriteArrayList if thread safety needed
```

**Example 3: Hotspot = Unexpected Deep Call Stack**
```
Observation: Flame graph shows many nested calls in HashMap operation
Analysis: Hash collisions causing long probe chains
Implication: Default hash function or small capacity is problematic
Action: Increase initial capacity or improve hash function
```

---

## Correlation with Benchmark Results

### Linking JFR Findings to Benchmark Timing

Benchmark results provide **overall timing**, while JFR profiles explain **why**.

#### Correlation Process

**Step 1: Establish Baseline Metrics**
```
Benchmark execution result:
  Sort.SortVector benchmark: 4.52 ms (3 iterations averaged)

JFR profile result:
  Total run time: 600 seconds
  Sort.SortVector hotspot: 45% of CPU time
  Implied: 45% × 600s = 270s in Sort.SortVector across all iterations
  Average per iteration: 270s / (many iterations) ≈ 4.5 ms ✓ Matches!
```

**Step 2: Identify Discrepancies**
```
Benchmark reports: 4.52 ms total
JFR predicts: Should be ~4.5 ms based on hot method timing

Variance: 0.02 ms (acceptable ±0.5% variation)
Conclusion: Profile accurately explains timing
```

**Step 3: Investigate Surprises**
```
Benchmark reports: 4.52 ms total
JFR shows: Only 3.8 ms in Sort.SortVector (accounting for all sampled time)

Variance: 0.72 ms unaccounted (16% discrepancy)
Investigation: 
  - Check for GC pauses during iteration
  - Examine lock contention events
  - Profile might undercount fast methods
```

### Building Correlation Report

**Template: Benchmark Result ↔ JFR Profile Correlation**

```markdown
## Benchmark: Sort.SortVector (10K elements)

### Benchmark Result
- Execution Time: 4.52 ms
- Variance: ±0.08 ms (within ±2%)
- Status: STABLE

### JFR Profile Analysis
- Method: Sort.SortVector
  - Sample Count: 2700/6000 (45%)
  - Implied Time: 270 seconds total (estimated across full run)
  - Per-iteration: ~4.5 ms (MATCHES benchmark)
  
- Sub-method breakdown:
  - Collections.sort: 43% of Sort.SortVector time
  - Reason: O(n log n) TimSort implementation
  - Expected: Yes, algorithm-limited
  
- Lock Overhead:
  - Vector.get: 7% total (not just in Sort method)
  - Contention wait: <0.1 ms
  - Impact on Sort.SortVector: ~0.3 ms (7% of 4.5 ms)
  - Acceptable: Yes

### Correlation Conclusion
✓ JFR profile explains 99% of benchmark timing
✓ Main time spent in expected algorithm (TimSort)
✓ Synchronization overhead acceptable
✓ No unexpected bottlenecks detected
```

### Correlation Across Multiple Benchmarks

**Table: Benchmark Results ↔ JFR Profile Summary**

| Benchmark | Exec Time | Expected Hotspot | JFR Hotspot | Match | Notes |
|-----------|-----------|------------------|-------------|-------|-------|
| Sort.SortVector | 4.52 ms | Collections.sort (O(n log n)) | 45% | ✓ | Algorithm-limited |
| Primes.SumPrimes(10K) | 245 ms | IsPrime inner loop (O(√n)) | 52% | ✓ | CPU-bound |
| DataStructures.modifyVector | 1.2 ms | Vector.get/set sync | 8% | ✓ | Lock overhead expected |
| Control.DoubleLoop | 3.8 ms | Inner loop (O(n²)) | 61% | ✓ | All CPU cycles in inner loop |

### When JFR and Benchmark Don't Align

**Possible Causes:**

1. **GC Pauses Not Captured in JFR Sampling**
   - Solution: Check GarbageCollection events specifically
   - May not appear in method samples if pauses occur between samples

2. **I/O Wait Times**
   - Solution: Check I/O events in JFR (FileRead, FileWrite)
   - May block execution time but not show in CPU samples

3. **Profile Overhead**
   - Solution: Compare profiled vs unprofiled runs
   - Profiling itself can add 5-10% overhead

4. **Sample Rate Too Low**
   - Solution: Increase sample rate (reduce period below 100ms)
   - Trade-off: Higher overhead but better granularity

5. **JIT Compilation During Run**
   - Solution: Ensure JVM warmup before measurement
   - JIT can introduce time spikes not captured in steady-state profile

---

## Expected Hotspots by Benchmark Category

### Algorithms.Sort Category

#### SortVector Benchmark
**Expected Hotspot:** `Collections.sort` and `TimSort` internal methods
- **Percentage:** 40-50% of total SortVector runtime
- **Reason:** O(n log n) complexity dominates time budget
- **Details:**
  - `TimSort.mergeAt`: 20-30% (merge operation cost)
  - `TimSort.binarySort`: 10-15% (element comparison cost)
  - `Vector.get`/`Vector.set`: 5-8% (array access + synchronization)
  
**Expected Profile Snapshot:**
```
Sample Distribution (10K element sort):
  Collections.sort: 45% ← Tier 1 Hotspot
  └─ TimSort methods: 44%
    ├─ mergeAt: 25%
    ├─ binarySort: 12%
    └─ gallopLeft: 7%
  Vector.get (inside sort): 3%
  Vector.set (inside sort): 2%
  Other: 8%
```

**Interpretation:**
- Expected behavior: TimSort completely dominates
- No optimization needed: Algorithm is optimal for this workload
- Synchronization overhead minimal relative to algorithm cost

#### DutchFlagPartition Benchmark
**Expected Hotspot:** Swap and comparison operations
- **Percentage:** 30-40% partition logic, 20-30% Vector operations
- **Reason:** O(n) partitioning requires many comparisons and swaps

**Expected Profile Snapshot:**
```
Sample Distribution (10K element partition):
  DutchFlagPartition: 35% ← Tier 1 Hotspot
  └─ Compare operations: 18%
  └─ Swap/get/set: 17%
  Vector.get/set: 25% ← Tier 2 Hotspot (sync overhead)
  GC / Memory: 5%
  Other: 10%
```

**Interpretation:**
- Vector synchronization (25%) is significant relative to work (35%)
- Consider: Lock contention may be >1% due to high call frequency
- Optimization opportunity: Thread-safe alternatives with better performance

#### MaxN Benchmark
**Expected Hotspot:** Heap operations and sorting
- **Percentage for n=1000:** 50-60% heap operations and final sort
- **Percentage for n=10:** 10-20% quick heap operations

**Expected Profile Snapshot (n=1000):**
```
Sample Distribution (10K elements, n=1000):
  Heap operations: 35% ← Tier 1 Hotspot
  └─ Insert: 20%
  └─ Extract: 15%
  Vector construction: 12% ← Tier 2 Hotspot
  Vector sorting: 10% ← Tier 2 Hotspot
  Vector.get: 8% ← Sync overhead
  Other: 23%
```

**Interpretation:**
- Heap performance dominates when n is large
- Vector construction and sorting secondary factors
- Synchronization overhead acceptable (8% < 10% threshold)

### Algorithms.Primes Category

#### IsPrime Benchmark
**Expected Hotspot:** Division loop and modulo operations
- **Percentage:** 60-70% prime checking loop
- **Reason:** O(√n) trial division is the only operation

**Expected Profile Snapshot:**
```
Sample Distribution (100-10000 range tests):
  IsPrime division loop: 65% ← Tier 1 Hotspot
  └─ Modulo operations: 35%
  └─ Comparison operations: 30%
  Method call overhead: 5%
  Other: 30%
```

**Interpretation:**
- Expected: Almost all time in division/modulo
- No optimization: Algorithm is optimal for primality testing
- Variance: Large numbers take longer (longer √n)

#### SumPrimes Benchmark
**Expected Hotspot:** IsPrime calls (nested O(n√n) structure)
- **Percentage:** 70-80% in IsPrime and its division loop
- **Reason:** Calls IsPrime n times; each is O(√n)

**Expected Profile Snapshot:**
```
Sample Distribution (SumPrimes(10000)):
  IsPrime method: 72% ← Tier 1 Hotspot
  └─ Division loop: 65%
  └─ Comparison: 7%
  SumPrimes loop: 15% ← Supporting code
  Integer operations: 8%
  Other: 5%
```

**Interpretation:**
- Expected: IsPrime dominates (O(n√n) structure)
- Heaviest benchmark in Primes category (expected)
- No optimization: Inherent algorithm complexity

#### PrimeFactors Benchmark
**Expected Hotspot:** Trial division loop, similar to IsPrime
- **Percentage:** 55-65% in division loop
- **Reason:** Iterates until √n, checking each divisor

**Expected Profile Snapshot:**
```
Sample Distribution (50 numbers, 5000-10000 range):
  Division loop: 60% ← Tier 1 Hotspot
  └─ Modulo operations: 35%
  └─ Vector add operations: 15%
  └─ Loop control: 10%
  Vector creation/management: 15% ← Tier 2 Hotspot
  Other: 25%
```

**Interpretation:**
- Expected: Division-based loop dominates
- Variable time: Highly composite numbers (many factors) take longer
- Small prime result set: Vector overhead relatively small

### DataStructures.DsVector Category

#### modifyVector Benchmark
**Expected Hotspot:** Vector.get and Vector.set synchronization
- **Percentage:** 70-80% in Vector synchronized methods
- **Reason:** Each operation is synchronized; called 1000-10000 times

**Expected Profile Snapshot:**
```
Sample Distribution (10K elements, get+set for each):
  Vector.get (synchronized): 45% ← Tier 1 Hotspot
  Vector.set (synchronized): 35% ← Tier 1 Hotspot
  Lock wait/contention: 8% ← Monitor overhead
  Other: 12%
```

**Interpretation:**
- Expected: Synchronization dominates small workload
- Lock contention check: Likely >1% overhead (8% in this case)
- Optimization opportunity: Replace Vector with ArrayList or use CopyOnWriteArrayList

#### searchVector Benchmark
**Expected Hotspot:** Vector.get in loop + comparison
- **Percentage:** 50-60% Vector.get (synchronized), 15-25% comparison

**Expected Profile Snapshot:**
```
Sample Distribution (10K element search):
  Vector.get (synchronized): 52% ← Tier 1 Hotspot
  Comparison operations: 20% ← Tier 2 Hotspot
  Loop control: 10%
  Other: 18%
```

**Interpretation:**
- Expected: Vector synchronization dominates linear search
- Lock contention: Likely >1% (50%+ overhead for non-existent lock wait suggests call overhead, not contention)
- Optimization: Use ArrayList for faster access

#### insertVector Benchmark
**Expected Hotspot:** Vector.add method (includes shifting)
- **Percentage:** 60-70% in Vector.add
- **Reason:** Insertion at position requires shift; O(n) per insert

**Expected Profile Snapshot:**
```
Sample Distribution (insertions into 10K element vector):
  Vector.add (synchronized): 62% ← Tier 1 Hotspot
  └─ Element shifting: 40%
  └─ Array copy: 15%
  └─ Lock operations: 7%
  Comparison/position logic: 15%
  Other: 23%
```

**Interpretation:**
- Expected: Array shifting dominates insertion time
- Lock contention: 7% shown; check if >1% wait time
- Optimization: Use LinkedList for frequent insertions, or pre-allocate larger Vector

#### filterVector Benchmark
**Expected Hotspot:** Vector iteration and conditional copying
- **Percentage:** 40-50% Vector.get, 25-35% Vector.add (for result)

**Expected Profile Snapshot:**
```
Sample Distribution (filter 10K elements):
  Vector.get (synchronized): 48% ← Tier 1 Hotspot
  Vector.add (result building): 28% ← Tier 2 Hotspot
  Comparison/filter logic: 18%
  Other: 6%
```

**Interpretation:**
- Expected: Two passes (iterate and add) create dual hotspots
- Lock contention: Two distinct lock points; check combined >1% threshold
- No optimization needed: Inherent cost of two passes

#### rotateVector Benchmark
**Expected Hotspot:** Element swaps/moves and indices
- **Percentage:** 50-60% get/set operations, 20-30% rotation logic

**Expected Profile Snapshot:**
```
Sample Distribution (rotate 10K element vector):
  Vector.get (synchronized): 35% ← Tier 1 Hotspot
  Vector.set (synchronized): 30% ← Tier 1 Hotspot
  Index calculation: 15%
  Loop control: 10%
  Other: 10%
```

**Interpretation:**
- Expected: Synchronization for every get/set operation
- Lock contention: Likely >1% due to 65% synchronized method overhead
- Optimization: LinkedList may be better for rotation

#### reverseVector Benchmark
**Expected Hotspot:** Vector.get and Vector.set for swap operations
- **Percentage:** 55-65% synchronized methods, 15-20% swap logic

**Expected Profile Snapshot:**
```
Sample Distribution (reverse 10K element vector):
  Vector.get (synchronized): 38% ← Tier 1 Hotspot
  Vector.set (synchronized): 32% ← Tier 1 Hotspot
  Swap logic: 18%
  Loop control: 8%
  Other: 4%
```

**Interpretation:**
- Expected: Swap requires 2 get + 2 set = 4 synchronized calls per element
- Lock contention: Likely >1% (70% synchronized overhead)
- Optimization: Use ArrayList or atomic reference array

### DataStructures.DsLinkedList Category

#### searchList Benchmark
**Expected Hotspot:** LinkedList.get traversal
- **Percentage:** 70-80% in LinkedList.get (traversal to nth element)
- **Reason:** O(n) per access due to sequential node traversal

**Expected Profile Snapshot:**
```
Sample Distribution (search 10K element LinkedList):
  LinkedList.get (synchronized): 74% ← Tier 1 Hotspot
  └─ Node traversal loop: 65%
  └─ Comparison operations: 9%
  Loop control: 15%
  Other: 11%
```

**Interpretation:**
- Expected: LinkedList.get is O(n) and dominates
- Critical hotspot: Much worse than Vector.get (which is O(1))
- Comparison: This is why LinkedList poor for random access

#### insertList Benchmark
**Expected Hotspot:** LinkedList insertion and traversal
- **Percentage:** 65-75% LinkedList.add (traversal + insertion)

**Expected Profile Snapshot:**
```
Sample Distribution (insert into 10K LinkedList):
  LinkedList.add (synchronized): 69% ← Tier 1 Hotspot
  └─ Traversal to position: 45%
  └─ Node manipulation: 24%
  Comparison logic: 15%
  Other: 16%
```

**Interpretation:**
- Expected: Traversal to insertion point is expensive (O(n) per insert)
- Use case appropriate: LinkedList good for front/back operations
- Poor for random position insertions (requires traversal)

#### appendList Benchmark
**Expected Hotspot:** LinkedList.add operations (back operations)
- **Percentage:** 40-50% LinkedList.add (optimized for back access)

**Expected Profile Snapshot:**
```
Sample Distribution (append to 10K LinkedList):
  LinkedList.add (tail optimized): 44% ← Tier 1 Hotspot
  Node creation: 25%
  Reference updates: 19%
  Loop control: 18%
  Other: 13%
```

**Interpretation:**
- Expected: Much faster than insertion (tail is O(1))
- LinkedList optimal use case: Back insertions
- This is where LinkedList outperforms Vector

### Control.Single Category

#### SingleLoop Benchmark
**Expected Hotspot:** Loop body operations (increments, comparisons)
- **Percentage:** 85-95% in loop body
- **Reason:** Loop is only operation; minimal overhead

**Expected Profile Snapshot:**
```
Sample Distribution (1M iterations):
  Loop increment (i++): 48% ← Tier 1 Hotspot
  Integer addition: 35% ← Tier 1 Hotspot
  Loop condition check: 12%
  Other: 5%
```

**Interpretation:**
- Expected: Almost all time in pure computation (i++)
- Minimal JVM overhead: Direct bytecode execution
- Use as baseline: How fast can JVM execute simple operations?

### Control.Double Category

#### DoubleLoop Benchmark
**Expected Hotspot:** Inner loop body (nested operations)
- **Percentage:** 90-95% in inner loop body
- **Reason:** O(n²) with minimal work per iteration

**Expected Profile Snapshot:**
```
Sample Distribution (1000×1000 iterations):
  Inner loop increment: 55% ← Tier 1 Hotspot
  Integer addition: 30% ← Tier 1 Hotspot
  Inner loop condition: 12%
  Other: 3%
```

**Interpretation:**
- Expected: Dominance by inner loop (1M iterations)
- Scaling: 100x slower than SingleLoop (1M vs 10K iterations)
- Cache behavior: May show degradation if inner loop doesn't fit in cache

---

## Troubleshooting and Best Practices

### Common Issues and Solutions

#### Issue 1: JFR Recording File Too Large

**Problem:** `.jfr` file exceeds 500 MB, difficult to analyze

**Causes:**
1. Recording duration too long
2. Sample rate too frequent (period <50ms)
3. Too many events enabled
4. High allocation rate (if allocation sampling enabled)

**Solutions:**
```bash
# Solution 1: Reduce recording duration
jcmd <PID> JFR.start \
  name=short_profile \
  duration=2m \         # Reduced from 10m
  filename=short.jfr \
  settings=profile

# Solution 2: Reduce sample frequency
jdk.ExecutionSample.period=200ms    # Changed from 100ms

# Solution 3: Disable non-critical events
jdk.ObjectAllocationSample.enabled=false   # Disable if not needed
jdk.SocketRead.enabled=false               # Disable if no I/O
jdk.SocketWrite.enabled=false

# Solution 4: Use a lighter profile
settings=default      # Lighter than 'profile'
```

#### Issue 2: Recording Shows No Hotspots (Flat Profile)

**Problem:** JFR shows random methods with even distribution (all ~0.5%)

**Causes:**
1. Sample rate too low (periods too large)
2. Recording duration too short (<1 minute)
3. Application not running during profiling
4. Sample collection failed silently

**Solutions:**
```bash
# Solution 1: Increase sample frequency
jdk.ExecutionSample.period=50ms     # More samples per second

# Solution 2: Extend recording
duration=10m        # At least 5m for meaningful profile

# Solution 3: Verify application is running
# Check system monitor during recording
# Monitor CPU usage (should be >50% if CPU-bound benchmark)

# Solution 4: Check event count in JMC
# Events tab → Check if ExecutionSample events > 1000
# If <100 total samples, too sparse for analysis
```

#### Issue 3: Lock Contention Shows >1% but No Apparent Cause

**Problem:** JFR reports high lock contention, but lock monitor isn't obvious

**Causes:**
1. Locks on shared object used frequently (e.g., Vector)
2. Contention on methods called in inner loop
3. Lock wait times accumulated over many operations

**Solutions:**
```
1. In JMC, go to Threads tab → Blocking events
2. Sort by "Wait time" (descending)
3. Identify monitor object
4. Search for object in code (Ctrl+F "Vector" or "HashMap")
5. Evaluate if thread-safety is necessary
6. Consider alternatives:
   - Vector → ArrayList (no thread safety)
   - Vector → CopyOnWriteArrayList (thread-safe, read-optimized)
   - HashMap → ConcurrentHashMap (thread-safe, higher performance)
```

#### Issue 4: GC Pause Times >100ms Detected

**Problem:** Benchmark shows GC pauses exceeding 100ms threshold

**Causes:**
1. Heap too small relative to allocation rate
2. Full garbage collection (not just young generation)
3. High object turnover rate

**Solutions:**
```bash
# Solution 1: Increase heap size
java -Xmx2g -Xms2g \        # Set initial and max heap
     -XX:+UseG1GC \          # Use G1GC for predictable pauses
     -XX:MaxGCPauseMillis=50 \  # Target 50ms pauses
     YourBenchmarkClass

# Solution 2: Monitor allocation rate
# In JMC → Memory tab → Allocation
# If >100MB/s, consider reducing test size

# Solution 3: Use ZGC for very low pause times
java -XX:+UseZGC \          # Z Garbage Collector (<10ms pauses)
     YourBenchmarkClass
```

#### Issue 5: JMC Crashes When Opening Large .jfr File

**Problem:** Java Mission Control crashes or becomes unresponsive with large file

**Causes:**
1. JMC lacks sufficient memory
2. File corruption
3. JMC version incompatible with JFR format

**Solutions:**
```bash
# Solution 1: Increase JMC memory
# Edit jmc.ini (in JMC installation directory)
-Xmx4g    # Increase from default 1-2g

# Solution 2: Convert .jfr to smaller file
jcmd <PID> JFR.dump \
  name=original_recording \
  filename=partial.jfr \
  maxage=2m              # Only last 2 minutes of recorded data

# Solution 3: Use command-line JFR tools instead
# jfr print --events jdk.ExecutionSample baseline_run.jfr | less
# (Useful for version mismatch)
```

### Best Practices for JFR Profiling

#### 1. Always Profile in Isolation

**Practice:** Run profiling on a quiet system

```bash
# Before profiling:
1. Close unnecessary applications
2. Stop background services
3. Disable screen lock (doesn't affect but clean)
4. Run on dedicated server if possible

# Verify quiet system:
top -b -n 1 | head -20   # Check CPU usage <20% idle
free -h                  # Check memory available
```

**Why:** Background processes corrupt profiling data with noise

#### 2. Establish Baseline + Candidate Comparison

**Practice:** Always record both baseline and optimization candidate

```
Phase 1: Baseline Recording
  - Run with original code
  - Record 5-10 minute JFR profile
  - Save as: baseline_2024_01_15.jfr
  - Save benchmark timing: baseline_2024_01_15.txt

Phase 2: Candidate Recording
  - Apply optimization to code
  - Rebuild and deploy
  - Record 5-10 minute JFR profile (same length as baseline)
  - Save as: candidate_2024_01_15.jfr
  - Save benchmark timing: candidate_2024_01_15.txt

Phase 3: Comparison
  - Open baseline and candidate in JMC (separate tabs)
  - Compare hotspot percentages
  - Verify timing matches expectation
```

#### 3. Match Recording Duration to Benchmark Duration

**Practice:** Record for entire benchmark execution, plus warm-up

```
Benchmark specification: 5-10 minute runs

Recommended recording approach:
1. Start JFR recording (10m 30s duration)
2. Wait 30 seconds (JVM warm-up)
3. Start benchmark (runs for 5-10 minutes)
4. JFR automatically stops when duration expires
5. Analyze recording

Why +30s extra: Captures warm-up period for context
```

#### 4. Use Consistent Settings Across Recordings

**Practice:** Apply identical JFR settings for baseline and candidate

```bash
# BASELINE recording
jcmd $PID JFR.start \
  name=baseline \
  duration=10m \
  filename=baseline.jfr \
  settings=profile \
  jdk.ExecutionSample.period=100ms \
  jdk.GarbageCollection.enabled=true

# CANDIDATE recording (identical except name/filename)
jcmd $PID JFR.start \
  name=candidate \
  duration=10m \
  filename=candidate.jfr \
  settings=profile \
  jdk.ExecutionSample.period=100ms \    # SAME
  jdk.GarbageCollection.enabled=true    # SAME
```

**Why:** Different settings make comparison meaningless

#### 5. Verify Hotspot Stability Across Multiple Runs

**Practice:** Run profiling 3+ times to confirm hotspot consistency

```
Run 1: Hotspot A (45%), Hotspot B (8%)
Run 2: Hotspot A (45%), Hotspot B (7%)
Run 3: Hotspot A (46%), Hotspot B (8%)

Stability assessment:
  Hotspot A: ±0.5% variance ✓ Stable
  Hotspot B: ±1% variance ✓ Stable
  Conclusion: Results reliable for comparison

If variance >5%, investigate:
  1. System contention (other processes running)
  2. Thermal throttling (CPU overheating)
  3. Dynamic frequency scaling (power management)
```

#### 6. Document Profiling Context

**Practice:** Record metadata alongside JFR file

```
File: baseline_2024_01_15.jfr

Metadata (store in README or metadata file):
- JDK Version: OpenJDK 21.0.1
- JVM Flags: -Xmx4g -Xms4g -XX:+UseG1GC
- Application Version: commit a1b2c3d
- Benchmark Configuration: Scenario B full suite
- Hardware: Intel Xeon E5-2680v4 @ 2.40GHz × 28 cores
- Recording Settings:
  - Duration: 10 minutes
  - Method sampling: 100ms period
  - GC events: enabled
  - Lock contention: enabled
- Benchmark Timing Result: 6m 42s total
- System Load: Average CPU 85%, Memory 60%

Result: This recording explains benchmark timing of 6m42s through
         hotspot analysis of Sort.SortVector (45%), IsPrime (22%),
         Vector sync overhead (8%).
```

**Why:** Future reference; critical for comparing old vs new recordings

#### 7. Archive JFR Files for Future Analysis

**Practice:** Keep recordings organized and searchable

```
Directory structure:
benchmark-profiles/
├── baseline/
│   ├── baseline_2024_01_15.jfr
│   ├── baseline_2024_01_15.txt (timing results)
│   └── baseline_2024_01_15_metadata.md
├── candidate/
│   ├── candidate_2024_01_15.jfr
│   ├── candidate_2024_01_15.txt (timing results)
│   └── candidate_2024_01_15_metadata.md
└── comparison_report_2024_01_15.md
```

**Commands:**
```bash
# Archive baseline
gzip baseline_2024_01_15.jfr
# Compress from ~20MB → ~5MB

# Later analysis
gunzip baseline_2024_01_15.jfr.gz
jmc baseline_2024_01_15.jfr &
```

### Benchmark + Profile Synchronization Checklist

Use this checklist before profiling session:

```
Before Baseline Recording:
  ☐ Code committed and tagged (for reference)
  ☐ System quiet (check top, background processes <10%)
  ☐ Benchmark parameters documented
  ☐ JVM flags documented
  ☐ Output directory created: ./jfr-profiles/

During Recording:
  ☐ JFR recording started (verify with jcmd check)
  ☐ Benchmark started (after 30s warmup)
  ☐ Monitor system (CPU, memory, I/O) during execution
  ☐ Note any anomalies (spikes, pauses, errors)

After Recording:
  ☐ JFR file verified (size >5MB, readable)
  ☐ Benchmark timing recorded
  ☐ Metadata file created
  ☐ File archived/compressed

Before Comparison:
  ☐ Baseline analysis complete
  ☐ Candidate recording collected (with same settings)
  ☐ Candidate analysis complete
  ☐ Comparison report generated
```

---

## Summary

This JFR profiling strategy provides a comprehensive framework for:

1. **Capturing** detailed runtime behavior during 5-10 minute benchmark runs
2. **Identifying** performance hotspots using clear, quantitative thresholds
3. **Analyzing** profiling data with industry-standard tools (JMC, flame graphs)
4. **Correlating** profiling findings with benchmark timing results
5. **Understanding** expected bottlenecks for each benchmark category

The strategy is **actionable** (specific threshold values), **automated** (jcmd command syntax), and **repeatable** (consistent methodology across runs). Use this guide alongside the Benchmark Design Document to establish a complete performance analysis pipeline.

