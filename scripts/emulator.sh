#!/usr/bin/env bash
# emulator.sh <start|stop|status> [avd-name]
#
# Headless AOSP emulator for runtime verification and instrumented tests.
# Uses the flake's `emulator` dev shell, which carries the emulator binary and
# an API 26 x86_64 AOSP system image: the app's minSdk, and no Google apps.
set -euo pipefail

ACTION="${1:-start}"
AVD="${2:-beans-api26}"
API="android-26"
ABI="x86_64"
TAG="default"

ROOT="$(git rev-parse --show-toplevel)"
cd "$ROOT"

export ANDROID_AVD_HOME="$ROOT/.avd"
mkdir -p "$ANDROID_AVD_HOME"

case "$ACTION" in
  start)
    if adb devices | grep -q emulator-5554; then
      echo "emulator: already running"
      exit 0
    fi
    if [[ ! -d "$ANDROID_AVD_HOME/$AVD.avd" ]]; then
      echo "==> creating AVD $AVD"
      echo no | avdmanager create avd \
        --name "$AVD" \
        --package "system-images;$API;$TAG;$ABI" \
        --device pixel_2 \
        --force
    fi
    echo "==> booting $AVD headless"
    emulator -avd "$AVD" -no-window -no-audio -no-boot-anim -gpu swiftshader_indirect \
      -no-snapshot -wipe-data >"$ROOT/.avd/emulator.log" 2>&1 &
    echo "==> waiting for boot"
    adb wait-for-device
    until [[ "$(adb shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" == "1" ]]; do
      sleep 2
    done
    adb shell input keyevent 82 || true
    echo "==> emulator ready"
    ;;
  stop)
    adb emu kill 2>/dev/null || true
    echo "==> emulator stopped"
    ;;
  status)
    adb devices
    ;;
  *)
    echo "usage: emulator.sh <start|stop|status> [avd-name]" >&2
    exit 1
    ;;
esac
