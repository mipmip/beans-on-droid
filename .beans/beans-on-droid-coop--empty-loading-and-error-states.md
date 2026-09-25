---
# beans-on-droid-coop
title: Empty, loading and error states
status: completed
type: epic
priority: normal
created_at: 2026-09-25T15:14:34Z
updated_at: 2026-09-25T19:29:09Z
parent: beans-on-droid-md1e
openspec-link: openspec/changes/archive/2026-09-25-readable-states
---

Every screen reads sensibly when there is nothing, when work is in flight, and when it failed.

Acceptance:
- [ ] No repos yet, repo with no beans, search with no matches
- [ ] Loading states for clone, refresh and index rebuild
- [ ] Distinct messages for auth failure, network error and missing .beans directory
- [ ] Every error state offers a retry
