package com.atlas;

/**
 * Parent class of every record type (ArsenalRecord, Personnel, Inspection, Document).
 *
 * It holds the three things all records share: an ID, a name and a status.
 * Child classes add their own fields and say how they are saved to a file (toFileLine)
 * and how they are shown on screen (toRow). This is inheritance and polymorphism.
 */
public abstract class BaseRecord {

    /** Separator used between fields when a record is saved to a text file. */
    public static final String DELIMITER = "|";

    /** Longest text value we accept, so a damaged file cannot break the layout. */
    public static final int MAX_TEXT_LENGTH = 60;

    private final int id;                 // never changes after creation
    private String name;
    private String status;
    private final String[] allowedStatuses;

    protected BaseRecord(int id, String name, String status, String[] allowedStatuses) {
        if (id <= 0) {
            throw new IllegalArgumentException("ID must be a positive number");
        }
        this.id = id;
        this.allowedStatuses = allowedStatuses;
        setName(name);
        setStatus(status);
    }

    // ---------- getters and setters (encapsulation) ----------

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public final void setName(String name) {
        this.name = cleanText(name, "name");
    }

    public String getStatus() {
        return status;
    }

    public final void setStatus(String status) {
        this.status = checkOneOf(status, allowedStatuses, "status");
    }

    // ---------- behaviour every record must provide ----------

    /** One line of text used to save this record in its data file. */
    public abstract String toFileLine();

    /** One formatted table row used to show this record on screen. */
    public abstract String toRow();

    /** Prints this record as one table row. */
    public void display() {
        System.out.println(toRow());
    }

    // ---------- shared helper methods for the child classes ----------

    /** Trims the text and rejects empty values, over-long values and the file delimiter. */
    protected static String cleanText(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("The " + fieldName + " cannot be empty");
        }
        String text = value.trim();
        if (text.length() > MAX_TEXT_LENGTH) {
            throw new IllegalArgumentException("The " + fieldName + " is too long");
        }
        if (text.contains(DELIMITER) || text.contains("\n") || text.contains("\r")) {
            throw new IllegalArgumentException("The " + fieldName + " contains a character that is not allowed");
        }
        return text;
    }

    /** Checks that value is one of the allowed options (ignoring case) and returns the official spelling. */
    protected static String checkOneOf(String value, String[] allowed, String fieldName) {
        if (value != null) {
            for (String option : allowed) {
                if (option.equalsIgnoreCase(value.trim())) {
                    return option;
                }
            }
        }
        throw new IllegalArgumentException("Invalid " + fieldName + ": " + value);
    }

    /** Checks that a date text is in yyyy-MM-dd format and returns it in that format. */
    protected static String checkDate(String value, String fieldName) {
        try {
            return java.time.LocalDate.parse(value == null ? "" : value.trim()).toString();
        } catch (java.time.format.DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid " + fieldName + ": " + value);
        }
    }

    /** Splits one file line into its fields and checks that the number of fields is right. */
    protected static String[] splitLine(String line, int expectedFields) {
        String[] parts = line.split("\\|", -1);
        if (parts.length != expectedFields) {
            throw new IllegalArgumentException("Expected " + expectedFields + " fields but found " + parts.length);
        }
        return parts;
    }

    /** Prints a table header followed by a line of dashes of the same width. */
    public static void printHeader(String header) {
        System.out.println(header);
        System.out.println("-".repeat(header.length()));
    }
}
