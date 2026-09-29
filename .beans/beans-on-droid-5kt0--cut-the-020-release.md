---
# beans-on-droid-5kt0
title: cut the 0.2.0 release
status: todo
type: task
priority: normal
created_at: 2026-09-29T12:01:28Z
updated_at: 2026-09-29T12:01:28Z
blocked_by:
    - beans-on-droid-s7qe
---

Use the release machinery for the first time. Split out of release
management because these can only happen after that change is archived:
release.sh refuses to run on a dirty working copy, so the tag comes after
the commit, not before it.

- [ ] ./scripts/release.sh minor --dry-run, read every line
- [ ] ./scripts/release.sh minor
- [ ] watch the release workflow; the first tag is also the first test of it
- [ ] confirm it built a SIGNED apk from the repository secrets
- [ ] download the published APK and check its SHA-256 against the attached file
- [ ] confirm the signer is CN=Beans on Droid, O=mipmip, C=NL
- [ ] install it on a real device
- [ ] only then tell anyone it exists

Already in place: VERSION at 0.2.0, the keystore, all four repository
secrets, release.sh dry-run verified for patch, minor and major, and a
signed APK built locally and installed on an emulator.

Anyone installing this build cannot later upgrade to an F-Droid build
without uninstalling. Say so in the release notes.
