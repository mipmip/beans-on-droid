## Context

See `proposal.md` - Why. This covers how.

The detail header (`ui/screen/BeanDetailScreen.kt`) already shows the id, as a
plain `Text` inside a `FlowRow` that also carries the status, type and archived
pills. So the id is on screen and inert.

Two pieces of prior art exist in the codebase. `RepoScreen.kt` already resolves
the platform `ClipboardManager` (it reads from the clipboard when filling the
add-repository field), so the service is a known quantity. Nothing in the app
writes to the clipboard yet, and nothing shows a snackbar yet, so the feedback
side is new. `material-icons-extended` is already a dependency.

`minSdk` is 26. Android only began showing its own clipboard confirmation
overlay in API 33, so on 26 through 32 a copy with no app-provided feedback is
silent.

## Goals / Non-Goals

**Goals:**

- One tap copies the bean's id from the detail screen.
- The copy is acknowledged on every supported Android version, not just 33+.

**Non-Goals:**

- Any copy affordance outside the detail screen.
- Composing a command, a path, or an agent mention for the clipboard.
- Share intents, clipboard history, or any Phase 2 write behaviour.

## Decisions

### The id becomes a chip, not a bar action or a long-press

Three placements were on the table. An inline icon button beside the id text
keeps the affordance next to its subject but crowds a `FlowRow` that already
holds the pills, and leaves a small tap target. A `TopAppBar` action is the most
idiomatic Android placement, but the bar's title is the bean *title*, so an icon
there would sit one element away from the thing it copies. A long-press on the
id text changes nothing visually and is undiscoverable.

A chip carries its own icon, reads as "this id is a thing you can take", and
puts the affordance on the id itself.

### The payload is the bare id

The alternatives were a ready-to-paste `beans show <id>`, the file path, or an
agent mention such as `@.beans/<id>-*.md`. Each of those encodes an assumption
about the tool at the other end. The bare id is what every one of those strings
is built from, so it is the most composable payload, and it survives the id being
used somewhere this app never anticipated.

### A snackbar, not a toast and not silence

Silence is defensible only above API 33, where the system draws its own overlay,
so it fails the goal on 26 through 32. A `Toast` would work but is unthemed and
awkward to assert in the Compose test setup this project uses. A Material 3
`SnackbarHost` is in-theme, brief, and assertable, so it is the choice.

### The affordance is named for assistive tech

The copy icon gets a content description naming the action, in the same shape as
the existing "Related beans" and "Parent x-0001" descriptions, so it is reachable
by a screen reader and by the existing instrumented tests.

## Risks / Trade-offs

- [Adding a `SnackbarHost` changes the detail `Scaffold`'s structure] → Keep the
  snackbar in the scaffold's `snackbarHost` slot and cover it with a test that
  asserts the confirmation appears.
- [On API 33+ the system overlay and the snackbar both appear] → Accepted. The
  system overlay is transient and out of our control; showing our own
  consistently is simpler and keeps behaviour uniform across versions.
- [Clipboard reads in tests need window focus] → The instrumented Compose test
  runs with a focused activity, the same way the existing paste test drives the
  clipboard, so reading back what the app wrote is reliable.

## Open Questions

None.
