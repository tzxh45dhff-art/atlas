package com.atlas;

import java.util.NoSuchElementException;

/**
 * Entry point of ATLAS: shows the login, then the main menu, and starts the chosen module.
 * All real work is done by the manager classes; Main only connects them.
 */
public class Main {

    public static void main(String[] args) {
        try {
            runApplication();
        } catch (NoSuchElementException e) {
            // happens when the input stream is closed (for example Ctrl+D)
            System.out.println("\nInput closed. Exiting ATLAS.");
            AuditLog.log("EXIT", "input closed");
        }
    }

    private static void runApplication() {
        printBanner();

        AuthManager authManager = new AuthManager();
        String user = authManager.login();
        if (user == null) {
            System.out.println("\nToo many failed attempts. Exiting ATLAS.");
            return;
        }

        // create the managers; they load their data files when they are created
        ArsenalManager arsenalManager = new ArsenalManager();
        PersonnelManager personnelManager = new PersonnelManager();
        InspectionManager inspectionManager = new InspectionManager(arsenalManager, personnelManager);
        DocumentManager documentManager = new DocumentManager();
        ReportManager reportManager = new ReportManager(arsenalManager, personnelManager, inspectionManager, documentManager);

        // arsenal and personnel records must not be deleted while inspections still refer to them
        arsenalManager.setInspectionManager(inspectionManager);
        personnelManager.setInspectionManager(inspectionManager);

        int choice;
        do {
            printMainMenu(user);
            choice = InputHelper.readInt("Enter your choice: ", 1, 7);
            switch (choice) {
                case 1:
                    arsenalManager.run();
                    break;
                case 2:
                    personnelManager.run();
                    break;
                case 3:
                    inspectionManager.run();
                    break;
                case 4:
                    documentManager.run();
                    break;
                case 5:
                    reportManager.run();
                    break;
                case 6:
                    authManager.changePasswordScreen(user);
                    break;
                default:
                    System.out.println("\nLogged out. Goodbye.");
                    AuditLog.log("LOGOUT", "normal exit");
                    break;
            }
        } while (choice != 7);
    }

    private static void printBanner() {
        System.out.println("========================================");
        System.out.println("                 ATLAS");
        System.out.println("       ARSENAL RECORDS MANAGEMENT");
        System.out.println("   (fictional prototype - not a real system)");
        System.out.println("========================================");
    }

    private static void printMainMenu(String user) {
        System.out.println();
        printBanner();
        System.out.println("Logged in as: " + user);
        System.out.println("1. Arsenal Records");
        System.out.println("2. Personnel Management");
        System.out.println("3. Inspection Records");
        System.out.println("4. Document Management");
        System.out.println("5. View Reports");
        System.out.println("6. Change Password");
        System.out.println("7. Exit");
    }
}
