package com.atlas;

import java.util.ArrayList;

/**
 * Manages inspection records: create, read, update and delete (CRUD).
 * Every inspection must point to an existing arsenal record and an existing, active inspector,
 * so this manager needs access to ArsenalManager and PersonnelManager.
 * Records are saved to data/inspections.txt after every change.
 */
public class InspectionManager {

    private static final String FILE_NAME = "inspections.txt";
    private static final String FILE_HEADER = "# id|title|arsenalId|inspectorId|date|result";
    private static final int FIRST_ID = 3001;

    private final ArrayList<Inspection> inspections = new ArrayList<>();
    private final ArsenalManager arsenalManager;
    private final PersonnelManager personnelManager;

    public InspectionManager(ArsenalManager arsenalManager, PersonnelManager personnelManager) {
        this.arsenalManager = arsenalManager;
        this.personnelManager = personnelManager;
        load();
    }

    // ====================== file handling ======================

    private void load() {
        for (String line : FileHelper.readLines(FILE_NAME)) {
            try {
                Inspection inspection = Inspection.fromFileLine(line);
                if (find(inspection.getId()) == null) {
                    inspections.add(inspection);
                } else {
                    System.out.println("Warning: duplicate ID skipped in " + FILE_NAME + ": " + line);
                }
            } catch (IllegalArgumentException e) {
                System.out.println("Warning: invalid line skipped in " + FILE_NAME + ": " + line);
            }
        }
    }

    private void save() {
        ArrayList<String> lines = new ArrayList<>();
        for (Inspection inspection : inspections) {
            lines.add(inspection.toFileLine());
        }
        if (!FileHelper.writeLines(FILE_NAME, FILE_HEADER, lines)) {
            System.out.println("WARNING: the change could not be saved to disk.");
        }
    }

    // ====================== core operations (no user input) ======================

    /** Linear search by ID. Returns null when no inspection has that ID. */
    public Inspection find(int id) {
        for (Inspection inspection : inspections) {
            if (inspection.getId() == id) {
                return inspection;
            }
        }
        return null;
    }

    public int nextId() {
        int highest = FIRST_ID - 1;
        for (Inspection inspection : inspections) {
            if (inspection.getId() > highest) {
                highest = inspection.getId();
            }
        }
        return highest + 1;
    }

    /** True if some inspection is about this arsenal item. */
    public boolean isArsenalReferenced(int arsenalId) {
        for (Inspection inspection : inspections) {
            if (inspection.getArsenalId() == arsenalId) {
                return true;
            }
        }
        return false;
    }

    /** True if this person is the inspector of some inspection. */
    public boolean isPersonnelReferenced(int personId) {
        for (Inspection inspection : inspections) {
            if (inspection.getInspectorId() == personId) {
                return true;
            }
        }
        return false;
    }

