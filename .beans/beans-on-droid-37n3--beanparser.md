---
# beans-on-droid-37n3
title: BeanParser
status: completed
type: epic
priority: normal
created_at: 2026-09-25T15:14:34Z
updated_at: 2026-09-25T18:06:52Z
parent: beans-on-droid-rhgf
blocked_by:
    - beans-on-droid-88wd
openspec-link: openspec/changes/archive/2026-09-25-bean-parser
---

The only code in the app that knows the bean file format. File in, Bean data class out. Malformed frontmatter is skipped, not fatal.

Acceptance:
- [ ] YAML frontmatter parsed with a FOSS parser (SnakeYAML Engine or kaml)
- [ ] Unit tests built on the fixture files from the research spike
- [ ] Edge cases covered: missing optional fields, empty body, malformed frontmatter skipped without crashing, unknown fields preserved
- [ ] Coverage on this package at or above 80 percent
