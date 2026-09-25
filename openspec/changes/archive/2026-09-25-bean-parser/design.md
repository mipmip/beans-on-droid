## Context

`docs/bean-format.md` is the input to this change. The open questions are how to
represent what was read and how to fail.

## Decisions

### `Bean` is a plain data class with an `extras` map

Typed fields for the eleven documented ones, plus
`extras: Map<String, String>` for everything else. Values in `extras` are
rendered to text at parse time rather than kept as `Any?`, because the only
consumer is a detail screen that shows them raw, and a stringly-typed map is far
easier to test and to display than a tree of unknown YAML nodes.

### Parsing returns a result type, never throws

`BeanParser.parse` returns `ParseResult`, either `Parsed(bean)` or
`Skipped(filename, reason)`. The spec requires one bad file not to take the index
down, and a result type makes that the default rather than something every caller
has to remember to wrap in a try.

### Timestamps are parsed leniently

`created_at` and `updated_at` are RFC 3339 in every observed file, but the field
is optional and the format is not guaranteed. They are parsed into `Instant`
when they can be, and a value that will not parse lands in `extras` rather than
failing the file. The display layer can then still show it.

### SnakeYAML, not kaml or SnakeYAML Engine

`org.yaml:snakeyaml` is Apache-2.0, targets Java 8 bytecode and runs on Android
without desugaring tricks. kaml would mean adding kotlinx-serialization for one
map, and SnakeYAML Engine only supports YAML 1.2 while bean files are written by
a Go YAML 1.1 library. SnakeYAML is loaded with a `SafeConstructor`, so a
frontmatter document cannot instantiate arbitrary classes.

### Frontmatter is split by hand, not by a library

The split is three lines of string handling: a leading `---`, the next `---` on
its own line, and the rest. Pulling in a frontmatter library for that would add a
dependency to own a rule the spec states directly, and the "a body may contain
`---`" scenario is easier to guarantee when the split is explicit.

### Filename parsing follows upstream's order exactly

Double dash, then dot, then single dash, then whole name. The order is not
arbitrary: ids carry a project prefix containing hyphens, so trying the single
dash form first would truncate every id in this very repository.

## Risks

- SnakeYAML's `SafeConstructor` rejects some exotic YAML that Go's parser
  accepts. Such a file becomes a skip rather than a crash, which is the
  specified behavior, and the reason is shown to the user.
