## Purpose

Getting a repository URL into the app without typing it. This capability owns
what counts as a URL in arbitrary shared text, how a forge's page URL becomes a
clone URL, and the rule that a URL arriving from outside the app is a
suggestion rather than an instruction.

## ADDED Requirements

### Requirement: A web address is extracted from arbitrary text

The system SHALL find the first `http` or `https` address in a piece of text
and SHALL report that no address was found when there is none.

#### Scenario: Text surrounding a URL

- **WHEN** the text is `Look at this https://github.com/hmans/beans nice one`
- **THEN** the extracted address is `https://github.com/hmans/beans`

#### Scenario: Text that is only a URL

- **WHEN** the text is a bare address
- **THEN** that address is extracted

#### Scenario: Trailing punctuation

- **WHEN** an address is followed by a full stop, a comma or a closing bracket
- **THEN** that character is not part of the extracted address

#### Scenario: Several addresses

- **WHEN** the text contains more than one address
- **THEN** the first is extracted

#### Scenario: No address

- **WHEN** the text contains no `http` or `https` address
- **THEN** the result reports that none was found

### Requirement: A page URL is normalised to a clone URL

The system SHALL convert a forge page URL into the URL of the repository it
belongs to, by discarding the query string and the fragment, and by truncating
a path that continues past the repository into a view of it.

Truncation SHALL only consider path segments from the third onward, so that a
repository whose own name matches a view segment is not destroyed. A `.git`
suffix SHALL be left as it is and SHALL NOT be added.

#### Scenario: Query string from a repository page

- **WHEN** the URL is `https://github.com/hmans/beans?tab=readme-ov-file`
- **THEN** the normalised URL is `https://github.com/hmans/beans`

#### Scenario: Fragment

- **WHEN** the URL ends with a `#readme` fragment
- **THEN** the fragment is removed

#### Scenario: A file view

- **WHEN** the URL is `https://github.com/hmans/beans/tree/main/pkg`
- **THEN** the normalised URL is `https://github.com/hmans/beans`

#### Scenario: An issues page

- **WHEN** the URL is `https://github.com/hmans/beans/issues/12`
- **THEN** the normalised URL is `https://github.com/hmans/beans`

#### Scenario: GitLab separates the view with a marker

- **WHEN** the URL is `https://gitlab.com/group/subgroup/proj/-/tree/main`
- **THEN** the normalised URL is `https://gitlab.com/group/subgroup/proj`

#### Scenario: A Gitea source view

- **WHEN** the URL is `https://codeberg.org/owner/repo/src/branch/main`
- **THEN** the normalised URL is `https://codeberg.org/owner/repo`

#### Scenario: A repository named after a view segment

- **WHEN** the URL is `https://github.com/someone/issues`
- **THEN** the normalised URL is unchanged

#### Scenario: An address that already clones

- **WHEN** the URL is `https://github.com/hmans/beans.git`
- **THEN** it is unchanged, and no `.git` is added to one that lacks it

#### Scenario: Trailing slash

- **WHEN** the URL ends with a slash
- **THEN** the slash is removed

#### Scenario: A host the rules do not recognise

- **WHEN** the URL is `https://git.example.test/team/repo`
- **THEN** it is returned unchanged rather than guessed at

### Requirement: A captured URL is never acted on by itself

The system SHALL present a captured URL in the add form, filled in and editable,
and SHALL NOT clone, add or modify anything until the person confirms.

#### Scenario: A scanned code

- **WHEN** a QR code containing a repository URL is scanned
- **THEN** the add form opens with that URL filled in
- **AND** no repository has been added

#### Scenario: A shared URL

- **WHEN** a URL is shared into the app from another application
- **THEN** the add form opens with that URL filled in
- **AND** no repository has been added

#### Scenario: Correcting a normalised URL

- **WHEN** the normaliser produced the wrong URL
- **THEN** the field can be edited before confirming

#### Scenario: Captured text that is not a URL

- **WHEN** the captured text contains no web address
- **THEN** the person is told that no repository URL was found
- **AND** the form is left as it was

### Requirement: Credentials are never taken from a captured URL

The system SHALL NOT read an access token from a scanned code, a shared URL or
any other captured input, and SHALL NOT populate the token field from one.

#### Scenario: A captured URL carrying credentials

- **WHEN** a captured URL contains embedded credentials or a token parameter
- **THEN** the token field remains empty
- **AND** the credentials are not stored
