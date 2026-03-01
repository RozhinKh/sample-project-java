# JMH Baseline Benchmark Execution Checklist

## Scope Definition

Baseline run includes only:
- `SortBenchmarks`
- `PrimesBenchmarks`
- `ControlBenchmarks`

Expected baseline count: 16 benchmark methods.

## Verification Checklist

### Build Configuration
- [x] JMH plugin configured
- [x] JMH dependencies configured
- [x] Baseline include pattern set to:
  - `.*(SortBenchmarks|PrimesBenchmarks|ControlBenchmarks).*`

### Task Configuration
- [x] `app:jmhBaseline` exists
- [x] `app:jmhBaseline` depends on `jmh`
- [x] Baseline output copied to `benchmark/baseline/jmh-result.json`

### Repository Scope Clarity
- [x] Baseline classes documented
- [x] Additional non-baseline benchmark classes documented
- [x] Scope mismatch issue removed from completion docs

### Execution
Run baseline:

```bash
./gradlew app:jmhBaseline
```

Optional:

```bash
./gradlew app:jmhBaseline -PjfrEnabled=true
```

### Output Checks
- [x] `benchmark/baseline/jmh-result.json` exists after run
- [x] JSON is valid JMH array
- [x] Entry count should be 16

### Comparison Readiness
- [x] JMH comparison script exists: `scripts/compare_jmh.py`
- [x] Script reads `primaryMetric.score` (not non-existent custom fields)

Usage:

```bash
python scripts/compare_jmh.py baseline.json candidate.json
```
