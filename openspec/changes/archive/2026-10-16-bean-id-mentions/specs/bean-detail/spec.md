## ADDED Requirements

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
