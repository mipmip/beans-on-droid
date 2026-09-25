## Purpose

Reading a single bean: its fields, its body as the author wrote it, and the
beans it is connected to. It exists so that nothing in a bean file is invisible
in the app, including the parts this app was not written to understand.

## ADDED Requirements

### Requirement: Frontmatter is shown

The system SHALL show the bean's id, title, status, type, priority, tags, order,
timestamps and slug, omitting the ones the bean does not set.

#### Scenario: A bean using several fields

- **WHEN** a bean with a priority, tags and a created timestamp is opened
- **THEN** each of those values is shown

#### Scenario: A field the bean does not set

- **WHEN** a bean has no priority
- **THEN** no empty priority row is shown

### Requirement: Unrecognised fields are shown raw

The system SHALL display frontmatter keys it does not map to a known field,
under a heading that distinguishes them, with their values as text.

#### Scenario: A bean with an extra field

- **WHEN** a bean declares a field this app does not know
- **THEN** the field's name and value are both shown

### Requirement: The body is rendered as Markdown

The system SHALL render the bean's body as Markdown rather than as plain text.

#### Scenario: Headings and emphasis

- **WHEN** a body contains a heading and bold text
- **THEN** they are rendered rather than shown with their markup

#### Scenario: Empty body

- **WHEN** a bean has no body
- **THEN** the screen shows its fields without an empty body area

### Requirement: Relationships are followable

The system SHALL list the bean's parent, children, blocking and blocked-by
relationships, and SHALL open the related bean when one is chosen.

#### Scenario: Parent and children

- **WHEN** a bean with a parent is opened
- **THEN** the parent is listed as a link
- **AND** opening the parent lists this bean as a child

#### Scenario: Following a link

- **WHEN** a related bean is chosen
- **THEN** that bean's detail view is opened

#### Scenario: Bean with no relationships

- **WHEN** a bean has no relationships
- **THEN** no relationship section is shown

### Requirement: A relationship to a missing bean degrades gracefully

The system SHALL show a relationship naming a bean that is not in the repository
as plain text saying so, and SHALL NOT offer it as a link.

#### Scenario: Dangling reference

- **WHEN** a bean is blocked by an id that no bean in the repository has
- **THEN** the id is shown with a note that it is not in this repository
- **AND** it is not tappable

### Requirement: Opening a bean that is not there

The system SHALL explain when the requested bean is not in the repository and
SHALL offer a way back.

#### Scenario: Unknown id

- **WHEN** the detail view is opened for an id the repository does not have
- **THEN** the screen says so and offers to go back
