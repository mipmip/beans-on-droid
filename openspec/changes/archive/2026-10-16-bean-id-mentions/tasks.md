## 1. Recognition

- [x] 1.1 Add a scan, free of Android types, that walks a body's text and returns every run of letters, digits and hyphens that stands alone as a word and that the lookup recognises; verify with JVM unit tests covering a known id, a punctuation boundary, an id followed by `.md`, the same id twice, an id-shaped word no bean has, and a run inside a path left alone
- [x] 1.2 Add a membership test to `BeanIndex`; verify with a unit test that an id a parsed bean carries is reported present with that bean, and that an id no bean has is reported absent without an error
- [x] 1.3 Name the closed-status rule (completed or scrapped) once in the vocabulary and use it for the status colours; verify with a unit test over every status the vocabulary lists, plus an absent status

## 2. Mentions in the body

- [x] 2.1 Wrap each recognised id in a mention span (tint, monospace, click) that opens the mentioned bean; verify with an instrumented test that a body naming another bean carries a clickable span over that id and that following it opens that bean, by the mechanism chosen in design.md
- [x] 2.2 Show a mention of a closed bean struck through and dimmed, and an open mention plain; verify with an instrumented test asserting that the span over a closed bean's id strikes through and the span over an open bean's id does not
- [x] 2.3 Leave an id-shaped word that no bean has as ordinary text, with no mention span and nothing to tap; verify with an instrumented test
- [x] 2.4 Feed the body renderer the lookup and the open callback from the detail screen, and re-apply the spans when the index changes; verify the existing detail tests still pass and that a mention is struck through after the repository is read again with that bean closed

## 3. Verification

- [x] 3.1 `./gradlew test` passes
- [x] 3.2 `./gradlew lint` passes
- [x] 3.3 `./gradlew connectedAndroidTest` passes
- [x] 3.4 Coverage stays at or above the floor enforced by the gate
- [x] 3.5 `./scripts/gate.sh` passes
- [x] 3.6 A body holding a mention, a code span and a table reads correctly on the emulator
