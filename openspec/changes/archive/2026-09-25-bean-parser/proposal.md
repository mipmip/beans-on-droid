## Why

Bean `beans-on-droid-37n3`. `docs/bean-format.md` describes the format and the
fixtures exercise it, but nothing reads a bean yet. This is the layer the whole
app sits on: if it drops a field or dies on one bad file, every screen above it
is wrong.

## What Changes

- Add a `Bean` model holding the typed frontmatter fields, the body, the id and
  slug taken from the filename, an archived flag, and an `extras` map for
  frontmatter keys the model does not recognise.
- Add `BeanParser`, the only code in the app that knows the file format. It
  splits frontmatter from body, parses the YAML, and maps it onto `Bean`.
- Accept a scalar where the model expects a list, and treat every field as
  optional.
- Report a file whose frontmatter cannot be parsed as a skip with a reason,
  rather than throwing.
- Add SnakeYAML as the YAML parser.

## Capabilities

### New Capabilities

- `bean-parsing`: turning a bean file on disk into a `Bean`, including which
  files are readable, what is kept, and what happens to a broken one.

### Modified Capabilities

None.

## Impact

- New: `bean/Bean.kt`, `bean/BeanParser.kt`, `bean/BeanId.kt` and their tests.
- New dependency: `org.yaml:snakeyaml` (Apache-2.0, FOSS).
- This package is subject to the 80 percent coverage rule.
