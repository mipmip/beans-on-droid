## 1. The default comparator

- [x] 1.1 Add the configured status, priority and type orders as a single source
      of truth, matching `docs/bean-format.md`
- [x] 1.2 Replace `BeanIndex.ORDERING` with status, order, priority, type, title
- [x] 1.3 Treat an absent priority as `normal`
- [x] 1.4 Sort an unrecognised status, priority or type last within its category
      without dropping the bean
- [x] 1.5 Tests for each level of the precedence, and for the unrecognised cases
- [x] 1.6 Check the resulting order against `beans list` on the upstream
      repository

## 2. Selectable sort

- [x] 2.1 `BeanSort` naming the fields: default, created, updated, status,
      priority, type, title, id
- [x] 2.2 Direction, defaulting to descending for time fields and ascending
      otherwise
- [x] 2.3 Add sort and direction to `BeanQuery`, defaulting to the default
      comparator
- [x] 2.4 Apply the sort at query time, keeping the default order precomputed
- [x] 2.5 Order beans missing the sorted field last, in both directions
- [x] 2.6 Break every non-default tie on id
- [x] 2.7 Tests per field, per direction, for missing values and for stability

## 3. Tree

- [x] 3.1 A tree result type carrying rows with their depth, and which top-level
      entries are context
- [x] 3.2 Build the tree from a query result: unparented matches at the top
      level, children beneath, ordered within each level
- [x] 3.3 Handle a parent that is not in the repository by placing the bean at
      the top level
- [x] 3.4 Handle depth beyond two
- [x] 3.5 Terminate on a parent cycle
- [x] 3.6 Report an excluded parent as context, excluded from the match count
- [x] 3.7 Tests for nesting, orphans, ordering within a level, depth, cycles,
      context rows and the count

## 4. List screen

- [x] 4.1 Sort control in the top bar showing the active sort and direction
- [x] 4.2 Indent nested rows under the default sort
- [x] 4.3 Flatten when a sort other than the default is active
- [x] 4.4 Flatten when a search term is present, and nest again when it is
      cleared
- [x] 4.5 Render a context row so it reads as context rather than as a match
- [x] 4.6 Keep a context row openable
- [x] 4.7 Keep the count reporting matches only

## 5. Priority in a row

- [x] 5.1 Show a marker for `critical` and `high`, and a different one for `low`
      and `deferred`
- [x] 5.2 Show nothing for `normal` or an absent priority
- [x] 5.3 Give each marker a content description, so it is not colour alone

## 6. Persistence

- [x] 6.1 Record the sort and direction on `RepoConfig`
- [x] 6.2 Restore them when a repository becomes active
- [x] 6.3 A stored list written before this change decodes with the default sort
- [x] 6.4 Tests for round-tripping, for two repositories differing, and for the
      older stored shape

## 7. Verification

- [x] 7.1 Instrumented: the default list is nested and status-ordered
- [x] 7.2 Instrumented: choosing a sort flattens and reorders
- [x] 7.3 Instrumented: searching flattens, clearing restores the tree
- [x] 7.4 Instrumented: a filter leaves a context row with the count unchanged
      by it
- [x] 7.5 Instrumented: the sort survives a restart
- [x] 7.6 Regenerate the F-Droid screenshots
- [x] 7.7 `./scripts/gate.sh` and `./scripts/e2e.sh` pass
