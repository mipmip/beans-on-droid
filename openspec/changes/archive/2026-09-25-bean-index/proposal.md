## Why

Bean `beans-on-droid-64w9`. The parser produces one bean at a time. Every screen
needs the set: filtered by status, type and tag, searched by text, sorted the way
the beans tool sorts, and with relationship ids resolved to real beans so the
detail screen can offer links. Doing that ad hoc in each view model would spread
the format's rules back out across the app.

## What Changes

- Add `BeanIndex`, built once from a list of parsed beans and queried after that.
- Add a `BeanQuery` describing a filter: statuses, types, tags, a text term, and
  whether archived beans are included.
- Resolve relationships: parent, children, blocking and blocked-by, unioned with
  the inverse relations found by scanning, since the two directions are stored
  independently and need not be mirrored.
- Sort by `order` lexicographically with beans lacking an order last, then by
  title.
- Expose the distinct statuses, types and tags present, so filter controls can be
  built from the data rather than from hardcoded lists.

## Capabilities

### New Capabilities

- `bean-index`: querying a set of beans by filter and text, and resolving the
  relationships between them.

### Modified Capabilities

None.

## Impact

- New: `index/BeanIndex.kt`, `index/BeanQuery.kt` and their tests.
- No new dependencies.
- This package is subject to the 80 percent coverage rule.
