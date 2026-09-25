## Method

`https://github.com/hmans/beans` was cloned at commit `99260bf` (2026-04-06) and
read three ways, because each answers a different question:

1. `.beans.yml` and the 115 live bean files in `.beans/`, for what the format
   looks like in practice and which fields actually occur.
2. `pkg/bean/bean.go`, for the authoritative field list and types. The struct
   tags are the contract; the live files are only a sample of it.
3. `pkg/bean/id.go` and `pkg/config/config.go`, for how the id is derived and
   what the default statuses, types and priorities are.

Reading the source mattered. Two fields in the model (`blocking`, and `order` on
most beans) are sparsely used in the live repository, and one (`blocking`) does
not occur at all there. A parser written from the sample alone would not have
handled them.

## Fixture selection

Fixtures are copied verbatim rather than written by hand, so the parser is tested
against bytes the real tool produced. The set covers, at minimum:

- a bean with only the required fields
- a bean with `tags`
- a bean with `blocked_by`
- a bean with `parent` and `order`
- an archived bean, taken from `.beans/archive/`
- a bean whose body contains substantial Markdown, including headings, lists and
  fenced code

Synthetic fixtures are added separately for the cases the real repository cannot
provide: malformed frontmatter, an empty body, and unknown fields. Those live
beside the real ones but in their own directory so the distinction stays visible.

## Provenance and licensing

`hmans/beans` is Apache-2.0, the same license as this project, so copying bean
files into the test resources is fine. `docs/bean-format.md` records the source
repository, the commit and the license, and the fixture directory carries a
README saying the same.

## Decision: unknown fields are kept

The briefing says never to drop a field. The parser therefore keeps every
frontmatter key it does not map to a typed field in an `extras` map, and the
detail screen shows them raw. This is recorded here because it constrains the
`Bean` model that `beans-on-droid-37n3` will define.
