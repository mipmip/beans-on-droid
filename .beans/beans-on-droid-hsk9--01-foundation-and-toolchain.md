---
# beans-on-droid-hsk9
title: 01 Foundation and toolchain
status: completed
type: milestone
priority: normal
created_at: 2026-09-25T15:14:34Z
updated_at: 2026-09-25T17:57:59Z
---

Everything needed before feature work can start: a reproducible nix dev shell, an Android Gradle skeleton that assembles, and a working build/test gate. Done when `nix develop -c ./gradlew assembleDebug` produces an APK that launches on an API 26 emulator.