    /** True if another inspection has the same title, arsenal item and date. */
    private boolean isDuplicate(String title, int arsenalId, String date, int excludeId) {
        for (Inspection inspection : inspections) {
            if (inspection.getId() != excludeId
                    && inspection.getName().equalsIgnoreCase(title)
                    && inspection.getArsenalId() == arsenalId
                    && inspection.getDate().equals(date)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Adds an inspection and saves.
     * Returns false if the arsenal item or the inspector does not exist, or if it is a duplicate.
     */
    public boolean addInspection(Inspection inspection) {
        if (find(inspection.getId()) != null
                || arsenalManager.find(inspection.getArsenalId()) == null
                || personnelManager.find(inspection.getInspectorId()) == null
                || isDuplicate(inspection.getName(), inspection.getArsenalId(), inspection.getDate(), 0)) {
            return false;
        }
        inspections.add(inspection);
        save();
        AuditLog.log("ADD", "INSPECTION " + inspection.getId() + " " + inspection.getName());
        return true;
    }

    /** Removes the inspection with this ID and saves. Returns false if not found. */
    public boolean removeById(int id) {
        for (int i = 0; i < inspections.size(); i++) {
            if (inspections.get(i).getId() == id) {
                Inspection removed = inspections.remove(i);
                save();
                AuditLog.log("DELETE", "INSPECTION " + removed.getId() + " " + removed.getName());
                return true;
            }
        }
        return false;
    }

    public ArrayList<Inspection> getAll() {
        return new ArrayList<>(inspections);
    }

    // ====================== numbers used by the reports ======================

    public int size() {
        return inspections.size();
    }

    public int countByResult(String result) {
        int count = 0;
        for (Inspection inspection : inspections) {
            if (inspection.getStatus().equals(result)) {
                count++;
            }
        }
        return count;
    }

    /** Date of the latest inspection (yyyy-MM-dd texts sort correctly), or null if there are none. */
    public String latestDate() {
        String latest = null;
        for (Inspection inspection : inspections) {
            if (latest == null || inspection.getDate().compareTo(latest) > 0) {
                latest = inspection.getDate();
            }
        }
        return latest;
    }

    // ====================== menu and screens ======================

    public void run() {
        int choice;
        do {
            System.out.println();
            System.out.println("===== INSPECTION RECORDS =====");
            System.out.println("1. Add inspection");
            System.out.println("2. View all inspections");
            System.out.println("3. Search by ID");
            System.out.println("4. Search by title");
            System.out.println("5. Update inspection");
            System.out.println("6. Delete inspection");
            System.out.println("0. Back to main menu");
            choice = InputHelper.readInt("Enter your choice: ", 0, 6);
            switch (choice) {
                case 1:
                    addScreen();
                    break;
                case 2:
                    viewAllScreen();
                    break;
                case 3:
                    searchByIdScreen();
                    break;
                case 4:
                    searchByTitleScreen();
                    break;
                case 5:
                    updateScreen();
                    break;
                case 6:
                    deleteScreen();
                    break;
                default:
                    break;
            }
        } while (choice != 0);
    }

    private void addScreen() {
        System.out.println("\n--- Add inspection ---");
        String title = InputHelper.readText("Inspection title (max 30 characters): ", 30);
        ArsenalRecord item = askForArsenal();
        if (item == null) {
            return;
        }
        Personnel inspector = askForInspector();
        if (inspector == null) {
            return;
        }
        String date = InputHelper.readDate("Inspection date (yyyy-MM-dd): ");
        String result = InputHelper.readChoice("Result:", Inspection.RESULTS);

        Inspection inspection = new Inspection(nextId(), title, item.getId(), inspector.getId(), date, result);
        if (addInspection(inspection)) {
            System.out.println("Inspection added with ID " + inspection.getId() + ".");
        } else {
            System.out.println("An inspection with the same title, item and date already exists. Nothing was added.");
        }
    }

    private void viewAllScreen() {
        System.out.println("\n--- All inspections ---");
        if (inspections.isEmpty()) {
            System.out.println("No records found.");
            return;
        }
        BaseRecord.printHeader(Inspection.header());
        for (Inspection inspection : inspections) {
            inspection.display();
        }
        System.out.println(inspections.size() + " record(s).");
    }

    private void searchByIdScreen() {
        Inspection inspection = askForInspection("Enter ID to search (0 to cancel): ");
        if (inspection != null) {
            BaseRecord.printHeader(Inspection.header());
            inspection.display();
            printLinkedNames(inspection);
        }
    }

    private void searchByTitleScreen() {
        String keyword = InputHelper.readText("Enter part of the title: ", 30).toLowerCase();
        int found = 0;
        for (Inspection inspection : inspections) {
            if (inspection.getName().toLowerCase().contains(keyword)) {
                if (found == 0) {
                    BaseRecord.printHeader(Inspection.header());
                }
                inspection.display();
                found++;
            }
        }
        System.out.println(found == 0 ? "No matching records." : found + " match(es).");
    }

    private void updateScreen() {
        Inspection inspection = askForInspection("Enter ID to update (0 to cancel): ");
        if (inspection == null) {
            return;
        }
        BaseRecord.printHeader(Inspection.header());
        inspection.display();

        String[] fields = {"Title", "Inspector", "Date", "Result"};
        int field = InputHelper.readChoiceIndex("Which field do you want to change?", fields);
        switch (field) {
            case 0: {
                String title = InputHelper.readText("New title (max 30 characters): ", 30);
                if (isDuplicate(title, inspection.getArsenalId(), inspection.getDate(), inspection.getId())) {
                    System.out.println("Another inspection already has that title, item and date. Not changed.");
                    return;
                }
                inspection.setName(title);
                break;
            }
            case 1: {
                Personnel inspector = askForInspector();
                if (inspector == null) {
                    return;
                }
                inspection.setInspectorId(inspector.getId());
                break;
            }
            case 2: {
                String date = InputHelper.readDate("New date (yyyy-MM-dd): ");
                if (isDuplicate(inspection.getName(), inspection.getArsenalId(), date, inspection.getId())) {
                    System.out.println("Another inspection already has that title, item and date. Not changed.");
                    return;
                }
                inspection.setDate(date);
                break;
            }
            default:
                inspection.setStatus(InputHelper.readChoice("New result:", Inspection.RESULTS));
                break;
        }
        save();
        AuditLog.log("UPDATE", "INSPECTION " + inspection.getId() + " field=" + fields[field]);
        System.out.println("Record updated.");
    }

    private void deleteScreen() {
        Inspection inspection = askForInspection("Enter ID to delete (0 to cancel): ");
        if (inspection == null) {
            return;
        }
        BaseRecord.printHeader(Inspection.header());
        inspection.display();
        if (InputHelper.confirm("Delete this record permanently?")) {
            removeById(inspection.getId());
            System.out.println("Record deleted.");
        } else {
            System.out.println("Deletion cancelled.");
        }
    }

    /** Shows the names behind the arsenal ID and inspector ID of one inspection. */
    private void printLinkedNames(Inspection inspection) {
        ArsenalRecord item = arsenalManager.find(inspection.getArsenalId());
        Personnel inspector = personnelManager.find(inspection.getInspectorId());
        System.out.println("Arsenal item : " + (item == null ? "(not found)" : item.getName()));
        System.out.println("Inspector    : " + (inspector == null ? "(not found)" : inspector.getName()));
    }

    /** Asks for an arsenal ID until an existing one is entered. Returns null if the user cancels with 0. */
    private ArsenalRecord askForArsenal() {
        while (true) {
            int id = InputHelper.readInt("Arsenal record ID (0 to cancel): ", 0, InputHelper.MAX_ID);
            if (id == 0) {
                return null;
            }
            ArsenalRecord item = arsenalManager.find(id);
            if (item != null) {
                System.out.println("  Item: " + item.getName());
                return item;
            }
            System.out.println("  No arsenal record with ID " + id + ".");
        }
    }

    /** Asks for an inspector ID until an existing, active person is entered. Returns null if the user cancels with 0. */
    private Personnel askForInspector() {
        while (true) {
            int id = InputHelper.readInt("Inspector personnel ID (0 to cancel): ", 0, InputHelper.MAX_ID);
            if (id == 0) {
                return null;
            }
            Personnel person = personnelManager.find(id);
            if (person == null) {
                System.out.println("  No personnel record with ID " + id + ".");
            } else if (!person.getStatus().equals("Active")) {
                System.out.println("  " + person.getName() + " is not Active and cannot be an inspector.");
            } else {
                System.out.println("  Inspector: " + person.getName());
                return person;
            }
        }
    }

    private Inspection askForInspection(String prompt) {
        int id = InputHelper.readInt(prompt, 0, InputHelper.MAX_ID);
        if (id == 0) {
            return null;
        }
        Inspection inspection = find(id);
        if (inspection == null) {
            System.out.println("No inspection with ID " + id + ".");
        }
        return inspection;
    }
}
