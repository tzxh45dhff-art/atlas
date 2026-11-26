# ATLAS - Arsenal Records Management System

A **fictional**, console-based records management prototype written in Core Java.
It manages arsenal inventory, personnel, inspections and documents, and generates reports.
It is an administrative CRUD application only. It is not connected to, and does not control, any real system.

* Language: Java 17 or newer (tested on 17; the code is also valid on 21)
* Interface: console, menu driven
* Storage: `ArrayList` in memory, plain text files in `data/`
* External libraries: none
* Build: Maven (`pom.xml`), or plain `javac` with the included scripts

## Problem statement

Organisations that keep inventory, staff, inspection and document records on paper or in scattered files face the same problems:
information is spread over many places, records are hard to search and update, duplicate or wrong entries creep in,
nobody can tell who changed what, and reports take a long time to prepare.
ATLAS addresses these problems with one centralised, validated, menu-driven record system.
(The data is fictional; the system is an administrative prototype, not a control system.)

## Objectives and status

| Objective | Status | Where to see it |
|-----------|--------|-----------------|
| Centralised record management application | Done | `Main` and the four managers |
| Add, view, search, update, delete records (CRUD) | Done | `ArsenalManager`, `PersonnelManager`, `InspectionManager`, `DocumentManager` |
| Maintain personnel and inspection information, with links between them | Done | `Inspection` + `InspectionManager` |
| Generate administrative reports and statistics, with export | Done | `ReportManager` |
| Basic authentication and input validation | Done | `AuthManager`, `InputHelper`, setters in the models |
| Apply core Java and OOP concepts | Done | see "Course concepts" below |
| Foundation for future database and GUI work | Done | models and managers are separate from storage and screens |

## Scope

**Current scope (Phase 1, this prototype):** console interface; four record modules with full CRUD; linked inspection records;
reports and file export; login with hashed passwords, forced first-time password change and lockout;
input validation and duplicate protection; text-file persistence with safe writes; audit log; automated tests.

**Out of scope for Phase 1:** database, graphical interface, multiple user roles, encryption of the data files,
multi-user (simultaneous) access, network access.

**Future scope:**

| Phase | Planned work |
|-------|--------------|
| Phase 2 | JDBC and MySQL instead of text files (this also gives foreign keys, concurrent users and fast search) |
| Phase 3 | Java Swing graphical interface |
| Later | Role-based permissions (admin and viewer), encrypted document and data storage, richer audit history, advanced search and sorting, CSV and PDF export |

## Course concepts used

| Concept | Where |
|---------|-------|
| Variables, data types, operators | all classes; counters, quantities, flags |
| Conditional statements | validation in `InputHelper`, the setters, `isStrongPassword` |
| Loops | menus (`do-while`), searches, counting, retry loops in `InputHelper` |
| `switch-case` | every menu (`run()` methods, `Main`) |
| Strings | names, `trim`, `split`, `contains`, `equalsIgnoreCase`, `String.format`, `StringBuilder` |
| Arrays | `static final String[]` choice lists (`STATUSES`, `CATEGORIES`, ...) |
| Methods | core methods and screen methods in every manager |
| Classes, objects, constructors | the four record classes and all managers |
| Encapsulation | private fields, getters and validating setters |
| Inheritance | `ArsenalRecord`, `Personnel`, `Inspection`, `Document` extend `BaseRecord` |
| Abstraction and polymorphism | abstract `BaseRecord` with `toRow()` and `toFileLine()` overridden by each child |
| Collections | `ArrayList` in every manager |
| Exception handling | `try/catch`, `IllegalArgumentException`, `IOException`, `NumberFormatException`, try-with-resources |
| File handling | `FileHelper`, data files, log file, report export |
| Packages and project structure | `com.atlas`, Maven layout |
| Date and time | `LocalDate`, `LocalDateTime` |
| Algorithms | linear search, counting, comparison of ISO dates |

## Quick start

You need a JDK (version 17 or newer). Check with `java -version`.

**Without Maven (simplest):**

```bash
./run.sh          # macOS / Linux
run.bat           # Windows
```

**With Maven:**

```bash
mvn compile exec:java          # run the application
mvn package                    # build target/atlas-1.0.0.jar
java -jar target/atlas-1.0.0.jar
```

Always start the program from the project folder, because the `data/` folder is found relative to where you run it.

