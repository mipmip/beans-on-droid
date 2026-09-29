---
# beans-on-droid-hkb4
title: show the app version in the app
status: completed
type: feature
priority: normal
created_at: 2026-09-29T12:11:20Z
updated_at: 2026-09-29T13:01:14Z
openspec-link: openspec/changes/archive/2026-09-29-show-app-version
---

The app displays its version nowhere. Someone with a sideloaded build
has no way to tell which one they are running, which matters most for
exactly the people getting interim APKs before F-Droid.

- versionName and versionCode, since F-Droid and bug reports use the code
- somewhere you would look: the repository screen is the only settings-ish
  surface the app has
- reachable without a repository configured, so a broken clone can still be
  reported against a known version
- BuildConfig needs enabling; the app does not generate it today
