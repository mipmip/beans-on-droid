# Parser fixtures

## `real/`

Bean files copied verbatim from [hmans/beans](https://github.com/hmans/beans) at
commit `99260bf1a6bec3e395b629406b737f6418653a17` (2026-04-06), which tracks its
own issues in `.beans/`. Upstream is Apache-2.0, the same license as this
project.

They are copied rather than rewritten so the parser is tested against bytes the
real tool produced. Do not tidy them. If a fixture looks odd, that is the point.

The set covers a bean with only the common fields, one with `tags`, one with
`blocked_by`, one with `parent` and `order`, one taken from `.beans/archive/`,
and one with a long Markdown body.

## `synthetic/`

Cases the upstream repository does not contain, written by hand: unknown
frontmatter fields, an empty body, frontmatter that does not parse, a file with
no frontmatter, a scalar where the model expects a list, and a filename with no
slug.

See [docs/bean-format.md](../../../../../../docs/bean-format.md) for the format
these fixtures exercise.