> Some IDE consoles (for example VS Code's Debug Console) are not real terminals, so the password is visible while typing.
> Use the integrated Terminal or a normal terminal window to get hidden password input.

### First login

| Username | Initial password |
|----------|------------------|
| `admin`  | `Atlas@2026`     |

The first login forces you to choose a new password (at least 8 characters, with a letter and a digit).
The accounts are saved in `data/users.txt` (hashed, never as plain text). To reset everything, delete `data/users.txt` and the default account is created again.
After 3 wrong attempts the program exits.

## Features

| Module | What it does |
|--------|--------------|
| Arsenal Records | Add, view, search (by ID or name), update, delete inventory records |
| Personnel Management | Same operations for staff, with department and clearance level (1-5) |
| Inspection Records | Same operations for inspections; each one must point to an existing arsenal item and an **Active** inspector |
| Document Management | Same operations for document records |
| Reports | Summaries for each module, a full overview, and export of the overview to `data/reports/` |
| Authentication | Login, forced first-time password change, change password option, 3-attempt limit |
| Audit log | Every login, add, update, delete and export is written to `data/atlas.log` |

Data protection built into the design:

* IDs are generated automatically, so duplicate IDs cannot happen.
* Duplicate records are refused (same name and place, same inspection title, item and date, and so on).
* Fixed choices (status, category, department, ...) are picked from numbered lists instead of typed, so spelling mistakes cannot enter the data.
* All input is validated: numbers, ranges, dates (`yyyy-MM-dd`), empty text, over-long text and the `|` character.
* An arsenal item or a person who is used by an inspection cannot be deleted (this prevents orphan records). Change the status instead.
* Files are written to a temporary file first and then moved into place, so a crash cannot leave a half-written data file.
* Damaged lines in a data file are skipped with a warning instead of crashing the program.

## Project structure

```
atlas/
├── pom.xml                      Maven build file
├── README.md
├── run.sh / run.bat             compile and run without Maven
├── test.sh / test.bat           compile and run the tests without Maven
├── data/                        text files that act as the "database"
│   ├── records.txt              arsenal records
│   ├── personnel.txt
│   ├── inspections.txt
│   └── documents.txt            (users.txt, atlas.log and reports/ appear after the first run)
├── docs/
│   ├── VIVA_NOTES.md            explanation of every class and expected viva questions
│   └── TEAM_PORTIONS.md         the project split into 4 portions, one per team member
└── src/
    ├── main/java/com/atlas/
    │   ├── Main.java            login, main menu, connects everything
    │   ├── AuthManager.java     users, password hashing, login
    │   ├── BaseRecord.java      parent class of all four record types
    │   ├── ArsenalRecord.java   model       ArsenalManager.java      CRUD
    │   ├── Personnel.java       model       PersonnelManager.java    CRUD
    │   ├── Inspection.java      model       InspectionManager.java   CRUD
    │   ├── Document.java        model       DocumentManager.java     CRUD
    │   ├── ReportManager.java   statistics and export
    │   ├── InputHelper.java     safe keyboard input
    │   ├── FileHelper.java      safe file reading and writing
    │   └── AuditLog.java        history of actions
    └── test/java/com/atlas/
        └── AtlasTests.java      self-checking tests
```

15 classes in total. The original plan had 10; the extra five (`BaseRecord`, `AuthManager`, `InputHelper`, `FileHelper`, `AuditLog`) keep repeated code in one place.

### How the classes fit together

```
Main ── AuthManager (login)
  │
  ├── ArsenalManager ───── ArsenalRecord ─┐
  ├── PersonnelManager ─── Personnel ─────┤  all extend BaseRecord
  ├── InspectionManager ── Inspection ────┤
  ├── DocumentManager ──── Document ──────┘
  └── ReportManager (reads from the four managers)

InputHelper, FileHelper, AuditLog are used by all of the above.
```

* **Model classes** only hold data and know how to turn themselves into a file line and a table row.
* **Manager classes** keep the `ArrayList`, load and save the file, and offer add / find / remove / update.
  Each manager has "core" methods without user input (easy to test) and "screen" methods that talk to the user.

## Data files

Plain text, one record per line, fields separated by `|`. Lines starting with `#` are comments.

| File | Fields |
|------|--------|
| `records.txt` | `id\|name\|category\|location\|quantity\|status` |
| `personnel.txt` | `id\|name\|designation\|department\|clearanceLevel\|status` |
| `inspections.txt` | `id\|title\|arsenalId\|inspectorId\|date\|result` |
| `documents.txt` | `id\|name\|type\|dateAdded\|status` |
| `users.txt` | `username\|salt\|passwordHash\|mustChangePassword` |

ID ranges: arsenal from 1001, personnel from 2001, inspections from 3001, documents from 4001.

## Tests

```bash
./test.sh        # or test.bat on Windows
```

76 checks cover validation, file round trips, duplicate rules, linked records, reports, password hashing and the input helper
(including bad input such as letters instead of numbers). The tests run in a temporary folder and never touch `data/`.

With Maven: `mvn test-compile exec:java@run-tests`.

## Algorithms

| Operation | Method | Complexity |
|-----------|--------|------------|
| Find by ID | linear search over the `ArrayList` | O(n) |
| Add | `ArrayList.add` | O(1) amortized (plus duplicate check O(n)) |
| Update / delete | find, then change or remove | O(n) |
| Reports | one counting loop per figure | O(n) |

Linear search is enough for the small data sets of this prototype.

## Known limitations (honest scope)

* There is one user level. Role-based permissions (admin / viewer) are future work.
* Data files are plain text and not encrypted. Passwords are hashed, but the records are not.
* Only one program instance should run at a time (no file locking).
* No database, no GUI. Planned next phases: JDBC with MySQL, then Swing.

## Team workflow

Each module (model + manager) is independent, so members can work in parallel on separate files and merge through Git.
Suggested commit style: one small commit per feature, for example `Add search by name to PersonnelManager`.
