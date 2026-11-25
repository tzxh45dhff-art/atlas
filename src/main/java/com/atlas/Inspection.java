package com.atlas;

/**
 * One inspection of an arsenal item carried out by a staff member.
 * The "name" from BaseRecord is the inspection title and the "status" is the result.
 * arsenalId and inspectorId link this record to ArsenalRecord and Personnel records.
 */
public class Inspection extends BaseRecord {

    public static final String[] RESULTS = {"Pending", "Passed", "Failed"};

    private int arsenalId;
    private int inspectorId;
    private String date;

    public Inspection(int id, String title, int arsenalId, int inspectorId, String date, String result) {
        super(id, title, result, RESULTS);
        setArsenalId(arsenalId);
        setInspectorId(inspectorId);
        setDate(date);
    }

    public int getArsenalId() {
        return arsenalId;
    }

    public void setArsenalId(int arsenalId) {
        if (arsenalId <= 0) {
            throw new IllegalArgumentException("Arsenal ID must be a positive number");
        }
        this.arsenalId = arsenalId;
    }

    public int getInspectorId() {
        return inspectorId;
    }

    public void setInspectorId(int inspectorId) {
        if (inspectorId <= 0) {
            throw new IllegalArgumentException("Inspector ID must be a positive number");
        }
        this.inspectorId = inspectorId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = checkDate(date, "date");
    }

    @Override
    public String toFileLine() {
        return getId() + DELIMITER + getName() + DELIMITER + arsenalId + DELIMITER
                + inspectorId + DELIMITER + date + DELIMITER + getStatus();
    }

    @Override
    public String toRow() {
        return String.format("%-5d  %-30s  %7d  %9d  %-10s  %-8s",
                getId(), getName(), arsenalId, inspectorId, date, getStatus());
    }

    public static String header() {
        return String.format("%-5s  %-30s  %7s  %9s  %-10s  %-8s",
                "ID", "Title", "Arsenal", "Inspector", "Date", "Result");
    }

    public static Inspection fromFileLine(String line) {
        String[] p = splitLine(line, 6);
        return new Inspection(Integer.parseInt(p[0].trim()), p[1], Integer.parseInt(p[2].trim()),
                Integer.parseInt(p[3].trim()), p[4], p[5]);
    }
}
