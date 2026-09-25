## Why

Bean `beans-on-droid-eroh`. This is the screen the app exists for. A beans
repository can hold a few hundred issues, so a plain list is not enough: finding
one means filtering and searching, and knowing the list is current means being
able to refresh it.

## What Changes

- Replace the bean list stub with the real screen: every bean for the active
  repository, showing id, title, status and type.
- Add a search field over title, body and id.
- Add filter chips for the statuses, types and tags actually present in the
  repository, plus a toggle for archived beans.
- Show how many beans match out of the total, and how many files could not be
  read.
- Pull to refresh, which fetches and reindexes.
- Distinguish an empty repository from a search that matches nothing, and offer
  the right way out of each.

## Capabilities

### New Capabilities

- `bean-list`: finding a bean among a repository's beans.

### Modified Capabilities

None.

## Impact

- Modified: `ui/screen/BeanListScreen.kt`.
- New: shared `Pill` and `Message` components, and a status and type colour map
  matching the beans tool's own.
