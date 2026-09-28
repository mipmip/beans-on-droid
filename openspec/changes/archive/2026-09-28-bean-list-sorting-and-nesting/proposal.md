## Why

Beans `beans-on-droid-r1vj` (sorting on regular fields) and
`beans-on-droid-4wzn` (show nesting of issues), both raised after using the app
against a real repository.

They are one picture. The bean list is a flattened, wrongly sorted version of
the list the beans tool itself prints, and the two beans are the two halves of
that gap.

The sorting half is a defect, not a preference. `BeanIndex` sorts by the `order`
field first. That field is the tool's manual drag key, a fractional index like
`Vy` or `zzzV`, and `pkg/bean/sort.go` never uses it as a primary key: it is the
second tiebreak inside a status group. Sorting by it first interleaves
in-progress, todo, draft and completed beans, so the list reads as random. On
the real `hmans/beans` repository the first rows come out as draft, todo, todo,
todo, where the tool shows in-progress first.

The nesting half is missing structure. 32 of the 115 active beans upstream
declare a parent, and the index already computes the child map, but the list
throws it away.

## What Changes

- Replace the index's ordering with the tool's comparator: status, then manual
  order, then priority, then type, then title. Absent priority counts as
  `normal`; unrecognised values sort last within their category.
- Add a sort selection to the query: the CLI's `created`, `updated`, `status`,
  `priority` and `id`, plus `title`, plus a direction toggle.
- Group the list as a tree under the default sort: roots in comparator order,
  children indented beneath their parent, beans with no parent interleaved with
  the roots.
- Flatten the tree whenever an explicit sort or a search term is active, which
  is what the CLI does.
- Keep a parent that a filter excluded as a dimmed context row when any of its
  children still match, so the grouping survives filtering without the count
  claiming the parent as a match.
- Show priority in a row, which the list currently omits entirely.
- Remember the sort choice per repository.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `bean-index`: the ordering requirement is replaced, and sorting becomes part
  of the query rather than a fixed property of the index. The index also gains
  the ability to return its results as a tree.
- `bean-list`: the list gains a sort control, tree grouping, a priority
  indicator, and rules for what filtering does to a tree.

## Impact

- Modified: `index/BeanIndex.kt`, `index/BeanQuery.kt`, `ui/screen/BeanListScreen.kt`,
  `viewmodel/AppViewModel.kt`, `store/` for the remembered sort.
- No new dependencies.
- Both packages are under the 80 percent coverage rule.
- Existing ordering tests and the screenshots will both change.
