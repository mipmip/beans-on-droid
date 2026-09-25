## Why

Bean `beans-on-droid-b8ob`. The dev shell can build an Android app but there is
no app to build. This change adds the single-module Gradle project that every
later change adds code to, with the identity settled: application id, minSdk and
version numbers are permanent once published, so they are fixed now rather than
discovered later.

## What Changes

- Add a single-module Gradle project (`:app`) with the Gradle wrapper.
- Add `gradle/libs.versions.toml` as the only place versions are declared.
- Set `applicationId io.github.mipmip.beansondroid`, `minSdk 26`,
  `targetSdk`/`compileSdk 36`, `versionCode 1`, `versionName 0.1.0`.
- Kotlin, Jetpack Compose, Material 3, a single activity and Navigation Compose.
- Enable core library desugaring, which JGit needs on API 26.
- Add a launchable placeholder screen so the skeleton is verifiable.

## Capabilities

### New Capabilities

None. This is project scaffolding with no user-facing behavior beyond a
placeholder screen, so `skip_specs: true` is set.

### Modified Capabilities

None.

## Impact

- New: `settings.gradle.kts`, `build.gradle.kts`, `app/`, `gradle/libs.versions.toml`,
  `gradle/wrapper/`, `gradlew`.
- New dependencies, all FOSS: AndroidX Core/Lifecycle/Activity, Compose BOM,
  Material 3, Navigation Compose, `desugar_jdk_libs`.
- From here on, `scripts/gate.sh` runs the Gradle half of the gate.
