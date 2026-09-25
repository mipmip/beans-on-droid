## Purpose

Turning a bean file, as written by the `beans` tool into a git repository, into a
value this app can display. This capability owns the contract with an external
file format the app does not control, so it defines what is read, what is kept
and how a file the format does not cover is handled.

## ADDED Requirements

### Requirement: Identity comes from the filename

The system SHALL derive a bean's id and slug from its filename and SHALL NOT
read them from the file's contents.

#### Scenario: Double dash separates id from slug

- **WHEN** a file is named `beans-5ucr--investigate-warp-integration.md`
- **THEN** the bean's id is `beans-5ucr` and its slug is
  `investigate-warp-integration`

#### Scenario: Prefix containing hyphens

- **WHEN** a file is named `beans-on-droid-88wd--research-spike.md`
- **THEN** the bean's id is `beans-on-droid-88wd`, not `beans`

#### Scenario: Legacy single dash

- **WHEN** a file is named `f7g-user-registration.md`, with no double dash and
  no dot
- **THEN** the bean's id is `f7g` and its slug is `user-registration`

#### Scenario: Filename with no separator

- **WHEN** a file is named `beansnoslug.md`
- **THEN** the bean's id is `beansnoslug` and its slug is empty

#### Scenario: Comment in the frontmatter is ignored

- **WHEN** a file named `beans-aaaa--x.md` begins its frontmatter with the
  comment `# beans-zzzz`
- **THEN** the bean's id is `beans-aaaa`

### Requirement: Typed frontmatter fields are read

The system SHALL read `title`, `status`, `type`, `priority`, `tags`,
`created_at`, `updated_at`, `order`, `parent`, `blocking` and `blocked_by` from
the frontmatter, and SHALL treat every one of them as optional.

#### Scenario: A bean using every field

- **WHEN** a bean declares all of the listed fields
- **THEN** each one is available on the parsed bean with its declared value

#### Scenario: A bean using only some fields

- **WHEN** a bean declares only `title` and `status`
- **THEN** parsing succeeds and the fields not declared are empty or absent
  rather than causing an error

### Requirement: Unrecognised frontmatter fields are preserved

The system SHALL keep every frontmatter key it does not map to a typed field,
together with its value rendered as text, and SHALL make them available for
display.

#### Scenario: Unknown scalar and nested fields

- **WHEN** a bean declares `assignee`, `estimate` and a nested `custom_block`
- **THEN** all three appear among the bean's extra fields
- **AND** no typed field is affected

### Requirement: A scalar is accepted where a list is expected

The system SHALL accept a single scalar value for `tags`, `blocking` and
`blocked_by` and SHALL treat it as a list of one.

#### Scenario: Scalar tag

- **WHEN** a bean declares `tags: single-tag`
- **THEN** the bean's tags are exactly `["single-tag"]`

### Requirement: The body is everything after the frontmatter

The system SHALL treat all content after the closing frontmatter delimiter as
the bean's Markdown body, with at most one trailing newline removed.

#### Scenario: Bean with no body

- **WHEN** a bean's file ends immediately after the closing delimiter
- **THEN** parsing succeeds and the body is empty

#### Scenario: Body containing delimiters

- **WHEN** a body contains a line consisting of three dashes
- **THEN** that line is part of the body and does not truncate it

### Requirement: A malformed file is skipped, not fatal

The system SHALL report a file whose frontmatter cannot be parsed as a skipped
file with a reason, and SHALL NOT raise an error that prevents other files from
being read.

#### Scenario: Frontmatter that is not valid YAML

- **WHEN** a file's frontmatter is not valid YAML
- **THEN** the result is a skip carrying the filename and a reason

#### Scenario: File with no frontmatter at all

- **WHEN** a file in the bean directory has no frontmatter delimiters
- **THEN** the result is a skip carrying the filename and a reason

#### Scenario: One bad file among many

- **WHEN** a directory holds ten valid bean files and one malformed one
- **THEN** ten beans are parsed and one skip is reported

### Requirement: Archived beans are identified

The system SHALL read beans from the archive directory alongside the active
ones and SHALL mark them as archived.

#### Scenario: Bean under the archive directory

- **WHEN** a bean file is read from `archive/` inside the bean directory
- **THEN** the parsed bean is marked archived

#### Scenario: Bean outside the archive directory

- **WHEN** a bean file is read from the bean directory itself
- **THEN** the parsed bean is not marked archived
