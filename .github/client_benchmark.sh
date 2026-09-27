#!/bin/bash
# Client-side rendering benchmark.
# Drives a headless dev client with clientdevbridge (https://github.com/CyclopsMC/clientdevbridge-cli),
# renders fixed scenes, and measures the time per frame of render sections using the vanilla client profiler.
# Results are written in the format of benchmark-action/github-action-benchmark (customSmallerIsBetter).
#
# Software rendering (llvmpipe) makes the total frame time mostly depend on rasterization,
# so the block entity section is the most relevant metric for mod-side rendering regressions.

set -euo pipefail

CDB="${CDB:-npx -y cyclops-clientdevbridge-cli@0.9.0}"
RESULTS_FILE="${RESULTS_FILE:-client_benchmark_results.json}"
SIZE="${SIZE:-16}"
# Part overlays only render within 15 blocks by default, so this scene is smaller and closer to the camera.
DISPLAY_SIZE="${DISPLAY_SIZE:-12}"
WARMUP_TICKS="${WARMUP_TICKS:-100}"
PROFILING_DIR="runs/client/debug/profiling"
WORK_DIR="$(mktemp -d)"

# Scenes are generated 2 blocks above the player, extending into positive x, y and z.
SCENE_ORIGIN="0 10 0"

# Camera position in front of a scene of the given size, at the given distance, looking south.
# Lowered by 2 blocks, as the eyes are above the teleported position.
camera() {
    local size=$1 distance=$2
    echo "$(( size / 2 )) $(( 10 + size / 2 )) -${distance}"
}

cleanup() {
    $CDB stop || true
    rm -rf "$WORK_DIR"
}
trap cleanup EXIT

$CDB start
$CDB world-reset --name client-benchmark
$CDB command "gamemode spectator"
$CDB command "gamerule doTileDrops false"

echo "[" > "$RESULTS_FILE"
FIRST=true

emit() {
    local name=$1 value=$2
    if [ "$FIRST" = true ]; then
        FIRST=false
    else
        echo "," >> "$RESULTS_FILE"
    fi
    printf '  {\n    "name": "%s",\n    "unit": "frame time (ms)",\n    "value": %s\n  }' "$name" "$value" >> "$RESULTS_FILE"
}

# Profile the current view, and output the average time per frame of the given profiler paths.
profile() {
    local scene=$1 size=$2 camera=$3
    $CDB command "tp @s $camera 0 0"
    $CDB wait --expr "mc.levelRenderer.hasRenderedAllSections()" --timeout 600000
    $CDB wait --ticks "$WARMUP_TICKS"
    $CDB screenshot --name "client-benchmark-$scene"

    local before
    before=$(ls "$PROFILING_DIR"/*.zip 2>/dev/null | wc -l || true)
    # Records for 10 seconds, after which the results are written to a zip file.
    $CDB eval "mc.debugClientMetricsStart({ c -> } as java.util.function.Consumer)"
    until [ "$(ls "$PROFILING_DIR"/*.zip 2>/dev/null | wc -l || true)" -gt "$before" ]; do sleep 2; done
    local zip
    zip=$(ls -t "$PROFILING_DIR"/*.zip | head -1)
    until unzip -p "$zip" client/profiling.txt > "$WORK_DIR/$scene.txt" 2>/dev/null; do sleep 2; done

    python3 - "$WORK_DIR/$scene.txt" > "$WORK_DIR/$scene.tsv" << 'EOF'
import re, sys
text = open(sys.argv[1]).read()
span = float(re.search(r'^Time span: (\d+) ms', text, re.M).group(1))
line_re = re.compile(r'^\[(\d+)\] [| ]*([^()]+)\((\d+)/\d+\) - [\d.]+%/([\d.]+)%$')
stack, sections = [], {}
for line in text.splitlines():
    m = line_re.match(line)
    if m:
        depth, name, calls, total = int(m.group(1)), m.group(2), int(m.group(3)), float(m.group(4))
        del stack[depth:]
        stack.append(name)
        sections.setdefault('.'.join(stack), (calls, total))
frames = sections['gameRenderer'][0]
for path, label in [('gameRenderer', 'FRAME'), ('gameRenderer.level.blockentities', 'BLOCK ENTITIES')]:
    total = sections.get(path, (0, 0.0))[1]
    print('%s\t%.3f' % (label, span * total / 100 / frames))
EOF
    while IFS=$'\t' read -r label value; do
        echo "$scene $label: $value ms"
        emit "CLIENT $label: ${scene}_size_${size}" "$value"
    done < "$WORK_DIR/$scene.tsv"
}

clear_scene() {
    local size=$1
    $CDB command "tp @s $(( size / 2 )) $(( 12 + size / 2 )) $(( size / 2 ))"
    $CDB command "integrateddynamics generatenetwork clear $(( size + 2 ))"
    # Fails when there are no items
    $CDB command "kill @e[type=item]" || true
}

# Baseline without any mod blocks
profile "control" "$SIZE" "$(camera "$SIZE" $(( SIZE + 2 )))"

# Cables without parts
$CDB command "tp @s $SCENE_ORIGIN"
$CDB command "integrateddynamics generatenetwork empty $SIZE"
profile "cables" "$SIZE" "$(camera "$SIZE" $(( SIZE + 2 )))"
clear_scene "$SIZE"

# Display panels showing values, facing the camera
$CDB command "tp @s $SCENE_ORIGIN"
$CDB command "integrateddynamics generatenetwork displaypanels $DISPLAY_SIZE"
profile "displaypanels" "$DISPLAY_SIZE" "$(camera "$DISPLAY_SIZE" 9)"
clear_scene "$DISPLAY_SIZE"

echo "" >> "$RESULTS_FILE"
echo "]" >> "$RESULTS_FILE"
cat "$RESULTS_FILE"
