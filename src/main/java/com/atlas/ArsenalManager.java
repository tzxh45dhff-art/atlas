package com.atlas;

import java.util.ArrayList;

/**
 * Manages all arsenal records: create, read, update and delete (CRUD).
 * Records are kept in an ArrayList while the program runs and saved to data/records.txt after every change.
 */
public class ArsenalManager {

    private static final String FILE_NAME = "records.txt";
    private static final String FILE_HEADER = "# id|name|category|location|quantity|status";
    private static final int FIRST_ID = 1001;

    private final ArrayList<ArsenalRecord> records = new ArrayList<>();
    private InspectionManager inspectionManager;   // set later, used to protect records that have inspections

    public ArsenalManager() {
        load();
    }

    public void setInspectionManager(InspectionManager inspectionManager) {
        this.inspectionManager = inspectionManager;
    }

    // ====================== file handling ======================

    private void load() {
        for (String line : FileHelper.readLines(FILE_NAME)) {
            try {
                ArsenalRecord record = ArsenalRecord.fromFileLine(line);
                if (find(record.getId()) == null) {
                    records.add(record);
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
        for (ArsenalRecord record : records) {
            lines.add(record.toFileLine());
        }
        if (!FileHelper.writeLines(FILE_NAME, FILE_HEADER, lines)) {
            System.out.println("WARNING: the change could not be saved to disk.");
        }
    }

    // ====================== core operations (no user input) ======================

    /** Linear search by ID. Returns null when no record has that ID. */
    public ArsenalRecord find(int id) {
        for (ArsenalRecord record : records) {
            if (record.getId() == id) {
                return record;
            }
        }
        return null;
    }

    /** Next free ID: one more than the highest existing ID. */
    public int nextId() {
        int highest = FIRST_ID - 1;
        for (ArsenalRecord record : records) {
            if (record.getId() > highest) {
                highest = record.getId();
            }
        }
        return highest + 1;
    }

    /** True if another record (not the one with excludeId) has the same name and location. */
    private boolean isDuplicate(String name, String location, int excludeId) {
        for (ArsenalRecord record : records) {
            if (record.getId() != excludeId
                    && record.getName().equalsIgnoreCase(name)
                    && record.getLocation().equalsIgnoreCase(location)) {
                return true;
            }
        }
        return false;
    }

    /** Adds a record and saves. Returns false if the ID or the name+location already exists. */
    public boolean addRecord(ArsenalRecord record) {
        if (find(record.getId()) != null || isDuplicate(record.getName(), record.getLocation(), 0)) {
            return false;
        }
        records.add(record);
        save();
        AuditLog.log("ADD", "ARSENAL " + record.getId() + " " + record.getName());
        return true;
    }

    /** Removes the record with this ID and saves. Returns false if it does not exist. */
    public boolean removeById(int id) {
        for (int i = 0; i < records.size(); i++) {
            if (records.get(i).getId() == id) {
                ArsenalRecord removed = records.remove(i);
                save();
                AuditLog.log("DELETE", "ARSENAL " + removed.getId() + " " + removed.getName());
                return true;
            }
        }
        return false;
    }

    /** Returns a copy of the list so other classes cannot change our records by accident. */
    public ArrayList<ArsenalRecord> getAll() {
        return new ArrayList<>(records);
    }

    // ====================== numbers used by the reports ======================

    public int size() {
        return records.size();
    }

    public int totalQuantity() {
        int total = 0;
        for (ArsenalRecord record : records) {
            total += record.getQuantity();
        }
        return total;
    }

    public int countByStatus(String status) {
        int count = 0;
        for (ArsenalRecord record : records) {
            if (record.getStatus().equals(status)) {
                count++;
            }
        }
        return count;
    }

    public int countByCategory(String category) {
        int count = 0;
        for (ArsenalRecord record : records) {
            if (record.getCategory().equals(category)) {
                count++;
            }
        }
        return count;
    }

    // ====================== menu and screens ======================

    public void run() {
        int choice;
        do {
            System.out.println();
            System.out.println("===== ARSENAL RECORDS =====");
            System.out.println("1. Add record");
            System.out.println("2. View all records");
            System.out.println("3. Search by ID");
            System.out.println("4. Search by name");
            System.out.println("5. Update record");
            System.out.println("6. Delete record");
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
                    searchByNameScreen();
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
        System.out.println("\n--- Add arsenal record ---");
        String name = InputHelper.readText("Item name (max 30 characters): ", 30);
        String category = InputHelper.readChoice("Category:", ArsenalRecord.CATEGORIES);
        String location = InputHelper.readText("Storage location (max 20 characters): ", 20);
        int quantity = InputHelper.readInt("Quantity (0-" + ArsenalRecord.MAX_QUANTITY + "): ", 0, ArsenalRecord.MAX_QUANTITY);
        String status = InputHelper.readChoice("Status:", ArsenalRecord.STATUSES);

        ArsenalRecord record = new ArsenalRecord(nextId(), name, category, location, quantity, status);
        if (addRecord(record)) {
            System.out.println("Record added with ID " + record.getId() + ".");
        } else {
            System.out.println("A record with the same name and location already exists. Nothing was added.");
        }
    }

    private void viewAllScreen() {
        System.out.println("\n--- All arsenal records ---");
        if (records.isEmpty()) {
            System.out.println("No records found.");
            return;
        }
        BaseRecord.printHeader(ArsenalRecord.header());
        for (ArsenalRecord record : records) {
            record.display();
        }
        System.out.println(records.size() + " record(s).");
    }

    private void searchByIdScreen() {
        ArsenalRecord record = askForRecord("Enter ID to search (0 to cancel): ");
        if (record != null) {
            BaseRecord.printHeader(ArsenalRecord.header());
            record.display();
        }
    }

    private void searchByNameScreen() {
        String keyword = InputHelper.readText("Enter part of the name: ", 30).toLowerCase();
        int found = 0;
        for (ArsenalRecord record : records) {
            if (record.getName().toLowerCase().contains(keyword)) {
                if (found == 0) {
                    BaseRecord.printHeader(ArsenalRecord.header());
                }
                record.display();
                found++;
            }
        }
        System.out.println(found == 0 ? "No matching records." : found + " match(es).");
    }

    private void updateScreen() {
        ArsenalRecord record = askForRecord("Enter ID to update (0 to cancel): ");
        if (record == null) {
            return;
        }
        BaseRecord.printHeader(ArsenalRecord.header());
        record.display();

        String[] fields = {"Name", "Category", "Location", "Quantity", "Status"};
        int field = InputHelper.readChoiceIndex("Which field do you want to change?", fields);
        switch (field) {
            case 0: {
                String name = InputHelper.readText("New name (max 30 characters): ", 30);
                if (isDuplicate(name, record.getLocation(), record.getId())) {
                    System.out.println("Another record already has that name and location. Not changed.");
                    return;
                }
                record.setName(name);
                break;
            }
            case 1:
                record.setCategory(InputHelper.readChoice("New category:", ArsenalRecord.CATEGORIES));
                break;
            case 2: {
                String location = InputHelper.readText("New location (max 20 characters): ", 20);
                if (isDuplicate(record.getName(), location, record.getId())) {
                    System.out.println("Another record already has that name and location. Not changed.");
                    return;
                }
                record.setLocation(location);
                break;
            }
            case 3:
                record.setQuantity(InputHelper.readInt("New quantity (0-" + ArsenalRecord.MAX_QUANTITY + "): ", 0, ArsenalRecord.MAX_QUANTITY));
                break;
            default:
                record.setStatus(InputHelper.readChoice("New status:", ArsenalRecord.STATUSES));
                break;
        }
        save();
        AuditLog.log("UPDATE", "ARSENAL " + record.getId() + " field=" + fields[field]);
        System.out.println("Record updated.");
    }

    private void deleteScreen() {
        ArsenalRecord record = askForRecord("Enter ID to delete (0 to cancel): ");
        if (record == null) {
            return;
        }
        if (inspectionManager != null && inspectionManager.isArsenalReferenced(record.getId())) {
            System.out.println("Cannot delete: inspection records refer to this item.");
            System.out.println("Change its status to Decommissioned instead.");
            return;
        }
        BaseRecord.printHeader(ArsenalRecord.header());
        record.display();
        if (InputHelper.confirm("Delete this record permanently?")) {
            removeById(record.getId());
            System.out.println("Record deleted.");
        } else {
            System.out.println("Deletion cancelled.");
        }
    }

    /** Asks for an ID and returns the record, or null if the user cancels (0) or the ID does not exist. */
    private ArsenalRecord askForRecord(String prompt) {
        int id = InputHelper.readInt(prompt, 0, InputHelper.MAX_ID);
        if (id == 0) {
            return null;
        }
        ArsenalRecord record = find(id);
        if (record == null) {
            System.out.println("No arsenal record with ID " + id + ".");
        }
        return record;
    }
}
