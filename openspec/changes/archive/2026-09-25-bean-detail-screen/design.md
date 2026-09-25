## Decisions

### Markwon in an `AndroidView`, not a Compose Markdown library

Markwon is Apache-2.0, mature, and renders what bean bodies actually contain:
task lists, tables and strikethrough, all of which appear in the upstream
repository's own beans. The pure-Compose alternatives either omit those
extensions or are young enough that a rendering bug would be mine to debug.

The cost is that the rendered body lives in a `TextView`, outside Compose's
semantics tree. That is a testing consequence, handled below, not a user-facing
one.

### The Markwon instance is remembered per context

Building a `Markwon` is not free and the detail screen recomposes on every state
change. It is built once per context and reused; only `setMarkdown` runs on
update. Colours are read from the theme and applied to the view, so the body
follows light and dark like the rest of the screen.

### Status and type are pills, not rows

They were briefly both: a pill in the header and a row in the field list. That
was noise, and an instrumented test caught it by finding two nodes with the same
text. The header pills stayed because they are scannable and coloured; the rows
went.

### Relationships are computed, not read

The screen asks `BeanIndex.relations(id)`, which unions the explicit `blocking`
and `blocked_by` fields with the inverse relations found by scanning, and derives
children from other beans' `parent`. So a bean shows its children even though no
bean file lists them, and it shows that it blocks something even when only the
other bean recorded the edge.

### An unresolved id is shown, not hidden

`BeanRelations.unresolved` carries ids that name no bean in the repository. They
are rendered as text saying the bean is not in this repository. Hiding them would
mean the app quietly disagrees with the file on disk, and a shallow clone of a
repository mid-refactor will have them.

## Verification

Seven instrumented tests: frontmatter fields, unrecognised fields shown raw,
Markdown actually rendered, relationships listed and tappable, children derived
on the parent, a dangling reference shown without a link, and an unknown bean id.

The Markdown assertion uses Espresso rather than Compose's finders, because the
rendered text is in a `TextView` that Compose's semantics tree does not contain.
Asserting through Compose would have meant asserting on the Markdown source,
which would pass whether or not it rendered.
