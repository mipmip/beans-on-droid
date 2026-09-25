#!/usr/bin/env bash
# screenshots.sh
#
# Drives the app on a running emulator into the states worth showing, then pulls
# the PNGs into the fastlane metadata directory. Needs an emulator already up:
#   nix develop .#emulator --command ./scripts/emulator.sh start
set -euo pipefail

ROOT="$(git rev-parse --show-toplevel)"
cd "$ROOT"

TARGET="fastlane/metadata/android/en-US/images/phoneScreenshots"
DEVICE_DIR="/sdcard/beans-screenshots"

echo "==> [1/2] capture"
nix develop --command ./gradlew connectedDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=io.github.mipmip.beansondroid.ScreenshotTest

echo "==> [2/2] pull into $TARGET"
mkdir -p "$TARGET"
adb pull "$DEVICE_DIR/." "$TARGET/" >/dev/null
ls -1 "$TARGET"
echo "==> screenshots updated"
