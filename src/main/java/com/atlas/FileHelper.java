package com.atlas;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;

/**
 * All reading and writing of text files happens here, so the manager classes
 * never deal with file handling or exceptions directly.
 *
 * Files live in the "data" folder. The folder can be changed with the Java option
 * -Datlas.data=somefolder (the tests use this to avoid touching real data).
 */
public class FileHelper {

    private FileHelper() {
        // utility class: nobody should create objects of it
    }

    /** Returns the File object for a file inside the data folder. */
    public static File resolve(String fileName) {
        return new File(System.getProperty("atlas.data", "data"), fileName);
    }

    /**
     * Reads a data file and returns its lines.
     * Blank lines and lines starting with # (comments) are skipped.
     * A missing file is not an error: an empty list is returned.
     */
    public static ArrayList<String> readLines(String fileName) {
        ArrayList<String> lines = new ArrayList<>();
        File file = resolve(fileName);
        if (!file.exists()) {
            return lines;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty() && !line.startsWith("#")) {
                    lines.add(line);
                }
            }
        } catch (IOException e) {
            System.out.println("Error: could not read " + file.getPath() + " (" + e.getMessage() + ")");
        }
        return lines;
    }

    /** Rewrites a whole data file: one comment header line followed by the given lines. */
    public static boolean writeLines(String fileName, String header, ArrayList<String> lines) {
        StringBuilder content = new StringBuilder();
        content.append(header).append(System.lineSeparator());
        for (String line : lines) {
            content.append(line).append(System.lineSeparator());
        }
        return writeText(fileName, content.toString());
    }

    /**
     * Writes text to a file safely: the text is first written to a temporary file and then
     * moved over the real file, so a crash in the middle never leaves a half-written file.
     */
    public static boolean writeText(String fileName, String content) {
        File target = resolve(fileName);
        File parent = target.getParentFile();
        if (parent != null) {
            parent.mkdirs();
        }
        File temp = new File(target.getPath() + ".tmp");
        try (FileWriter writer = new FileWriter(temp, StandardCharsets.UTF_8)) {
            writer.write(content);
        } catch (IOException e) {
            System.out.println("Error: could not write " + target.getPath() + " (" + e.getMessage() + ")");
            return false;
        }
        try {
            Files.move(temp.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException e) {
            System.out.println("Error: could not save " + target.getPath() + " (" + e.getMessage() + ")");
            return false;
        }
    }

    /** Adds one line at the end of a file (used by the audit log). */
    public static boolean appendLine(String fileName, String line) {
        File target = resolve(fileName);
        File parent = target.getParentFile();
        if (parent != null) {
            parent.mkdirs();
        }
        try (FileWriter writer = new FileWriter(target, StandardCharsets.UTF_8, true)) {
            writer.write(line + System.lineSeparator());
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
