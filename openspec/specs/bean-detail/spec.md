# bean-detail Specification

## Purpose
Reading a single bean: its fields, its body as the author wrote it, and the
beans it is connected to. It exists so that nothing in a bean file is invisible
in the app, including the parts this app was not written to understand.

## Requirements

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

### Requirement: The bean id can be copied

The system SHALL offer the bean's id on the detail screen as a value that can be
copied in one action, and SHALL place that id on the system clipboard when the
action is taken. The value copied SHALL be the bare id exactly as the bean file
names it, not the title, the slug, the file path or a command built from it.

#### Scenario: The id is shown and copyable

- **WHEN** the detail view is shown for a bean
- **THEN** the bean's id is visible
- **AND** a copy affordance for that id is offered

#### Scenario: Taking the id

- **WHEN** the copy affordance is chosen
- **THEN** the bean's id is on the system clipboard, exactly as the bean file names it

#### Scenario: The copy is acknowledged

- **WHEN** the copy affordance is chosen
- **THEN** a message identifies the id that was copied
- **AND** the message disappears on its own

#### Scenario: No id to copy

- **WHEN** the detail view is opened for an id the repository does not have
- **THEN** no copy affordance is offered

### Requirement: Ids the repository has are marked in the body

The system SHALL mark, in a bean's rendered body, each id that the repository
holds, and SHALL show it as a mention that is visually distinct from the text
around it. The system SHALL mark an id only where it stands alone as a word, and
SHALL leave a hyphenated word that no bean uses as an id as ordinary text.

#### Scenario: A body naming a bean that exists

- **WHEN** a bean's body contains the id of another bean in the repository
- **THEN** that id is shown as a mention
- **AND** the text around it is unchanged

#### Scenario: A word shaped like an id that no bean has

- **WHEN** a body contains a hyphenated word that no bean in the repository uses
  as an id
- **THEN** it is shown as ordinary body text
- **AND** it is not tappable

#### Scenario: A mention inside a table row

- **WHEN** a cell of a table in a body holds the id of a bean in the repository
- **THEN** that id is shown as a mention like any other

#### Scenario: A mention inside a code span

- **WHEN** a body holds the id of a bean in the repository inside a code span
- **THEN** that id is shown as a mention like any other

#### Scenario: The same id several times

- **WHEN** a body names the same bean more than once
- **THEN** each occurrence is shown as a mention

#### Scenario: A bean naming itself

- **WHEN** a bean's body contains its own id
- **THEN** that id is shown as a mention like any other

### Requirement: A mention opens the bean it names

The system SHALL open the detail view of the mentioned bean when a mention is
chosen, and SHALL NOT offer a mention an action other than that.

#### Scenario: Following a mention

- **WHEN** a mention in a body is chosen
- **THEN** the detail view of the bean that id names is opened

#### Scenario: A mention of a bean that cannot be shown

- **WHEN** a body names an id that no bean in the repository has
- **THEN** there is no mention to choose

### Requirement: A mention of a closed bean is shown as closed

The system SHALL show a mention whose bean has the status `completed` or
`scrapped` as struck through, and SHALL show a mention of any other bean,
including one whose status is absent, without a strikethrough.

#### Scenario: A mention of a closed bean

- **WHEN** a body names a bean whose status is completed or scrapped
- **THEN** that mention is struck through

#### Scenario: A mention of an open bean

- **WHEN** a body names a bean whose status is todo, in-progress or draft
- **THEN** that mention is not struck through

#### Scenario: A bean with no status

- **WHEN** a body names a bean that declares no status
- **THEN** that mention is not struck through

#### Scenario: A bean closed after the repository was refreshed

- **WHEN** a bean that a mention names is closed in the repository and the
  repository is read again
- **THEN** the mention is struck through
