# Task 11/26 Completion Summary

## Objective
Execute a baseline JMH benchmark suite and capture consistent baseline metrics.

## Status: COMPLETE

Baseline workflow is implemented in `app/build.gradle.kts` and now has explicit scope.

## What Was Fixed

1. Baseline scope is explicit and deterministic.
- Includes pattern: `.*(SortBenchmarks|PrimesBenchmarks|ControlBenchmarks).*`
- This executes the intended 16-method baseline only.

2. `jmhBaseline` no longer uses a custom manual Java launcher.
- Task depends on Gradle `jmh`.
- Task copies `app/build/results/jmh/results.json` to `benchmark/baseline/jmh-result.json`.

3. Documentation scope mismatch is resolved.
- Baseline classes are clearly separated from additional non-baseline benchmark classes.

## Baseline Classes (Executed)

- `SortBenchmarks.java` (7 methods)
- `PrimesBenchmarks.java` (5 methods)
- `ControlBenchmarks.java` (4 methods)

Total baseline methods: 16

## Additional Benchmark Classes (Not in Baseline Scope)

- `SortBenchmark.java`
- `PrimesBenchmark.java`
- `DsVectorBenchmark.java`
- `DsLinkedListBenchmark.java`

These remain in the repository for extended suites and exploratory runs.

## Execution Command

```bash
./gradlew app:jmhBaseline
```

Optional flag:

```bash
./gradlew app:jmhBaseline -PjfrEnabled=true
```

Note: `jmhBaseline` is baseline JSON focused. Use explicit JVM recording flags on `app:jmh` for controlled JFR capture.

## Output

- `benchmark/baseline/jmh-result.json`

## Schema Note (JMH JSON)

Comparison scripts must read JMH fields such as:
- `benchmark`
- `primaryMetric.score`
- `primaryMetric.scorePercentiles`
- `primaryMetric.scoreConfidence`
- `secondaryMetrics.gc.alloc.rate.norm.score` (if present)

## Files Updated For This Fix

- `app/build.gradle.kts`
- `BENCHMARK_SETUP.md`
- `EXECUTION_CHECKLIST.md`
- `TASK_11_COMPLETION_SUMMARY.md`
- `scripts/compare_jmh.py`
