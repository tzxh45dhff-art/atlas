package com.atlas;

import java.util.ArrayList;

/**
 * Manages personnel records: create, read, update and delete (CRUD).
 * Records are kept in an ArrayList and saved to data/personnel.txt after every change.
 */
public class PersonnelManager {

    private static final String FILE_NAME = "personnel.txt";
    private static final String FILE_HEADER = "# id|name|designation|department|clearanceLevel|status";
    private static final int FIRST_ID = 2001;

    private final ArrayList<Personnel> people = new ArrayList<>();
    private InspectionManager inspectionManager;   // set later, used to protect staff who carried out inspections

    public PersonnelManager() {
        load();
    }

    public void setInspectionManager(InspectionManager inspectionManager) {
        this.inspectionManager = inspectionManager;
    }

    // ====================== file handling ======================

    private void load() {
        for (String line : FileHelper.readLines(FILE_NAME)) {
            try {
                Personnel person = Personnel.fromFileLine(line);
                if (find(person.getId()) == null) {
                    people.add(person);
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
        for (Personnel person : people) {
            lines.add(person.toFileLine());
        }
        if (!FileHelper.writeLines(FILE_NAME, FILE_HEADER, lines)) {
            System.out.println("WARNING: the change could not be saved to disk.");
        }
    }

    // ====================== core operations (no user input) ======================

    /** Linear search by ID. Returns null when nobody has that ID. */
    public Personnel find(int id) {
        for (Personnel person : people) {
            if (person.getId() == id) {
                return person;
            }
        }
        return null;
    }

    public int nextId() {
        int highest = FIRST_ID - 1;
        for (Personnel person : people) {
            if (person.getId() > highest) {
                highest = person.getId();
            }
        }
        return highest + 1;
    }

    /** True if another person (not the one with excludeId) has the same name and department. */
    private boolean isDuplicate(String name, String department, int excludeId) {
        for (Personnel person : people) {
            if (person.getId() != excludeId
                    && person.getName().equalsIgnoreCase(name)
                    && person.getDepartment().equalsIgnoreCase(department)) {
                return true;
            }
        }
        return false;
    }

    /** Adds a person and saves. Returns false if the ID or the name+department already exists. */
    public boolean addPerson(Personnel person) {
        if (find(person.getId()) != null || isDuplicate(person.getName(), person.getDepartment(), 0)) {
            return false;
        }
        people.add(person);
        save();
        AuditLog.log("ADD", "PERSONNEL " + person.getId() + " " + person.getName());
        return true;
    }

    /** Removes the person with this ID and saves. Returns false if not found. */
    public boolean removeById(int id) {
        for (int i = 0; i < people.size(); i++) {
            if (people.get(i).getId() == id) {
                Personnel removed = people.remove(i);
                save();
                AuditLog.log("DELETE", "PERSONNEL " + removed.getId() + " " + removed.getName());
                return true;
            }
        }
        return false;
    }

    public ArrayList<Personnel> getAll() {
        return new ArrayList<>(people);
    }

    // ====================== numbers used by the reports ======================

    public int size() {
        return people.size();
    }

    public int countByStatus(String status) {
        int count = 0;
        for (Personnel person : people) {
            if (person.getStatus().equals(status)) {
                count++;
            }
        }
        return count;
    }

    public int countByDepartment(String department) {
        int count = 0;
        for (Personnel person : people) {
            if (person.getDepartment().equals(department)) {
                count++;
            }
        }
        return count;
    }

    public int countByClearance(int level) {
        int count = 0;
        for (Personnel person : people) {
            if (person.getClearanceLevel() == level) {
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
            System.out.println("===== PERSONNEL MANAGEMENT =====");
            System.out.println("1. Add person");
            System.out.println("2. View all personnel");
            System.out.println("3. Search by ID");
            System.out.println("4. Search by name");
            System.out.println("5. Update person");
            System.out.println("6. Delete person");
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
        System.out.println("\n--- Add person ---");
        String name = InputHelper.readText("Full name (max 25 characters): ", 25);
        String designation = InputHelper.readText("Designation (max 22 characters): ", 22);
        String department = InputHelper.readChoice("Department:", Personnel.DEPARTMENTS);
        int clearance = InputHelper.readInt("Clearance level (" + Personnel.MIN_CLEARANCE + "-" + Personnel.MAX_CLEARANCE + "): ",
                Personnel.MIN_CLEARANCE, Personnel.MAX_CLEARANCE);
        String status = InputHelper.readChoice("Status:", Personnel.STATUSES);

        Personnel person = new Personnel(nextId(), name, designation, department, clearance, status);
        if (addPerson(person)) {
            System.out.println("Person added with ID " + person.getId() + ".");
        } else {
            System.out.println("Someone with the same name and department already exists. Nothing was added.");
        }
    }

    private void viewAllScreen() {
        System.out.println("\n--- All personnel ---");
        if (people.isEmpty()) {
            System.out.println("No records found.");
            return;
        }
        BaseRecord.printHeader(Personnel.header());
        for (Personnel person : people) {
            person.display();
        }
        System.out.println(people.size() + " record(s).");
    }

    private void searchByIdScreen() {
        Personnel person = askForPerson("Enter ID to search (0 to cancel): ");
        if (person != null) {
            BaseRecord.printHeader(Personnel.header());
            person.display();
        }
    }

    private void searchByNameScreen() {
        String keyword = InputHelper.readText("Enter part of the name: ", 25).toLowerCase();
        int found = 0;
        for (Personnel person : people) {
            if (person.getName().toLowerCase().contains(keyword)) {
                if (found == 0) {
                    BaseRecord.printHeader(Personnel.header());
                }
                person.display();
                found++;
            }
        }
        System.out.println(found == 0 ? "No matching records." : found + " match(es).");
    }

    private void updateScreen() {
        Personnel person = askForPerson("Enter ID to update (0 to cancel): ");
        if (person == null) {
            return;
        }
        BaseRecord.printHeader(Personnel.header());
        person.display();

        String[] fields = {"Name", "Designation", "Department", "Clearance level", "Status"};
        int field = InputHelper.readChoiceIndex("Which field do you want to change?", fields);
        switch (field) {
            case 0: {
                String name = InputHelper.readText("New name (max 25 characters): ", 25);
                if (isDuplicate(name, person.getDepartment(), person.getId())) {
                    System.out.println("Someone with that name already exists in this department. Not changed.");
                    return;
                }
                person.setName(name);
                break;
            }
            case 1:
                person.setDesignation(InputHelper.readText("New designation (max 22 characters): ", 22));
                break;
            case 2: {
                String department = InputHelper.readChoice("New department:", Personnel.DEPARTMENTS);
                if (isDuplicate(person.getName(), department, person.getId())) {
                    System.out.println("Someone with that name already exists in that department. Not changed.");
                    return;
                }
                person.setDepartment(department);
                break;
            }
            case 3:
                person.setClearanceLevel(InputHelper.readInt("New clearance level (" + Personnel.MIN_CLEARANCE + "-"
                        + Personnel.MAX_CLEARANCE + "): ", Personnel.MIN_CLEARANCE, Personnel.MAX_CLEARANCE));
                break;
            default:
                person.setStatus(InputHelper.readChoice("New status:", Personnel.STATUSES));
                break;
        }
        save();
        AuditLog.log("UPDATE", "PERSONNEL " + person.getId() + " field=" + fields[field]);
        System.out.println("Record updated.");
    }

    private void deleteScreen() {
        Personnel person = askForPerson("Enter ID to delete (0 to cancel): ");
        if (person == null) {
            return;
        }
        if (inspectionManager != null && inspectionManager.isPersonnelReferenced(person.getId())) {
            System.out.println("Cannot delete: this person is listed as inspector in inspection records.");
            System.out.println("Change their status to Retired instead.");
            return;
        }
        BaseRecord.printHeader(Personnel.header());
        person.display();
        if (InputHelper.confirm("Delete this record permanently?")) {
            removeById(person.getId());
            System.out.println("Record deleted.");
        } else {
            System.out.println("Deletion cancelled.");
        }
    }

    private Personnel askForPerson(String prompt) {
        int id = InputHelper.readInt(prompt, 0, InputHelper.MAX_ID);
        if (id == 0) {
            return null;
        }
        Personnel person = find(id);
        if (person == null) {
            System.out.println("No personnel record with ID " + id + ".");
        }
        return person;
    }
}
