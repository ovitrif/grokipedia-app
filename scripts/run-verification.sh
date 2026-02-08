#!/bin/bash
# Self-verification script for Claude Code
# Runs Maestro tests with comprehensive artifact collection:
# - Screenshots at each step
# - Video recording with frame extraction
# - Real-time log streaming + structured log file

set -e

# Configuration
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
ARTIFACTS_DIR="$PROJECT_ROOT/.verification"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
RUN_DIR="$ARTIFACTS_DIR/run_$TIMESTAMP"

# Default test file
TEST_FILE="${1:-.maestro/04_core_functionalities.yaml}"
FRAME_RATE="${2:-1}"  # Frames per second to extract

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo "=========================================="
echo "CLAUDE CODE SELF-VERIFICATION"
echo "=========================================="
echo "Test: $TEST_FILE"
echo "Artifacts: $RUN_DIR"
echo "Frame rate: ${FRAME_RATE} fps"
echo "=========================================="

# Create artifact directories
mkdir -p "$RUN_DIR/screenshots"
mkdir -p "$RUN_DIR/frames"
mkdir -p "$RUN_DIR/logs"

# Check for connected device
DEVICE_COUNT=$(adb devices | grep -v "List" | grep -c "device$" || true)
if [ "$DEVICE_COUNT" -eq 0 ]; then
    echo -e "${RED}ERROR: No Android device connected${NC}"
    echo "Start an emulator with: emulator -avd <avd_name>"
    exit 1
fi

echo -e "${GREEN}Device connected${NC}"

# Function to cleanup background processes
cleanup() {
    echo ""
    echo "Cleaning up..."
    # Kill log streaming if running
    if [ -n "$LOG_PID" ] && kill -0 "$LOG_PID" 2>/dev/null; then
        kill "$LOG_PID" 2>/dev/null || true
    fi
    # Kill video recording if running
    if [ -n "$VIDEO_PID" ]; then
        adb shell "kill -INT \$(pgrep screenrecord)" 2>/dev/null || true
    fi
}
trap cleanup EXIT

# Clear old logs
echo "Clearing logcat..."
adb logcat -c

# Start log streaming in background
LOG_FILE="$RUN_DIR/logs/logcat_full.txt"
FILTERED_LOG="$RUN_DIR/logs/logcat_filtered.txt"
echo "Starting log capture -> $LOG_FILE"
adb logcat -v time > "$LOG_FILE" 2>&1 &
LOG_PID=$!

# Start video recording
VIDEO_FILE="/sdcard/verification_recording.mp4"
echo "Starting video recording..."
adb shell "screenrecord --time-limit 180 $VIDEO_FILE" &
VIDEO_PID=$!
sleep 1  # Give screenrecord time to start

# Run Maestro test
echo ""
echo "=========================================="
echo "RUNNING MAESTRO TEST"
echo "=========================================="
MAESTRO_OUTPUT="$RUN_DIR/maestro_output.txt"

cd "$PROJECT_ROOT"
set +e  # Don't exit on test failure
maestro test "$TEST_FILE" --format junit 2>&1 | tee "$MAESTRO_OUTPUT"
TEST_EXIT_CODE=${PIPESTATUS[0]}
set -e

# Move screenshots created during this test run (within last 5 minutes)
find "$PROJECT_ROOT" -maxdepth 1 -name "*.png" -mmin -5 -exec mv {} "$RUN_DIR/screenshots/" \; 2>/dev/null || true

# Stop video recording
echo ""
echo "Stopping video recording..."
adb shell "kill -INT \$(pgrep screenrecord)" 2>/dev/null || true
sleep 2  # Wait for file to be written

# Pull video from device
echo "Pulling video from device..."
adb pull "$VIDEO_FILE" "$RUN_DIR/recording.mp4" 2>/dev/null || echo "Warning: Could not pull video"
adb shell rm "$VIDEO_FILE" 2>/dev/null || true

# Extract frames from video
if [ -f "$RUN_DIR/recording.mp4" ]; then
    echo "Extracting frames at ${FRAME_RATE} fps..."
    ffmpeg -i "$RUN_DIR/recording.mp4" -vf "fps=$FRAME_RATE" -q:v 2 "$RUN_DIR/frames/frame_%04d.jpg" -y 2>/dev/null
    FRAME_COUNT=$(ls -1 "$RUN_DIR/frames/" 2>/dev/null | wc -l | tr -d ' ')
    echo "Extracted $FRAME_COUNT frames"
