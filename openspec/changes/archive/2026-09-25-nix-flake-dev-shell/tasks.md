## 1. Flake

- [x] 1.1 Write `flake.nix` with an explicit `systems` list and a `forAllSystems`
      built on `nixpkgs.lib.genAttrs`
- [x] 1.2 Import nixpkgs per system with `allowUnfree` and
      `android_sdk.accept_license`
- [x] 1.3 Compose the Android SDK with pinned versions, without emulator, system
      images, sources or NDK
- [x] 1.4 Provide `devShells.default` with JDK 17, Gradle, Kotlin, the SDK, git,
      jq and shellcheck
- [x] 1.5 Export `JAVA_HOME`, `ANDROID_HOME`, `ANDROID_SDK_ROOT` and the aapt2
      override
- [x] 1.6 Add `checks.shell-scripts` (shellcheck) and `checks.nix-format`
      (nixpkgs-fmt), and a `formatter` output
- [x] 1.7 Commit `flake.lock`

## 2. Verification

- [x] 2.1 `nix flake check` passes
- [x] 2.2 `nix develop` resolves `javac`, `sdkmanager` and `gradle`
- [x] 2.3 `ANDROID_HOME` and `ANDROID_SDK_ROOT` are set inside the shell and the
      directory contains `build-tools`, `platforms` and `platform-tools`
- [x] 2.4 `flake.nix` contains no reference to flake-utils
