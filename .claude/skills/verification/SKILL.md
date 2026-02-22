---
name: verification
description: Self-verification workflow for testing feature implementations. Use after making code changes to verify they work correctly via E2E tests with screenshot and log capture.
allowed-tools: Bash, Read, Glob, Grep
---

# Verification Skill

Run E2E tests and analyze results to verify feature implementations.

## Quick Verify (Recommended)

Fast iteration with screenshots and logs:

```bash
./scripts/quick-verify.sh .maestro/04_core_functionalities.yaml
```

## Full Verify

Complete verification with video frame extraction:

```bash
./scripts/run-verification.sh .maestro/04_core_functionalities.yaml
```

## Analyze Results

```bash
./scripts/analyze-logs.sh
```

## Artifacts Location

After running verification, artifacts are at `.verification/latest/`:

| File | Contents |
|------|----------|
| `screenshots/*.png` | Screenshots at each test step |
| `frames/*.jpg` | Video frames (full verify only) |
| `logs/app.txt` | All logs from app process |
| `logs/tagged.txt` | `[FOCUS]`, `[KEYBOARD]`, `[TEST]` logs |
| `logs/errors.txt` | Errors and exceptions |
| `SUMMARY.md` | Run summary (full verify only) |

## Reading Screenshots

Use the Read tool to view screenshots:

```
Read .verification/latest/screenshots/smoke_test_launch.png
```

## Available Tests

| Test | Purpose | Duration |
|------|---------|----------|
| `01_smoke.yaml` | App launches, WebView loads | ~5s |
| `02_navigation.yaml` | Back button, navigation | ~10s |
| `04_core_functionalities.yaml` | Core features, favorites | ~20s |

## Creating New Tests

Copy the template:
```bash
cp .maestro/00_verification_template.yaml .maestro/my_feature_test.yaml
```

Edit to add your test steps with screenshots:
```yaml
appId: io.github.grokipedia
---
- launchApp
- takeScreenshot: 01_initial
- tapOn:
    text: "Element"
- takeScreenshot: 02_after_tap
- assertVisible:
    text: "Expected Result"
```

## Debugging Failures

1. Read screenshots to see UI state
2. Check `logs/errors.txt` for exceptions
3. Check `logs/tagged.txt` for feature logs
4. Check `logs/app.txt` for full sequence
