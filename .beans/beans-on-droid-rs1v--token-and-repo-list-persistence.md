---
# beans-on-droid-rs1v
title: Token and repo list persistence
status: in-progress
type: epic
priority: normal
created_at: 2026-09-25T15:14:34Z
updated_at: 2026-09-25T18:28:52Z
parent: beans-on-droid-rhgf
---

Repo list in DataStore. Token encrypted with an Android Keystore-backed key, never in plain preferences.

Acceptance:
- [ ] Repo list persists across process death
- [ ] Token encrypted at rest with a Keystore-backed key
- [ ] Removing a repo also removes its token
- [ ] No token value ever reaches a log statement
