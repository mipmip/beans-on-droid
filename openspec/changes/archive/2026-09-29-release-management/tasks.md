## 1. Single source of truth

- [x] 1.1 Add a top-level `VERSION` holding `0.2.0`
- [x] 1.2 `app/build.gradle.kts` reads it for `versionName`
- [x] 1.3 Derive `versionCode` as `major*1000000 + minor*10000 + patch*100 +
      rebuild`, with rebuild zero unless overridden
- [x] 1.4 Fail the build with a clear message when `VERSION` is missing or
      unparseable, rather than defaulting silently
- [x] 1.5 Confirm the built APK reports versionName 0.2.0 and versionCode 20000

## 2. Signing

- [x] 2.1 Generate a release keystore outside the repository
- [x] 2.2 Release signing configuration reading a local `keystore.properties`
      first, then the environment
- [x] 2.3 Fall back to an unsigned release build when the key is absent, so a
      contributor can still build
- [x] 2.4 Add keystore patterns to `.gitignore`
- [x] 2.5 Store the keystore and its secrets as repository secrets
- [x] 2.6 Verify a signed APK installs over nothing, and that a debug-signed
      install must be removed first
- [x] 2.7 Fail loudly when signing is half configured, rather than quietly
      producing an unsigned APK

## 3. Release script

- [x] 3.1 `scripts/release.sh patch|minor|major [--dry-run]`
- [x] 3.2 Refuse to run on a dirty working copy or off the main bookmark
- [x] 3.3 Bump `VERSION`
- [x] 3.4 Roll `## [Unreleased]` into a dated section and leave a fresh empty
      `[Unreleased]`
- [x] 3.5 Write `fastlane/metadata/android/en-US/changelogs/<versionCode>.txt`
      from that section, as plain text without markdown heading markers
- [x] 3.6 Commit with jj, tag `v<version>`, push the bookmark and the tag
- [x] 3.7 `--dry-run` prints every file it would touch and the tag, changing
      nothing
- [x] 3.8 Passes shellcheck in `nix flake check`
- [x] 3.9 Dry-run verified for patch, minor and major from 0.2.0

## 4. Gate workflow

- [x] 4.1 `.github/workflows/gate.yml` on push and pull request
- [x] 4.2 Install nix and run `./scripts/gate.sh`
- [x] 4.3 Cache the nix store between runs
- [x] 4.4 Publish the coverage report as an artefact

## 5. Release workflow

- [x] 5.1 `.github/workflows/release.yml` on a `v*` tag
- [x] 5.2 Read the signing key from the repository secrets into the build
      (proven locally with the same key; the workflow run itself is covered by
      the release bean)
- [x] 5.3 Compute and attach checksums
- [x] 5.4 Create the GitHub release with notes taken from the changelog section
      for that version
- [x] 5.5 Name the artefact with its version
- [x] 5.6 Do not run on a tag whose version does not match `VERSION`

## 6. End-to-end workflow

- [x] 6.1 `.github/workflows/e2e.yml`, run on request
- [x] 6.2 Boot the API 26 emulator and run the instrumented suite
- [x] 6.3 Publish the test report as an artefact

## 7. Documentation

- [x] 7.1 `docs/RELEASING.md`: the procedure, the version scheme, the four
      files a release touches
- [x] 7.2 State the key backup obligation as part of the procedure
- [x] 7.3 Explain that an interim install may need one uninstall before moving
      to an F-Droid build, and how reproducible builds would avoid it
- [x] 7.4 README: how a friend installs the APK, and that caveat
- [x] 7.5 `docs/fdroid.md`: record the interim channel and the signing position

## 8. Verification

- [x] 8.1 `./scripts/gate.sh` passes
- [x] 8.2 Dry-run the release, inspect every diff it proposes
- [x] 8.3 A signed APK installs on a device, and a differently signed one is
      refused
- [x] 8.4 Cutting `0.2.0` and checking the published artefact is tracked
      separately, because a release can only follow this change being archived
