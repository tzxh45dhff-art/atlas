# ATLAS - Team Portions and Study Guide (4 members)

This guide splits the project into **four portions**, one per team member, and tells each member:

* exactly **which files to read, in which order, and what to look for in each**,
* **how to learn** their portion (a step-by-step study path),
* **experiments** to run so the code stops being "text" and becomes something they understand,
* the **demo** they run, the **2-minute talk** they give, and a **small extension** they build themselves.

The model answers to viva questions are in [`VIVA_NOTES.md`](VIVA_NOTES.md). This file points to the sections there.

> **Rule for the whole team:** you should be able to open your files and explain every line in plain words,
> without reading from a script. If you cannot explain a line, that line is your next thing to study.

---

## Contents

1. [The four portions at a glance](#1-the-four-portions-at-a-glance)
2. [Everyone starts here: the 30-minute orientation](#2-everyone-starts-here-the-30-minute-orientation)
3. [How to learn code you did not write](#3-how-to-learn-code-you-did-not-write)
4. [Portion 1 - Data model and Documents](#4-portion-1---data-model-and-documents)
5. [Portion 2 - Arsenal and Personnel CRUD](#5-portion-2---arsenal-and-personnel-crud)
6. [Portion 3 - Inspections and Reports](#6-portion-3---inspections-and-reports)
7. [Portion 4 - Login, input, files, startup and build](#7-portion-4---login-input-files-startup-and-build)
8. [Four-day plan](#8-four-day-plan)
9. [Team rehearsal and the combined demo](#9-team-rehearsal-and-the-combined-demo)
10. [Git workflow for four people](#10-git-workflow-for-four-people)
11. [Before you present](#11-before-you-present)

---

## 1. The four portions at a glance

| | Portion 1 | Portion 2 | Portion 3 | Portion 4 |
|---|---|---|---|---|
| **Theme** | What a record *is* | Everyday CRUD on two modules | Linked records and reports | Everything around the records |
| **Main files** | `BaseRecord`, `ArsenalRecord`, `Personnel`, `Inspection`, `Document`, `DocumentManager` | `ArsenalManager`, `PersonnelManager` | `InspectionManager`, `ReportManager` | `Main`, `AuthManager`, `InputHelper`, `FileHelper`, `AuditLog`, plus `pom.xml`, `run.sh`, `test.sh` |
| **Lines (approx.)** | 700 | 640 | 560 | 600 plus build files |
| **Core Java ideas** | classes, constructors, inheritance, abstract, `static final`, exceptions | `ArrayList`, loops, linear search, `switch`, methods | objects that use other objects, counting, `StringBuilder`, dates as text | `Scanner`, exceptions, file I/O, hashing, `static`, compiling and running |
| **Difficulty** | medium (inheritance is new) | easiest to read, most repetition | medium | hardest (security part), but small files |
| **Study time** | about 8 hours | about 7 hours | about 7 hours | about 8 hours |
| **You depend on** | portion 4 (file and input helpers) | portions 1 and 4 | portions 1, 2 and 4 | nobody (others depend on you) |
| **Others depend on you** | everybody (models) | portion 3 (find and counts) | nobody | everybody (input, files, log) |

All paths below are inside `src/main/java/com/atlas/` unless a full path is given.
Test file: `src/test/java/com/atlas/AtlasTests.java`. Data files: `data/`.

---

## 2. Everyone starts here: the 30-minute orientation

Do this **together or individually before your own portion**. It gives you the picture all four portions fit into.

| Step | Do this | Why |
|------|---------|-----|
| 1 | Read `README.md` (10 min) | What the project is, how to run it, the folder structure |
| 2 | Run the app: `./run.sh`, log in (`admin` / `Atlas@2026`), visit every menu once (10 min) | You cannot understand code for a program you never used |
| 3 | Open `data/records.txt`, then add an arsenal record in the app, then open the file again | See that "the database" is a text file that changes |
| 4 | Read the **architecture** and **start-up trace** in `VIVA_NOTES.md`, sections 2 to 4 (10 min) | Shows how the files cooperate |
| 5 | Skim `Main.java` (93 lines): just see which objects it creates and which menu number calls which module | The "table of contents" of the program |

Two ideas to hold on to for the whole project:

1. **Model classes** (`ArsenalRecord`, `Personnel`, `Inspection`, `Document`) only *hold data*.
2. **Manager classes** (`ArsenalManager`, ...) keep an `ArrayList` of models and *do things with them*: add, find, update, delete, save, load, show.

---

## 3. How to learn code you did not write

Use this loop for every file. It is the same for all four members.

| Phase | What you do | Time per file |
|-------|-------------|---------------|
| **1. Predict** | Read only the class comment and the method names. Guess what each method does. Write guesses in a notebook. | 5 min |
| **2. Read** | Read the file top to bottom, one method at a time. For every line ask "what does this do and why is it needed?" Look up every word you do not know. | 20-40 min |
| **3. Run** | Use the part of the app that uses this file. Watch the output. Open the data file before and after. | 10 min |
| **4. Trace** | Pick one action (for example "add a record") and follow it through the methods, with the debugger or with temporary `System.out.println` lines. | 15 min |
| **5. Break** | Change something on purpose, predict the result, run it, check. Then undo it (`git restore .`). The experiment tables below give ideas. | 15 min |
| **6. Teach back** | Explain the file to a teammate in 5 minutes without reading. Every time you get stuck, go back to phase 2. | 10 min |

Tips:

* **Use the debugger.** In VS Code, open the file, click left of a line number to add a breakpoint, then use *Run and Debug* on `Main`. Step with F10 (next line) and F11 (into a method). Watch the variables panel: you see the `ArrayList` fill up.
* **Compile errors are teachers.** When an experiment breaks the build, read the message: it tells you what Java expects.
* **Undo safely.** `git restore .` throws away all uncommitted changes in tracked files (the sample data in `data/` is tracked, so it is restored too). Careful: this also throws away your own uncommitted work, so commit your extension before experimenting, or experiment on a separate branch.
* **Keep a "words I did not know" list** (abstract, static, final, override, ...). The glossary in `VIVA_NOTES.md` section 7 covers most of them.
* **Do not memorise code. Memorise the story** of what happens and why (the trace sections in `VIVA_NOTES.md`).

---

## 4. Portion 1 - Data model and Documents

### Snapshot

| | |
|---|---|
| **You own** | `BaseRecord`, `ArsenalRecord`, `Personnel`, `Inspection`, `Document`, `DocumentManager` |
| **Your story** | "I designed what a record *is*. All four record types share an ID, a name and a status, so I wrote that once in a parent class; each child adds its own fields. Then I built the simplest complete module, Documents." |
| **Viva answers** | `VIVA_NOTES.md` section 8.1 |

### Java topics to refresh first

Classes and objects, constructors, `this`, access modifiers (`private`, `public`, `protected`), `final`, `static`,
inheritance (`extends`, `super`), `abstract`, `@Override`, arrays (`String[]`), `ArrayList`,
exceptions (`throw new IllegalArgumentException(...)`), and the `String` methods `trim`, `split`, `contains`, `equalsIgnoreCase`, `String.format`.

### Reading list (in this order)

| # | File | What to look for | Time |
|---|------|------------------|------|
| 1 | `data/documents.txt` | What one saved record looks like: fields separated by `\|`, a `#` comment on line 1 | 5 min |
| 2 | `BaseRecord.java` | **Fields:** why `id` is `final` and the others are not. **Constructor:** why it receives `allowedStatuses`. **`setName` / `setStatus`:** why they are `final`. **`abstract` methods** `toFileLine()` and `toRow()`. **Helpers:** `cleanText`, `checkOneOf`, `checkDate`, `splitLine`, `printHeader`. Ask: what does each helper reject? | 45 min |
| 3 | `Document.java` (smallest child) | The constructor's first line `super(id, name, status, STATUSES)`. The `static final String[]` lists. How `toFileLine` builds the line and how `fromFileLine` takes it apart. `@Override`. `header()` is `static`, why? | 25 min |
| 4 | `ArsenalRecord.java` | Same pattern as `Document`, plus a number field with a range check in `setQuantity`. Compare side by side with `Document`: what is identical, what differs? | 20 min |
| 5 | `Personnel.java`, `Inspection.java` | Skim: clearance level range, the two ID links in `Inspection`, and the date check | 15 min |
| 6 | `DocumentManager.java` | Read in this order: fields, constructor, `load`, `save`, `find`, `nextId`, `isDuplicate`, `addDocument`, `removeById`, `getAll`, the count methods, `run`, then each `...Screen` method, then `askForDocument` | 60 min |
| 7 | `AtlasTests.java`: `testModels` and `testDocumentManager` | How the tests prove that bad values are rejected and that save-and-load round trips work | 20 min |
| 8 | Skim only the signatures of `InputHelper.readChoice` and `FileHelper.readLines / writeLines` | You call them but portion 4 owns them | 10 min |

### Study path

1. **Day 1, morning:** orientation (section 2), then files 1-3. Goal: explain how `Document` gets its ID, name and status from `BaseRecord`.
2. **Day 1, afternoon:** files 4-5, then draw the class diagram on paper (`BaseRecord` at the top, four children below, with their extra fields).
3. **Day 2:** file 6. Trace "add a document" (`VIVA_NOTES.md` section 4.2 shows the same trace for arsenal). Run the experiments below.
4. **Day 3:** file 7, then build your extension (below).
5. **Day 4:** rehearse the talk and the demo, then teach one other member about inheritance.

### Experiments (do, predict, check)

| # | Do this | What you should see | What it teaches |
|---|---------|---------------------|-----------------|
| 1 | In `Document.java`, add `"Rejected"` to `STATUSES`, run the app, add a document | "Rejected" appears as a menu choice | The list is the single source of truth for allowed values |
| 2 | In `data/documents.txt`, change one status to `Banana`, start the app | A warning "invalid line skipped" and 4 records instead of 5 | Setters validate even when loading from a file |
| 3 | In `data/documents.txt`, copy a line and keep the same ID | "duplicate ID skipped" warning | Load checks IDs too |
| 4 | In `Document.java`, delete the `toRow()` method and compile | Compile error "is not abstract and does not override abstract method" | What `abstract` forces on children |
| 5 | In `BaseRecord`, remove the word `abstract` from the class line | Compile error | A class with abstract methods must be abstract |
| 6 | In `DocumentManager.addScreen`, try to type a name with `\|` | The input is refused ("character is not allowed") | Why the delimiter must never appear in data |
| 7 | Add a document, then delete the highest-ID document, then add another | The new one gets the **same** ID again | `nextId` = highest + 1, a known limitation |
| 8 | Put a breakpoint in `addDocument`, add a document from the app | Step through: duplicate check, `documents.add`, `save()`, `AuditLog.log` | The full "add" path |

After each experiment run `git restore .` to undo.

### Self-test (answer without looking; model answers in `VIVA_NOTES.md` 8.1)

1. Why does `BaseRecord` exist? What would the code look like without it?
2. What is the difference between `private`, `protected` and `public`? Where is each used here?
3. What does `super(id, name, status, STATUSES)` do?
4. What is method overriding? Which methods do the children override?
5. What is polymorphism, and where could you show it in the program?
6. Why are `STATUSES` and `TYPES` `static final`?
7. Why is `id` `final` but `name` has a setter?
8. What happens if the file has a line with 3 fields instead of 5?

### Your 2-minute talk

1. (20 s) "Every record has an ID, a name and a status, so I made one parent class, `BaseRecord`." Show the class diagram.
2. (40 s) "Each child adds its own fields and says how it is saved and shown." Open `Document.java`, point at `extends`, `super`, `toFileLine`, `toRow`.
3. (30 s) "Setters check values, so bad data cannot get into an object." Show `cleanText` and `checkOneOf`.
4. (30 s) "Documents is the simplest full module: create, read, update, delete, saved to `documents.txt`." Switch to the demo.

### Your demo path (about 2 minutes)

Menu keys: `4` (Document Management).

1. `1` Add document: name `Safety Checklist`, type `3` (Policy), status `1` (Pending). The app says "Document added with ID 4006".
2. `2` View all: the new row appears. Open `data/documents.txt` and show the new line.
3. `1` Add again with the same name and type: **refused as a duplicate**.
4. `5` Update ID `4006`, field `3` (Status), new status `3` (Approved). `3` Search by ID `4006` to show the change.
5. `6` Delete ID `4006`, confirm with `y`. Open the file again: the line is gone.

### Make it yours: "View documents by status"

Write this yourself; it is a small, safe feature that uses everything you studied.

1. In `DocumentManager`, add a **core** method `public ArrayList<Document> findByStatus(String status)` that loops over `documents` and returns the matching ones in a new `ArrayList`.
2. Add a **screen** method `viewByStatusScreen()`: ask `InputHelper.readChoice("Status:", Document.STATUSES)`, call your core method, print the header with `BaseRecord.printHeader(Document.header())`, then each row, then the count.
3. In `run()`: print `"7. View by status"`, change `readInt(..., 0, 6)` to `0, 7`, add `case 7:`.
4. In `AtlasTests.testDocumentManager`, add a check that `findByStatus("Pending")` returns the right number.
5. Run `./test.sh`, then commit on your own branch under your own name (section 10).

**Done when:** the menu shows option 7, it prints only documents of the chosen status, "no matches" does not crash, and the test passes.

### Questions you may get about other portions

* "How does the manager know where the file is?" (portion 4: `FileHelper.resolve`)
* "What stops me typing `abc` as a quantity?" (portion 4: `InputHelper.readInt`)
* "How is a record shown in the report?" (portion 3: reports only count, they do not print rows)

---

## 5. Portion 2 - Arsenal and Personnel CRUD

### Snapshot

| | |
|---|---|
| **You own** | `ArsenalManager`, `PersonnelManager` |
| **Your story** | "I wrote the two main everyday modules. Records live in an `ArrayList` while the program runs and are saved to a text file after every change. Searching is a linear search." |
| **Viva answers** | `VIVA_NOTES.md` section 8.2 |

### Java topics to refresh first

`ArrayList` (`add`, `remove`, `get`, `size`, `isEmpty`), for-each loop and indexed `for`, `do-while`, `switch` with `case` and `break`,
methods that return `null`, `private` versus `public` methods, `equalsIgnoreCase`, `toLowerCase`, `contains`, the ternary operator (`a ? b : c`).

### Reading list (in this order)

| # | File | What to look for | Time |
|---|------|------------------|------|
| 1 | `data/records.txt` and `data/personnel.txt` | The two file formats; the sample IDs (1001-1006 and 2001-2006) | 5 min |
| 2 | `ArsenalManager.java` - **the master template**, read slowly | **Fields:** the `ArrayList`, `FILE_NAME`, `FIRST_ID`. **`load` and `save`:** how a line becomes an object and back. **Core methods:** `find` (linear search), `nextId`, `isDuplicate`, `addRecord`, `removeById`, `getAll`. **Counts:** `countByStatus`, `countByCategory`, `totalQuantity`. **Menu:** `run` with `do-while` and `switch`. **Screens:** `addScreen`, `viewAllScreen`, `searchByIdScreen`, `searchByNameScreen`, `updateScreen`, `deleteScreen`, `askForRecord` | 90 min |
| 3 | `PersonnelManager.java` | Do not read it line by line. **Compare with `ArsenalManager`** and list only the differences (different fields, different duplicate rule, different update choices). | 30 min |
| 4 | `ArsenalRecord.java`, `Personnel.java` | The getters and setters your update screens call | 15 min |
| 5 | `InputHelper.java`: `readInt`, `readText`, `readChoiceIndex`, `confirm` | What each one guarantees about its return value | 20 min |
| 6 | `InspectionManager.java`: only `isArsenalReferenced` and `isPersonnelReferenced` | Called from your `deleteScreen` to protect linked records | 10 min |
| 7 | `AtlasTests.java`: `testArsenalManager`, `testPersonnelManager` | Every rule in your managers has a test | 20 min |

### Study path

1. **Day 1:** orientation, then file 1 and the first half of file 2 (fields, `load`, `save`, core methods). Draw the lifecycle: *file -> `load` -> `ArrayList` -> user changes -> `save` -> file*.
2. **Day 2:** the second half of file 2 (menu and screens), then file 3 (comparison). Trace "delete a record" including the protection rule (`VIVA_NOTES.md` section 4.3).
3. **Day 3:** experiments, then build your extension.
4. **Day 4:** rehearse; teach portion 3's person how `find` and `getAll` work (they use them).

### Experiments

| # | Do this | What you should see | What it teaches |
|---|---------|---------------------|-----------------|
| 1 | Put a breakpoint in `find`, then search an ID that is near the end of the list | The loop steps through every record before returning | Linear search, O(n) |
| 2 | Rename `data/records.txt` temporarily, run, add one record | The app starts empty, then creates a new file with the header comment | A missing file is not an error; `save` creates it |
| 3 | While the app is running, edit `data/records.txt` by hand, then add a record in the app and look at the file | Your manual edit is overwritten | The `ArrayList` in memory is the "truth"; `save` rewrites the whole file |
| 4 | In `ArsenalManager.deleteScreen`, comment out the `isArsenalReferenced` check, try to delete `1001`, then check Inspections | The arsenal item is gone but inspection 3001 still points at it | Why the protection rule exists |
| 5 | Change `FIRST_ID` to `5001`, delete `data/records.txt`, add a record | The new record gets ID `5001` | How `nextId` starts |
| 6 | In `ArsenalManager.getAll`, return `records` instead of `new ArrayList<>(records)`. Then add the line `arsenalManager.getAll().clear();` at the start of `ReportManager.arsenalReport`, run **Reports -> Arsenal summary**, and view all arsenal records | The arsenal list is now empty, because the report emptied the manager's own list. With the copy (original code) nothing happens. | Why `getAll()` returns a copy (defensive copy). Undo both changes afterwards. |
| 7 | Search by name with part of a name in different cases (`guid`, `GUID`) | Same results | `toLowerCase` makes search case-insensitive |
| 8 | Update a record's name to the name of another record in the same location | Refused: "Another record already has that name and location" | `isDuplicate(..., excludeId)` |

### Self-test (answers in `VIVA_NOTES.md` 8.2)

1. Why use an `ArrayList` and not an array?
2. Walk through `find`. What is its time complexity? Why is that acceptable here?
3. Why are `addRecord` / `removeById` separate from `addScreen` / `deleteScreen`?
4. How does `nextId` work, and what is its weakness?
5. What does `isDuplicate(name, location, excludeId)` do with `excludeId`?
6. Why does `getAll()` return a copy?
7. Why do we ask `y/n` before deleting, and why do we refuse to delete some records?
8. What happens if the file contains a damaged line?

### Your 2-minute talk

1. (30 s) "While the program runs, records are objects in an `ArrayList`; on disk they are lines in a text file." Draw *file -> list -> file*.
2. (45 s) "Find is a linear search: I check each record's ID until it matches." Show `find`.
3. (30 s) "After every add, update or delete I call `save`, so nothing is lost if the program closes." Show `save`.
4. (15 s) "Personnel works the same way; only the fields and the duplicate rule differ."

### Your demo path (about 2 minutes)

Menu keys: `1` (Arsenal Records).

1. `1` Add: name `Relay Unit RU-2`, category `4`, location `Site Alpha`, quantity `20`, status `1`. New ID `1007`.
2. `4` Search by name: type `relay`. The record appears (case-insensitive).
3. `5` Update ID `1007`, field `4` (Quantity), new value `35`. `3` Search by ID `1007` to show it.
4. `6` Delete ID `1001`: **refused** (inspection 3001 uses it). Explain why.
5. `6` Delete ID `1007`, confirm `y`. Open `data/records.txt`: the line is gone.
6. Type `abc` or `99` at a menu to show that invalid input is handled.

### Make it yours: "View low-stock items" (Personnel variant: "View by department")

1. Core method in `ArsenalManager`: `public ArrayList<ArsenalRecord> findBelowQuantity(int limit)` - return records with `getQuantity() < limit`.
2. Screen method `lowStockScreen()`: `InputHelper.readInt("Show items with quantity below: ", 1, ArsenalRecord.MAX_QUANTITY)`, call the core method, print header and rows, print "No items" if the list is empty.
3. Add menu line `7. View low-stock items` in `run()`, change `readInt` range to `0, 7`, add `case 7:`.
4. Add checks in `testArsenalManager` (add two records with different quantities and assert the result size).
5. Personnel variant: `findByDepartment(String department)` using `InputHelper.readChoice("Department:", Personnel.DEPARTMENTS)`.

**Done when:** option 7 works, it shows nothing harmful when no record matches, and tests pass.

### Questions you may get about other portions

* "Where do the status choices come from?" (portion 1: `static final String[]` in the model classes)
* "How is the password checked before I can reach this menu?" (portion 4)
* "How do reports use your counts?" (portion 3: `ReportManager` calls `countByStatus` etc.)

---

## 6. Portion 3 - Inspections and Reports

### Snapshot

| | |
|---|---|
| **You own** | `InspectionManager`, `ReportManager` |
| **Your story** | "Inspections are the only records that depend on other records. An inspection must point to an existing arsenal item and an active inspector. I made sure those links stay valid, then I built the reports that count and summarise all the data." |
| **Viva answers** | `VIVA_NOTES.md` section 8.3 |

### Java topics to refresh first

Constructors with parameters, fields that hold *other objects*, counting loops, `String` comparison (`equals`, `compareTo`),
`StringBuilder`, `String.format` (`%d`, `%s`, `%.1f`, `%%`, `%n`), integer versus decimal division, `LocalDate` basics, `null` checks.

### Reading list (in this order)

| # | File | What to look for | Time |
|---|------|------------------|------|
| 1 | `data/inspections.txt` | Fields `id`, `title`, `arsenalId`, `inspectorId`, `date`, `result`. Which seed IDs do they point to? | 5 min |
| 2 | `Inspection.java` | `arsenalId` and `inspectorId` are numbers that "point" to other records. The date is saved as text `yyyy-MM-dd`. | 15 min |
| 3 | `InspectionManager.java` | **Constructor:** it receives `ArsenalManager` and `PersonnelManager`. **`addInspection`:** the four reasons it returns `false`. **`isArsenalReferenced`, `isPersonnelReferenced`:** used by portion 2's delete screens. **`isDuplicate`:** three fields compared. **`latestDate`:** how text dates are compared. **`addScreen`, `askForArsenal`, `askForInspector`:** loops that retry until valid. **`updateScreen`:** four fields. **`printLinkedNames`:** turning IDs into names | 90 min |
| 4 | `ReportManager.java` | Builder methods return a `String`; `run` prints them; `exportOverview` writes a file. Study `inspectionReport`: pass rate, "never inspected" list. | 45 min |
| 5 | `ArsenalManager.find`, `getAll` and the `count...` methods; `PersonnelManager.find`; `DocumentManager.countByType` | The "provider" methods your classes call | 15 min |
| 6 | `FileHelper.writeText` (only the idea) | How the report file is written | 10 min |
| 7 | `AtlasTests.java`: `testInspectionManager`, `testReports` | Each of the four rejection reasons is tested | 20 min |

### Study path

1. **Day 1:** orientation, then files 1-2 and the first half of file 3 (constructor to `isDuplicate`). Draw the **link diagram**: 3001 -> arsenal 1001 and inspector 2002, etc., using the seed data.
2. **Day 2:** the rest of file 3 and file 4. Trace "add an inspection" (`VIVA_NOTES.md` section 4.5).
3. **Day 3:** experiments and your extension.
4. **Day 4:** rehearse; teach portion 2's person how `isArsenalReferenced` protects their delete.

### Experiments

| # | Do this | What you should see | What it teaches |
|---|---------|---------------------|-----------------|
| 1 | Add an inspection for item `1004` with inspector `2005` | "Sana Qureshi is not Active and cannot be an inspector." Then use `2002` | Business rules in `askForInspector` |
| 2 | Add an inspection with arsenal ID `9999` | "No arsenal record with ID 9999." and the question repeats; `0` cancels | The retry loop |
| 3 | Enter the date `2026-13-45` | "Please use the format yyyy-MM-dd..." | Dates are validated in `InputHelper.readDate` |
| 4 | Open **Reports -> Inspection summary** before and after adding a Passed inspection | The pass rate and the "never inspected" list change | How the report reads live data |
| 5 | Edit `data/inspections.txt`: change an arsenal ID to `9999`, start the app, search that inspection by ID | The names show `(not found)` - the file loaded without checking links | **Known limitation:** links are checked when adding, not when loading |
| 6 | In `data/inspections.txt`, change every result to `Pending`, start the app, open **Reports -> Inspection summary** | "Pass rate: no completed inspections yet" instead of a number | Why `inspectionReport` checks `completed > 0` (it avoids dividing by zero) |
| 7 | Run **Reports -> Export**, open the new file in `data/reports/` | Same text as on screen, with a timestamp in the file name | `writeText` and `LocalDateTime` formatting |
| 8 | In `addInspection`, comment out the `arsenalManager.find(...)` condition and run `./test.sh` | The test "inspection of a missing arsenal item is rejected" **fails** | Tests protect rules |

### Self-test (answers in `VIVA_NOTES.md` 8.3)

1. Why does `InspectionManager` receive two other managers in its constructor?
2. List the four conditions under which `addInspection` returns `false`.
3. Why can an arsenal item with inspections not be deleted?
4. How is the pass rate calculated, and why are Pending inspections excluded?
5. How does `latestDate` find the newest date when dates are text?
6. Why does `ReportManager` never change data?
7. What does `%.1f%%` print?
8. What can go wrong if someone edits `inspections.txt` by hand? (Experiment 5)

### Your 2-minute talk

1. (30 s) "An inspection says: item X was checked by person Y on date Z with result R. X and Y must exist." Show the link diagram.
2. (40 s) "`InspectionManager` is given the other managers, so it can look the item and the person up before adding." Show the constructor and `addInspection`.
3. (30 s) "The reverse rule: you cannot delete something that inspections still refer to." Show `isArsenalReferenced`.
4. (20 s) "`ReportManager` only reads: it counts and builds text, shown on screen or exported to a file."

### Your demo path (about 2 minutes)

Menu keys: `3` (Inspection Records), then `5` (Reports).

1. `3` -> `1` Add: title `Spot Check`, arsenal `1004`, inspector `2005` -> **refused** (On Leave). Use `2002`. Date `2026-13-45` -> refused, then `2026-11-25`. Result `2` (Passed). New ID `3005`.
2. `3` Search by ID `3005`: the item and inspector **names** are shown.
3. `0` back, `5` Reports -> `3` Inspection summary: pass rate, and item `1004` is no longer in the "never inspected" list.
4. `6` Export: show the new file in `data/reports/`.

### Make it yours: "Overdue pending inspections"

1. Core method in `InspectionManager`: `public ArrayList<Inspection> findOverduePending(String today)` - return inspections whose status is `Pending` and whose date is **earlier** than `today` (use `getDate().compareTo(today) < 0`; the date text sorts correctly).
2. In `ReportManager`: a new method `overdueReport()` that calls it with `LocalDate.now().toString()` and builds a text list; add a menu option and a line in the overview.
3. Test in `testInspectionManager` with a fixed `today` string (so the test never depends on the real date).
4. (Alternative: "Inspections per inspector": loop over `personnelManager.getAll()`, count with a small loop.)

**Done when:** the report lists exactly the pending inspections with dates before today, handles an empty list, and the test passes.

### Questions you may get about other portions

* "Where does the date check happen?" (portion 4: `InputHelper.readDate`, and portion 1: `BaseRecord.checkDate`)
* "How does a record become a line in `inspections.txt`?" (portion 1: `toFileLine`)
* "Who writes the file?" (portion 4: `FileHelper`)

---

## 7. Portion 4 - Login, input, files, startup and build

### Snapshot

| | |
|---|---|
| **You own** | `Main`, `AuthManager`, `InputHelper`, `FileHelper`, `AuditLog` and the build files (`pom.xml`, `run.sh`, `run.bat`, `test.sh`, `.gitignore`) |
| **Your story** | "I built everything *around* the records: starting the program, logging in, asking the user safely, reading and writing files without losing data, and keeping a history of what happened." |
| **Viva answers** | `VIVA_NOTES.md` section 8.4 |

### Java topics to refresh first

`Scanner` and `System.in`, `try / catch`, checked versus unchecked exceptions (`IOException`, `NumberFormatException`),
`try-with-resources`, `File`, `BufferedReader`, `FileWriter`, `static` methods and utility classes, `private` constructors,
a nested class, `char[]` versus `String`, how compiling works (`javac`, `.class`, `java`).

### Reading list (in this order)

| # | File | What to look for | Time |
|---|------|------------------|------|
| 1 | `Main.java` (93 lines) | The order things happen: banner, `AuthManager`, `login`, create the managers, wire them together, menu loop. The `catch (NoSuchElementException ...)`. | 20 min |
| 2 | `InputHelper.java` | One `static` `Scanner` for the whole program. Every method loops until the answer is valid. Why `nextLine()` plus `parseInt` instead of `nextInt()`. `readPassword` and `System.console()`. | 40 min |
| 3 | `FileHelper.java` | `resolve` (the `atlas.data` property), `readLines` (skips `#` and blank lines), `writeLines`, `writeText` (temporary file then move), `appendLine`. Try-with-resources in each. | 40 min |
| 4 | `AuditLog.java` | `static` state (`currentUser`), `log`, the time format | 10 min |
| 5 | `AuthManager.java` (237 lines, read in this order) | constants -> nested class `User` -> constructor (creates the default admin) -> `load` / `save` -> `findUser` -> `addUser` -> `authenticate` -> `setPassword` -> `isStrongPassword` -> `newSalt` / `hash` -> `login` -> `forcePasswordChange` -> `changePasswordScreen` -> `askAndSetNewPassword` | 90 min |
| 6 | `pom.xml`, `run.sh`, `test.sh`, `.gitignore` | What each line says; which folders are generated | 20 min |
| 7 | `AtlasTests.java`: `testInputHelper`, `testFileHelper`, `testAuth` | How a test feeds fake keyboard input with `System.setIn` | 25 min |
| 8 | After running the app: `data/users.txt`, `data/atlas.log` | The hashed password line; the audit trail | 10 min |

### Study path

1. **Day 1:** orientation, then files 1-3. Goal: explain what happens between starting the program and seeing the main menu.
2. **Day 2:** files 4-5. This is the demanding part: take the password hashing slowly and use the "black box" explanation below. Trace "login" (`VIVA_NOTES.md` section 4.4).
3. **Day 3:** files 6-8, experiments, extension.
4. **Day 4:** rehearse; teach portion 1's person how a file line is written and read.

### The password hashing "black box" explanation (learn this by heart)

* We never save the password.
* For each user we save a random **salt** (16 random bytes, written in hex) and a **hash**.
* A **hash function** (here PBKDF2, built into Java) turns *password + salt* into a long string. The same input always gives the same output, but you cannot go backwards from the output to the password.
* To check a login: hash what was typed with the saved salt, compare with the saved hash.
* The salt makes two users with the same password get different hashes.
* PBKDF2 repeats the calculation 65,536 times on purpose, so guessing passwords is slow.

### Experiments

| # | Do this | What you should see | What it teaches |
|---|---------|---------------------|-----------------|
| 1 | Open `data/users.txt` after first login | Four fields: name, salt, hash, `false`. No readable password. | Passwords are hashed |
| 2 | Delete `data/users.txt`, run, log in with `Atlas@2026` | "First run" message, forced password change | The default admin is recreated |
| 3 | Enter a wrong password three times | "Attempts left: 2, 1, 0" then the program exits; open `data/atlas.log` | Lockout and audit log |
| 4 | At the main menu type `abc`, then `99`, then `-3` | A message each time, no crash | `readInt` loops until valid |
| 5 | At the login prompt press Ctrl+D | "Input closed. Exiting ATLAS." | The `NoSuchElementException` catch in `Main` |
| 6 | Run in the VS Code Debug Console, then in the Terminal | Password visible in the first, hidden in the second | `System.console()` is `null` in IDE consoles |
| 7 | In `FileHelper.writeText`, add `throw new RuntimeException("boom");` right after the `writer.write(content)` line (inside the try), add a record in the app, then look at the data folder | The program stops with the error (expected), the real data file is **untouched**, and a half-finished `.tmp` file is left over | Why we write to a temporary file first: a crash while writing never damages the real file |
| 8 | Run `javac -d out src/main/java/com/atlas/*.java`, then open the `out/com/atlas` folder | One `.class` file per class (and `AuthManager$User.class` for the nested class) | What compiling produces |
| 9 | Run `./test.sh` and read the output | 76 checks, all PASS | How the test program works |
| 10 | Change `MAX_ATTEMPTS` to `1` in `AuthManager`, run, enter a wrong password | The program exits after one try | Constants make behaviour easy to change |

### Self-test (answers in `VIVA_NOTES.md` 8.4)

1. Why do we not use `nextInt()`?
2. What does try-with-resources do and why is it better than calling `close()` yourself?
3. What is a salt? What is a hash? Why not store the password?
4. What happens after three wrong passwords?
5. Why does `FileHelper.writeText` write to a `.tmp` file first?
6. Why are `InputHelper`, `FileHelper` and `AuditLog` full of `static` methods and have a `private` constructor?
7. What is the difference between a `.java` file and a `.class` file?
8. What does `-Datlas.data=...` do?

### Your 2-minute talk

1. (30 s) "`Main` starts the program: it shows the login, then creates the four managers and shows the menu." Show `Main`.
2. (40 s) "Passwords are never stored. I store a random salt and a hash; at login I recompute the hash and compare." Show `authenticate`; open `users.txt`.
3. (30 s) "Every question goes through `InputHelper` and loops until the answer is valid, so typing letters can never crash the program." Demo `abc` at the menu.
4. (20 s) "`FileHelper` writes to a temporary file first, so the real file is never half-written. `AuditLog` records who did what."

### Your demo path (about 3 minutes)

1. Delete `data/users.txt` (the default account will be recreated), then `./run.sh`.
2. Log in as `admin` with `Atlas@2026` -> forced change: try `abc` (rejected), then a mismatch (rejected), then a good password.
3. At the main menu type `abc` -> rejected, no crash.
4. After the other members' demos, choose `6` Change Password once, then `7` Exit.
5. Open `data/atlas.log` and show login, add, delete, export and logout lines with your user name. Open `data/users.txt` and show there is no readable password.
6. Optional: run `./test.sh` and show 76 PASS.

### Make it yours: "View recent activity"

1. Core method in `AuditLog`: `public static ArrayList<String> lastLines(int count)` - use `FileHelper.readLines("atlas.log")` and return the last `count` lines (careful with lists shorter than `count`).
2. In `Main.printMainMenu`, add `7. View recent activity` and make Exit option `8`; change `readInt(..., 1, 7)` to `1, 8`; shift the `switch` cases; update the loop condition `choice != 8`.
3. A small screen method that prints the lines or "No activity yet".
4. Test in `AtlasTests` (write two log lines, ask for the last one).
5. Update the menu text in `README.md` and the numbers in `TEAM_PORTIONS.md` demo paths.

**Done when:** the new option works with an empty log and a long log, exit still works, tests pass.

### Questions you may get about other portions

* "How does a record turn into a line?" (portion 1)
* "How does the manager delete a record?" (portion 2)
* "How are the links between inspections checked?" (portion 3)

---

## 8. Four-day plan

| Day | Everyone | Portion-specific |
|-----|----------|------------------|
| **1** | Orientation (section 2), run the app, read the class diagram | Reading list items 1 to about half of your main file; first teach-back to yourself in a notebook |
| **2** | 30-minute stand-up: each says what confused them | Finish reading; do the trace for your main action; start experiments |
| **3** | Peer review: swap with the next portion (1 reads 2, 2 reads 3, 3 reads 4, 4 reads 1) and ask each other the self-test questions | Experiments; build the "make it yours" extension; push a branch |
| **4** | Full rehearsal with the combined demo (section 9), time it, answer cross-questions | Fix gaps; polish the talk |

Study time is a guide. If you finish early, read the next portion's main file: the viva often asks the whole team about the whole project.

---

## 9. Team rehearsal and the combined demo

### Speaking order (about 10 minutes total)

| Order | Who | Minutes | What |
|-------|-----|---------|------|
| 1 | Portion 4 | 2 | Start the program, the login, forced password change, invalid input, then "the rest of the team will show the modules" |
| 2 | Portion 1 | 2 | The class diagram, then the Documents demo |
| 3 | Portion 2 | 2 | Arsenal CRUD demo including the refused delete |
| 4 | Portion 3 | 2 | Add an inspection, reports, export |
| 5 | Portion 4 | 2 | Change password, exit, show `atlas.log` and `users.txt`, run the tests |

### Reset before every rehearsal

```bash
git restore data            # restores the sample records
# then delete data/users.txt, data/atlas.log and the data/reports folder by hand (they are generated files)
```

After the reset, the first login forces a new password, which is a good start to the demo. Write the new password on your notes before the viva.

### One continuous demo (checklist)

1. Login `admin` / `Atlas@2026`, set a new password (portion 4).
2. Menu `4` Documents: add `Safety Checklist` (type 3, status 1), see ID 4006, try the same again (refused) (portion 1).
3. Menu `1` Arsenal: add `Relay Unit RU-2`, update its quantity, try to delete `1001` (refused), delete `1007` (portion 2).
4. Menu `3` Inspections: add `Spot Check` for `1004` with inspector `2005` (refused), `2002` (accepted) (portion 3).
5. Menu `5` Reports: inspection summary, export (portion 3).
6. Menu `6` Change password, menu `7` Exit; show the log; run `./test.sh` (portion 4).

### Cross-questions every member must be able to answer

| Question | Who answers first | Where the answer is |
|----------|-------------------|---------------------|
| What are the model classes and the manager classes? | everyone | `VIVA_NOTES.md` section 2 |
| How is data saved and loaded? | everyone | section 4.6 |
| What is CRUD and where is each part? | everyone | section 4.2, 4.3 |
| Why does the program not crash on bad input? | everyone | section 8.4 |
| What is the time complexity of searching? | everyone | section 9 |
| What does your teammate's extension do? | everyone | read the pull request |

---

## 10. Git workflow for four people

* The existing history was created from one account. For your extension, create **your own branch** and commit with **your own name and email** so the history shows who did what:

  ```bash
  git config user.name "Your Name"
  git config user.email "your-github-email@example.com"
  git switch -c feature/<short-name>
  # make the change, run ./test.sh
  git add -A
  git commit -m "Add view documents by status"
  git push -u origin feature/<short-name>
  ```

  Then open a **pull request** on GitHub. One teammate reviews it before merging into `main`.
* One commit per idea. Write messages in plain words: *what* changed and *why*.
* Always run `./test.sh` before pushing, and add at least one test for your extension.
* Do not edit someone else's portion files without telling them. Merge conflicts in a shared file (for example `AtlasTests.java`) are normal: pull `main` first and resolve them carefully.
* Never commit `data/users.txt`, `data/atlas.log`, `out/` or `target/` (they are in `.gitignore`).

---

## 11. Before you present

* **Explain, don't recite.** Each member must run their demo without notes and explain any line of their files in plain words.
* **Be honest about the origin of the code.** The project was produced with AI assistance. Check what your course allows, and in the viva describe your contribution accurately: what you studied, what you tested, and the extension you wrote yourself. Teachers value honesty plus real understanding far more than polished memorised answers.
* **Practise swapping**: every member answers at least one basic question about every other portion.
* **Know the limits**: the "known limitations" in `README.md` and the "trap questions" in `VIVA_NOTES.md` section 10 are better admitted than discovered.
* **Have a plan B**: if the live demo fails, `VIVA_NOTES.md` section 12 lists what to do.
