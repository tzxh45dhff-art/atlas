package com.atlas;

/**
 * One fictional arsenal inventory record.
 * Extends BaseRecord, so the ID, name and status come from the parent class.
 */
public class ArsenalRecord extends BaseRecord {

    public static final String[] CATEGORIES = {
        "Guidance Module", "Delivery System", "Storage Container", "Support Equipment"
    };
    public static final String[] STATUSES = {
        "Active", "In Storage", "Under Maintenance", "Decommissioned"
    };
    public static final int MAX_QUANTITY = 100000;

    private String category;
    private String location;
    private int quantity;

    public ArsenalRecord(int id, String name, String category, String location, int quantity, String status) {
        super(id, name, status, STATUSES);
        setCategory(category);
        setLocation(location);
        setQuantity(quantity);
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = checkOneOf(category, CATEGORIES, "category");
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = cleanText(location, "location");
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        if (quantity < 0 || quantity > MAX_QUANTITY) {
            throw new IllegalArgumentException("Quantity must be between 0 and " + MAX_QUANTITY);
        }
        this.quantity = quantity;
    }

    @Override
    public String toFileLine() {
        return getId() + DELIMITER + getName() + DELIMITER + category + DELIMITER
                + location + DELIMITER + quantity + DELIMITER + getStatus();
    }

    @Override
    public String toRow() {
        return String.format("%-5d  %-30s  %-18s  %-20s  %5d  %-18s",
                getId(), getName(), category, location, quantity, getStatus());
    }

    public static String header() {
        return String.format("%-5s  %-30s  %-18s  %-20s  %5s  %-18s",
                "ID", "Name", "Category", "Location", "Qty", "Status");
    }

    /** Builds a record from one line of records.txt. Throws IllegalArgumentException if the line is damaged. */
    public static ArsenalRecord fromFileLine(String line) {
        String[] p = splitLine(line, 6);
        return new ArsenalRecord(Integer.parseInt(p[0].trim()), p[1], p[2], p[3],
                Integer.parseInt(p[4].trim()), p[5]);
    }
}
