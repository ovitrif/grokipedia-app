#!/bin/bash
# Quick verification - screenshots and logs only (no video)
# Faster iteration for Claude Code during development

set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
ARTIFACTS_DIR="$PROJECT_ROOT/.verification"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
RUN_DIR="$ARTIFACTS_DIR/quick_$TIMESTAMP"

TEST_FILE="${1:-.maestro/04_core_functionalities.yaml}"

echo "Quick verification: $TEST_FILE"

mkdir -p "$RUN_DIR/screenshots"
mkdir -p "$RUN_DIR/logs"

# Check device
if ! adb devices | grep -q "device$"; then
    echo "ERROR: No device connected"
    exit 1
fi

# Clear and start log capture
adb logcat -c
LOG_FILE="$RUN_DIR/logs/logcat.txt"
adb logcat -v time > "$LOG_FILE" 2>&1 &
LOG_PID=$!

cleanup() {
    kill "$LOG_PID" 2>/dev/null || true
}
trap cleanup EXIT

# Track start time for screenshot filtering
START_TIME=$(date +%s)

# Run test (screenshots saved to working directory)
cd "$PROJECT_ROOT"
set +e
maestro test "$TEST_FILE" 2>&1 | tee "$RUN_DIR/output.txt"
EXIT_CODE=${PIPESTATUS[0]}
set -e

# Stop logging
kill "$LOG_PID" 2>/dev/null || true
LOG_PID=""

# Move screenshots created during this test run (within last 2 minutes)
find "$PROJECT_ROOT" -maxdepth 1 -name "*.png" -mmin -2 -exec mv {} "$RUN_DIR/screenshots/" \; 2>/dev/null || true

# Filter logs - app package logs + errors
APP_LOG="$RUN_DIR/logs/app.txt"
ERROR_LOG="$RUN_DIR/logs/errors.txt"

# Get all logs from app process (package name gets truncated to "thub.grokipedia" in logcat)
grep -E "(io\.github\.grokipedia|thub\.grokipedia)" "$LOG_FILE" > "$APP_LOG" 2>/dev/null || true

# Extract errors, exceptions, crashes from app logs
grep -i -E "(exception|error|fatal|crash|assert|fail)" "$APP_LOG" > "$ERROR_LOG" 2>/dev/null || true

# Also keep a filtered log with tagged messages for quick reference
grep -E "\[(FOCUS|KEYBOARD|TEST)" "$LOG_FILE" > "$RUN_DIR/logs/tagged.txt" 2>/dev/null || true

# Create symlink
ln -sfn "$RUN_DIR" "$ARTIFACTS_DIR/latest"

echo ""
echo "Result: $([ $EXIT_CODE -eq 0 ] && echo 'PASSED' || echo 'FAILED')"
echo "Artifacts: $RUN_DIR"

exit $EXIT_CODE
