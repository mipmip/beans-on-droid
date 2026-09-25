## Why

Bean `beans-on-droid-88wd`. The parser is the one piece of this app that must
match an external format exactly, and the briefing is explicit that the format
must be derived from the real thing rather than guessed. Getting this wrong is
expensive: every screen reads what the parser produces, and a field silently
dropped here is a field missing everywhere.

## What Changes

- Document the bean file format in `docs/bean-format.md`, derived from the
  `hmans/beans` repository and its Go source rather than from prose.
- Copy a fixture set of real bean files into the app's unit test resources,
  recording where they came from and under which license.
- Record the handling rule for fields the parser does not recognise.

## Capabilities

### New Capabilities

None. Research and fixtures; no production code and no behavior, so
`skip_specs: true`. The behavior this enables is specified by
`beans-on-droid-37n3`.

### Modified Capabilities

None.

## Impact

- New: `docs/bean-format.md`, `app/src/test/resources/fixtures/`.
- Every later parser and index decision cites this document.
