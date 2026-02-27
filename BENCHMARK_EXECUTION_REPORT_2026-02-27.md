# Benchmark Execution Report (2026-02-27)

## Context
- Branch: `feat-jmh-baseline-performance-suite-bdc65`
- Command runner: PowerShell on Windows
- Goal: Execute benchmark workflow empirically and capture real behavior

## Commands Executed
1. `git pull`
2. `git log --oneline -5`
3. `git status --short --branch`
4. `./gradlew tasks --all --console=plain` (captured to `gradle-tasks-output.txt`)
5. Benchmark workflow command:
   - `./gradlew app:jmhBaseline -PjfrEnabled=true --console=plain`
   - captured to `benchmark-run-output.txt`

## Observed Results
- Repository was already up to date and clean before run.
- Gradle task discovery shows benchmark tasks including:
  - `app:jmhBaseline`
  - `app:jmh`
- Benchmark source files are present under `app/src/jmh/java/benchmarks`.

## Workflow Runtime Behavior
- `app:jmhBaseline` successfully started and printed its JMH launch command.
- Launch parameters included:
  - `-wi 5`
  - `-i 5`
  - `-to 1s`
  - pattern `benchmarks\..*`
  - JFR enabled
- The run did not complete in practical time in this environment and timed out in this execution session.
- Partial artifacts created during/after run:
  - `benchmark/baseline/profile.jfr` (present)
  - `benchmark/baseline/jmh-result.json` exists but unchanged in Git-tracked state in this run.

## Visibility in Git
- Tracked benchmark source/config files were not modified by the run itself.
- This report and raw logs are added so the execution evidence is visible to other agents/reviewers.

## Raw Evidence Files
- `gradle-tasks-output.txt`
- `benchmark-run-output.txt`
