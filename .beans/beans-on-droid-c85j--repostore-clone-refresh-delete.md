---
# beans-on-droid-c85j
title: 'RepoStore: clone, refresh, delete'
status: in-progress
type: epic
priority: normal
created_at: 2026-09-25T15:14:34Z
updated_at: 2026-09-25T18:10:14Z
parent: beans-on-droid-rhgf
---

JGit-backed storage of one working copy per repo in app-private storage. Shallow clone at depth 1. Refresh is fetch plus hard reset to the remote branch, never a merge, because the app never writes.

Acceptance:
- [ ] Clone by HTTPS URL with optional token auth
- [ ] Refresh = fetch + hard reset to the tracked remote branch
- [ ] Delete removes the working copy and its entry
- [ ] JGit version verified to actually run on API 26 at runtime, not just compile
- [ ] Failure modes surface as typed errors: auth failure, network error, no .beans directory
- [ ] Findings on JGit and minSdk 26 recorded in the README if any workaround was needed
