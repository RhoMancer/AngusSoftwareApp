#!/bin/sh
# Parses UNIFIED_COVERAGE.md and enforces combined coverage thresholds.
# Defaults: 38% lines, 53% branches (floor to prevent regressions;
# @Composable code excluded from Kover since it can't be unit-tested).
set -e
REPORT="$1"
MIN_LINES="$2"
MIN_BRANCHES="$3"

if [ ! -f "$REPORT" ]; then
    echo "ERROR: Coverage report not found at $REPORT"
    exit 1
fi

LINE_PCT=$(grep "Line Coverage" "$REPORT" | grep -o '[0-9]*\.[0-9]*%' | tail -1 | tr -d '%')
BRANCH_PCT=$(grep "Branch Coverage" "$REPORT" | grep -o '[0-9]*\.[0-9]*%' | tail -1 | tr -d '%')

echo "Combined Line Coverage: ${LINE_PCT}% (minimum: ${MIN_LINES}%)"
echo "Combined Branch Coverage: ${BRANCH_PCT}% (minimum: ${MIN_BRANCHES}%)"

FAILED=0
if [ -z "$LINE_PCT" ] || awk "BEGIN{exit !($LINE_PCT < $MIN_LINES)}"; then
    echo "FAIL: Line coverage ${LINE_PCT}% is below minimum ${MIN_LINES}%"
    FAILED=1
fi
if [ -z "$BRANCH_PCT" ] || awk "BEGIN{exit !($BRANCH_PCT < $MIN_BRANCHES)}"; then
    echo "FAIL: Branch coverage ${BRANCH_PCT}% is below minimum ${MIN_BRANCHES}%"
    FAILED=1
fi

if [ $FAILED -eq 0 ]; then
    echo "Coverage verification PASSED"
fi

exit $FAILED
