## Context

See proposal.md for motivation. The state that shapes the approach:

- A body is rendered by `ui/Markdown.kt` with Markwon 4.6.2 into a `TextView`
  inside an `AndroidView`, with the tables, strikethrough and task list plugins
  registered. A mention is therefore a span, not a composable.
- Markwon's core plugin sets `LinkMovementMethod` on a `TextView` that has none
  (read out of the 4.6.2 bytecode), so links in a body are already clickable and
  a clickable span needs no new movement method and no new dependency.
- `BeanDetailScreen` already holds the index and an open-bean callback that
  navigates to another detail view, so a mention can reuse both.
- `BeanIndex` keeps its ids in a private map and exposes only `all`, so there is
  no membership test to ask today.
- `BeanVisuals` already groups `completed` and `scrapped` for status colour.
- Bodies in the real fixtures are full of hyphenated words that are not ids:
  `in-progress`, `follow-up`, `per-bean`, `claude-code-acp`. Shape alone cannot
  tell those from `beans-27wn`.
- There is no Robolectric in the build, and Markwon needs a context, so anything
  that touches the rendered text cannot run as a JVM unit test.

## Goals / Non-Goals

**Goals:**

- Recognise mentions by asking the index, so nothing is marked that the
  repository cannot open.
- Keep the recognition itself free of Android types, so it is unit tested on the
  JVM and only the span application needs a device.
- Reuse the existing navigation, index and status colour vocabulary rather than
  adding a parallel path.

**Non-Goals:**

- No mention syntax. A mention stays a bare id.
- No mention awareness anywhere but a body: not the list, not the header, not
  the relations, not the frontmatter.
- No cache of rendered bodies or of mention state.
- No change to relationship resolution.

## Decisions

**Recognition is index membership, not id shape.** A candidate is a run of
letters, digits and hyphens; it becomes a mention when the index holds a bean
with that id. Alternatives were a shape rule (`<prefix>-<suffix>`) and a shape
rule with the prefix derived from the repository's own ids. Both were rejected
because the evidence above shows id-shaped non-ids everywhere, and a false
mention that opens nothing is worse than a missed one that can be found by
search.

**Candidates come from the rendered text, and must stand alone as a word.** The
run is compared whole, so `beans-27wn,` and `beans-27wn.md` are mentions while
`in-progress` is not. Two known misses follow and are accepted for now: an id
written inside a path (`.beans/beans-l4ag--auto-reload.md` is one run), and an
id followed immediately by a hyphen. Both would be fixed by splitting a run on a
double hyphen and trimming a trailing hyphen, which is left for a later change
rather than widened now.

**A range that already carries a URL span is left alone.** A markdown link whose
text happens to be an id keeps going to its own target, and its author's
intent wins over ours.

**A mention is two spans.** A `ClickableSpan` subclass carries the tint, the
strikethrough and the click, and a typeface span alongside it carries the
monospace face, because `CharacterStyle` cannot set a typeface. The alternative
was rewriting the markdown source into a link and intercepting a custom scheme
through Markwon's link resolver; that mutates the author's text and still needs
a custom span factory for the strikethrough.

**Closed means `completed` or `scrapped`.** That pair is named once and shared
with the status colours in `BeanVisuals`, so a status cannot be closed in one
place and open in another. An archived bean is not closed by that rule.

**A closed mention drops the tint and the colour.** It is struck through and
shown in the outline colour that closed beans already use elsewhere, so the
tint keeps meaning "this is an open pointer". Keeping the tint and adding a
strikethrough was rejected as busier and more confusing.

**The scan is a pure function in the UI package; the membership test is in the
index.** The scan takes the text and a lookup and returns ranges, which keeps it
unit testable and keeps it out of the data layer. `BeanIndex` gains the
membership test that the lookup is built on.

**Spans are re-applied whenever the index changes.** The body view rebuilds its
rendered text on each update rather than caching it, so a bean that is closed in
the repository is struck through after the next read, with no extra
invalidation to write.

**A self-mention behaves like any other mention.** It opens the bean it names,
which is the same bean. Excluding it would need a special case with no benefit.

**A mention inside a table row is marked too.** Markwon draws each table row as
a replacement span that holds and draws its own cells, so the cell text never
reaches the body's own text and a scan over that text alone would miss it. The
renderer reads the cells out of the row spans and marks each cell's own text, and
the body view moves the text with a table aware movement method so a tap inside a
row reaches the mention. The tables plugin publishes no accessor for a row's
cells, so reading them takes reflection on one private field; the release build
does not minify, which is what keeps that safe, and it is the thing to revisit
first if minification is ever turned on. The emulator look confirmed the result:
the cell mention carries the same tint as a mention in ordinary text.

## Risks / Trade-offs

- Styling inside existing spans: a tick or background of another span can be
  overridden where they overlap. The emulator look covered a mention in a table
  cell and a code span beside the mentions, and the cell mention reads as
  intended; a mention inside a code span takes the mention styling over the code
  background, which is the wanted reading of "the mention wins".
- The body `TextView` is invisible to Compose semantics, so mention assertions go
  through Espresso, as body text assertions already do. Mitigation: the
  recognition logic is pure and unit tested, so instrumented tests only prove
  that a mention span exists, that a closed mention is struck through, and that
  a mention opens its bean.
- Our strikethrough and the one `StrikethroughPlugin` draws for `~~` look
  identical, so the two cannot be told apart in a screenshot. This is the
  intent; a test has to assert the span rather than the look.
- Rebuilding the span set on every recomposition costs work proportional to the
  body length. Bodies are small, so no cache is added; if a body with thousands
  of ids ever shows up, that is the moment to measure.
- The strict word match misses ids written as file paths. Recorded above.

## Open Questions

None. The instrumented test reaches the body `TextView`, asserts that the span
covers exactly the id, and invokes the span's click, because Espresso can only
target a whole view and the mention's hit area belongs to `LinkMovementMethod`
rather than to this app.
