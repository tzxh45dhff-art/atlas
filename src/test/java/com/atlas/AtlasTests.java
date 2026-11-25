package com.atlas;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;

/**
 * Simple self-checking tests written in plain Java (no test library needed).
 * Each check prints PASS or FAIL, and the program ends with a failure code if any check failed.
 * The tests work in a temporary data folder, so the real files in data/ are never changed.
 *
 * Run with:  ./test.sh   (or test.bat on Windows)
 */
public class AtlasTests {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) throws IOException {
        File tempDir = Files.createTempDirectory("atlas-test-").toFile();
        System.setProperty("atlas.data", tempDir.getPath());

        // the input test must run first: it replaces System.in before InputHelper is first used
        testInputHelper();
        testModels();
        testFileHelper();
        testArsenalManager();
        testPersonnelManager();
        testInspectionManager();
        testDocumentManager();
        testReports();
        testAuth();

        deleteFolder(tempDir);
        System.out.println();
        System.out.println("Passed: " + passed + "   Failed: " + failed);
        if (failed > 0) {
            System.exit(1);
        }
    }

    // ====================== tiny test helpers ======================

    private static void check(String name, boolean condition) {
        if (condition) {
            passed++;
            System.out.println("PASS  " + name);
        } else {
            failed++;
            System.out.println("FAIL  " + name);
        }
    }

    /** Passes if the given code throws IllegalArgumentException. */
    private static void checkRejected(String name, Runnable code) {
        try {
            code.run();
            check(name, false);
        } catch (IllegalArgumentException e) {
            check(name, true);
        }
    }

    private static void deleteFolder(File folder) {
        File[] children = folder.listFiles();
        if (children != null) {
            for (File child : children) {
                deleteFolder(child);
            }
        }
        folder.delete();
    }

    private static ArsenalRecord sampleArsenal(int id, String name) {
        return new ArsenalRecord(id, name, "Guidance Module", "Site Test", 10, "Active");
    }

    // ====================== tests ======================

    private static void testInputHelper() {
        String typed = String.join("\n",
                "abc", "99", "5",                  // readInt: letters and out-of-range are rejected
                "", "a|b", "Valid Text",           // readText: empty and "|" are rejected
                "2026-02-30", "2026-02-28",        // readDate: impossible date is rejected
                "maybe", "y",                      // confirm: only yes/no accepted
                "7", "2") + "\n";                  // readChoiceIndex: 7 is out of range, 2 is chosen
        System.setIn(new ByteArrayInputStream(typed.getBytes()));

        check("readInt skips letters and out-of-range numbers", InputHelper.readInt("", 1, 10) == 5);
        check("readText skips empty text and the | character", InputHelper.readText("", 30).equals("Valid Text"));
        check("readDate skips impossible dates", InputHelper.readDate("").equals("2026-02-28"));
        check("confirm waits for a clear yes or no", InputHelper.confirm(""));
        check("readChoiceIndex returns a 0-based position", InputHelper.readChoiceIndex("", new String[]{"A", "B", "C"}) == 1);
    }

    private static void testModels() {
        ArsenalRecord record = new ArsenalRecord(1, "Item", "guidance module", "Site A", 5, "active");
        check("status and category spelling is normalised", record.getStatus().equals("Active")
                && record.getCategory().equals("Guidance Module"));
        checkRejected("negative quantity is rejected", () -> record.setQuantity(-1));
        checkRejected("unknown status is rejected", () -> record.setStatus("Exploded"));
        checkRejected("empty name is rejected", () -> record.setName("   "));
        checkRejected("| in a name is rejected", () -> record.setName("a|b"));
        checkRejected("ID 0 is rejected", () -> new ArsenalRecord(0, "Item", "Guidance Module", "Site A", 5, "Active"));
        checkRejected("clearance 9 is rejected", () -> new Personnel(1, "Name", "Clerk", "Security", 9, "Active"));
        checkRejected("impossible inspection date is rejected", () -> new Inspection(1, "Check", 1, 1, "2026-99-99", "Passed"));

        ArsenalRecord a = ArsenalRecord.fromFileLine(record.toFileLine());
        check("ArsenalRecord survives save and load", a.toFileLine().equals(record.toFileLine()));
        Personnel p = new Personnel(2, "Asha Rao", "Clerk", "Security", 2, "Active");
        check("Personnel survives save and load", Personnel.fromFileLine(p.toFileLine()).toFileLine().equals(p.toFileLine()));
        Inspection i = new Inspection(3, "Check", 1, 2, "2026-01-01", "Passed");
        check("Inspection survives save and load", Inspection.fromFileLine(i.toFileLine()).toFileLine().equals(i.toFileLine()));
        Document d = new Document(4, "Policy A", "Policy", "2026-01-01", "Pending");
        check("Document survives save and load", Document.fromFileLine(d.toFileLine()).toFileLine().equals(d.toFileLine()));

        checkRejected("line with too few fields is rejected", () -> ArsenalRecord.fromFileLine("1|Item|Guidance Module"));
        checkRejected("line with a text quantity is rejected", () -> ArsenalRecord.fromFileLine("1|Item|Guidance Module|Site|many|Active"));

        BaseRecord asBase = p;   // polymorphism: a Personnel object used through its parent type
        check("toRow works through the parent type", asBase.toRow().contains("Asha Rao"));
    }

    private static void testFileHelper() {
        ArrayList<String> lines = new ArrayList<>();
        lines.add("one|1");
        lines.add("two|2");
        check("writeLines succeeds", FileHelper.writeLines("sample.txt", "# header comment", lines));
        ArrayList<String> loaded = FileHelper.readLines("sample.txt");
        check("readLines skips the comment header and returns the data", loaded.size() == 2 && loaded.get(1).equals("two|2"));
        check("a missing file gives an empty list", FileHelper.readLines("does-not-exist.txt").isEmpty());
        check("no temporary file is left behind", !new File(FileHelper.resolve("sample.txt").getPath() + ".tmp").exists());
    }

    private static void testArsenalManager() {
        ArsenalManager manager = new ArsenalManager();
        check("a new manager starts empty when no file exists", manager.size() == 0);
        check("first ID is 1001", manager.nextId() == 1001);
        check("add works", manager.addRecord(sampleArsenal(1001, "Alpha")));
        check("same ID is rejected", !manager.addRecord(sampleArsenal(1001, "Beta")));
        check("same name and location is rejected", !manager.addRecord(sampleArsenal(1002, "alpha")));
        check("next ID follows the highest ID", manager.nextId() == 1002);
        check("find returns the record", manager.find(1001) != null && manager.find(1001).getName().equals("Alpha"));
        check("find returns null for an unknown ID", manager.find(9999) == null);

        ArsenalManager reloaded = new ArsenalManager();
        check("records are saved to the file and loaded again", reloaded.size() == 1 && reloaded.find(1001) != null);

        check("total quantity is summed", manager.totalQuantity() == 10);
        check("countByStatus works", manager.countByStatus("Active") == 1 && manager.countByStatus("In Storage") == 0);
        check("remove works", manager.removeById(1001) && manager.size() == 0);
        check("removing an unknown ID returns false", !manager.removeById(1001));
        check("getAll returns a copy", manager.getAll() != manager.getAll());
    }

    private static void testPersonnelManager() {
        PersonnelManager manager = new PersonnelManager();
        check("first personnel ID is 2001", manager.nextId() == 2001);
        check("add person works", manager.addPerson(new Personnel(2001, "Asha Rao", "Clerk", "Security", 2, "Active")));
        check("same name and department is rejected", !manager.addPerson(new Personnel(2002, "ASHA RAO", "Boss", "Security", 3, "Active")));
        check("same name in another department is allowed", manager.addPerson(new Personnel(2002, "Asha Rao", "Boss", "Logistics", 3, "Active")));
        check("countByDepartment works", manager.countByDepartment("Security") == 1);
        check("countByClearance works", manager.countByClearance(3) == 1);
        check("personnel are saved and loaded again", new PersonnelManager().size() == 2);
    }

    private static void testInspectionManager() {
        ArsenalManager arsenal = new ArsenalManager();
        PersonnelManager personnel = new PersonnelManager();
        arsenal.addRecord(sampleArsenal(1001, "Alpha"));
        InspectionManager inspections = new InspectionManager(arsenal, personnel);

        check("inspection of a missing arsenal item is rejected",
                !inspections.addInspection(new Inspection(3001, "Check", 5555, 2001, "2026-01-01", "Passed")));
        check("inspection by a missing inspector is rejected",
                !inspections.addInspection(new Inspection(3001, "Check", 1001, 7777, "2026-01-01", "Passed")));
        check("valid inspection is added",
                inspections.addInspection(new Inspection(3001, "Check", 1001, 2001, "2026-01-01", "Passed")));
        check("duplicate inspection is rejected",
                !inspections.addInspection(new Inspection(3002, "check", 1001, 2002, "2026-01-01", "Failed")));
        check("arsenal reference is detected", inspections.isArsenalReferenced(1001) && !inspections.isArsenalReferenced(1002));
        check("personnel reference is detected", inspections.isPersonnelReferenced(2001) && !inspections.isPersonnelReferenced(2002));
        check("latest date is found", "2026-01-01".equals(inspections.latestDate()));
        check("countByResult works", inspections.countByResult("Passed") == 1 && inspections.countByResult("Failed") == 0);
    }

    private static void testDocumentManager() {
        DocumentManager manager = new DocumentManager();
        check("first document ID is 4001", manager.nextId() == 4001);
        check("add document works", manager.addDocument(new Document(4001, "Policy A", "Policy", "2026-01-01", "Pending")));
        check("duplicate name and type is rejected", !manager.addDocument(new Document(4002, "policy a", "Policy", "2026-02-02", "Pending")));
        check("countByType works", manager.countByType("Policy") == 1 && manager.countByType("Report") == 0);
        check("documents are saved and loaded again", new DocumentManager().size() == 1);
    }

    private static void testReports() {
        ArsenalManager arsenal = new ArsenalManager();
        PersonnelManager personnel = new PersonnelManager();
        InspectionManager inspections = new InspectionManager(arsenal, personnel);
        DocumentManager documents = new DocumentManager();
        ReportManager reports = new ReportManager(arsenal, personnel, inspections, documents);

        check("arsenal report shows the total", reports.arsenalReport().contains("Total records  : 1"));
        check("inspection report shows the pass rate", reports.inspectionReport().contains("100.0%"));
        check("overview contains all four sections", reports.overviewReport().contains("ARSENAL SUMMARY")
                && reports.overviewReport().contains("PERSONNEL SUMMARY")
                && reports.overviewReport().contains("INSPECTION SUMMARY")
                && reports.overviewReport().contains("DOCUMENT SUMMARY"));
    }

    private static void testAuth() {
        check("weak passwords are rejected", !AuthManager.isStrongPassword("short1", "bob")
                && !AuthManager.isStrongPassword("onlyletters", "bob")
                && !AuthManager.isStrongPassword("12345678", "bob")
                && !AuthManager.isStrongPassword("Bob12345", "bob12345"));
        check("a strong password is accepted", AuthManager.isStrongPassword("Strong123", "bob"));

        AuthManager auth = new AuthManager();
        check("default admin exists and must change the password", auth.mustChangePassword("admin"));
        check("default password works", auth.authenticate("admin", "Atlas@2026"));
        check("username is not case sensitive", auth.authenticate("ADMIN", "Atlas@2026"));
        check("wrong password fails", !auth.authenticate("admin", "wrong"));
        check("unknown user fails", !auth.authenticate("nobody", "Atlas@2026"));
        check("weak new password is refused", !auth.setPassword("admin", "weak"));
        check("strong new password is accepted", auth.setPassword("admin", "Better456"));
        check("old password stops working", !auth.authenticate("admin", "Atlas@2026"));
        check("new password works and the change-flag is cleared", auth.authenticate("admin", "Better456")
                && !auth.mustChangePassword("admin"));
        check("a new user can be added", auth.addUser("officer", "Officer789", false) && auth.authenticate("officer", "Officer789"));
        check("duplicate user is refused", !auth.addUser("Officer", "Another123", false));

        String fileText = String.join("\n", FileHelper.readLines("users.txt"));
        check("passwords are not stored as plain text", !fileText.contains("Better456") && !fileText.contains("Officer789"));
        check("users survive a restart", new AuthManager().authenticate("officer", "Officer789"));
    }
}
