---
# beans-on-droid-f006
title: Build, test and coverage gate
status: completed
type: epic
priority: normal
created_at: 2026-09-25T15:14:34Z
updated_at: 2026-09-25T17:57:59Z
parent: beans-on-droid-hsk9
openspec-link: openspec/changes/archive/2026-09-25-coverage-gate
---

The gate that /mip:ship runs before archiving a change. Build, unit tests, lint and a coverage floor.

Acceptance:
- [ ] Jacoco configured with a verification task
- [ ] Coverage floor: 70 percent overall, 80 percent on the parser and index packages
- [ ] `./gradlew lint` reports no errors
- [ ] scripts/ship-change.sh runs the gate via nix develop and aborts the ship on failure
