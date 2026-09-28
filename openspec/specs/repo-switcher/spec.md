# repo-switcher Specification

## Purpose
The screen where someone adds a repository, chooses which one they are reading,
and removes one they no longer want. It is the only place in the app that takes a
credential, so what it shows and what it hides are part of the contract.

## Requirements

### Requirement: The repository list shows its state

The system SHALL list every configured repository with its name and URL, SHALL
mark which one is active, and SHALL indicate which ones have a stored token
without revealing it.

#### Scenario: Active repository is marked

- **WHEN** repositories are listed
- **THEN** exactly one is shown as active

#### Scenario: Repository with a token

- **WHEN** a repository was added with a token
- **THEN** it is marked as using a token
- **AND** the token itself is not shown

#### Scenario: No repositories

- **WHEN** no repository has been added
- **THEN** the screen explains that one can be added with an HTTPS clone URL and
  that private repositories also need a token

### Requirement: Adding a repository

The system SHALL offer a form for a clone URL, an optional name and an optional
token, SHALL mask the token as it is typed, and SHALL add the repository when the
URL is valid.

The form SHALL offer ways to fill the URL without typing it: scanning a QR code
and pasting from the clipboard. It SHALL also be able to open already filled in,
when a URL was captured from outside the app.

#### Scenario: Valid repository

- **WHEN** a reachable repository's URL is submitted with a name
- **THEN** it appears in the list under that name
- **AND** it becomes the active repository

#### Scenario: Token is masked

- **WHEN** a token is typed into the form
- **THEN** its characters are not displayed

#### Scenario: Pasting a URL

- **WHEN** the clipboard holds a repository URL and paste is chosen
- **THEN** the URL field is filled with it

#### Scenario: Pasting text around a URL

- **WHEN** the clipboard holds text with a URL inside it
- **THEN** the URL field is filled with the URL alone

#### Scenario: Opening already filled in

- **WHEN** a URL has been captured from outside the app
- **THEN** the form is shown with that URL in place, awaiting confirmation

### Requirement: The URL is checked before the network

The system SHALL reject an invalid clone URL with an explanation and SHALL NOT
contact the network or add a repository in that case.

#### Scenario: SSH URL

- **WHEN** an SSH URL is submitted
- **THEN** the form explains that SSH is not supported and to use the HTTPS URL
- **AND** no repository is added

### Requirement: A clone in progress is visible

The system SHALL indicate that a clone is running and SHALL prevent the form
being submitted or dismissed again until it finishes.

#### Scenario: While cloning

- **WHEN** a clone is running
- **THEN** the screen says so
- **AND** the confirm and cancel actions are unavailable

#### Scenario: After a failed clone

- **WHEN** a clone fails
- **THEN** the reason is shown in the form
- **AND** the URL that was typed is still there to correct

### Requirement: Switching the active repository

The system SHALL make a repository active when it is chosen, and the beans shown
elsewhere SHALL be that repository's.

#### Scenario: Choosing another repository

- **WHEN** a repository that is not active is chosen
- **THEN** it becomes the active one

### Requirement: Removing a repository is confirmed

The system SHALL ask for confirmation before removing a repository and SHALL say
that the local copy and any token are deleted while the server is untouched.

#### Scenario: Cancelling

- **WHEN** removal is started and then cancelled
- **THEN** the repository is still listed

#### Scenario: Confirming

- **WHEN** removal is confirmed
- **THEN** the repository is no longer listed

### Requirement: A QR code can be scanned into the form

The system SHALL offer to scan a QR code from the add form, SHALL read a web
address from the code, and SHALL fill the URL field with the normalised result.

The camera SHALL be used only while the scanner is open.

#### Scenario: Scanning a repository URL

- **WHEN** a code containing a repository URL is scanned
- **THEN** the scanner closes
- **AND** the URL field holds that repository's URL

#### Scenario: Scanning a page URL

- **WHEN** a code containing a forge page URL is scanned
- **THEN** the URL field holds the repository URL derived from it

#### Scenario: Scanning something that is not a URL

- **WHEN** a code containing no web address is scanned
- **THEN** the scanner reports that the code holds no repository URL
- **AND** scanning continues

#### Scenario: Leaving the scanner

- **WHEN** the scanner is dismissed without a successful scan
- **THEN** the form is unchanged
- **AND** the camera is released

### Requirement: The camera is requested only when scanning is chosen

The system SHALL request camera access at the moment scanning is first chosen,
SHALL explain why it is needed, and SHALL keep the rest of the app usable when
access is refused.

#### Scenario: Granting access

- **WHEN** scanning is chosen and access is granted
- **THEN** the scanner opens

#### Scenario: Refusing access

- **WHEN** access is refused
- **THEN** the form says the camera is needed for scanning
- **AND** the URL can still be typed or pasted

#### Scenario: Access refused permanently

- **WHEN** access has been refused so that the system will not ask again
- **THEN** the form explains that access must be granted in system settings

#### Scenario: A device with no camera

- **WHEN** the device has no camera
- **THEN** scanning is not offered
- **AND** the rest of the form works

### Requirement: A URL shared from another application opens the form

The system SHALL accept plain text shared from another application, extract a
web address from it, and open the add form with the normalised URL in place.

#### Scenario: Sharing from a browser

- **WHEN** a repository page is shared into the app from a browser
- **THEN** the add form opens with the repository URL filled in

#### Scenario: Sharing while the app is already open

- **WHEN** a URL is shared while the app is running
- **THEN** the add form is shown with that URL, rather than the share being lost

#### Scenario: Sharing text with no URL

- **WHEN** shared text contains no web address
- **THEN** the app says so rather than opening an empty form
