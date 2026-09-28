## MODIFIED Requirements

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

## ADDED Requirements

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
