## Context

The briefing fixes the app identity and the stack. The open decisions are which
versions to pin and how to keep JGit working on API 26.

## Decisions

### Versions

Resolved against the live repositories rather than from memory:

| Component   | Version      |
|-------------|--------------|
| AGP         | 9.4.1        |
| Kotlin      | 2.4.20       |
| Gradle      | 9.8.0        |
| Compose BOM | 2026.09.00   |
| compileSdk  | 36           |

All of them are declared in `gradle/libs.versions.toml`. No version literal may
appear in a build file.

### Single module

The briefing's layering (RepoStore, BeanParser, BeanIndex, UI) is enforced by
package boundaries, not by Gradle modules. A PoC does not need the build-time
cost of a multi-module split, and the coverage gate can target packages just as
well as modules. Phase 2 can split later without moving behavior.

### Core library desugaring

JGit uses `java.nio.file` and other Java 8+ APIs. minSdk 26 provides
`java.nio.file` natively, but desugaring is enabled anyway so the rest of JGit's
Java 8+ surface resolves. This is the briefing's stated requirement and the
cheapest insurance against a runtime `NoClassDefFoundError`.

### JGit version chosen later

The dependency is added in the RepoStore change, not here, because the version
can only be settled by running it on an API 26 device. Recording the choice next
to the runtime evidence keeps the two together.

### Placeholder screen

The skeleton ships a single composable naming the app. Without it "the skeleton
builds" is unverifiable at runtime, and the first real screen would be the first
thing ever run on a device.

## What AGP 9 actually required

Three things only surfaced by building:

1. **The `org.jetbrains.kotlin.android` plugin is gone.** AGP 9 has built-in
   Kotlin support and refuses to start when the plugin is also applied. Only
   `com.android.application` and `org.jetbrains.kotlin.plugin.compose` are
   applied.
2. **compileSdk must be 37.** Compose BOM 2026.09.00 pulls
   `androidx.compose.ui:ui-android:1.12.1`, which requires compiling against API
   37 or later. The flake was bumped to platform 37.0 and build-tools 37.0.0 to
   match. Note that nixpkgs names the platform directory `android-37.0`, because
   API 37 uses Android's minor SDK versioning; AGP resolves it from
   `compileSdk = 37` without extra configuration.
3. **`buildToolsVersion` must be pinned.** AGP 9.4.1 defaults to build-tools
   36.0.0 and tries to install it, which fails against a read-only nix store SDK.
   Setting `buildToolsVersion` from the version catalog makes it use what the
   flake provides.

`compileSdk`, `minSdk` and `buildTools` therefore live in
`gradle/libs.versions.toml` alongside the library versions, so the flake and the
Gradle build are bumped from one place.

## The gate loses its coverage step, briefly

`scripts/gate.sh` was written expecting `jacocoCoverageVerification`. That task
does not exist yet, and a gate that references a missing task fails for the wrong
reason. This change drops it from the gate; the coverage gate change
(`beans-on-droid-f006`) puts it back along with the Jacoco configuration.

## Risks

- AGP 9 is a major version. The fallback, if something later proves unworkable,
  is AGP 8.13.2. Nothing so far has needed it.
