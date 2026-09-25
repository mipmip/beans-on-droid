## Context

The index is built once per clone or refresh and then queried on every keystroke
in the search box. It is in memory and the briefing's target is a few hundred
beans, so the shape of the data matters more than clever algorithms.

## Decisions

### Built once, immutable afterwards

`BeanIndex(beans)` precomputes the id lookup, the children map, the union of
blocking edges in both directions, and the facet lists. Query time then does no
graph work. A refresh builds a new index rather than mutating the old one, which
removes every question about partially updated state in the UI.

### Relationship edges are unioned, not trusted

`docs/bean-format.md` records that `blocking` and `blocked_by` are stored
independently and need not mirror each other. The index therefore builds the
blocking graph from both directions and deduplicates. A reader that trusted only
the explicit field would show a different answer depending on which bean it was
looking from.

### Unresolved ids are reported, not dropped

A relationship naming a bean that is not in the set is returned as an unresolved
id alongside the resolved beans. Silently dropping it would hide a real thing in
the user's repository; faking a bean would be worse. The detail screen shows it
as a plain id with no link.

### Search is a case-insensitive substring over title, body and id

Not tokenised, not fuzzy. A few hundred beans is small enough that a linear scan
per keystroke is imperceptible, and substring matching is what someone typing a
bean id or a word from a title expects. Including the id means pasting an id
into the search box finds the bean, which is the most common way one bean leads
to another.

### Ordering mirrors the beans tool

`order` is an opaque lexicographic key. Beans without one sort last rather than
first, because an absent order means "not deliberately placed" and those belong
at the end. Title is the tiebreaker so the list is stable across refreshes.

## Risks

- A linear scan per keystroke is fine at a few hundred beans and would not be at
  fifty thousand. The performance epic measures it at the size the briefing
  states; if the number ever moves, this is the place to revisit.
