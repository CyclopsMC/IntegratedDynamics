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

    # Parse the line: preset=<preset> size=<size> avgTickTime=<time>
    # Using sed instead of grep -oP for macOS compatibility
    preset=$(echo "$line" | sed -n 's/.*preset=\([^ ]*\).*/\1/p')
    size=$(echo "$line" | sed -n 's/.*size=\([0-9]*\).*/\1/p')
    tickTime=$(echo "$line" | sed -n 's/.*avgTickTime=\([0-9.]*\).*/\1/p')

    if [ -n "$preset" ] && [ -n "$size" ] && [ -n "$tickTime" ]; then
        if [ "$FIRST" = true ]; then
            FIRST=false
        else
            echo "," >> "$BENCH_FILE"
        fi

        cat >> "$BENCH_FILE" << EOF
  {
    "name": "${preset}_size_${size}",
    "unit": "ms",
    "value": $tickTime
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

