## Context

Two beans, raised after running the app against the real `hmans/beans`
repository. They look like separate features and are one problem: the list is a
flattened, wrongly sorted copy of the list the tool itself prints.

Everything below was checked against the upstream source and against the tool's
actual output, not inferred.

## What the evidence says

### The current ordering is a defect

`BeanIndex.ORDERING` is `order.isEmpty()`, then `order`, then `title`, then
`id`. `pkg/bean/sort.go` is:

```
1. status      in-progress, todo, draft, completed, scrapped
2. order       beans that have one first, then lexicographic
3. priority    critical, high, normal, low, deferred   (absent counts as normal)
4. type        milestone, epic, bug, feature, task
5. title       case-insensitive
```

The app has levels 2 and 5 and none of 1, 3 or 4. Level 1 is the one that
matters: sorting by a fractional index first interleaves every status. On the
upstream repository the app's first four rows are draft, todo, todo, todo, where
the tool leads with three in-progress beans.

`order` is the tool's manual drag key. It is meaningful inside a status column
on a board. As a primary key in a flat list it is noise.

### The tool's own answer to sort against tree

Sorting and nesting compete for the same axis, and the tool has already decided:

```
beans list                  tree, with box-drawing characters
beans list --sort updated   flat
beans list --sort priority  flat
```

An explicit sort collapses the tree. Adopting that costs nothing, matches what a
beans user already expects, and removes the need to define what "sorted tree by
update time" would even mean.

Search flattens for the same reason, by analogy rather than by precedent: a
search result is a set of matches, and nesting implies a structure the matches
do not have.

### The data is shallow and mostly flat

Measured over the 115 active beans upstream:

| | count |
|---|---|
| roots with children | 5 |
| children | 32 |
| standalone | 78 |
| maximum depth | 2 |

No parent is itself a child. Two thirds of the list is flat whatever the tree
does, and the tool interleaves standalone beans with parents at the top level
rather than giving orphans their own section. The spec follows that.

The format permits arbitrary depth, so the implementation must handle it even
though nobody uses it, and must terminate on a cycle. A malformed repository
where two beans name each other as parent has to produce a list, not a hang.

## Decisions

### Sorting moves into the query, ordering stays in the index

`BeanQuery` gains a sort field and a direction. `BeanIndex` keeps owning the
comparators. The alternative, sorting in the view model, would put the
configured status and priority orders in the UI layer, which is exactly the
spreading-out the index exists to prevent.

`BeanIndex.all` is currently pre-sorted once at construction. With a selectable
sort that property is gone: the default order is precomputed, and a non-default
sort is applied at query time. At 600 beans a sort costs well under a frame, and
the measured search cost of 2 ms is the evidence that a per-query pass is
affordable.

### The vocabulary extends the CLI's, and says so

The CLI offers `created`, `updated`, `status`, `priority`, `id` and the default.
This change offers all of those plus `title` and `type`, plus a direction.

Two deliberate divergences:

- **`title` and `type`.** The CLI has neither, and both are obvious things to
  want on a phone where you are scanning rather than scripting.
- **Direction.** The CLI has no direction flag; `created` and `updated` are
  hardwired to newest first. Keeping that as the default for time fields
  preserves the familiar behaviour, and the toggle makes oldest-first reachable
  without a second menu entry.

`id` is kept for parity even though a nanoid ordering is close to meaningless,
because removing it would be a silent divergence where these two are stated
ones.

### Missing values sort last, in both directions

A bean with no `created_at` goes after every bean that has one, and stays there
when the direction is reversed. The alternative, letting absent values flip to
the top under descending, would make a repository with sparse timestamps look
different every time the arrow is tapped. This mirrors what the CLI does with
nil timestamps.

### Ties break on id

The default comparator ends on title, which is not unique. Every other sort ends
on id, which is. Without that, two beans with the same `updated_at` could swap
places between refreshes and the list would appear to shuffle on its own.

### A filtered-out parent becomes a context row

Three options were considered:

| Option | Cost |
|---|---|
| Promote orphaned children to the top level | Loses the grouping exactly when a filter has made the list hardest to read |
| Hide children whose parent is hidden | A filter would silently drop matching beans, which is a lie |
| Keep the parent as a context row | Extra state in the result, and a row that is visible but not a match |

The third is the only one that keeps the filter honest and the structure
intact. The cost is real and bounded: the index has to return which top-level
entries are context, and the count has to ignore them. The spec pins both,
because a context row counted as a match would make "2 of 10" wrong in a way
nobody would notice quickly.

A context row is still openable. It is a real bean, and refusing to open it
would be surprising.

### Priority becomes visible

The list shows status and type and not priority, so the third key of the default
comparator is invisible and the resulting order looks arbitrary even once it is
correct. The tool prints `!` before a raised title and a downward arrow before a
lowered one, and shows nothing for normal.

Marking only the ends of the scale keeps the common case clean: 108 of 115
upstream beans are `normal`, so a marker on every row would be noise on 94
percent of them.

### The sort is remembered per repository

`RepoConfig` already exists per repository and is already persisted as part of
`RepoList`. A sort preference belongs there rather than in a global setting,
because a repository of milestones and a repository of bugs want different
defaults, and switching between them should not mean re-choosing.

This grows `RepoList`'s serialised shape. It is decoded with
`ignoreUnknownKeys`, and a stored list written before this change simply has no
sort recorded, so an absent value has to mean the default rather than an error.

## Risks

- **The tree plus filters plus search plus a sort is four interacting states.**
  The spec pins the interactions rather than leaving them to emerge, and the
  combinations that matter (sort flattens, search flattens, filter produces
  context rows) each get a scenario. The risk is the combination nobody wrote
  down, which is why the flattening rule is stated once as a rule rather than
  per case.
- **Every existing ordering test changes**, as do the committed screenshots.
  That is the point of the change, but it means a large diff in which a genuine
  regression could hide. The ordering scenarios are written against the tool's
  documented precedence so they can be checked against `beans list` output
  directly.
- **Collapsing groups is deliberately out of scope.** At five groups and 115
  beans the list scrolls fine, and collapse state would have to survive filters,
  sorts, refreshes and rotation. It is worth its own bean once the tree exists
  and it is clear whether anyone wants it.
