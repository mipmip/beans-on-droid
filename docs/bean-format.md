# The bean file format

Derived from [hmans/beans](https://github.com/hmans/beans) at commit
`99260bf1a6bec3e395b629406b737f6418653a17` (2026-04-06), which tracks its own
issues in `.beans/`. Where the live files were ambiguous, the Go source was the
tiebreaker: `pkg/bean/bean.go`, `pkg/bean/id.go` and `pkg/config/config.go`.

Upstream is Apache-2.0, the same license as this project.

## Layout

```
<repo root>/
  .beans.yml            configuration
  .beans/               one Markdown file per bean
    <id>--<slug>.md
    archive/            beans moved out of the active set
      <id>--<slug>.md
```

`.beans.yml` may point `beans.path` somewhere other than `.beans`. A repository
with no such directory is not a beans repository, and the app must say so rather
than showing an empty list.

## The id comes from the filename

This is the single most important thing about the format. The id is **not** a
YAML field. `pkg/bean/bean.go` tags it `yaml:"-"`, and `ParseFilename` in
`pkg/bean/id.go` derives id and slug from the file name, trying four forms in
order:

| Form        | Example                        | Id          | Slug            |
|-------------|--------------------------------|-------------|-----------------|
| Double dash | `beans-5ucr--investigate.md`   | `beans-5ucr`| `investigate`   |
| Dot         | `beans-5ucr.investigate.md`    | `beans-5ucr`| `investigate`   |
| Single dash | `f7g-user-registration.md`     | `f7g`       | `user-registration` |
| No separator| `beansnoslug.md`               | `beansnoslug`| (empty)        |

Order matters. Ids contain the project prefix and prefixes contain hyphens, so
`beans-on-droid-88wd--spike.md` only parses correctly because the double-dash
form is tried before the single-dash one.

The first line inside the frontmatter is usually a YAML comment repeating the id:

```yaml
---
# beans-5ucr
title: Investigate Warp integration
```

It is a comment. A YAML parser drops it, and nothing should be read from it.

## Frontmatter fields

Every field is optional in practice; a file missing `title` still parses. Types
are from the Go struct.

| Field        | Type       | Notes                                              |
|--------------|------------|----------------------------------------------------|
| `title`      | string     | Display name                                        |
| `status`     | string     | One of the configured statuses                      |
| `type`       | string     | One of the configured types                         |
| `priority`   | string     | One of the configured priorities                    |
| `tags`       | string[]   | Lowercase, `[a-z][a-z0-9]*(-[a-z0-9]+)*`            |
| `created_at` | timestamp  | RFC 3339, UTC in practice                           |
| `updated_at` | timestamp  | RFC 3339, UTC in practice                           |
| `order`      | string     | Opaque sort key, lexicographic (`Vy`, `zzzV`)       |
| `parent`     | string     | Id of the parent bean                               |
| `blocking`   | string[]   | Ids this bean blocks                                |
| `blocked_by` | string[]   | Ids blocking this bean                              |

Observed frequency across the 115 live beans: `title`, `status`, `type`,
`created_at` and `updated_at` on all of them; `priority` on 108; `order` on 40;
`parent` on 32; `tags` on 13; `blocked_by` on 1; `blocking` on none.

`blocking` never appears in the sample. It exists in the model, so the parser
handles it. This is why the Go source was read rather than the files alone.

## Relationships

- `parent` is a single id pointing up a hierarchy: milestone, epic, feature,
  task or bug. Children are found by scanning for beans whose `parent` is this
  bean; there is no `children` field.
- `blocking` lists beans that cannot proceed until this one is done.
- `blocked_by` is the inverse.

The two blocking fields are stored independently and are not guaranteed to be
mirrored, so a reader that wants the full picture must union the explicit lists
with the inverse relations it finds by scanning.

## Body

Everything after the closing `---` is the body, as Markdown. Upstream trims one
trailing newline. An empty body is valid.

Bodies commonly contain task lists (`- [ ]`, `- [x]`) and, on completed beans, a
`## Summary of Changes` section.

## Sort order

`order` is an opaque lexicographic key, not a number. `Vy` sorts before `zzzV`.
Beans without an `order` sort after those with one. Upstream generates these keys
so a bean can be reordered without renumbering its siblings.

## Defaults

These are compiled into the tool, not read from `.beans.yml`
(`pkg/config/config.go`).

Statuses, in display order:

| Name          | Color  | Archives | Meaning                              |
|---------------|--------|----------|--------------------------------------|
| `in-progress` | yellow | no       | Currently being worked on            |
| `todo`        | green  | no       | Ready to be worked on                |
| `draft`       | blue   | no       | Needs refinement                     |
| `completed`   | gray   | yes      | Finished successfully                |
| `scrapped`    | gray   | yes      | Will not be done                     |

Types: `milestone`, `epic`, `bug`, `feature`, `task`.

Priorities, most to least urgent: `critical`, `high`, `normal`, `low`,
`deferred`. A bean with no priority sorts as `normal`.

## Unrecognised fields

Upstream may add fields, and a repository may carry fields this app has never
seen. The parser keeps every frontmatter key it does not map to a typed field in
an `extras` map, and the detail screen shows them raw. Nothing is dropped.

## Consequences for the parser

1. Read the id from the filename, never from the body.
2. Try the filename forms in the documented order.
3. Treat every frontmatter field as optional.
4. Accept a scalar where a list is expected, and vice versa, rather than failing.
5. Skip a file whose frontmatter does not parse; do not fail the whole index.
6. Keep unknown keys.
7. Read `.beans/archive/` as well, and mark those beans as archived.
