## Why

GitHub issue [#2](https://github.com/mipmip/beans-on-droid/issues/2). This change
has no originating bean: it comes from an issue, not from a bean in `.beans/`.

Bean bodies name other beans by id in their prose. Today those ids are inert.
Nothing marks them as pointers, nothing follows them, and nothing says whether
the bean they name is still open, so a reader has to remember the id and search
for it.

## What Changes

- A bean's Markdown body recognises ids that the repository has, and shows each
  one as a mention: tinted, monospace, and tappable.
- Choosing a mention opens that bean, through the same navigation the
  relationship links already use.
- A mention of a bean whose status is closed (completed or scrapped) is struck
  through and dimmed.
- An id-shaped token that no bean in the repository has stays plain text and is
  not tappable, in line with the rule that a relationship to a missing bean is
  not offered as a link.
- `BeanIndex` gains an id membership test, so a body mention can only be
  recognised by asking the index.

### Non-goals

- No change to ids anywhere outside a body. The list rows, the header chip and
  the relations section are untouched.
- No mention parsing in titles, slugs, tags, extras or frontmatter values.
- No mention syntax of its own: no `[[id]]`, no `@id`, no command and no file
  path. A mention is a bare id that the repository has.
- No backlinks, no "referenced by" section, no mention counts, no link preview.
- No dependency is introduced. The body is already rendered by Markwon 4.6.2
  (Apache-2.0, FOSS), whose core plugin already sets the movement method that
  makes spans clickable.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `bean-detail`: a body shows the ids of beans the repository has as mentions
  that open the mentioned bean, and a mention of a closed bean is struck through.
- `bean-index`: an id can be tested for membership without being resolved into a
  relationship.

## Impact

- Modified `app/src/main/kotlin/io/github/mipmip/beansondroid/ui/Markdown.kt`:
  the body renderer takes the id lookup and the open callback and wraps each
  recognised id in a span.
- Modified `app/src/main/kotlin/io/github/mipmip/beansondroid/ui/screen/BeanDetailScreen.kt`:
  passes the index and its existing open-bean callback to the body renderer.
- Modified `app/src/main/kotlin/io/github/mipmip/beansondroid/index/BeanIndex.kt`:
  membership test over the ids the index holds.
- Modified `app/src/main/kotlin/io/github/mipmip/beansondroid/ui/BeanVisuals.kt`:
  the closed-status grouping the status colours already use becomes a named
  predicate the mention styling shares.
- New: the scan that finds id runs in a rendered body, kept free of Android
  classes so it is unit tested on the JVM.
- Modified instrumented tests in
  `app/src/androidTest/kotlin/io/github/mipmip/beansondroid/ui/BeanDetailScreenTest.kt`:
  mention styling and following a mention from the body.
- Dependencies: none added. Markwon and the Compose interop already ship in the
  build, and both are FOSS.
