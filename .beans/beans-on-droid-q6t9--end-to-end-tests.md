---
# beans-on-droid-q6t9
title: End to end tests
status: in-progress
type: epic
priority: normal
created_at: 2026-09-25T15:14:34Z
updated_at: 2026-09-25T19:33:48Z
parent: beans-on-droid-haij
---

Instrumented Compose tests that prove the PoC does what the briefing claims, driven against a local git repo served from the test device.

Acceptance:
- [ ] e2e: add repo, clone, land on a populated bean list
- [ ] e2e: filter and search narrow the list correctly
- [ ] e2e: open a bean, follow a relationship link, land on the right bean
- [ ] e2e: pull to refresh picks up a new commit in the fixture repo
- [ ] e2e: a bad token surfaces the auth error state
- [ ] Tests run headless in CI against an emulator
