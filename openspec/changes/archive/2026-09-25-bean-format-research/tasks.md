## 1. Research

- [x] 1.1 Clone `hmans/beans` and record the commit
- [x] 1.2 Read `.beans.yml` and survey which frontmatter keys occur across the
      live bean files
- [x] 1.3 Read `pkg/bean/bean.go` for the authoritative field list and types
- [x] 1.4 Read `pkg/bean/id.go` for how the id and slug are derived
- [x] 1.5 Read `pkg/config/config.go` for the default statuses, types and
      priorities

## 2. Document

- [x] 2.1 Write `docs/bean-format.md`: file layout, filename forms, every
      frontmatter field with its type and whether it is optional
- [x] 2.2 Document the relationship fields and their direction
- [x] 2.3 Document the archive directory
- [x] 2.4 Document the default statuses, types and priorities
- [x] 2.5 State the handling rule for unrecognised fields

## 3. Fixtures

- [x] 3.1 Copy real bean files covering the cases listed in the design into
      `app/src/test/resources/fixtures/real/`
- [x] 3.2 Add synthetic fixtures for malformed frontmatter, an empty body and
      unknown fields under `app/src/test/resources/fixtures/synthetic/`
- [x] 3.3 Add a fixture README recording provenance, commit and license
