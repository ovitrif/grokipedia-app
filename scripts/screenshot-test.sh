#!/usr/bin/env bash
set -euo pipefail

USAGE="Usage: $0 {record|verify|compare}"

if [ $# -lt 1 ]; then
    echo "$USAGE"
    exit 1
fi

ACTION="$1"
shift

case "$ACTION" in
    record)
        echo "Recording reference screenshots..."
        ./gradlew :composeApp:recordRoborazziDebug "$@"
        echo "Reference screenshots saved to composeApp/src/test/screenshots/"
        ;;
    verify)
        echo "Verifying screenshots against references..."
        ./gradlew :composeApp:verifyRoborazziDebug "$@"
        echo "All screenshots match references."
        ;;
    compare)
        echo "Generating comparison diffs..."
        ./gradlew :composeApp:compareRoborazziDebug "$@"
        echo "Diff images saved to composeApp/build/outputs/roborazzi/"
        ;;
    *)
        echo "$USAGE"
        exit 1
        ;;
esac
