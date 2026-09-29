## Why

Bean `beans-on-droid-hkb4`. The app displays its version nowhere, and
`BuildConfig` is not even generated, so the version is not reachable from code.

That matters most for the people getting interim APKs before F-Droid: a
sideloaded build has no listing to check, so "which version are you on" has no
answer, and a bug report cannot be tied to a build.

## What Changes

- Enable `BuildConfig` generation, which the app does not currently do.
- Show the version name and version code at the bottom of the repository
  screen, the only settings-shaped surface the app has.
- Keep it visible whether or not a repository is configured, so a failed first
  clone can still be reported against a known build.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `repo-switcher`: the screen gains a footer identifying the running build.

## Impact

- Modified: `app/build.gradle.kts`, `ui/screen/RepoScreen.kt`.
- No new dependencies.
