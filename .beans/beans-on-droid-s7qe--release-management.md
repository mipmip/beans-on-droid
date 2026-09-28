---
# beans-on-droid-s7qe
title: release management
status: in-progress
type: task
priority: normal
created_at: 2026-09-28T15:00:11Z
updated_at: 2026-09-28T15:10:54Z
blocking:
    - beans-on-droid-4vhg
---

- changelog management
- semantic versioning
- single source of truth for app-version
- easy to handle script: patch/minor/major
- jj/git compatible

Android specifics the nivis version did not have:

- two numbers, not one: versionName is the semver, versionCode is a
  monotonic integer that F-Droid and Play order releases by, and it can
  never go backwards
- versionCode and versionName are currently literals in
  app/build.gradle.kts, so the single source of truth has to feed both
- fastlane wants a per-release changelog at
  fastlane/metadata/android/en-US/changelogs/<versionCode>.txt, so a
  release has to write that file, not only CHANGELOG.md
- a release APK needs signing, and who holds the key has to be decided:
  F-Droid signs its own builds, a GitHub release artefact does not
- deciding whether a tag is the trigger, given ship-change.sh already
  commits and pushes per change
