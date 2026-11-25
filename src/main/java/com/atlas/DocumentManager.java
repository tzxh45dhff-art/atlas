package com.atlas;

import java.time.LocalDate;
import java.util.ArrayList;

/**
 * Manages document records: create, read, update and delete (CRUD).
 * Records are kept in an ArrayList and saved to data/documents.txt after every change.
 */
public class DocumentManager {

    private static final String FILE_NAME = "documents.txt";
    private static final String FILE_HEADER = "# id|name|type|dateAdded|status";
    private static final int FIRST_ID = 4001;

    private final ArrayList<Document> documents = new ArrayList<>();

    public DocumentManager() {
        load();
    }

    // ====================== file handling ======================

    private void load() {
        for (String line : FileHelper.readLines(FILE_NAME)) {
            try {
                Document document = Document.fromFileLine(line);
                if (find(document.getId()) == null) {
                    documents.add(document);
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
        for (Document document : documents) {
            lines.add(document.toFileLine());
        }
        if (!FileHelper.writeLines(FILE_NAME, FILE_HEADER, lines)) {
            System.out.println("WARNING: the change could not be saved to disk.");
        }
    }

    // ====================== core operations (no user input) ======================

    /** Linear search by ID. Returns null when no document has that ID. */
    public Document find(int id) {
        for (Document document : documents) {
            if (document.getId() == id) {
                return document;
            }
        }
        return null;
    }

    public int nextId() {
        int highest = FIRST_ID - 1;
        for (Document document : documents) {
            if (document.getId() > highest) {
                highest = document.getId();
            }
        }
        return highest + 1;
    }

    /** True if another document (not the one with excludeId) has the same name and type. */
    private boolean isDuplicate(String name, String type, int excludeId) {
        for (Document document : documents) {
            if (document.getId() != excludeId
                    && document.getName().equalsIgnoreCase(name)
                    && document.getType().equalsIgnoreCase(type)) {
                return true;
            }
        }
        return false;
    }

    /** Adds a document and saves. Returns false if the ID or the name+type already exists. */
    public boolean addDocument(Document document) {
        if (find(document.getId()) != null || isDuplicate(document.getName(), document.getType(), 0)) {
            return false;
        }
        documents.add(document);
        save();
        AuditLog.log("ADD", "DOCUMENT " + document.getId() + " " + document.getName());
        return true;
    }

    /** Removes the document with this ID and saves. Returns false if not found. */
    public boolean removeById(int id) {
        for (int i = 0; i < documents.size(); i++) {
            if (documents.get(i).getId() == id) {
                Document removed = documents.remove(i);
                save();
                AuditLog.log("DELETE", "DOCUMENT " + removed.getId() + " " + removed.getName());
                return true;
            }
        }
        return false;
    }

    public ArrayList<Document> getAll() {
        return new ArrayList<>(documents);
    }

    // ====================== numbers used by the reports ======================

    public int size() {
        return documents.size();
    }

    public int countByStatus(String status) {
        int count = 0;
        for (Document document : documents) {
            if (document.getStatus().equals(status)) {
                count++;
            }
        }
        return count;
    }

    public int countByType(String type) {
        int count = 0;
        for (Document document : documents) {
            if (document.getType().equals(type)) {
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
            System.out.println("===== DOCUMENT MANAGEMENT =====");
            System.out.println("1. Add document");
            System.out.println("2. View all documents");
            System.out.println("3. Search by ID");
            System.out.println("4. Search by name");
            System.out.println("5. Update document");
            System.out.println("6. Delete document");
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
        System.out.println("\n--- Add document ---");
        String name = InputHelper.readText("Document name (max 35 characters): ", 35);
        String type = InputHelper.readChoice("Document type:", Document.TYPES);
        String status = InputHelper.readChoice("Status:", Document.STATUSES);

        // the date added is today's date, so the user cannot enter a wrong one
        Document document = new Document(nextId(), name, type, LocalDate.now().toString(), status);
        if (addDocument(document)) {
            System.out.println("Document added with ID " + document.getId() + ".");
        } else {
            System.out.println("A document with the same name and type already exists. Nothing was added.");
        }
    }

    private void viewAllScreen() {
        System.out.println("\n--- All documents ---");
        if (documents.isEmpty()) {
            System.out.println("No records found.");
            return;
        }
        BaseRecord.printHeader(Document.header());
        for (Document document : documents) {
            document.display();
        }
        System.out.println(documents.size() + " record(s).");
    }

    private void searchByIdScreen() {
        Document document = askForDocument("Enter ID to search (0 to cancel): ");
        if (document != null) {
            BaseRecord.printHeader(Document.header());
            document.display();
        }
    }

    private void searchByNameScreen() {
        String keyword = InputHelper.readText("Enter part of the name: ", 35).toLowerCase();
        int found = 0;
        for (Document document : documents) {
            if (document.getName().toLowerCase().contains(keyword)) {
                if (found == 0) {
                    BaseRecord.printHeader(Document.header());
                }
                document.display();
                found++;
            }
        }
        System.out.println(found == 0 ? "No matching records." : found + " match(es).");
    }

    private void updateScreen() {
        Document document = askForDocument("Enter ID to update (0 to cancel): ");
        if (document == null) {
            return;
        }
        BaseRecord.printHeader(Document.header());
        document.display();

        String[] fields = {"Name", "Type", "Status"};
        int field = InputHelper.readChoiceIndex("Which field do you want to change?", fields);
        switch (field) {
            case 0: {
                String name = InputHelper.readText("New name (max 35 characters): ", 35);
                if (isDuplicate(name, document.getType(), document.getId())) {
                    System.out.println("Another document already has that name and type. Not changed.");
                    return;
                }
                document.setName(name);
                break;
            }
            case 1: {
                String type = InputHelper.readChoice("New type:", Document.TYPES);
                if (isDuplicate(document.getName(), type, document.getId())) {
                    System.out.println("Another document already has that name and type. Not changed.");
                    return;
                }
                document.setType(type);
                break;
            }
            default:
                document.setStatus(InputHelper.readChoice("New status:", Document.STATUSES));
                break;
        }
        save();
        AuditLog.log("UPDATE", "DOCUMENT " + document.getId() + " field=" + fields[field]);
        System.out.println("Record updated.");
    }

    private void deleteScreen() {
        Document document = askForDocument("Enter ID to delete (0 to cancel): ");
        if (document == null) {
            return;
        }
        BaseRecord.printHeader(Document.header());
        document.display();
        if (InputHelper.confirm("Delete this record permanently?")) {
            removeById(document.getId());
            System.out.println("Record deleted.");
        } else {
            System.out.println("Deletion cancelled.");
        }
    }

    private Document askForDocument(String prompt) {
        int id = InputHelper.readInt(prompt, 0, InputHelper.MAX_ID);
        if (id == 0) {
            return null;
        }
        Document document = find(id);
        if (document == null) {
            System.out.println("No document with ID " + id + ".");
        }
        return document;
    }
}
