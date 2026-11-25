# ATLAS - Team Portions (4 members)

The project is split into **four portions of roughly equal size** (about 560-700 lines each, comments included).
Each portion is a set of whole files, so every member can open "their" files, run "their" part of the app,
explain it, and extend it. File names below are in `src/main/java/com/atlas/`.

| Portion | Theme | Files | Lines |
|---------|-------|-------|-------|
| **1** | Data model and Documents | `BaseRecord`, `ArsenalRecord`, `Personnel`, `Inspection`, `Document`, `DocumentManager` | ~700 |
| **2** | Arsenal and Personnel CRUD | `ArsenalManager`, `PersonnelManager` | ~640 |
| **3** | Inspections and Reports | `InspectionManager`, `ReportManager` | ~560 |
| **4** | Login, input, files and startup | `Main`, `AuthManager`, `InputHelper`, `FileHelper`, `AuditLog` | ~600 |

Everyone also reads `AtlasTests.java` (in `src/test/java/com/atlas/`): each portion lists the test methods that check it.

Suggested order of learning: **1 -> 2 -> 3 -> 4** is the order in which the code depends on each other,
but all four can study in parallel because the portions only meet at the small "hand-off" points listed below.

---

## 0. First, the folder structure (is it all basic Java?)

Yes. This is how every normal Java project is laid out.

| What you see | What it is |
|--------------|------------|
| `src/main/java/com/atlas/*.java` | The **source code** you write. `com/atlas` matches the first line of each file, `package com.atlas;`. A package is just a folder name that groups classes. |
| `.class` files (in `out/` or `target/classes/`) | The **compiled code**. The command `javac` turns each `.java` file into a `.class` file of *bytecode*. The Java Virtual Machine (`java` command) runs that bytecode. This is why Java runs on any computer that has a JVM. You never edit or submit `.class` files; they are rebuilt every time, and the `.gitignore` keeps them out of GitHub. |
| `pom.xml` | The Maven build file. It only says "this is Java 17, here is the main class, here is how to package it". Maven is the industry-standard build tool. |
| `src/main` and `src/test` | Maven's standard split between the real program and its tests. |
| `data/` | The text files that act as the database. |
| `run.sh`, `test.sh` | Shortcuts that run `javac` and `java` for you when Maven is not installed. |

Two-line summary to say in the viva: **"We write `.java` files, `javac` compiles them to `.class` bytecode, and the JVM runs the bytecode."**

### Which parts go beyond beginner Java?

The program is mostly what you listed in the PPT (classes, `ArrayList`, loops, `switch`, `Scanner`, files, exceptions).
A few places use slightly more advanced features. Each is marked **[hard bit]** below with a plain explanation,
so nobody is surprised:

* `abstract` class and `@Override` (BaseRecord and its children)
* `try-with-resources` and `Files.move` (FileHelper)
* PBKDF2 password hashing with `SecureRandom` and `HexFormat` (AuthManager) - the most advanced part
* `StringBuilder` and `String.format` (ReportManager, table rows)
* lambdas such as `() -> record.setQuantity(-1)` (only in the test file)

---

## Portion 1 - Data model and Documents

**You own:** `BaseRecord`, `ArsenalRecord`, `Personnel`, `Inspection`, `Document`, `DocumentManager`

**The story you tell:** "I designed what a record *is*. All four record types share an ID, name and status, so I wrote that once
in a parent class and the four children add their own fields. Then I built the simplest full CRUD module, Documents."

**Concepts you demonstrate**
* Class, object, constructor, `this`
* Encapsulation: private fields, getters, setters that validate
* Inheritance (`extends`, `super(...)`)
* Abstract class and polymorphism (`toRow()` and `toFileLine()` are written differently in each child)
* `static final` constants (the lists of allowed statuses and types)
* `ArrayList` CRUD, linear search, menu with `switch`

**Hard bits, in plain words**
* `abstract class BaseRecord`: a parent that is never created by itself, only through children. It forces each child to write its own `toRow()` and `toFileLine()`.
* `protected static String cleanText(...)`: a helper that children can call without creating an object. It trims text and rejects empty text, text that is too long, and the `|` character.
* `checkOneOf(...)`: checks a value against the allowed list and returns the correct spelling.
* `fromFileLine(...)`: reads one saved line and builds an object back from it.

