# ATLAS - Viva Notes

Everything here uses ordinary Core Java. If you can explain these notes you can explain the whole project.
The companion file [`TEAM_PORTIONS.md`](TEAM_PORTIONS.md) says **who studies what and in which order**; this file holds the **explanations, traces and model answers**.

## Contents

1. [The project in 60 seconds and in 5 minutes](#1-the-project-in-60-seconds-and-in-5-minutes)
2. [Architecture: the big picture](#2-architecture-the-big-picture)
3. [Class by class](#3-class-by-class)
4. [Step-by-step traces](#4-step-by-step-traces)
5. [Key code, explained](#5-key-code-explained)
6. [OOP concepts: where to find each](#6-oop-concepts-where-to-find-each)
7. [Glossary](#7-glossary)
8. [Viva questions by portion](#8-viva-questions-by-portion)
9. [General and team questions, complexity](#9-general-and-team-questions-complexity)
10. [Trap questions and honest answers](#10-trap-questions-and-honest-answers)
11. [Live-coding requests you may get](#11-live-coding-requests-you-may-get)
12. [If something goes wrong in the demo](#12-if-something-goes-wrong-in-the-demo)
13. [What is different from the PPT](#13-what-is-different-from-the-ppt)
14. [How the tests work](#14-how-the-tests-work)

---

## 1. The project in 60 seconds and in 5 minutes

### 60 seconds

"ATLAS is a console application written in Core Java that manages **fictional** arsenal records. It has four modules - arsenal, personnel, inspections and documents - each with create, read, update and delete. While the program runs, records are objects in `ArrayList`s; they are saved in plain text files so nothing is lost when the program closes. You must log in first; passwords are stored hashed. All input is validated, so wrong input cannot crash the program or put bad data in the records. There are reports, an audit log and automated tests."

### 5 minutes (say it in this order)

1. **Problem:** manual records are scattered, hard to search, easy to duplicate and have no accountability (slide 3).
2. **Solution:** one console program with four record modules, reports and login (slide 5).
3. **How it is built:** *model classes* hold data, *manager classes* hold an `ArrayList` of models and do the work, `Main` connects them (section 2).
4. **How data is stored:** `ArrayList` in memory, text file on disk, saved after every change (section 4.6).
5. **What makes it safe:** validation on every input, duplicate checks, protected deletes (linked records), hashed passwords, audit log (section 8.4).
6. **How we know it works:** 76 automated checks (section 14).
7. **What next:** database (JDBC and MySQL), GUI (Swing), roles, encryption.

---

## 2. Architecture: the big picture

```
                 USER (keyboard and screen)
                          |
                    InputHelper  <-- every question goes through here
                          |
                        Main  --- AuthManager (login, passwords)
                          |
   +----------+-----------+------------+-----------+
   |          |           |            |           |
Arsenal   Personnel   Inspection   Document     Report
Manager   Manager     Manager      Manager      Manager
   |          |           |            |        (reads the other four)
ArsenalRecord Personnel Inspection  Document
   \__________|___________|____________/
              |
         BaseRecord   <- parent class of the four record types
              |
          FileHelper  ---->  data/*.txt files
          AuditLog    ---->  data/atlas.log
```

### Two kinds of classes

| Kind | Classes | Job |
|------|---------|-----|
| **Model** | `BaseRecord`, `ArsenalRecord`, `Personnel`, `Inspection`, `Document` | Hold the data of **one** record; know how to turn themselves into a file line and a table row |
| **Manager** | `ArsenalManager`, `PersonnelManager`, `InspectionManager`, `DocumentManager`, `ReportManager`, `AuthManager` | Hold **many** records (an `ArrayList`), load and save them, add, find, update, delete, show |
| **Helper** | `InputHelper`, `FileHelper`, `AuditLog` | Shared tools used by everybody (safe input, safe files, history) |
| **Start** | `Main` | Login, main menu, creates and connects the managers |

### Two layers inside every manager

* **Core methods** (`find`, `nextId`, `addRecord`, `removeById`, counts): no keyboard, no printing. They can be tested automatically.
* **Screen methods** (`addScreen`, `viewAllScreen`, ...): ask the user, print, then call the core methods.

### Files on disk

| File | Content | Written by |
|------|---------|-----------|
| `data/records.txt` | arsenal records | `ArsenalManager` |
| `data/personnel.txt` | personnel | `PersonnelManager` |
| `data/inspections.txt` | inspections | `InspectionManager` |
| `data/documents.txt` | documents | `DocumentManager` |
| `data/users.txt` | users with salt and hash | `AuthManager` |
| `data/atlas.log` | history of actions | `AuditLog` |
| `data/reports/*.txt` | exported reports | `ReportManager` |

Record file format: one record per line, fields separated by `|`, lines starting with `#` are comments. Example:
`1001|Guidance Module GM-12|Guidance Module|Site Alpha|24|Active`

### How the records link together

```
Inspection 3001 ----arsenalId----> ArsenalRecord 1001
            \-----inspectorId----> Personnel 2002
```

An inspection needs an existing arsenal item and an Active inspector. In reverse, an arsenal item or a person that is used by an inspection cannot be deleted.

---

## 3. Class by class

| Class | Lines | Job | Java concepts you can point at |
|-------|-------|-----|-------------------------------|
| `BaseRecord` | 120 | Parent of all records: ID, name, status, shared checks | abstract class, encapsulation, constructor, `static` helpers, exceptions |
| `ArsenalRecord` | 78 | One inventory record | `extends`, `super`, `@Override`, `static final` arrays |
| `Personnel` | 75 | One staff member | same pattern, clearance range check |
| `Inspection` | 75 | One inspection (links two IDs) | same pattern, date check |
| `Document` | 57 | One document record | same pattern |
| `ArsenalManager` | 319 | CRUD for arsenal | `ArrayList`, linear search, `switch`, `do-while` |
| `PersonnelManager` | 320 | CRUD for personnel | same |
| `InspectionManager` | 384 | CRUD for inspections, checks links | objects using other objects (constructor parameters) |
| `DocumentManager` | 292 | CRUD for documents | same as arsenal |
| `ReportManager` | 176 | Statistics, text reports, export | `StringBuilder`, `String.format`, loops |
| `AuthManager` | 237 | Users, password hashing, login | nested class, hashing, `SecureRandom` |
| `InputHelper` | 125 | Safe keyboard input | `Scanner`, `while(true)`, `try/catch` |
| `FileHelper` | 106 | Safe file read and write | try-with-resources, `IOException`, `File` |
| `AuditLog` | 35 | History of actions | `static`, date formatting |
| `Main` | 93 | Starts everything, main menu | `switch`, `do-while`, object creation |
| `AtlasTests` | 238 | Self-checking tests | `System.setIn`, lambdas (tests only) |

---

## 4. Step-by-step traces

Use these to *tell the story* of the program. Method names are real; open the file and follow along.

### 4.1 Starting the program

1. `Main.main` calls `runApplication()` inside a `try`, to catch `NoSuchElementException` (input closed).
2. `printBanner()` prints the title.
3. `new AuthManager()` -> `load()` reads `data/users.txt`. If there are no users, the default `admin` account is created (with "must change password" set to true).
4. `authManager.login()` asks for username and password up to 3 times (details in 4.4). If it fails, the program prints "Too many failed attempts" and ends.
5. After a good login, `Main` creates the managers. Each manager's **constructor calls `load()`**, which reads its file with `FileHelper.readLines`, turns each line into an object with `fromFileLine`, and adds it to the `ArrayList`. A damaged line prints a warning and is skipped.
6. `InspectionManager` is created with the arsenal and personnel managers as parameters (it needs them to check links).
7. `setInspectionManager(...)` gives the arsenal and personnel managers a reference back, so their delete screens can ask "is this item used by an inspection?".
8. The `do-while` menu loop runs until the user chooses 7. Each choice calls `run()` of one manager.
9. On exit `AuditLog.log("LOGOUT", ...)` records the end of the session.

### 4.2 "Add an arsenal record" (create)

1. Main menu `1` -> `arsenalManager.run()` shows the module menu; `1` -> `addScreen()`.
2. `InputHelper.readText("Item name...", 30)` repeats until the text is non-empty, at most 30 characters and has no `|`.
3. `InputHelper.readChoice(...)` shows a numbered list of categories and returns the chosen one (no typing, so no spelling mistakes). The same for status.
4. `InputHelper.readInt(...)` repeats until a whole number in the allowed range is typed.
5. `new ArsenalRecord(nextId(), name, category, location, quantity, status)`:
   * `nextId()` loops over the list and returns *highest ID + 1* (or 1001 for an empty list).
   * The constructor calls `super(...)` -> `BaseRecord` checks the ID, `setName` -> `cleanText`, `setStatus` -> `checkOneOf`.
   * Then `setCategory`, `setLocation`, `setQuantity` check their values. Any problem throws `IllegalArgumentException`.
6. `addRecord(record)`: returns `false` if the ID exists or the same name and location exist (`isDuplicate`).
7. Otherwise `records.add(record)`, then `save()`: every record is converted with `toFileLine()` and `FileHelper.writeLines` rewrites the whole file.
8. `AuditLog.log("ADD", ...)` appends a line to `atlas.log`.
9. The screen prints "Record added with ID 1007".

### 4.3 "Delete an arsenal record" (delete, with protection)

1. Module menu `6` -> `deleteScreen()`.
2. `askForRecord(...)` reads an ID (0 = cancel) and calls `find(id)`. If nothing is found it prints a message and returns `null`.
3. `inspectionManager.isArsenalReferenced(id)` loops over all inspections. If any inspection uses this item, the delete is **refused** with advice to set the status to Decommissioned instead.
4. Otherwise the record's row is shown and `InputHelper.confirm("Delete this record permanently?")` asks y/n.
5. On `y`: `removeById(id)` finds the position with an indexed `for` loop, calls `records.remove(i)`, calls `save()` (the file is rewritten without that line) and logs "DELETE".

### 4.4 Logging in

1. `login()` loops `attempt = 1..3`.
2. `InputHelper.readText` reads the username; `InputHelper.readPassword` reads the password (hidden in a real terminal).
3. `authenticate(username, password)`:
   * `findUser` searches the user list ignoring case.
   * Unknown user: it still computes a hash (so the time taken does not reveal that the name does not exist) and returns `false`.
   * Known user: hash the typed password **with that user's salt** using PBKDF2, compare with the stored hash using `MessageDigest.isEqual`.
4. Success: `AuditLog.setUser(name)`, log "LOGIN success". If `mustChange` is true (first login) the user must set a new password: it must match when typed twice, follow the rules (8+ characters, a letter, a digit, not the username) and differ from the old one.
5. Failure: log "LOGIN failed", print attempts left. After the 3rd failure: log the lockout and return `null`; `Main` ends the program.

### 4.5 "Add an inspection" (linked records)

1. `addScreen()` reads the title (max 30 characters).
2. `askForArsenal()` loops: reads an ID; `0` cancels; `arsenalManager.find(id)` must find an item, else "No arsenal record with ID ..." and ask again.
3. `askForInspector()` loops: the person must exist **and** have status `Active` (otherwise a specific message is shown).
4. `InputHelper.readDate` repeats until a real date in `yyyy-MM-dd` form is typed (`2026-02-30` is refused).
5. `InputHelper.readChoice` gives the result (Pending, Passed, Failed).
6. `new Inspection(nextId(), ...)` then `addInspection(inspection)` returns `false` if: the ID exists, the arsenal item does not exist, the inspector does not exist, or an inspection with the same title, item and date exists. (The screen already checks the first two existence rules while asking, but the core method checks again so it is safe on its own and testable.)
7. On success: add, save, log.

### 4.6 Saving and loading

**Save (object -> text):**

```
ArsenalRecord(1001, "Guidance Module GM-12", "Guidance Module", "Site Alpha", 24, "Active")
        |  toFileLine()
        v
1001|Guidance Module GM-12|Guidance Module|Site Alpha|24|Active
```

* The manager collects one line per record and calls `FileHelper.writeLines(fileName, headerComment, lines)`.
* `writeText` writes the content to `file.tmp` first and then moves it over the real file. If the program crashes while writing, the real file is still the old complete one.

**Load (text -> object):**

1. `FileHelper.readLines` returns the lines that are not blank and do not start with `#`.
2. `fromFileLine(line)` splits the line with `split("\\|", -1)` and checks that there are exactly the expected number of fields.
3. Numbers are converted with `Integer.parseInt`; the constructor validates everything again.
4. A bad line (wrong field count, text instead of a number, unknown status, impossible date) throws `IllegalArgumentException`; the manager prints "invalid line skipped" and continues.

---

## 5. Key code, explained

### Linear search (`find`)

```java
public ArsenalRecord find(int id) {
    for (ArsenalRecord record : records) {
        if (record.getId() == id) {
            return record;
        }
    }
    return null;
}
```
Look at every record from the first; return it when the ID matches; return `null` if the loop ends. Worst case n comparisons: O(n).

### Next ID

```java
int highest = FIRST_ID - 1;
for (ArsenalRecord record : records) {
    if (record.getId() > highest) highest = record.getId();
}
return highest + 1;
```
Weakness: if you delete the record with the highest ID, the next new record reuses that number.

### Delete

```java
for (int i = 0; i < records.size(); i++) {
    if (records.get(i).getId() == id) {
        records.remove(i);
        save();
        return true;
    }
}
return false;
```
An indexed loop is used because `remove(i)` needs the position.

### Safe number input

```java
while (true) {
    System.out.print(prompt);
    String text = scanner.nextLine().trim();
    try {
        int number = Integer.parseInt(text);
        if (number >= min && number <= max) return number;
    } catch (NumberFormatException e) { }
    System.out.println("  Please enter a whole number from " + min + " to " + max + ".");
}
```
Read a whole line, convert it, and ask again on any problem. This avoids `nextInt()`, which crashes on letters and leaves the Enter key in the buffer.

### Inheritance and polymorphism

```java
public abstract class BaseRecord { ... public abstract String toRow(); }
public class Document extends BaseRecord {
    @Override public String toRow() { return String.format("%-5d  %-35s ...", ...); }
}
BaseRecord r = new Personnel(...);   // a Personnel used through its parent type
r.display();                          // calls display() in BaseRecord -> toRow() of Personnel
```

### Safe file write

```java
try (FileWriter writer = new FileWriter(temp, StandardCharsets.UTF_8)) {
    writer.write(content);
}                                   // file closed automatically
Files.move(temp.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
```

### Password check

```java
String attempt = hash(password, user.salt);                 // PBKDF2 of password + salt
return MessageDigest.isEqual(attempt.getBytes(), user.hash.getBytes());
```

---

## 6. OOP concepts: where to find each

| Concept | Where | How to say it |
|---------|-------|---------------|
| **Class and object** | `new ArsenalRecord(...)` in `ArsenalManager.addScreen` | "A class is the blueprint; each record in the list is an object made from it." |
| **Constructor** | `Document(int id, String name, ...)` | "It runs when `new` is used and sets up the fields; it calls `super(...)` first." |
| **Encapsulation** | every field is `private`; `setQuantity` rejects negative numbers | "Other classes cannot put wrong values in; they must use the setters that check." |
| **Inheritance** | `ArsenalRecord extends BaseRecord` (and 3 more) | "The ID, name and status code is written once in the parent." |
| **Abstraction** | `BaseRecord` is abstract; managers hide how data is stored behind `find`, `addRecord`, `removeById` | "The menu code says *what* to do, not *how* the file is written." |
| **Polymorphism** | `toRow()` and `toFileLine()` are declared abstract in `BaseRecord`, written differently in each child | "Same method name, different behaviour depending on the real object." |
| **Overriding** | `@Override` on `toRow`, `toFileLine` | "The child replaces the parent's abstract method." |
| **`static`** | `ArsenalRecord.CATEGORIES`, all methods of `InputHelper` | "Belongs to the class, not to one object." |
| **`final`** | `private final int id`, `static final` constants | "Cannot be changed after it is set." |
| **Exceptions** | `IllegalArgumentException` in setters, `IOException` in `FileHelper` | "A way to say 'this is wrong' and handle it somewhere else." |
| **Collections** | `ArrayList<ArsenalRecord>` | "A list that grows by itself." |

---

## 7. Glossary

| Word | Plain meaning |
|------|---------------|
| **abstract class** | A class that cannot be created directly; only its children can. It may declare methods that children must write. |
| **access modifier** | `public` (everyone), `protected` (the class, children, same package), `private` (only inside the class). |
| **array** | A fixed-size list: `String[] CATEGORIES`. |
| **`ArrayList`** | A list that grows and shrinks automatically. |
| **bytecode / `.class` file** | The compiled form of a `.java` file, produced by `javac` and run by the JVM. |
| **checked / unchecked exception** | Checked (`IOException`) must be caught or declared; unchecked (`IllegalArgumentException`, `NumberFormatException`) need not be. |
| **class / object** | Blueprint / one thing built from the blueprint. |
| **constructor** | Special method with the class name; runs on `new`. |
| **defensive copy** | Returning a copy of a list (`new ArrayList<>(records)`) so others cannot change the original. |
| **encapsulation** | Hiding fields (`private`) and giving controlled access (getters, setters). |
| **`final`** | Cannot be changed (variable) or overridden (method). |
| **for-each loop** | `for (ArsenalRecord r : records)`: visit every element. |
| **hash** | A fixed-length fingerprint of data; cannot be reversed. |
| **inheritance** | A class gets the fields and methods of its parent with `extends`. |
| **JDK / JRE / JVM** | Development kit (has `javac`) / runtime / the virtual machine that runs bytecode. |
| **linear search** | Check items one by one from the start. O(n). |
| **null** | "Nothing": a reference that points to no object. `find` returns it when no record matches. |
| **override** | A child class writes its own version of a parent's method. |
| **package** | A folder-like group of classes; here `com.atlas`. |
| **polymorphism** | One method name, different behaviour depending on the object's real class. |
| **regex** | A pattern language; `\\|` in `split` means "the literal `|` character". |
| **salt** | A random value added to a password before hashing, so equal passwords give different hashes. |
| **`Scanner`** | Reads text typed on the keyboard. |
| **`static`** | Belongs to the class itself, not to an object. |
| **`StringBuilder`** | A tool to build a long text efficiently by appending pieces. |
| **`super`** | Refers to the parent class; `super(...)` calls its constructor. |
| **`switch`** | Chooses a block of code by comparing a value to `case` labels. |
| **`this`** | The current object. |
| **try-with-resources** | `try (File f = ...) { }`: closes the file automatically. |
| **unit test** | A small program that checks one piece of code gives the expected result. |

---

## 8. Viva questions by portion

### 8.1 Portion 1 - Data model and Documents

| Question | Model answer |
|----------|--------------|
| Why does `BaseRecord` exist? | All four record types have an ID, a name and a status. Writing that once in a parent class removes repeated code and keeps the rules (for example "name cannot be empty") in one place. |
| Why is it `abstract`? | We never need a plain "record", only specific kinds. Abstract also forces every child to write `toRow()` and `toFileLine()`. |
| What does `super(id, name, status, STATUSES)` do? | It calls the parent constructor to set the ID, name and status and to give the parent the list of allowed statuses for this kind of record. |
| Where is encapsulation used? | All fields are `private`. To change a value you call a setter, and the setter checks it (for example `setQuantity` rejects negative numbers, `setStatus` accepts only allowed values). |
| What is polymorphism here? | `display()` is written once in the parent and calls `toRow()`. Each child has its own `toRow()`, so the right one runs for the real object. |
| What does `@Override` do? | It tells the compiler "I am replacing a parent method"; if the name or parameters do not match, the compiler reports an error. |
| Why `static final String[]` for statuses? | They belong to the class (not to one object) and never change. One list serves the menus, the validation and the reports. |
| Why is `id` `final` but `name` has a setter? | The ID identifies the record forever; other records point to it. A name may be corrected. |
| What happens if a file line has the wrong number of fields? | `splitLine` throws `IllegalArgumentException`; the manager prints "invalid line skipped" and continues. |
| Why can the delimiter `\|` not appear in names? | It would split one field into two when the line is read back. `cleanText` and `InputHelper.readText` refuse it. |
| Why `split("\\|", -1)`? | `\|` is a special character in patterns, so it is escaped as `\\|`. The `-1` keeps empty fields at the end, so a missing last field is detected. |
| How does a `Document` get its date? | `DocumentManager.addScreen` uses `LocalDate.now()`, so the user cannot enter a wrong date. |
| What is the difference between `fromFileLine` and the constructor? | `fromFileLine` cuts the text into fields and converts numbers, then calls the constructor, which validates every value. |

### 8.2 Portion 2 - Arsenal and Personnel CRUD

| Question | Model answer |
|----------|--------------|
| Why `ArrayList` instead of an array? | The number of records is not known in advance. An `ArrayList` grows and shrinks automatically. |
| How does search work? | A linear search: loop over the list and compare IDs until one matches. Worst case n steps, O(n). It is fine for small data. |
| Why separate `addRecord` from `addScreen`? | `addRecord` has no keyboard or printing, so it can be tested automatically. `addScreen` only asks the user and calls it. |
| How is a new ID chosen? | Highest existing ID plus one (1001 if empty). This prevents duplicate IDs. Weakness: if the highest ID is deleted, the next record reuses it. |
| How do you stop duplicate records? | Before adding, `isDuplicate` compares name and location (ignoring case). When updating, `excludeId` makes sure a record is not compared with itself. |
| Why does `getAll()` return a copy? | If it returned the real list, any class could clear or change the manager's data by accident (defensive copy). |
| What does `save()` do and when is it called? | It converts every record to a line and rewrites the whole file. It is called after every add, update and delete. |
| Why do you ask y/n before deleting? | Deleting is permanent. The confirmation prevents mistakes. |
| Why can't I delete item 1001? | An inspection refers to it. Deleting it would leave an inspection pointing to nothing. We refuse and suggest changing the status to Decommissioned. |
| What happens if the program is closed without choosing exit? | Nothing is lost: data was already saved after the last change. |
| What does `do-while` do in `run()`? | It shows the menu at least once and repeats until the user chooses 0. |
| Why `switch` and not many `if` statements? | The choice is one value compared with several constants; `switch` is shorter and clearer. |
| What is the ternary in `found == 0 ? "No matching records." : found + " match(es)."`? | A short `if-else` that gives one of two values. |
| Why is search by name case-insensitive? | Both the keyword and the name are converted with `toLowerCase()` before `contains`. |

### 8.3 Portion 3 - Inspections and Reports

| Question | Model answer |
|----------|--------------|
| Why does `InspectionManager` take two managers in its constructor? | To check that the arsenal item and the inspector exist before saving an inspection. One object uses another through a reference it is given. |
| When does `addInspection` return false? | The ID already exists, the arsenal item does not exist, the inspector does not exist, or the same title, item and date already exists. |
| Why must an inspector be Active? | A retired person or one on leave cannot carry out an inspection. This is a business rule checked in `askForInspector`. |
| Why can't an item with inspections be deleted? | It would leave "orphan" inspections. `isArsenalReferenced` checks this before deleting. |
| How is the pass rate calculated? | passed x 100 / (passed + failed). Pending inspections are not completed, so they are excluded. If there are no completed inspections the report says so (this also avoids dividing by zero). |
| How do you find the latest date when dates are text? | Dates are saved as `yyyy-MM-dd`; this format sorts alphabetically in date order, so `compareTo` works. |
| Why does `ReportManager` not change any data? | Its job is only to read and summarise. It uses the other managers' counting methods and `getAll()` copies. |
| What is `StringBuilder` for? | To build a long text step by step. Joining strings with `+` in loops creates many temporary objects. |
| What does `%.1f%%` print? | A decimal number with one digit after the point, followed by a `%` sign (`%%` prints one percent character). |
| How is a report exported? | `exportOverview` builds the text, then `FileHelper.writeText` saves it in `data/reports/` with the date and time in the file name. |
| What if someone edits `inspections.txt` by hand and points to a missing item? | Loading does not check links (only format and duplicate IDs), so the record loads; `printLinkedNames` then shows "(not found)". Links are enforced when adding, not when loading. It is a known limitation; a database with foreign keys would enforce it. |

### 8.4 Portion 4 - Login, input, files, startup and build

| Question | Model answer |
|----------|--------------|
| Why not use `nextInt()`? | It crashes on letters and leaves the Enter key in the input, which spoils the next `nextLine()`. We read a whole line and convert it with `Integer.parseInt` inside `try/catch`. |
| What does `InputHelper` guarantee? | Every method keeps asking until the answer is valid, so callers always receive a valid value. |
| Why is there only one `Scanner`? | Creating several `Scanner`s on `System.in` can lose input. One shared `static` `Scanner` avoids that. |
| What is try-with-resources? | `try (BufferedReader r = ...) { }` closes the file automatically, also when an error happens. |
| Why write to a `.tmp` file first? | If the program crashes while writing, the real data file is still the complete old one. Only after a successful write is the new file moved into place. |
| What happens when a data file is missing? | `readLines` returns an empty list; the manager starts empty and creates the file at the first save. |
| Why do you store a hash instead of the password? | If the file is stolen, the passwords are not revealed; a hash cannot be turned back into the password. |
| What is a salt? | A random value stored with each user and mixed into the password before hashing. Two users with the same password get different hashes, and precomputed tables are useless. |
| Which hashing is used? | PBKDF2 with HMAC-SHA-256, 65,536 repetitions, 256-bit result: a password-hashing method built into Java. The repetition makes guessing passwords slow. |
| Why `MessageDigest.isEqual`? | It compares in constant time, so the time taken does not reveal how many characters matched. |
| Why does `authenticate` hash even for an unknown user? | So the response time is the same for known and unknown names. |
| What are the password rules? | At least 8 characters, a letter, a digit, not equal to the username. |
| What happens after 3 wrong passwords? | The program logs the lockout and exits. |
| Why `System.console()`? | It reads a password without showing it. In IDE consoles it is `null`, so the program falls back to `Scanner` (visible). |
| What does `AuditLog` record? | Time, user, action and details of logins, adds, updates, deletes, exports and logout. Never passwords. |
| Why `static` methods and a `private` constructor in the helpers? | They need no objects; a private constructor stops anyone creating one. |
| What does `-Datlas.data=...` do? | It tells `FileHelper` to use another data folder. The tests use it so they never touch real data. |
| What does `Main` catch `NoSuchElementException` for? | It occurs when the input ends (Ctrl+D). The program exits cleanly instead of printing a stack trace. |
| What is a `.class` file? | The compiled bytecode that `javac` makes from a `.java` file; the JVM runs it. Not committed to Git. |
| What does `pom.xml` do? | It tells Maven the project's name, Java version, main class and how to package a runnable jar. We have no libraries, so no dependencies. |

---

## 9. General and team questions, complexity

### 9.1 General questions

| Question | Model answer |
|----------|--------------|
| Why Java? | Object-oriented, a rich standard library (collections, files), runs on any computer with a JVM. |
| Which OOP concepts did you use? | Classes and objects, encapsulation, abstraction, inheritance and polymorphism. |
| What is CRUD? | Create, Read, Update, Delete. For each module: add, view and search, update a field, delete. |
| How is information stored? | In `ArrayList`s while running and in text files for permanent storage. |
| What happens when the program closes? | The lists in memory disappear, but every change was already saved to the files, so the next start loads them again. |
| Which exceptions do you handle? | `NumberFormatException` (input), `IllegalArgumentException` (invalid values and damaged lines), `IOException` (files), `NoSuchElementException` (input closed). |
| Why no database? | Phase 1 focuses on Core Java. A database (JDBC and MySQL) is the planned Phase 2. |
| How will you improve it? | JDBC and MySQL, a Swing interface, roles (admin and viewer), encrypted files. |
| Is this a real weapons system? | No. It is a fictional administrative records prototype with made-up data. |
| How did you test it? | 76 automated checks in `AtlasTests` plus manual runs of every menu (section 14). |
| What is the project structure? | `src/main/java/com/atlas` code, `src/test/java/com/atlas` tests, `data` files, `pom.xml` build file, scripts to run and test. |

### 9.2 Complexity (time) summary

| Operation | How | Complexity |
|-----------|-----|-----------|
| Find a record by ID | linear search | O(n) |
| Add a record | duplicate check (loop) + `ArrayList.add` | O(n) (the add itself is O(1) amortized) |
| Update a record | find, then setter | O(n) |
| Delete a record | find index, `remove(i)` (shifts the later items) | O(n) |
| View all | loop | O(n) |
| Search by name | loop with `contains` | O(n) |
| Save after a change | rewrites the whole file | O(n) |
| Each report figure | one counting loop | O(n) |
| `size()` | stored count | O(1) |

With a few hundred records all of this is instant. A `HashMap<Integer, Record>` would make find O(1), and a database would remove the "rewrite the whole file" cost; neither is needed at this scale.

---

## 10. Trap questions and honest answers

| Question | Honest answer |
|----------|---------------|
| Who wrote this code? | Answer truthfully for your team and your course rules. If AI tools were used, say so, and describe what *you* studied, tested and extended (your own extension and pull request). Then show that you understand your files. |
| Is plain text storage secure? | Records are **not** encrypted; anyone with file access can read them. Passwords are hashed. Encryption and a database with access control are future work. |
| Why not a `HashMap`? | The project scope uses `ArrayList` as the main structure and linear search. For this data size it is fast enough; with many thousands of records a `HashMap` or a database index would be better. |
| What would break with a million records? | Memory use, the O(n) search, and rewriting the whole file after every change. A database solves all three. |
| What if two people run the program at once? | Not supported: each would load its own copy and the last save would overwrite the other's changes (no file locking). A database is the answer. |
| What if someone edits a data file by hand? | Bad lines are skipped with a warning; duplicate IDs are skipped. Links between records are not checked when loading, so a hand-made inspection could point to a missing item (shows "(not found)"). |
| Why did you use 15 classes when the plan said 10? | Shared code (parent record, input, files, login, history) was moved into classes so it is written once. It is easier to read and to divide among four people. |
| Why auto-generated IDs? | The user cannot create duplicate IDs by typing one. |
| Why `\|` as the separator and not a comma? | Names and titles may contain commas; `\|` is refused in all input, so it can never appear in data. |
| Why is the password visible in my IDE? | IDE consoles do not provide a real terminal. In a normal terminal it is hidden. |
| Is PBKDF2 enough? | It is an accepted standard. Stronger choices (bcrypt, Argon2) need an extra library; for a console prototype PBKDF2 from the standard library is a good, explainable choice. |
| Why Java 17, when the slides say 21? | The code uses no feature newer than 17, so it runs on both. |
| Why are the tests not JUnit? | We kept the project free of external libraries. The tests are plain Java with `check(name, condition)`; moving them to JUnit later is easy. |
| Reuse of IDs after deleting the last record? | Yes: `nextId` is highest plus one. With a database we would use an auto-increment key that never reuses numbers. |
| Can a deleted inspection be recovered? | No. There is no undo; the audit log only says that it was deleted. |

---

## 11. Live-coding requests you may get

A teacher may say "add this feature now". Know *where* to go. All files are in `src/main/java/com/atlas/`.

| Request | Where and how |
|---------|---------------|
| Add a new status (for example `Rejected`) to documents | `Document.STATUSES`: add the word to the array. Menus, validation and reports show it (reports loop over this array). |
| Change the minimum password length | `AuthManager.MIN_PASSWORD_LENGTH`. Also update the rules message if it hardcodes a number (it uses the constant). |
| Change the number of login attempts | `AuthManager.MAX_ATTEMPTS`. |
| Allow longer names | The second argument of `InputHelper.readText(..., 30)` in the manager's `addScreen` and `updateScreen`, **and** the column width in the model's `toRow()` and `header()` (for example `%-30s`). |
| Add a menu option | In the manager's `run()`: print the new line, increase the maximum in `InputHelper.readInt(..., 0, 6)`, add a `case`, write the new screen method. |
| Count records with a condition (for example quantity above 20) | Copy the pattern of `countByStatus`: loop, `if`, `count++`, `return count`. |
| Show only the records of a status | Loop over the list and print only those where `getStatus().equals(chosen)`. |
| Sort records by name | Simplest explanation: `records.sort((a, b) -> a.getName().compareToIgnoreCase(b.getName()));` (a lambda that says how to compare two records). Without lambdas: a selection sort with two nested loops swapping elements. |
| Add a new field (for example `owner` to `Document`) | In the model: field, getter, setter with validation, constructor parameter, `toFileLine`, `toRow`, `header`, and the field count in `fromFileLine`. In the manager: ask for it in `addScreen`, optionally in `updateScreen`. In `data/documents.txt`: add the new field to every line. In `AtlasTests`: adjust any constructor calls. |
| Make the search by ID show "not found" differently | Edit `askForRecord` in the manager. |
| Explain this line | Use the traces in section 4 and the snippets in section 5. |

---

## 12. If something goes wrong in the demo

| Problem | What to do |
|---------|-----------|
| Compile error after you edited something | `git restore .` returns every file to the last saved version; run again. |
| Data looks wrong after experiments | `git restore data`, then delete `data/atlas.log` and `data/reports` by hand if you want a clean start. |
| Forgot the password | Delete `data/users.txt` and restart: the default account `admin` / `Atlas@2026` is created again and must be changed at login. |
| "java: command not found" or an error about the version | Install a JDK 17 or newer (`java -version` and `javac -version` must both work). |
| "Could not find or load main class" | Run from the project folder with `./run.sh` (or `run.bat`), which compiles first. |
| Warnings "invalid line skipped" | A data line is damaged (wrong fields, bad status, bad date). Explain: the program keeps running and skips only that line. |
| Program printed "Input closed. Exiting ATLAS." | The input ended (Ctrl+D or piped input). Start it again from a normal terminal. |
| Password visible when typing | You are in an IDE console; use the terminal. |
| Everything fails | Have a backup: a screen recording of a successful run and the output of `./test.sh` (76 PASS). |

---

## 13. What is different from the PPT

| PPT | Project | Why |
|-----|---------|-----|
| 10 classes | 15 classes | Shared code moved into `BaseRecord`, `InputHelper`, `FileHelper`, `AuthManager`, `AuditLog` |
| "Inheritance can be added later" | Inheritance is used (`BaseRecord`) | Removes duplication; real example of polymorphism |
| Main menu has 6 options (6 = Exit) | 7 options (6 = Change Password, 7 = Exit) | Changing a password is a standard requirement |
| IDs typed by the user | IDs generated automatically | Prevents duplicate IDs |
| Java 21 | Java 17 or newer | No Java 21 feature is needed |
| Authentication: "login" | Hashed passwords, forced first change, lockout | Basic industry practice |
| Not in the PPT | Audit log, report export, linked records, automated tests | Accountability, usability, correctness |

Update these slides: **5** (modules and classes), **7** (architecture), **10-11** (concepts, inheritance now used), **15** (menu), and the class count in Part B/E of the plan.

---

## 14. How the tests work

* `AtlasTests` is an ordinary Java program with a `main` method. Run it with `./test.sh` (Windows: `test.bat`).
* `check("description", condition)` prints `PASS` or `FAIL` and counts them; at the end the program exits with an error code if anything failed.
* `checkRejected("description", () -> code)` passes when the code throws `IllegalArgumentException`.
* The tests set `atlas.data` to a **temporary folder** first, so the real files in `data/` are never touched; the folder is deleted at the end.
* The input test replaces the keyboard with `System.setIn(new ByteArrayInputStream(...))` and feeds bad answers (letters, empty text, impossible dates) before a good one, to prove that `InputHelper` keeps asking.
* What is covered: the models (validation and save-and-load round trips), `FileHelper`, the four managers (add, duplicate, find, remove, reload from file, counts), linked inspections, reports, password rules, hashing and "passwords are not stored as plain text".
* Why we have them: when you change something (for example your extension), a failing test tells you immediately what you broke.
* Why not JUnit: to keep the project free of libraries; the structure is the same, and moving to JUnit later is simple.
