---
# beans-on-droid-g1gq
title: Nix flake and dev shell
status: completed
type: epic
priority: normal
created_at: 2026-09-25T15:14:34Z
updated_at: 2026-09-25T17:40:34Z
parent: beans-on-droid-hsk9
openspec-link: openspec/changes/archive/2026-09-25-nix-flake-dev-shell
---

Plain nix flake, no flake-utils. Supported systems enumerated explicitly with nixpkgs.lib.genAttrs. Provides a devShell with JDK 17, the Android SDK (platform 36, build-tools, platform-tools, cmdline-tools) and Gradle, plus formatter and a flake check.

Acceptance:
- [ ] flake.nix uses genAttrs over an explicit systems list, not flake-utils
- [ ] `nix develop` gives a shell where `javac`, `sdkmanager` and `gradle` resolve
- [ ] ANDROID_HOME and ANDROID_SDK_ROOT are exported in the shell
- [ ] `nix flake check` passes
- [ ] flake.lock is committed
