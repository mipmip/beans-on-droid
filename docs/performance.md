# Performance at a few hundred beans

The briefing asks the app to handle "at least a few hundred beans without
noticeable lag". This is the measurement, not an estimate.

## Method

`PerformanceTest` generates a repository of 600 beans, each with a full set of
frontmatter fields, a tag, a parent for nine beans out of ten, and a body of
around 400 words including a task list. It commits them, clones the repository
the way the app does, and times the operations the app performs.

Everything below was measured on the project's own test environment: a headless
AOSP x86_64 emulator at API 26, the lowest version the app supports, running
under `nix develop .#emulator`. A real phone from the last few years will be
faster. An emulator is the pessimistic case, which is the useful one.

Each timing runs the operation once to warm up, then measures.

## Results

| Operation                                   | Time   |
|---------------------------------------------|--------|
| Parse and index 600 beans from disk         | 224 ms |
| One search over 600 beans                   | 2 ms   |
| One filter query over 600 beans             | <1 ms  |
| One relationship lookup                     | <1 ms  |
| Scroll the list from the first row to row 600 | 66 ms |
| Type a search and see the list narrow to one | 236 ms |

## Reading these

**Indexing is the only part that takes real time**, and it happens once per
clone or refresh, behind a progress state. 224 ms for 600 beans is roughly
0.4 ms per bean, and it is dominated by reading 600 files and parsing 600 YAML
documents.

**Search costs about 2 ms per keystroke.** A frame at 60 Hz is 16.7 ms, so a
linear scan over every bean's title, body and id on every keystroke has room to
spare. This is why the list does not debounce its search: debouncing would add
latency to protect against a cost that is not there.

**Filtering and relationship lookups are effectively free**, because `BeanIndex`
precomputes the id map, the children map and the blocking graph when it is built.

**Scrolling 600 rows takes 66 ms** and produced no visible stutter.

## Where this stops being true

The index is a list and every query is a linear scan. At 600 beans that is the
right trade, and at 50,000 it would not be: search would cost around 170 ms per
keystroke, which is three dropped frames. If a repository that large ever turns
up, the fix is an inverted index for the search term, and the numbers above are
the baseline to beat.

Memory was not the constraint at this size: 600 parsed beans with their bodies
are a few megabytes, held for the process lifetime so switching screens does not
re-read them.

## Reproducing

```bash
./scripts/e2e.sh
adb logcat -d -s BeansPerf
```
