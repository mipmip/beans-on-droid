## Why

Beans `beans-on-droid-s7qe` (release management) and `beans-on-droid-4vhg`
(build APKs with GitHub runners at release).

There is no way to give anyone this app. There are no tags, no CI, the release
variant produces `app-release-unsigned.apk`, and the version is two literals in
`app/build.gradle.kts` that nothing derives from anything. The only artefact
that has ever reached a phone is a debug build installed over a cable.

F-Droid remains the destination, but inclusion takes time and friends want it
now. That means an interim channel, which means a signing key, which means a
decision that cannot be taken back later.

## What Changes

- Add a top-level `VERSION` file as the single source of truth, read by Gradle
  for `versionName` and used to derive `versionCode`.
- Derive `versionCode` from the version rather than maintaining it by hand,
  with a reserved slot so the same version can be rebuilt.
- Add `scripts/release.sh patch|minor|major [--dry-run]`: bump the version, roll
  `## [Unreleased]` into a dated section, write the fastlane changelog named
  after the new `versionCode`, commit with jj, tag and push.
- Add a release signing configuration that reads its key from the environment,
  so a release build is signed in CI and falls back to unsigned locally for
  anyone without the key.
- Add `.github/workflows/gate.yml`: the existing gate on every push and pull
  request, through nix so CI and local runs share a toolchain.
- Add `.github/workflows/release.yml`: on a `v*` tag, build the signed APK,
  publish it as a GitHub release with notes taken from the changelog section,
  and attach checksums.
- Add `.github/workflows/e2e.yml`, run on request, booting the API 26 emulator
  and running the instrumented suite.
- Add `docs/RELEASING.md`, covering the key, its backup, and what installing an
  interim APK means for a later move to F-Droid.
- Set the first managed release to `0.2.0`.

## Capabilities

### New Capabilities

None. Build and release tooling with no change to what the app does, so
`skip_specs: true`.

### Modified Capabilities

None.

## Impact

- New: `VERSION`, `scripts/release.sh`, three workflow files,
  `docs/RELEASING.md`.
- Modified: `app/build.gradle.kts`, `CHANGELOG.md`, `.gitignore`,
  `docs/fdroid.md`, `README.md`.
- A signing key is created outside the repository and held as repository
  secrets. Losing it ends the ability to update the app for anyone who
  installed a build signed with it.
- Anyone who installs an interim APK cannot upgrade to an F-Droid build signed
  with a different key. The design states how that is avoided and what happens
  if it cannot be.
