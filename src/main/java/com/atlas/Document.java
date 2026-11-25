package com.atlas;

/**
 * One administrative document record (the document itself is not stored, only its details).
 */
public class Document extends BaseRecord {

    public static final String[] TYPES = {"Report", "Certificate", "Policy", "Audit Record", "Manual"};
    public static final String[] STATUSES = {"Pending", "Reviewed", "Approved", "Archived"};

    private String type;
    private String dateAdded;

    public Document(int id, String name, String type, String dateAdded, String status) {
        super(id, name, status, STATUSES);
        setType(type);
        setDateAdded(dateAdded);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = checkOneOf(type, TYPES, "document type");
    }

    public String getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(String dateAdded) {
        this.dateAdded = checkDate(dateAdded, "date");
    }

    @Override
    public String toFileLine() {
        return getId() + DELIMITER + getName() + DELIMITER + type + DELIMITER
                + dateAdded + DELIMITER + getStatus();
    }

    @Override
    public String toRow() {
        return String.format("%-5d  %-35s  %-12s  %-10s  %-10s",
                getId(), getName(), type, dateAdded, getStatus());
    }

    public static String header() {
        return String.format("%-5s  %-35s  %-12s  %-10s  %-10s",
                "ID", "Name", "Type", "Date Added", "Status");
    }

    public static Document fromFileLine(String line) {
        String[] p = splitLine(line, 5);
        return new Document(Integer.parseInt(p[0].trim()), p[1], p[2], p[3], p[4]);
    }
}
