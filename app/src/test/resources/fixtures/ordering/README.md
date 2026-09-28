# Ordering oracle

`beans-list-order.tsv` is the output of `beans list --json` against
[hmans/beans](https://github.com/hmans/beans) at commit
`99260bf1a6bec3e395b629406b737f6418653a17`, flattened to
`id, status, order, priority, type, title` and kept **in the order the tool
emitted it**.

The tool sorts with `pkg/bean/sort.go`. The row order in this file is therefore
the answer, and `BeanOrderingOracleTest` checks that sorting these 254 rows with
this app's comparator reproduces it exactly.

Regenerate with:

```bash
beans list --json | python3 -c '...'   # see the test for the field order
```

Upstream is Apache-2.0, the same licence as this project.
