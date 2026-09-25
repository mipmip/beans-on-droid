## Decisions

### Filters are built from the data, not hardcoded

`BeanIndex.facets` reports the statuses, types and tags actually present. The
chips are generated from that. A repository using custom statuses therefore gets
usable filters, and a repository using three of the five default statuses does
not show two chips that can only ever return nothing.

### The count is always on screen

"12 of 340" is the cheapest way to make a filter's effect legible, and it is the
thing that tells someone their search is too narrow before they conclude the bean
is missing. It also makes the instrumented tests assert on behavior rather than
on which rows happen to be visible in the viewport.

### Skipped files are reported in the list header

The parser is built to skip a malformed file rather than fail. That is only
defensible if the user can tell it happened, so the count of unreadable files
sits next to the bean count, in the error colour. Anything quieter would make
"skipped" indistinguishable from "not there".

### Three empty states, not one

No repository, a repository with no beans, and a filter that matches nothing are
three different problems with three different fixes. Each gets its own message
and its own action: add a repository, open the repository list, clear the
filters. Collapsing them into one "nothing to show" would leave the user to
work out which of the three they are in.

### Status and type colours mirror the beans tool

`pkg/config/config.go` assigns each status and type a colour. Using the same
mapping means someone who uses the CLI or the web UI reads the same signal here.
The values are approximations of the terminal colour names in Material terms,
not exact matches, because a terminal yellow is not a legible foreground on a
light surface.

### Search runs on every keystroke, unthrottled

`BeanIndex.query` is a linear scan and the target is a few hundred beans, so a
keystroke costs a pass over a list that fits comfortably in memory. Debouncing
would add latency to the common case to protect against a case the performance
epic will measure.

## Verification

Nine instrumented tests on an API 26 emulator, driving the real screen against a
repository cloned from a local git repository built by the test: listing,
searching by title and by body, a search with no match and recovering from it,
filtering by type, by type and status together, by tag, and opening a bean.