**Demo path (2 minutes):** run the app -> `4` Document Management -> `1` add a document -> `2` view -> open `data/documents.txt` and show the new line -> back, try adding the same name and type again (it is refused).

**Tests to read:** `testModels`, `testDocumentManager`

**Hand-offs:** Portions 2 and 3 call your models' getters and setters. Portion 4's `FileHelper` is what your managers call to read and write files.

**Possible viva questions**
* Why is `BaseRecord` abstract? / Where is inheritance used? / What is polymorphism here?
* Why are the fields `private`? What does the setter check?
* What happens if I type a text instead of a number for quantity? (Portion 4's `InputHelper` stops it; and the setter would also throw an exception.)

**Make it yours (small extension, commit under your own name):** add menu option **"View documents by status"** to `DocumentManager`.
Ask for a status with `InputHelper.readChoice`, loop over the list, print only the matching rows.

---

## Portion 2 - Arsenal and Personnel CRUD

**You own:** `ArsenalManager`, `PersonnelManager`

**The story you tell:** "I wrote the two largest everyday modules. Records live in an `ArrayList` while the program runs
and are saved to a text file after every change. Search is a linear search."

**Concepts you demonstrate**
* `ArrayList<T>`: add, remove, loop, `size()`
* Linear search (`find(int id)`) and its O(n) cost
* CRUD split into "core" methods (`addRecord`, `removeById`) and "screen" methods (`addScreen`, `deleteScreen`)
* Menu loop with `do-while` and `switch`
* Duplicate checking and automatic IDs (`nextId` = highest ID + 1)
* Counting methods for reports (`countByStatus`, `countByDepartment`, ...)

**Hard bits, in plain words**
* `return new ArrayList<>(records)` in `getAll()`: gives other classes a *copy* so they cannot change your list by accident.
* `case 0: { ... }` with braces in the update screens: braces let you declare a variable inside one `case`.
* `setInspectionManager(...)`: the delete screen asks the inspection manager "is this item used?" so it can refuse. It is set after creation because the two managers need each other.

**Demo path:** `1` Arsenal Records -> add an item -> search by name (type part of the name) -> update its quantity -> try to delete `1001` (refused because an inspection uses it) -> delete your own new item (allowed, with a y/n question) -> show `data/records.txt` after each step.

**Tests to read:** `testArsenalManager`, `testPersonnelManager`

**Hand-offs:** You call `InputHelper` and `FileHelper` (Portion 4) and use the models (Portion 1). Portion 3 calls your `find()`, `getAll()` and counting methods.

**Possible viva questions**
* Why `ArrayList` instead of an array? / What is the time complexity of find? / What if the file has a damaged line?
* Why can the first item you add get ID 1007? / How do you stop duplicate records?
* What does `removeById` do after removing from the list? (saves the file)

**Make it yours:** add **"View low-stock items"** to `ArsenalManager`: ask for a number, show all records whose quantity is below it.
(For Personnel: **"View by department"**.)

---

## Portion 3 - Inspections and Reports

**You own:** `InspectionManager`, `ReportManager`

**The story you tell:** "Inspections are the only records that *depend on other records*. An inspection must point to an
existing arsenal item and an active inspector. I made sure those links are always valid, and then I built the reports
that count and summarise all the data."

**Concepts you demonstrate**
* Objects holding references to other objects (`InspectionManager` receives `ArsenalManager` and `PersonnelManager` in its constructor)
* Data integrity: no inspection without a valid item and inspector; items in use cannot be deleted
* Counting loops, percentages (pass rate), finding the latest date
* Building text with `StringBuilder`; formatted numbers
* Saving a report to a file

**Hard bits, in plain words**
* Constructor with parameters: `new InspectionManager(arsenalManager, personnelManager)` is how one object gets access to others.
* `askForArsenal()` / `askForInspector()`: keep asking until a valid ID is entered, or `0` cancels.
* `StringBuilder`: a text-building tool that is faster and tidier than joining strings with `+` in a loop.
* `String.format("%.1f%%", value)`: prints a number with 1 decimal place, followed by a percent sign (`%%` prints one `%`).
* Dates are saved as `yyyy-MM-dd` text; this format sorts correctly with `compareTo`, which is how the latest date is found.

**Demo path:** `3` Inspection Records -> add an inspection for arsenal `1004` with inspector `2005` (refused: On Leave) -> use `2001` -> view all -> `0` back -> `5` View Reports -> `3` Inspection summary (see pass rate and "never inspected" items) -> `6` export -> open the file in `data/reports/`.

**Tests to read:** `testInspectionManager`, `testReports`

**Hand-offs:** You use `find`, `getAll` and the count methods from Portion 2 (and the document counts from Portion 1). `Main` (Portion 4) creates your two objects.

**Possible viva questions**
* What is a "linked record" and why do you check it? / Why can't I delete an item that has inspections?
* How is the pass rate calculated? Why is Pending not counted?
* Why does `ReportManager` never change any data?

**Make it yours:** add a report **"Inspections per inspector"** (loop over personnel, count their inspections) or
**"Overdue pending inspections"** (Pending and date earlier than `LocalDate.now()`).

---

## Portion 4 - Login, input, files and startup

**You own:** `Main`, `AuthManager`, `InputHelper`, `FileHelper`, `AuditLog`

**The story you tell:** "I built everything that sits *around* the records: starting the program, logging in,
asking the user safely, reading and writing the files without losing data, and keeping a history of what happened."

**Concepts you demonstrate**
* `Scanner` input and why we read a whole line and convert (`Integer.parseInt` in `try/catch`)
* Exception handling (`NumberFormatException`, `IOException`, `NoSuchElementException`)
* File handling: `BufferedReader`, `FileWriter`, appending, comments in data files
* Authentication, password rules, login attempts
* `static` utility classes
* Program start-up: creating all the managers in `Main` and running the menu

**Hard bits, in plain words**
* `try-with-resources` (`try (BufferedReader reader = ...) { ... }`): the file is closed automatically, even if an error occurs.
* Writing to a `.tmp` file and then `Files.move`: if the program crashes while saving, the old file is still intact.
* **Password hashing** (the most advanced part): we never save the password. We save a random `salt` and a `hash` made with PBKDF2.
  At login we hash what was typed with the saved salt and compare. Treat the hash function as a "black box" that turns a password into a long string
  that cannot be turned back. A salt makes two equal passwords produce different hashes.
* `System.console()`: hides the typed password in a real terminal. In an IDE console it is `null`, and we fall back to `Scanner`.
* `static` in `AuditLog`: one shared log for the whole program, without creating an object.

**Demo path:** delete `data/users.txt` -> run the app -> log in as `admin` / `Atlas@2026` -> forced password change (try a weak one first) -> type `abc` in the main menu (it is rejected, no crash) -> open `data/atlas.log` and `data/users.txt` and show that the password is not readable.

**Tests to read:** `testInputHelper`, `testFileHelper`, `testAuth`

**Hand-offs:** Everyone else calls `InputHelper`, `FileHelper` and `AuditLog`. You call all four managers from `Main`.

**Possible viva questions**
* Why do you not use `nextInt()`? / What does try-with-resources do?
* Why store a hash and not the password? What is a salt?
* What happens after 3 wrong passwords? What if the input stream closes (Ctrl+D)?
* Where is a user's action recorded?

**Make it yours:** add main-menu option **"View recent activity"** that prints the last 10 lines of `data/atlas.log`
(use `FileHelper.readLines("atlas.log")` and print the last 10 entries).

---

## Working with Git as four people

* The current history was created by one account. For each "Make it yours" task, create a branch and **commit with your own name and email**:

  ```bash
  git config user.name "Your Name"
  git config user.email "you@example.com"
  git switch -c feature/<your-extension>
  # make the change, run ./test.sh, then
  git add -A && git commit -m "Add view documents by status"
  git push -u origin feature/<your-extension>
  ```
  Then open a pull request on GitHub for a teammate to review. Reviewing each other's pull requests is also a good way to learn the other portions.
* Keep each commit small (one feature, one fix).
* Run `./test.sh` before every push. Add one new test for your extension in `AtlasTests.java`.

## Before you present

* **Explain, don't recite.** Each member should be able to run their demo path without notes, and explain any line of their files in plain words.
* **Say honestly what you wrote and what you studied.** The project was generated with AI assistance. Check your course's rules on AI-assisted work, and
  describe your contribution accurately in the viva; the "Make it yours" extensions are the part that is truly yours.
* Practise swapping: each member should be able to answer one basic question about *another* portion. The viva often asks the whole team about the whole project.
