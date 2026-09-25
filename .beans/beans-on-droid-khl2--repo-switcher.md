---
# beans-on-droid-khl2
title: Repo switcher
status: completed
type: epic
priority: normal
created_at: 2026-09-25T15:14:34Z
updated_at: 2026-09-25T18:50:28Z
parent: beans-on-droid-md1e
openspec-link: openspec/changes/archive/2026-09-25-repo-switcher
---

Add a repo by HTTPS URL with an optional token, remove a repo, and switch the active one.

Acceptance:
- [ ] Add form validates the URL before cloning
- [ ] Clone progress is visible and cancellable
- [ ] Removing a repo deletes its working copy and token
- [ ] Switching repos rebuilds the index for the new repo
