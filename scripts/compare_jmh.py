#!/usr/bin/env python3
"""Compare two JMH JSON result files and report regressions/improvements."""

from __future__ import annotations

import json
import sys
from pathlib import Path


def load_results(path: Path) -> dict[str, dict]:
    data = json.loads(path.read_text(encoding="utf-8"))
    if not isinstance(data, list):
        raise ValueError(f"{path} is not a JMH JSON array")
    out = {}
    for entry in data:
        name = entry.get("benchmark")
        metric = entry.get("primaryMetric", {})
        score = metric.get("score")
        if name is None or score is None:
            continue
        out[name] = entry
    return out


def get_score(entry: dict) -> float:
    return float(entry["primaryMetric"]["score"])


def main() -> int:
    if len(sys.argv) not in (3, 4):
        print("Usage: compare_jmh.py <baseline.json> <candidate.json> [regression_threshold_pct]")
        return 2

    baseline_path = Path(sys.argv[1])
    candidate_path = Path(sys.argv[2])
    threshold_pct = float(sys.argv[3]) if len(sys.argv) == 4 else 5.0

    baseline = load_results(baseline_path)
    candidate = load_results(candidate_path)

    common = sorted(set(baseline.keys()) & set(candidate.keys()))
    if not common:
        print("No overlapping benchmarks between baseline and candidate.")
        return 2

    regressions = []
    improvements = []

    print("benchmark,baseline_ms_per_op,candidate_ms_per_op,percent_change,status")
    for name in common:
        base = get_score(baseline[name])
        cand = get_score(candidate[name])
        # Positive means candidate faster; negative means regression.
        pct = ((base - cand) / base) * 100 if base != 0 else 0.0
        status = "WITHIN_TOLERANCE"
        if pct < -threshold_pct:
            status = "REGRESSION"
            regressions.append((name, pct))
        elif pct > threshold_pct:
            status = "IMPROVEMENT"
            improvements.append((name, pct))
        print(f"{name},{base:.6f},{cand:.6f},{pct:.2f},{status}")

    print("")
    print(f"Compared: {len(common)} benchmarks")
    print(f"Improvements (>{threshold_pct:.1f}%): {len(improvements)}")
    print(f"Regressions (<-{threshold_pct:.1f}%): {len(regressions)}")

    if regressions:
        print("Regression details:")
        for name, pct in regressions:
            print(f"  {name}: {pct:.2f}%")
        return 1
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
