---
# beans-on-droid-7ie3
title: f-droid submission
status: draft
type: task
priority: normal
created_at: 2026-09-28T15:11:46Z
updated_at: 2026-09-28T15:11:46Z
blocked_by:
    - beans-on-droid-s7qe
---

Get the app listed in F-Droid, which is the distribution goal the whole
project was built for.

- RFP issue on gitlab.com/fdroid/rfp
- metadata recipe in fdroiddata: build entry, UpdateCheckMode and
  AutoUpdateMode reading tags
- reproducible build verification, so F-Droid ships OUR signature instead
  of theirs
- anti-features check: none expected, INTERNET plus CAMERA once the
  scanner lands

Obstacles to check before submitting, not after:

- their build server has no nix. The recipe runs plain gradle against
  their own SDK, so `./gradlew assembleRelease` has to work with
  ANDROID_HOME alone. Verify it does, outside the flake.
- we pin compileSdk 37 and buildTools 37.0.0, which are recent. Confirm
  their buildserver has them before assuming.
- AGP 9.4.1 and Gradle 9.8 likewise.
- gradle-wrapper.properties pins distributionSha256Sum and the wrapper
  jar is committed. F-Droid substitutes its own gradle, so check the two
  do not fight.

Already done and reusable:

- fastlane/metadata/android/en-US is populated with title, descriptions,
  a changelog per versionCode and four real screenshots, so the listing
  content comes from the repo rather than being written again
- docs/fdroid.md holds the dependency licence audit

Reproducibility is the part that matters most. Anyone who installs an
interim APK signed with our key cannot upgrade to an F-Droid-signed build
without uninstalling and losing their repositories and tokens. Verified
reproducible builds are what avoids that, and the flake already pins the
toolchain, which is the hard half.
