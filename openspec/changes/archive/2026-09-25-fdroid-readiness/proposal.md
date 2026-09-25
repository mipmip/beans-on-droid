## Why

Bean `beans-on-droid-hjxa`. The briefing's F-Droid requirements are hard
constraints, and most of them were satisfied while building rather than checked.
Satisfied-by-accident and verified are different things, particularly for "only
FOSS dependencies", which is a claim about a transitive graph nobody has looked
at.

## What Changes

- Audit every artifact on the release runtime classpath and record its licence.
- Verify no Play Services, Firebase, Crashlytics, analytics or ad SDK is present.
- Pin the Gradle distribution's SHA-256 and record the wrapper jar's checksum,
  and state the wrapper jar as the one deviation from "no jars in the repo".
- Capture real screenshots from a run on an API 26 emulator, and add
  `scripts/screenshots.sh` to regenerate them.
- Record all of it in `docs/fdroid.md`.

## Capabilities

### New Capabilities

None. Audit, metadata and tooling, so `skip_specs: true`.

### Modified Capabilities

None.

## Impact

- New: `docs/fdroid.md`, `ScreenshotTest`, `scripts/screenshots.sh`, four
  screenshots.
- Modified: `gradle/wrapper/gradle-wrapper.properties`.
