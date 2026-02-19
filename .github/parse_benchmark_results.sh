#!/bin/bash
# Parse benchmark results from game tests and convert to JSON format
# This script reads the benchmark results file and converts it to JSON format
# for use with benchmark-action/github-action-benchmark

set -e

BENCH_FILE="benchmark_results.json"
RESULTS_FILE="runs/gameTestServer/logs/benchmark_results.txt"

echo "Parsing benchmark results..."

# Initialize the JSON array
echo "[" > "$BENCH_FILE"

# Default values if file doesn't exist
if [ ! -f "$RESULTS_FILE" ]; then
    echo "Benchmark results file not found at $RESULTS_FILE"
    exit 1;
fi

FIRST=true
while IFS= read -r line; do
    # Skip empty lines
    if [ -z "$line" ]; then
        continue
    fi

    # Parse the line: preset=<preset> size=<size> avgNetworkTickTime=<time> avgServerTickTime=<time>
    # Using sed instead of grep -oP for macOS compatibility
    preset=$(echo "$line" | sed -n 's/.*preset=\([^ ]*\).*/\1/p')
    size=$(echo "$line" | sed -n 's/.*size=\([0-9]*\).*/\1/p')
    networkTickTime=$(echo "$line" | sed -n 's/.*avgNetworkTickTime=\([0-9.]*\).*/\1/p')
    serverTickTime=$(echo "$line" | sed -n 's/.*avgServerTickTime=\([0-9.]*\).*/\1/p')

    if [ -n "$preset" ] && [ -n "$size" ] && [ -n "$networkTickTime" ]; then
        # Output network tick time metric
        if [ "$FIRST" = true ]; then
            FIRST=false
        else
            echo "," >> "$BENCH_FILE"
        fi

        cat >> "$BENCH_FILE" << EOF
  {
    "name": "NETWORK LOAD: ${preset}_size_${size}",
    "unit": "tick time (ms)",
    "value": $networkTickTime
  }
EOF
    fi

    # Output server tick time metric if available
    if [ -n "$serverTickTime" ]; then
        echo "," >> "$BENCH_FILE"
        cat >> "$BENCH_FILE" << EOF
  {
    "name": "SERVER LOAD: ${preset}_size_${size}",
    "unit": "tick time (ms)",
    "value": $serverTickTime
  }
EOF
    fi
done < "$RESULTS_FILE"

# Close the JSON array
echo "" >> "$BENCH_FILE"
echo "]" >> "$BENCH_FILE"

# Display the results
echo "✓ Benchmark results parsed successfully:"
echo ""
cat "$BENCH_FILE"

