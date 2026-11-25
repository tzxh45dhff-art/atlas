package com.atlas;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

/**
 * Builds statistical reports from the other managers (it only reads their data, never changes it).
 * Reports can be shown on screen and the full overview can be exported to a text file.
 */
public class ReportManager {

    private static final String LINE = "----------------------------------------";

    private final ArsenalManager arsenalManager;
    private final PersonnelManager personnelManager;
    private final InspectionManager inspectionManager;
    private final DocumentManager documentManager;

    public ReportManager(ArsenalManager arsenalManager, PersonnelManager personnelManager,
                         InspectionManager inspectionManager, DocumentManager documentManager) {
        this.arsenalManager = arsenalManager;
        this.personnelManager = personnelManager;
        this.inspectionManager = inspectionManager;
        this.documentManager = documentManager;
    }

    // ====================== report builders (return text) ======================

    public String arsenalReport() {
        StringBuilder text = new StringBuilder();
        text.append("ARSENAL SUMMARY\n").append(LINE).append("\n");
        text.append(String.format("Total records  : %d%n", arsenalManager.size()));
        text.append(String.format("Total quantity : %d%n", arsenalManager.totalQuantity()));
        text.append("By status:\n");
        for (String status : ArsenalRecord.STATUSES) {
            text.append(String.format("  %-20s %d%n", status, arsenalManager.countByStatus(status)));
        }
        text.append("By category:\n");
        for (String category : ArsenalRecord.CATEGORIES) {
            text.append(String.format("  %-20s %d%n", category, arsenalManager.countByCategory(category)));
        }
        return text.toString();
    }

    public String personnelReport() {
        StringBuilder text = new StringBuilder();
        text.append("PERSONNEL SUMMARY\n").append(LINE).append("\n");
        text.append(String.format("Total personnel: %d%n", personnelManager.size()));
        text.append("By status:\n");
        for (String status : Personnel.STATUSES) {
            text.append(String.format("  %-20s %d%n", status, personnelManager.countByStatus(status)));
        }
        text.append("By department:\n");
        for (String department : Personnel.DEPARTMENTS) {
            text.append(String.format("  %-20s %d%n", department, personnelManager.countByDepartment(department)));
        }
        text.append("By clearance level:\n");
        for (int level = Personnel.MIN_CLEARANCE; level <= Personnel.MAX_CLEARANCE; level++) {
            text.append(String.format("  Level %-14d %d%n", level, personnelManager.countByClearance(level)));
        }
        return text.toString();
    }

    public String inspectionReport() {
        int passed = inspectionManager.countByResult("Passed");
        int failed = inspectionManager.countByResult("Failed");
        int completed = passed + failed;

        StringBuilder text = new StringBuilder();
        text.append("INSPECTION SUMMARY\n").append(LINE).append("\n");
        text.append(String.format("Total inspections: %d%n", inspectionManager.size()));
        for (String result : Inspection.RESULTS) {
            text.append(String.format("  %-20s %d%n", result, inspectionManager.countByResult(result)));
        }
        if (completed > 0) {
            text.append(String.format("Pass rate (completed inspections): %.1f%%%n", passed * 100.0 / completed));
        } else {
            text.append("Pass rate: no completed inspections yet\n");
        }
        String latest = inspectionManager.latestDate();
        text.append("Latest inspection date: ").append(latest == null ? "none" : latest).append("\n");

        // arsenal items that have never been inspected (they need attention)
        ArrayList<ArsenalRecord> notInspected = new ArrayList<>();
        for (ArsenalRecord item : arsenalManager.getAll()) {
            if (!inspectionManager.isArsenalReferenced(item.getId())) {
                notInspected.add(item);
            }
        }
        text.append("Arsenal items never inspected: ").append(notInspected.size()).append("\n");
        for (ArsenalRecord item : notInspected) {
            text.append("  ").append(item.getId()).append("  ").append(item.getName()).append("\n");
        }
        return text.toString();
    }

    public String documentReport() {
        StringBuilder text = new StringBuilder();
        text.append("DOCUMENT SUMMARY\n").append(LINE).append("\n");
        text.append(String.format("Total documents: %d%n", documentManager.size()));
        text.append("By status:\n");
        for (String status : Document.STATUSES) {
            text.append(String.format("  %-20s %d%n", status, documentManager.countByStatus(status)));
        }
        text.append("By type:\n");
        for (String type : Document.TYPES) {
            text.append(String.format("  %-20s %d%n", type, documentManager.countByType(type)));
        }
        return text.toString();
    }

    /** All four reports together, with a title and the time of creation. */
    public String overviewReport() {
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return "ATLAS - SYSTEM OVERVIEW\n"
                + "Generated: " + time + " by " + AuditLog.getUser() + "\n"
                + "========================================\n\n"
                + arsenalReport() + "\n"
                + personnelReport() + "\n"
                + inspectionReport() + "\n"
                + documentReport();
    }

    // ====================== menu ======================

    public void run() {
        int choice;
        do {
            System.out.println();
            System.out.println("===== REPORTS =====");
            System.out.println("1. Arsenal summary");
            System.out.println("2. Personnel summary");
            System.out.println("3. Inspection summary");
            System.out.println("4. Document summary");
            System.out.println("5. Full system overview");
            System.out.println("6. Export full overview to a file");
            System.out.println("0. Back to main menu");
            choice = InputHelper.readInt("Enter your choice: ", 0, 6);
            System.out.println();
            switch (choice) {
                case 1:
                    System.out.print(arsenalReport());
                    break;
                case 2:
                    System.out.print(personnelReport());
                    break;
                case 3:
                    System.out.print(inspectionReport());
                    break;
                case 4:
                    System.out.print(documentReport());
                    break;
                case 5:
                    System.out.print(overviewReport());
                    break;
                case 6:
                    exportOverview();
                    break;
                default:
                    break;
            }
        } while (choice != 0);
    }

    private void exportOverview() {
        String stamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        String fileName = "reports/atlas-report-" + stamp + ".txt";
        if (FileHelper.writeText(fileName, overviewReport())) {
            System.out.println("Report saved to " + FileHelper.resolve(fileName).getPath());
            AuditLog.log("EXPORT", fileName);
        } else {
            System.out.println("The report could not be saved.");
        }
    }
}
