## The fixture has to be realistic

600 beans with one-line bodies would measure nothing useful. Each generated bean
carries a full frontmatter set, a tag, a parent for nine out of ten of them so
the relationship graph is dense, and a body of roughly 400 words with a task
list. That makes the search scan real text and the index build a real graph.

## The emulator is the right place to measure

An API 26 AOSP emulator is slower than any phone the app will run on, and it is
the minimum version supported. Numbers taken there are a floor. Measuring on the
development machine's JVM would have produced faster, meaningless figures.

## Assertions, not just numbers

Each timing has a bound an order of magnitude above the measured value: indexing
under 4 seconds against 224 ms measured, twenty searches under 2 seconds against
40 ms. Tight bounds would make the suite flaky on a loaded CI machine; loose ones
still catch the regressions that matter, which are the ones that change the shape
of the cost rather than shaving it.

## Results

| Operation                                     | Measured |
|-----------------------------------------------|----------|
| Parse and index 600 beans                     | 224 ms   |
| One search                                    | 2 ms     |
| One filter query                              | under 1 ms |
| One relationship lookup                       | under 1 ms |
| Scroll from row 1 to row 600                  | 66 ms    |
| Search narrowing 600 beans to one, end to end | 236 ms   |

The 2 ms search is the number that matters, because it is the one the
no-debounce decision rests on. A 60 Hz frame is 16.7 ms.

`docs/performance.md` also records where this stops being true: at tens of
thousands of beans a linear scan per keystroke would drop frames, and the fix
would be an inverted index. Writing down the limit is the point of measuring.
