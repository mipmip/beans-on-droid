---
# beans-on-droid-f006
title: Build, test and coverage gate
status: todo
type: epic
priority: normal
created_at: 2026-09-25T15:14:34Z
updated_at: 2026-09-25T15:14:34Z
parent: beans-on-droid-hsk9
---

The gate that /mip:ship runs before archiving a change. Build, unit tests, lint and a coverage floor.

Acceptance:
- [ ] Jacoco configured with a verification task
- [ ] Coverage floor: 70 percent overall, 80 percent on the parser and index packages
- [ ] `./gradlew lint` reports no errors
- [ ] scripts/ship-change.sh runs the gate via nix develop and aborts the ship on failure
