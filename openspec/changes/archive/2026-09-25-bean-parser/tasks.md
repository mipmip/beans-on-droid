## 1. Model

- [x] 1.1 `Bean` data class with the eleven typed fields, `id`, `slug`, `body`,
      `archived` and `extras`
- [x] 1.2 `ParseResult` as `Parsed` or `Skipped(filename, reason)`

## 2. Filename parsing

- [x] 2.1 `BeanId.parseFilename` implementing the four forms in upstream's order
- [x] 2.2 Tests: double dash, dot, legacy single dash, no separator, prefix
      with hyphens

## 3. Parser

- [x] 3.1 Split frontmatter from body without a library, tolerating a body that
      contains `---`
- [x] 3.2 Parse the frontmatter with SnakeYAML using a safe constructor
- [x] 3.3 Map the typed fields, accepting a scalar where a list is expected
- [x] 3.4 Parse timestamps leniently, falling back to `extras`
- [x] 3.5 Collect unrecognised keys into `extras` as rendered text
- [x] 3.6 Return `Skipped` with a reason for unparseable or frontmatter-less files
- [x] 3.7 Mark beans read from the archive directory as archived

## 4. Tests

- [x] 4.1 Parse every fixture under `fixtures/real/` and assert the expected
      fields, including the archived one
- [x] 4.2 Unknown fields fixture: `assignee`, `estimate` and `custom_block` are
      all in `extras`
- [x] 4.3 Empty body fixture parses with an empty body
- [x] 4.4 Malformed frontmatter and no-frontmatter fixtures produce `Skipped`
- [x] 4.5 Scalar-where-list fixture yields single-element lists
- [x] 4.6 A body containing a `---` line is not truncated
- [x] 4.7 One malformed file among many does not prevent the others parsing
- [x] 4.8 Coverage on `bean` is at or above 80 percent

## 5. Dependency

- [x] 5.1 Add `org.yaml:snakeyaml` to the version catalog and the app module
