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

   **Ingredient Index Game Tests** (`src/integrationtest/java/org/cyclops/integrateddynamics/gametest/GameTestsPerformanceIngredientIndex.java`)
   - Measures the ingredient positions index operations that storage networks perform on every insertion and extraction
   - These are not covered by the network presets above, as those don't contain any storage positions
   - Writes results to the same `benchmark_results.txt` file

3. **Network Generation Command** (`src/main/java/org/cyclops/integrateddynamics/command/CommandGenerateNetwork.java`)
   - Provides `/integrateddynamics generatenetwork` command for manual testing
   - Supports different network presets: `empty`, `idle`, `redstoneioclock`, `redstoneioclockvariables`, `displaypanels`, `clear`
   - Can be used in both single-player and multiplayer environments

## Network Presets

### Empty Network
- **Preset**: `empty`
- **Description**: A cube of logic cables with no parts attached
- **Benchmark**: Measures baseline performance with cable networks only
- **Sizes tested**: 25×25×25

### Large Empty Network
- **Preset**: `empty`
- **Description**: A large cube of logic cables with no parts attached, placed above the test template as it does not fit inside of it
- **Benchmark**: Measures the overhead per cable, which is too small to exceed the noise of the server tick time at smaller sizes
- **Sizes tested**: 32×32×32

### Control
- **Preset**: `control`
- **Description**: No network at all
- **Benchmark**: Measures the server tick time without any network, as baseline for the other benchmarks
- **Sizes tested**: 0

### Idle Network
- **Preset**: `idle`
- **Description**: A cube of logic cables with random parts on all outer surfaces
- **Benchmark**: Measures performance with active network elements (parts)
- **Sizes tested**: 25×25×25

### Redstone IO Clock Network
- **Preset**: `redstoneioclock`
- **Description**: A cube of logic cables with redstone readers on the east side connected to redstone writers on the west side, creating a clock signal
- **Benchmark**: Measures performance with active parts
- **Sizes tested**: 25×25×25

### Redstone IO Clock with Variables Network
- **Preset**: `redstoneioclockvariables`
- **Description**: A cube of logic cables with redstone readers on the east side connected to redstone writers on the west side, creating a clock signal with variables in between.
- **Benchmark**: Measures performance with active parts and operator variables
- **Sizes tested**: 25×25×25

### Display Panels Network
- **Preset**: `displaypanels`
- **Description**: A square wall of logic cables with display panels facing north, each showing a different integer value
- **Benchmark**: Used by the client benchmark to measure the rendering of part overlays
- **Sizes tested**: 12×12

## Performance Metrics

The benchmarking system measures two key metrics:

- **Average Network Tick Time (ms)**: The average time the Integrated Dynamics network subsystem takes to process one game tick. This measures the network-specific performance impact.
- **Average Server Tick Time (ms)**: The average time the entire Minecraft server takes per game tick. This measures the overall server performance impact, including both Integrated Dynamics and all other server operations.
- **Network Size**: The dimensions of the generated network cube

These metrics are tracked separately in the benchmark results to distinguish between network-specific performance and overall server performance impact.

Next to these, the ingredient index benchmarks measure a third metric:

- **Average Operation Time (ms)**: The average time a single ingredient positions index operation takes.
- **Index Size**: The number of distinct instances that is indexed

The following index operations are benchmarked:

| Benchmark | Description |
|-----------|-------------|
| `index_lookup_exact` | Look up the positions of an exact instance, as done when a specific instance is extracted |
| `index_lookup_item` | Look up the positions of an instance while ignoring data components |
| `index_lookup_nonempty_first` | Look up the first non-empty position, as done during quantity-based extractions |
| `index_lookup_nonempty_all` | Iterate over all non-empty positions |
| `index_modification` | Remove and re-add a position, as done when the contents of a storage position change |

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
5. Writes results to `run/logs/benchmark_results.txt`

Similarly, if you want to run the fuzzing tests, you can use:
```bash
FUZZING_ITERATIONS=10 ./gradlew runGameTestServer
```

## Result Format

Results are written in the following format:
```
preset=empty size=25 avgNetworkTickTime=6.25 avgServerTickTime=3.50
preset=idle size=25 avgNetworkTickTime=7.50 avgServerTickTime=4.20
preset=index_lookup_exact size=5000 avgOperationTime=0.000512
```

Results are then converted to JSON format for the benchmark action. Each preset generates two metrics - one for network tick time and one for server tick time:
```json
[
  {
    "name": "empty_size_25_network_tick_time",
    "unit": "ms",
    "value": 6.25
  },
  {
    "name": "empty_size_25_server_tick_time",
    "unit": "ms",
    "value": 3.50
  },
  {
    "name": "index_lookup_exact_size_5000_operation_time",
    "unit": "ms",
    "value": 0.000512
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
   /integrateddynamics generatenetwork empty 50
   ```

2. **Generate an idle network**:
   ```
   /integrateddynamics generatenetwork idle 50
   ```

3. **Measure network performance**:
   ```
   /integrateddynamics networkdiagnostics measure 10
   ```

4. **Clear generated networks**:
   ```
   /integrateddynamics generatenetwork clear 100
   ```

## Client Benchmark

Next to the server-side benchmarks above, the `Client Benchmark` job in `.github/workflows/performance.yml`
measures client-side rendering performance, to detect FPS regressions.

The script `.github/client_benchmark.sh` uses [clientdevbridge](https://github.com/CyclopsMC/clientdevbridge-cli)
to launch a headless dev client, generates fixed scenes with the `/integrateddynamics generatenetwork` command,
and measures each scene for 10 seconds with the vanilla client profiler (the same as F3+L).
The scenes are:

- `control`: an empty view, as baseline
- `cables`: a 16×16×16 cube of logic cables without parts
- `displaypanels`: a 12×12 wall of display panels showing values

For each scene, the following metrics are reported as the average time per frame:

- **CLIENT FRAME**: the total time spent in rendering a frame.
  As CI machines have no GPU, rendering happens in software (llvmpipe), so this mostly depends on the rasterization of chunk geometry.
- **CLIENT BLOCK ENTITIES**: the time spent in rendering block entities, such as the cable and part overlay renderers.
  This is the most relevant metric for rendering regressions within the mod.

Screenshots of each scene are uploaded as a workflow artifact, to check what was measured.

The client benchmark can also be executed locally, where `clientdevbridge` is installed:
```bash
CDB=clientdevbridge bash .github/client_benchmark.sh
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

To add new ingredient index benchmarks, add a new `@GameTest` method in `GameTestsPerformanceIngredientIndex`.

