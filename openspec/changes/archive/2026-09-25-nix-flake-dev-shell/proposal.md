## Why

Bean `beans-on-droid-g1gq`. Nothing in this project can be built reproducibly
until the toolchain is pinned. An Android build needs a specific JDK, a specific
Android SDK and a Gradle that agrees with both. Pinning them in a flake means the
build behaves the same on this machine, on a fresh checkout and in CI.

## What Changes

- Add `flake.nix` and a committed `flake.lock` providing a dev shell with JDK 17,
  the Android SDK (platform 36, build-tools 36.0.0, platform-tools 37.0.1,
  cmdline-tools 22.0) and Gradle.
- Enumerate supported systems explicitly and map them with
  `nixpkgs.lib.genAttrs`. No flake-utils.
- Export `JAVA_HOME`, `ANDROID_HOME` and `ANDROID_SDK_ROOT` in the shell, and
  point Gradle at the SDK's `aapt2` so it does not try to fetch its own.
- Add flake `checks` for shell script linting and nix formatting, plus a
  `formatter` output.

## Capabilities

### New Capabilities

None. This is build tooling, so `skip_specs: true` is set: no user-observable
behavior changes.

### Modified Capabilities

None.

## Impact

- New: `flake.nix`, `flake.lock`.
- Every later change is built and tested through this shell.
- Contributors without nix are unaffected; the README documents the manual
  toolchain equivalent.
