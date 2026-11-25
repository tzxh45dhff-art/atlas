package com.atlas;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Keeps a simple history of who did what, saved in data/atlas.log.
 * Every login and every add, update and delete is recorded with the time and the user name.
 * Passwords are never written to the log.
 */
public class AuditLog {

    private static final String FILE_NAME = "atlas.log";
    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static String currentUser = "-";

    private AuditLog() {
        // utility class
    }

    public static void setUser(String user) {
        currentUser = user;
    }

    public static String getUser() {
        return currentUser;
    }

    /** Adds one line such as: 2026-11-25 10:15:30 | admin | ADD | ARSENAL 1007 Guidance Module */
    public static void log(String action, String details) {
        String time = LocalDateTime.now().format(FORMAT);
        FileHelper.appendLine(FILE_NAME, time + " | " + currentUser + " | " + action + " | " + details);
    }
}
