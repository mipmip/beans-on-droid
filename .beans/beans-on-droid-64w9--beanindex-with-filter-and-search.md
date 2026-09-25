---
# beans-on-droid-64w9
title: BeanIndex with filter and search
status: todo
type: epic
priority: normal
created_at: 2026-09-25T15:14:34Z
updated_at: 2026-09-25T15:14:34Z
parent: beans-on-droid-rhgf
---

In-memory index rebuilt on clone and refresh. Queries for status, type and tag filters plus text search over title and body.

Acceptance:
- [ ] Rebuild is a pure function of the parsed bean set
- [ ] Filters combine (status AND type AND tags) with search
- [ ] Search matches title and body, case-insensitive
- [ ] Resolves relationship ids to bean references for the detail screen
- [ ] Unit tested; a few hundred beans index and query without noticeable lag