fi

# Stop log streaming
kill "$LOG_PID" 2>/dev/null || true
LOG_PID=""

# Create filtered log files
echo "Creating filtered log files..."

# App-specific logs (package name gets truncated to "thub.grokipedia")
APP_LOG="$RUN_DIR/logs/app.txt"
grep -E "(io\.github\.grokipedia|thub\.grokipedia)" "$LOG_FILE" > "$APP_LOG" 2>/dev/null || true

# Tagged logs for quick reference
grep -E "\[(FOCUS|KEYBOARD|TEST)" "$LOG_FILE" > "$RUN_DIR/logs/tagged.txt" 2>/dev/null || true

# Errors and crashes from app
ERROR_LOG="$RUN_DIR/logs/errors.txt"
grep -i -E "(exception|error|fatal|crash|assert|fail)" "$APP_LOG" | grep -v "No error" > "$ERROR_LOG" 2>/dev/null || true
ERROR_COUNT=$(wc -l < "$ERROR_LOG" | tr -d ' ')

# Keep chromium/WebView errors separately
grep -E "(chromium|WebView)" "$LOG_FILE" | grep -i -E "(error|exception|fatal)" > "$RUN_DIR/logs/webview_errors.txt" 2>/dev/null || true

# Generate summary report
SUMMARY_FILE="$RUN_DIR/SUMMARY.md"
echo "# Verification Run Summary" > "$SUMMARY_FILE"
echo "" >> "$SUMMARY_FILE"
echo "**Timestamp**: $TIMESTAMP" >> "$SUMMARY_FILE"
echo "**Test File**: $TEST_FILE" >> "$SUMMARY_FILE"
echo "" >> "$SUMMARY_FILE"

if [ $TEST_EXIT_CODE -eq 0 ]; then
    echo "**Result**: PASSED" >> "$SUMMARY_FILE"
else
    echo "**Result**: FAILED (exit code: $TEST_EXIT_CODE)" >> "$SUMMARY_FILE"
fi

echo "" >> "$SUMMARY_FILE"
echo "## Artifacts" >> "$SUMMARY_FILE"
echo "" >> "$SUMMARY_FILE"
echo "- Screenshots: \`$RUN_DIR/screenshots/\`" >> "$SUMMARY_FILE"
echo "- Video frames: \`$RUN_DIR/frames/\` ($FRAME_COUNT frames)" >> "$SUMMARY_FILE"
echo "- Full logs: \`$RUN_DIR/logs/logcat_full.txt\`" >> "$SUMMARY_FILE"
echo "- Filtered logs: \`$RUN_DIR/logs/logcat_filtered.txt\`" >> "$SUMMARY_FILE"
echo "- Error log: \`$RUN_DIR/logs/errors.txt\` ($ERROR_COUNT potential errors)" >> "$SUMMARY_FILE"
echo "" >> "$SUMMARY_FILE"

# Add error summary if any
if [ "$ERROR_COUNT" -gt 0 ]; then
    echo "## Errors Found" >> "$SUMMARY_FILE"
    echo "\`\`\`" >> "$SUMMARY_FILE"
    head -20 "$ERROR_LOG" >> "$SUMMARY_FILE"
    echo "\`\`\`" >> "$SUMMARY_FILE"
fi

# Print summary
echo ""
echo "=========================================="
echo "VERIFICATION COMPLETE"
echo "=========================================="
if [ $TEST_EXIT_CODE -eq 0 ]; then
    echo -e "${GREEN}TEST PASSED${NC}"
else
    echo -e "${RED}TEST FAILED${NC}"
fi
echo ""
echo "Artifacts saved to: $RUN_DIR"
echo "  - Screenshots: $(ls -1 "$RUN_DIR/screenshots/" 2>/dev/null | wc -l | tr -d ' ') files"
echo "  - Frames: $FRAME_COUNT files"
echo "  - Log errors: $ERROR_COUNT potential issues"
echo ""
echo "To view:"
echo "  Summary:     cat $SUMMARY_FILE"
echo "  Screenshots: ls $RUN_DIR/screenshots/"
echo "  Frames:      ls $RUN_DIR/frames/"
echo "  Errors:      cat $RUN_DIR/logs/errors.txt"
echo "=========================================="

# Create latest symlink for easy access
ln -sfn "$RUN_DIR" "$ARTIFACTS_DIR/latest"

exit $TEST_EXIT_CODE
