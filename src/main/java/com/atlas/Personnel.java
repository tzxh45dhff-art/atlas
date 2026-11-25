package com.atlas;

/**
 * One member of staff. Extends BaseRecord (ID, name and status come from the parent).
 */
public class Personnel extends BaseRecord {

    public static final String[] DEPARTMENTS = {
        "Operations", "Security", "Logistics", "Compliance", "Administration"
    };
    public static final String[] STATUSES = {"Active", "On Leave", "Retired"};
    public static final int MIN_CLEARANCE = 1;
    public static final int MAX_CLEARANCE = 5;

    private String designation;
    private String department;
    private int clearanceLevel;

    public Personnel(int id, String name, String designation, String department, int clearanceLevel, String status) {
        super(id, name, status, STATUSES);
        setDesignation(designation);
        setDepartment(department);
        setClearanceLevel(clearanceLevel);
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = cleanText(designation, "designation");
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = checkOneOf(department, DEPARTMENTS, "department");
    }

    public int getClearanceLevel() {
        return clearanceLevel;
    }

    public void setClearanceLevel(int clearanceLevel) {
        if (clearanceLevel < MIN_CLEARANCE || clearanceLevel > MAX_CLEARANCE) {
            throw new IllegalArgumentException("Clearance level must be between " + MIN_CLEARANCE + " and " + MAX_CLEARANCE);
        }
        this.clearanceLevel = clearanceLevel;
    }

    @Override
    public String toFileLine() {
        return getId() + DELIMITER + getName() + DELIMITER + designation + DELIMITER
                + department + DELIMITER + clearanceLevel + DELIMITER + getStatus();
    }

    @Override
    public String toRow() {
        return String.format("%-5d  %-25s  %-22s  %-16s  %9d  %-10s",
                getId(), getName(), designation, department, clearanceLevel, getStatus());
    }

    public static String header() {
        return String.format("%-5s  %-25s  %-22s  %-16s  %9s  %-10s",
                "ID", "Name", "Designation", "Department", "Clearance", "Status");
    }

    public static Personnel fromFileLine(String line) {
        String[] p = splitLine(line, 6);
        return new Personnel(Integer.parseInt(p[0].trim()), p[1], p[2], p[3],
                Integer.parseInt(p[4].trim()), p[5]);
    }
}
