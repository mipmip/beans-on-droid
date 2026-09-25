## Why

Bean `beans-on-droid-obp8`. The list can find a bean but not show it. Reading a
bean means its body rendered as Markdown rather than as raw text, every
frontmatter field visible including the ones this app does not understand, and
its relationships turned into something you can follow.

## What Changes

- Replace the detail stub with the real screen.
- Render the body as Markdown with Markwon, including tables, strikethrough and
  task lists, which bean bodies use.
- Show the frontmatter fields, and show unrecognised fields raw under their own
  heading.
- List parent, children, blocking and blocked-by as tappable links, and show a
  relationship pointing at a bean that is not present as plain text.
- Handle being opened on a bean id that is not in the repository.

## Capabilities

### New Capabilities

- `bean-detail`: reading one bean, and moving between related beans.

### Modified Capabilities

None.

## Impact

- Modified: `ui/screen/BeanDetailScreen.kt`.
- New: `ui/Markdown.kt`.
- New dependencies: Markwon core plus its tables, strikethrough and task list
  extensions (Apache-2.0), which bring commonmark-java (BSD-2). Both FOSS.
