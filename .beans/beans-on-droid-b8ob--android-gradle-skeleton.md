---
# beans-on-droid-b8ob
title: Android Gradle skeleton
status: completed
type: epic
priority: normal
created_at: 2026-09-25T15:14:34Z
updated_at: 2026-09-25T17:53:39Z
parent: beans-on-droid-hsk9
openspec-link: openspec/changes/archive/2026-09-25-android-project-skeleton
---

Single-module Android app with a Gradle version catalog. Kotlin, Compose, Material 3, single activity, Navigation Compose. Current stable AGP, Kotlin and Compose BOM.

Acceptance:
- [ ] applicationId io.github.mipmip.beansondroid, minSdk 26, versionCode 1, versionName 0.1.0
- [ ] gradle/libs.versions.toml holds every version; no hardcoded versions in build files
- [ ] Core library desugaring enabled for JGit's Java 8+ API use
- [ ] `./gradlew assembleDebug` succeeds
- [ ] App launches to an empty scaffold on API 26
