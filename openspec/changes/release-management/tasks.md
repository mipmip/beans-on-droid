## 1. Single source of truth

- [ ] 1.1 Add a top-level `VERSION` holding `0.2.0`
- [ ] 1.2 `app/build.gradle.kts` reads it for `versionName`
- [ ] 1.3 Derive `versionCode` as `major*1000000 + minor*10000 + patch*100 +
      rebuild`, with rebuild zero unless overridden
- [ ] 1.4 Fail the build with a clear message when `VERSION` is missing or
      unparseable, rather than defaulting silently
- [ ] 1.5 Confirm the built APK reports versionName 0.2.0 and versionCode 20000

## 2. Signing

- [ ] 2.1 Generate a release keystore outside the repository
- [ ] 2.2 Release signing configuration reading keystore, passwords and alias
      from the environment
- [ ] 2.3 Fall back to an unsigned release build when the key is absent, so a
      contributor can still build
- [ ] 2.4 Add keystore patterns to `.gitignore`
- [ ] 2.5 Store the keystore and its secrets as repository secrets
- [ ] 2.6 Verify a signed APK installs over nothing, and that a debug-signed
      install must be removed first

## 3. Release script

- [ ] 3.1 `scripts/release.sh patch|minor|major [--dry-run]`
- [ ] 3.2 Refuse to run on a dirty working copy or off the main bookmark
- [ ] 3.3 Bump `VERSION`
- [ ] 3.4 Roll `## [Unreleased]` into a dated section and leave a fresh empty
      `[Unreleased]`
- [ ] 3.5 Write `fastlane/metadata/android/en-US/changelogs/<versionCode>.txt`
      from that section, as plain text without markdown heading markers
- [ ] 3.6 Commit with jj, tag `v<version>`, push the bookmark and the tag
- [ ] 3.7 `--dry-run` prints every file it would touch and the tag, changing
      nothing
- [ ] 3.8 Passes shellcheck in `nix flake check`
- [ ] 3.9 Dry-run verified for patch, minor and major from 0.2.0

## 4. Gate workflow

- [ ] 4.1 `.github/workflows/gate.yml` on push and pull request
- [ ] 4.2 Install nix and run `./scripts/gate.sh`
- [ ] 4.3 Cache the nix store between runs
- [ ] 4.4 Publish the coverage report as an artefact

## 5. Release workflow

- [ ] 5.1 `.github/workflows/release.yml` on a `v*` tag
- [ ] 5.2 Build the signed release APK from the repository secrets
- [ ] 5.3 Compute and attach checksums
- [ ] 5.4 Create the GitHub release with notes taken from the changelog section
      for that version
- [ ] 5.5 Name the artefact with its version
- [ ] 5.6 Do not run on a tag whose version does not match `VERSION`

## 6. End-to-end workflow

- [ ] 6.1 `.github/workflows/e2e.yml`, run on request
- [ ] 6.2 Boot the API 26 emulator and run the instrumented suite
- [ ] 6.3 Publish the test report as an artefact

## 7. Documentation

- [ ] 7.1 `docs/RELEASING.md`: the procedure, the version scheme, the four
      files a release touches
- [ ] 7.2 State the key backup obligation as part of the procedure
- [ ] 7.3 Explain that an interim install may need one uninstall before moving
      to an F-Droid build, and how reproducible builds would avoid it
- [ ] 7.4 README: how a friend installs the APK, and that caveat
- [ ] 7.5 `docs/fdroid.md`: record the interim channel and the signing position

## 8. Verification

- [ ] 8.1 `./scripts/gate.sh` passes
- [ ] 8.2 Dry-run the release, inspect every diff it proposes
- [ ] 8.3 Cut `0.2.0` and check the artefact before announcing it
- [ ] 8.4 Install the released APK on a real device
