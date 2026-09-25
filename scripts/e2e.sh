#!/usr/bin/env bash
# e2e.sh
#
# Boots a headless API 26 emulator, runs the instrumented tests against it, and
# shuts it down. This is the CI entry point; locally you can keep an emulator
# running and call `./gradlew connectedDebugAndroidTest` yourself.
set -euo pipefail

ROOT="$(git rev-parse --show-toplevel)"
cd "$ROOT"

KEEP="${KEEP_EMULATOR:-0}"

cleanup() {
  if [[ "$KEEP" != "1" ]]; then
    nix develop .#emulator --command ./scripts/emulator.sh stop || true
  fi
}
trap cleanup EXIT

echo "==> [1/2] boot emulator"
nix develop .#emulator --command ./scripts/emulator.sh start

echo "==> [2/2] instrumented tests"
nix develop --command ./gradlew connectedDebugAndroidTest

echo "==> e2e passed"
