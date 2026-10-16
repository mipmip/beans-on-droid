## Why

GitHub issue [#1](https://github.com/mipmip/beans-on-droid/issues/1). This
change has no originating bean: it comes from the project's issue tracker, not
from `.beans/`.

The detail screen shows a bean's id as inert text, but the id is the handle you
need everywhere else. It is what you type into an agent session, a
`beans show` command, or an OpenSpec change to point at this one bean. Today
you read it off the screen and retype it, and a four-character id like
`rn3b` is exactly the kind of thing that gets mistyped.

## What Changes

- The bean id in the detail header becomes a tappable chip carrying a copy
  icon. Tapping it puts the bare id on the clipboard.
- A snackbar confirms the copy, so the action is acknowledged on every
  supported Android version, including the ones that show no system clipboard
  overlay.
- Nothing else on the detail screen changes, and no other screen gains a copy
  affordance.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `bean-detail`: the id shown in the header becomes copyable.

## Non-goals

- No copy affordance on the bean list or any other screen.
- No copying of anything but the id: not the title, the slug, the body, or the
  file path.
- No command or path is assembled for the clipboard. The payload is the bare id
  exactly as the file names it.
- No share intent, no clipboard history, no Phase 2 write behaviour.

## Impact

- Modified: `ui/screen/BeanDetailScreen.kt`.
- No new dependencies. The chip, the snackbar and the copy icon all come from
  Material 3 and `material-icons-extended`, both already on the classpath and
  both FOSS (Apache-2.0).
