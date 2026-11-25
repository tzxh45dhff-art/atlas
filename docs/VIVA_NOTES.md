# ATLAS - Viva Notes

Everything here uses ordinary Core Java. If you can explain these notes you can explain the whole project.

## 1. The big picture in four sentences

1. Data lives in four text files in `data/`.
2. When the program starts, each **manager** reads its file and fills an `ArrayList` of **model** objects.
3. The user works through console menus; every add, update or delete changes the `ArrayList` and then rewrites the file.
4. `Main` only does the login and the main menu; the managers do the actual work.

## 2. Class by class

| Class | Job | Java concepts you can point at |
|-------|-----|-------------------------------|
| `BaseRecord` | Parent of all records: ID, name, status, and shared checks | abstract class, encapsulation (private fields, getters, setters), constructor, `static` helpers, exceptions |
| `ArsenalRecord`, `Personnel`, `Inspection`, `Document` | One record each | inheritance (`extends`), `super(...)`, method overriding (`toFileLine`, `toRow`), `static` constants and arrays |
| `ArsenalManager`, `PersonnelManager`, `InspectionManager`, `DocumentManager` | CRUD on an `ArrayList` + file load/save + menu | `ArrayList`, loops, linear search, `switch`, `do-while`, methods |
| `ReportManager` | Counts and percentages, builds report text, exports to file | `StringBuilder`, `String.format`, loops |
| `AuthManager` | Login, password rules, password hashing | nested class, `SecureRandom`, hashing, `boolean` logic |
| `InputHelper` | Asks the user and repeats until the answer is valid | `Scanner`, `while(true)`, `try/catch` for `NumberFormatException` |
| `FileHelper` | Reads and writes text files | try-with-resources, `BufferedReader`, `FileWriter`, `IOException` |
| `AuditLog` | Appends a line to `atlas.log` for every action | `static` methods, date and time formatting |
| `Main` | Login, main menu, creates the managers | `switch`, `do-while`, object creation, catching `NoSuchElementException` |

## 3. The OOP concepts, with where to find them

* **Class and object**: `new ArsenalRecord(...)` creates one record object.
* **Encapsulation**: every field is `private`; other classes use getters and setters, and the setters check the value (for example the quantity cannot be negative).
* **Inheritance**: the four record classes `extend BaseRecord`, so ID, name and status are written once.
* **Polymorphism**: `BaseRecord` declares `toRow()` and `toFileLine()` as `abstract`; each child class gives its own version. Code holding a `BaseRecord` can call `display()` and the correct child version runs.
* **Abstraction**: a manager's menu code calls `addRecord`, `find`, `removeById` without caring how they store data.

## 4. CRUD in this project (the part you will be asked to show)

| Operation | Where | Idea |
|-----------|-------|------|
| Create | `ArsenalManager.addScreen()` then `addRecord()` | ask for each field, make an object, check duplicates, `records.add`, save |
| Read | `viewAllScreen()`, `searchByIdScreen()`, `searchByNameScreen()` | loop over the list, print each row; search by ID uses `find()` |
| Update | `updateScreen()` | find the record, ask which field, call the setter, save |
| Delete | `deleteScreen()` then `removeById()` | find, ask for confirmation, `records.remove(i)`, save |

`find(int id)` is the linear search:

```java
for (ArsenalRecord record : records) {
    if (record.getId() == id) {
        return record;
    }
}
return null;
```

## 5. How data is saved and loaded

* Save: every record turns itself into one text line with `toFileLine()` (fields joined by `|`). The manager writes all lines to the file.
* Load: each line is cut with `split`, converted back to an object by `fromFileLine()`. Bad lines throw `IllegalArgumentException`, are skipped, and a warning is printed.
* The `|` character is refused in all input, so it can never break the format.
* `FileHelper.writeText` writes to `file.tmp` first and then moves it over the real file. A crash therefore cannot destroy the data file.

## 6. Login and passwords

* The password is **never** saved. For each user we save a random `salt` and a `hash` made with PBKDF2 (built into Java).
* At login we hash what was typed with the saved salt and compare. Same input gives same hash; the hash cannot be turned back into the password.
* First login with the default account forces a password change. Three wrong attempts end the program.

## 7. What is different from the PPT (be ready to explain)

| PPT | Project | Why |
|-----|---------|-----|
| 10 classes | 15 classes | Shared code moved into `BaseRecord`, `InputHelper`, `FileHelper`, `AuthManager`, `AuditLog` instead of being copied four times |
| "Inheritance can be added later" | Inheritance is used (`BaseRecord`) | Removes duplication and gives a real polymorphism example |
| Main menu has 6 options (6 = Exit) | 7 options (6 = Change Password, 7 = Exit) | Password change is a standard requirement |
| IDs typed by the user | IDs generated automatically | Prevents duplicate IDs |
| Java 21 | Java 17 or newer | The code does not need any Java 21 feature, so it works on both |
| Authentication: "login" | Hashed passwords, forced change, lockout | Basic industry practice |
| Not in PPT | Audit log, report export, linked records, automated tests | Accountability, usability, correctness |

Update slides 5, 7, 11, 15 and Part B/E of the plan to match.

## 8. Viva questions (extra to the ones in the PPT)

| Question | Answer |
|----------|--------|
| Why is `BaseRecord` abstract? | We never need a plain "record"; only specific kinds. Abstract also lets us force every child to write `toRow` and `toFileLine`. |
| Where is encapsulation used? | All fields are private; setters validate (for example `setQuantity` rejects negatives). |
| What is polymorphism here? | `display()` calls `toRow()`; each child class has its own `toRow()`, so the correct one runs. |
| Why not `Scanner.nextInt()`? | It leaves the Enter key in the buffer and crashes on letters. We read a whole line and convert it inside `try/catch`. |
| What does `try-with-resources` do? | Closes the file automatically, even when an error happens. |
| How do you stop duplicate records? | IDs are generated as highest + 1; names are compared with `equalsIgnoreCase` before adding. |
| Why can't I delete an arsenal item that has inspections? | The inspection would point to nothing. We refuse and suggest changing the status instead. |
| Why store a hash and not the password? | If the file leaks, the passwords are not revealed. |
| What is a salt? | A random value mixed into the password before hashing so two users with the same password get different hashes. |
| What is the time complexity of finding a record? | O(n), linear search. Fine for small data; a `HashMap` would give O(1) but is not needed here. |
| What if two people run the program at once? | Not supported in this prototype (no file locking). A database in Phase 2 solves this. |
| How did you test it? | `AtlasTests` runs 76 checks in a temporary folder: validation, file save/load, duplicates, links between records, reports, login. |
| What is `static`? | A member that belongs to the class, not to one object, for example `ArsenalRecord.CATEGORIES`. |
| Why `ArrayList` and not an array? | It grows and shrinks automatically; we do not know how many records there will be. |
| What would you improve next? | JDBC + MySQL, Swing GUI, roles (admin / viewer), encrypted files. |
