#!/bin/bash
# Analyze logs from verification run
# Usage: ./analyze-logs.sh [run_directory]

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
ARTIFACTS_DIR="$PROJECT_ROOT/.verification"

RUN_DIR="${1:-$ARTIFACTS_DIR/latest}"

if [ ! -d "$RUN_DIR" ]; then
    echo "ERROR: Run directory not found: $RUN_DIR"
    exit 1
fi

echo "Analyzing: $RUN_DIR"
echo ""

# Check for errors from app
if [ -f "$RUN_DIR/logs/errors.txt" ]; then
    ERROR_COUNT=$(wc -l < "$RUN_DIR/logs/errors.txt" | tr -d ' ')
    if [ "$ERROR_COUNT" -gt 0 ]; then
        echo "=== APP ERRORS ($ERROR_COUNT) ==="
        cat "$RUN_DIR/logs/errors.txt"
        echo ""
    else
        echo "=== NO APP ERRORS ==="
        echo ""
    fi
fi

# Check for WebView errors
if [ -f "$RUN_DIR/logs/webview_errors.txt" ]; then
    WV_COUNT=$(wc -l < "$RUN_DIR/logs/webview_errors.txt" | tr -d ' ')
    if [ "$WV_COUNT" -gt 0 ]; then
        echo "=== WEBVIEW ERRORS ($WV_COUNT) ==="
        cat "$RUN_DIR/logs/webview_errors.txt"
        echo ""
    fi
fi

# Show tagged logs (FOCUS, KEYBOARD, TEST)
if [ -f "$RUN_DIR/logs/tagged.txt" ]; then
    TAGGED_COUNT=$(wc -l < "$RUN_DIR/logs/tagged.txt" | tr -d ' ')
    echo "=== TAGGED LOGS ($TAGGED_COUNT lines) ==="
    cat "$RUN_DIR/logs/tagged.txt"
    echo ""
fi

# Show app logs summary
if [ -f "$RUN_DIR/logs/app.txt" ]; then
    APP_LINES=$(wc -l < "$RUN_DIR/logs/app.txt" | tr -d ' ')
    echo "=== APP LOGS (last 30 of $APP_LINES lines) ==="
    tail -30 "$RUN_DIR/logs/app.txt"
    echo ""
fi

echo "=== ARTIFACTS ==="
echo "Screenshots: $(ls -1 "$RUN_DIR/screenshots/" 2>/dev/null | wc -l | tr -d ' ') files"
[ -d "$RUN_DIR/frames" ] && echo "Frames: $(ls -1 "$RUN_DIR/frames/" 2>/dev/null | wc -l | tr -d ' ') files"
echo ""
echo "Log files:"
echo "  App logs:      $RUN_DIR/logs/app.txt"
echo "  Tagged logs:   $RUN_DIR/logs/tagged.txt"
echo "  Errors:        $RUN_DIR/logs/errors.txt"
echo "  Full logcat:   $RUN_DIR/logs/logcat.txt"
