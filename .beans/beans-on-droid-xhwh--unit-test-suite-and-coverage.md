---
# beans-on-droid-xhwh
title: Unit test suite and coverage
status: in-progress
type: epic
priority: normal
created_at: 2026-09-25T15:14:34Z
updated_at: 2026-09-25T19:29:23Z
parent: beans-on-droid-haij
---

Thorough unit coverage of the data layer and view models. The parser and index are the parts that must not be wrong.

Acceptance:
- [ ] `./gradlew test` passes
- [ ] Coverage at or above 70 percent overall and 80 percent on parser and index
- [ ] ViewModel state transitions tested with a test dispatcher
- [ ] RepoStore tested against a local bare git repo fixture, no network
