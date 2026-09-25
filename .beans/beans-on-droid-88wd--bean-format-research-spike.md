---
# beans-on-droid-88wd
title: Bean format research spike
status: completed
type: epic
priority: normal
created_at: 2026-09-25T15:14:34Z
updated_at: 2026-09-25T17:59:57Z
parent: beans-on-droid-rhgf
openspec-link: openspec/changes/archive/2026-09-25-bean-format-research
---

Derive the bean file format from the real thing instead of guessing. Clone https://github.com/hmans/beans, read .beans.yml and a spread of bean files, and consult the Go source where a field's meaning is unclear.

Acceptance:
- [ ] Written findings committed under docs/bean-format.md: every frontmatter field, its type and whether it is optional
- [ ] Relationship fields documented (parent, blocking, blocked-by, and anything else present)
- [ ] A fixture set copied into app test resources, covering several real beans
- [ ] Unknown or undocumented fields have a stated handling rule: keep as generic key-value, never drop
