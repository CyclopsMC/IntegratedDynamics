# Performance Benchmarking Setup

This document describes the performance benchmarking infrastructure for Integrated Dynamics, which measures the continuous performance of network operations.
Performance results are tracked in https://github.com/CyclopsMC/cyclops-performance-results

## Overview

The performance benchmarking system consists of three main components:

1. **GitHub Workflow** (`.github/workflows/performance.yml`)
   - Executes on the same triggers as CI (push and pull_request)
   - Runs game tests to measure network performance
   - Uses `benchmark-action/github-action-benchmark` to track performance evolution

2. **Game Tests** (`src/integrationtest/java/org/cyclops/integrateddynamics/gametest/GameTestsPerformance.java`)
   - Generates networks with different presets for benchmarking
   - Measures performance metrics for each preset
   - Writes results to `build/logs/benchmark_results.txt`

3. **Network Generation Command** (`src/main/java/org/cyclops/integrateddynamics/command/CommandGenerateNetwork.java`)
   - Provides `/integrateddynamics generatenetwork` command for manual testing
   - Supports different network presets: `emptynetwork`, `idlenetwork`, `clear`
   - Can be used in both single-player and multiplayer environments

## Network Presets

### Empty Network
- **Preset**: `emptynetwork`
- **Description**: A cube of logic cables with no parts attached
- **Benchmark**: Measures baseline performance with cable networks only
- **Sizes tested**: 25×25×25

### Idle Network
- **Preset**: `idlenetwork`
- **Description**: A cube of logic cables with random parts on all outer surfaces
- **Benchmark**: Measures performance with active network elements (parts)
- **Sizes tested**: 25×25×25

## Performance Metrics

The benchmarking system measures:
- **Average Tick Time (ms)**: The average time it takes the network to process one game tick
- **Network Size**: The dimensions of the generated network cube

## Game Test Execution

The game tests are automatically executed as part of the GitHub workflow:

```bash
PERFORMANCE_BENCHMARK_ENABLED=true ./gradlew runGameTestServer
```

This command:
1. Starts a game test server
2. Runs `GameTestsPerformance` game tests
3. Generates networks of different sizes with different presets
4. Measures performance metrics
5. Writes results to `runs/gameTestServer/logs/benchmark_results.txt`

## Result Format

Results are written in the following format:
```
preset=emptynetwork size=25 avgTickTime=6.25
preset=idlenetwork size=25 avgTickTime=7.50
```

Results are then converted to JSON format for the benchmark action:
```json
[
  {
    "name": "emptynetwork_size_25",
    "unit": "ms",
    "value": 6.25,
    "preset": "emptynetwork",
    "size": 25
  }
]
```

## Benchmark Tracking

The `benchmark-action/github-action-benchmark` GitHub action automatically:
- Stores benchmark results in GitHub Pages branch (`gh-pages`)
- Generates historical performance charts
- Alerts when performance degrades beyond 150% of baseline
- Creates comments on PRs when alerts are triggered

## Manual Testing

To manually test network performance in a Minecraft world:

1. **Generate an empty network**:
   ```
   /integrateddynamics generatenetwork emptynetwork 50
   ```

2. **Generate an idle network**:
   ```
   /integrateddynamics generatenetwork idlenetwork 50
   ```

3. **Measure network performance**:
   ```
   /integrateddynamics networkdiagnostics measure 10
   ```

4. **Clear generated networks**:
   ```
   /integrateddynamics generatenetwork clear 100
   ```

## Integration with CI/CD

The performance workflow runs on:
- Every push to any branch
- Every pull request

Performance degradation is tracked across commits and branches, helping to identify performance regressions early in the development cycle.

## Adding New Benchmarks

To add new network presets or benchmarks:

1. Add a new preset enum value in `CommandGenerateNetwork.NetworkPreset`
2. Add corresponding generation method in `CommandGenerateNetworkExecutor`
3. Add a new `@GameTest` method in `GameTestsPerformance`
4. The workflow will automatically execute and track the new benchmark

