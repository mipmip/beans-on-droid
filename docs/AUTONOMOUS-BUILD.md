# Autonomous build instructions

Read this together with [AGENTS.md](../AGENTS.md) and
[docs/BRIEFING.md](BRIEFING.md). The briefing says what to build. AGENTS.md says
how this repo works. This file says how to drive the build from start to finish
without supervision.

## The loop

Repeat until no epic is left:

1. `beans list --json --ready` and pick the lowest milestone number with unfinished
   epics, then the first ready epic under it. Dependencies are already encoded
   with `blocked-by`, so a blocked epic will not appear.
2. `beans update <epic-id> -s in-progress`.
3. Write the OpenSpec proposal for that epic:
   `openspec/changes/<change-name>/proposal.md`, `tasks.md`, and the spec deltas
   the schema asks for. The proposal must link back to the bean, and its tasks
   must cover every acceptance checkbox in the bean body.
4. `openspec validate <change-name> --strict`. Fix what it reports.
5. Implement. Check off items in `tasks.md` as each one actually lands, not in
   advance.
6. Write the tests for this epic in the same change. An epic is not implemented
   until its tests pass.
7. Add user-facing entries to `CHANGELOG.md` under `## [Unreleased]`.
8. `./scripts/ship-change.sh <change-name>`. This gates, archives, commits, and
   pushes. If the gate fails, nothing is archived or committed: fix and run again.
9. `beans update <epic-id> -s completed` and add the archive path to the bean
   frontmatter as `openspec-link: openspec/changes/archive/...`.

Status and `openspec-link` are the only things you may change in a bean file.
The acceptance boxes in a bean body stay as written; they are the contract the
change's `tasks.md` must cover, and it is `tasks.md` you tick off. Do not append
a summary to a bean either. The OpenSpec archive is the record of what was done.

When every epic under a milestone is completed, set the milestone to `completed`
too.

## Order

Milestone 01 before 02 before 03 before 04, with one exception: the bean format
research spike in milestone 02 should run early, because the fixtures it produces
are what the parser tests are built on. It is already marked as blocking
`BeanParser`.

Do not start UI work before `BeanIndex` is done and tested. The UI talks to
ViewModels, the ViewModels talk to the index and the store, and nothing in the
UI layer may know the bean file format.

## Rules that are not negotiable

- **Derive the bean format from the real repository.** Clone
  `https://github.com/hmans/beans`, read its `.beans/` directory and `.beans.yml`,
  and read the Go source where a field is unclear. Do not infer the schema from
  the briefing or from this repo's own `.beans/`.
- **Unknown frontmatter fields are kept**, parsed as generic key-value pairs and
  shown raw in the detail view. Never drop a field you did not recognise.
- **FOSS only.** No Play Services, Firebase, Crashlytics, analytics, ads, or
  prebuilt jars in the repo. If a dependency choice is not clearly FOSS, pick a
  different one and say why in the proposal.
- **Verify JGit at runtime on API 26**, not just at compile time. If it needs a
  workaround, document the workaround in the README.
- **No comments in code.** See AGENTS.md.
- **Commits are authored by Pim Snel** with no attribution trailers.

## Testing expectations

Thorough, and the gate enforces it.

- Unit tests for `BeanParser` built on fixture files copied from the real
  hmans/beans `.beans/` directory, plus edge cases: missing optional fields,
  empty body, malformed frontmatter skipped rather than fatal, unknown fields
  preserved.
- Unit tests for `BeanIndex`: each filter alone, filters combined, search over
  title and body, relationship resolution.
- `RepoStore` tested against a local bare git repository created in the test
  fixture. No network in unit tests.
- ViewModel state transitions tested with a test dispatcher.
- Instrumented end-to-end tests for the five flows listed in the "End to end
  tests" epic, driven against a local git repository, not against GitHub.
- Coverage at or above 70 percent overall and 80 percent on the parser and index
  packages, enforced by `jacocoCoverageVerification`.

## When something is genuinely blocked

Do not silently work around the briefing. Write the problem and the options into
the proposal, pick the most robust working option, implement it, and record the
decision in the README. Create a follow-up bean for anything deferred.
