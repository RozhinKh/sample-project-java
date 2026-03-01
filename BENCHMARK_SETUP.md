# JMH Baseline Benchmark Setup

## Overview

This document describes the setup for executing the Scenario B baseline benchmark suite with Java Flight Recorder (JFR) profiling enabled.

## Components Configured

### 1. JMH Gradle Plugin Integration
- **Plugin**: me.champeau.jmh v0.7.2
- **Location**: `app/build.gradle.kts`
- **Configuration**:
  - Warmup iterations: 5
  - Measurement iterations: 5
  - Benchmark mode: Average time (avgt)
  - Time unit: Milliseconds (ms)
  - Result format: JSON

### 2. JMH Dependencies
- **JMH Core**: org.openjdk.jmh:jmh-core v1.36
- **JMH Annotation Processor**: org.openjdk.jmh:jmh-generator-annprocess v1.36
- **Location**: gradle/libs.versions.toml and app/build.gradle.kts

### 3. Benchmark Classes
Baseline-executed classes in `app/src/jmh/java/benchmarks/`:
- **SortBenchmarks.java**: 7 benchmark methods
  - sortVectorSmall, sortVectorMedium, sortVectorLarge
  - dutchFlagPartitionSmall, dutchFlagPartitionMedium
  - maxNMedium, maxNLarge
  
- **PrimesBenchmarks.java**: 5 benchmark methods
  - sumPrimesSmall, sumPrimesMedium
  - primeFactorsSmall, primeFactorsMedium
  - isPrimeSmall
  
- **ControlBenchmarks.java**: 4 benchmark methods
  - sumRangeSmall, sumRangeLarge
  - maxArraySmall, sumSquareSmall

**Total**: 16 benchmark methods

Additional benchmark classes also exist in the same directory (for extended suites), but are intentionally excluded from the baseline task:
- `SortBenchmark.java`
- `PrimesBenchmark.java`
- `DsVectorBenchmark.java`
- `DsLinkedListBenchmark.java`

### 4. JFR Configuration
- **File**: `app/src/jfrConfig.jfc`
- **Configured Events**:
  - jdk.ExecutionSample (100ms sampling interval)
  - jdk.GarbageCollection
  - jdk.GCPhaseParallel
  - jdk.ObjectAllocationInNewTLAB/OutsideTLAB
  - jdk.JavaMonitorEnter (10ms threshold)
  - jdk.JavaMonitorWait
  - jdk.ThreadPark
  - jdk.FileRead/FileWrite (1ms threshold)

### 5. Custom jmhBaseline Task
- **Location**: `app/build.gradle.kts`
- **Features**:
  - Creates benchmark/baseline/ directory
  - Executes baseline suite via the Gradle `jmh` task
  - Restricts baseline scope to `SortBenchmarks`, `PrimesBenchmarks`, and `ControlBenchmarks`
  - Copies generated `results.json` to `benchmark/baseline/jmh-result.json`
  - Provides detailed console output of baseline includes and output file size

### 6. Output Locations
- **Benchmark Results**: `benchmark/baseline/jmh-result.json`
- **JFR Recording**: `benchmark/baseline/profile.jfr`

## Execution Instructions

### Without JFR Profiling
```bash
./gradlew jmhBaseline
```

### With JFR Profiling (Recommended)
```bash
./gradlew jmhBaseline -PjfrEnabled=true
```
Note: `jmhBaseline` records baseline JSON. For controlled JFR capture, run `app:jmh` with explicit JVM recording flags.

## Expected Execution
- **Duration**: 5-10 minutes
- **Output Format**: JSON (compatible with JMH analysis tools)
- **Memory Requirements**: ~512MB heap (adjustable with -Xmx flag)

## Verification Steps

After execution, verify:

1. **jmh-result.json exists**:
   ```bash
   ls -lh benchmark/baseline/jmh-result.json
   ```
   Expected: File with size > 10KB containing valid JSON

2. **profile.jfr exists** (if JFR enabled):
   ```bash
   ls -lh benchmark/baseline/profile.jfr
   ```
   Expected: File with size > 1MB

3. **JSON structure is valid**:
   ```bash
   jq . benchmark/baseline/jmh-result.json
   ```
   Expected: Valid JSON with benchmark results array

4. **16 benchmark methods present**:
   ```bash
   jq '.[] | .benchmark' benchmark/baseline/jmh-result.json | wc -l
   ```
   Expected: 16 entries

## Benchmark Design Alignment

The 16 benchmark methods follow the Scenario B benchmark design specification:

- **Algorithms.Sort**: 3 methods (SortVector, DutchFlagPartition, MaxN)
- **Algorithms.Primes**: 2 methods (IsPrime, SumPrimes, PrimeFactors)
- **Control**: 3 methods (sumRange, maxArray, sumSquare, sumMatrix)
- **DataStructures**: Additional methods as needed

Each benchmark uses:
- Thread-scoped state (@State(Scope.Thread))
- Trial-level setup with test data generation
- Proper input data sizes (1K-10K elements)
- Multiple iterations to capture steady-state performance

## Troubleshooting

### Build fails
- Ensure Java 17+ is installed: `java -version`
- Clean build: `./gradlew clean build`

### Benchmarks don't run
- Check classpath: `./gradlew jmhClasspath`
- Verify benchmark classes compile: `./gradlew jmhCompile`

### JFR recording not created
- Ensure -PjfrEnabled=true is specified
- Check disk space in benchmark/baseline directory
- Verify JVM supports JFR (Java 11+)

### Performance anomalies
- Increase fork count in build.gradle.kts (currently 1)
- Run on idle system to reduce background interference
- Check for GC pauses in JFR recording

## Output Analysis

The jmh-result.json contains:
- Benchmark name and workload parameters
- Average time (`primaryMetric.score`)
- Confidence interval (`primaryMetric.scoreConfidence`)
- Percentiles (`primaryMetric.scorePercentiles`)
- Allocated memory (`secondaryMetrics.gc.alloc.rate.norm`)
- GC overhead metrics

The profile.jfr can be analyzed with:
- JDK Mission Control (JMC)
- Flame graph tools (async-profiler)
- Custom scripts (jcmd)

Example:
```bash
jcmd <pid> JFR.dump filename=export.jfr
```

Or open with JMC (GUI):
```bash
jmc benchmark/baseline/profile.jfr
```

## Notes

- Benchmark classes use synchronized Vector class to test contention
- Test data is generated randomly with fixed seed for reproducibility
- Warmup iterations ensure JIT compilation before measurements
- Results are valid only on the same hardware/JVM configuration
