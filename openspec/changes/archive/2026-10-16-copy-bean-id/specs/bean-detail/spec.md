## ADDED Requirements

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
