package com.atlas;

import java.io.Console;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Every question asked to the user goes through this class.
 *
 * Each method keeps asking until the user gives a valid answer, so invalid input
 * (letters instead of numbers, empty text, wrong dates) can never crash the program
 * or reach the records.
 *
 * All text is read with nextLine() and converted afterwards. This avoids the classic
 * Scanner problem where nextInt() leaves the Enter key behind in the input.
 */
public class InputHelper {

    /** Largest ID a user can type. */
    public static final int MAX_ID = 999999;

    private static final Scanner scanner = new Scanner(System.in);

    private InputHelper() {
        // utility class
    }

    /** Reads a non-empty text of at most maxLength characters that does not contain the | character. */
    public static String readText(String prompt, int maxLength) {
        while (true) {
            System.out.print(prompt);
            String text = scanner.nextLine().trim();
            if (text.isEmpty()) {
                System.out.println("  Input cannot be empty.");
            } else if (text.length() > maxLength) {
                System.out.println("  Please use at most " + maxLength + " characters.");
            } else if (text.contains(BaseRecord.DELIMITER)) {
                System.out.println("  The " + BaseRecord.DELIMITER + " character is not allowed.");
            } else {
                return text;
            }
        }
    }

    /** Reads a whole number between min and max (both included). */
    public static int readInt(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String text = scanner.nextLine().trim();
            try {
                int number = Integer.parseInt(text);
                if (number >= min && number <= max) {
                    return number;
                }
            } catch (NumberFormatException e) {
                // fall through to the message below
            }
            System.out.println("  Please enter a whole number from " + min + " to " + max + ".");
        }
    }

    /** Shows a numbered list of options and returns the position (starting at 0) of the chosen one. */
    public static int readChoiceIndex(String title, String[] options) {
        System.out.println(title);
        for (int i = 0; i < options.length; i++) {
            System.out.println("  " + (i + 1) + ". " + options[i]);
        }
        return readInt("Choose 1-" + options.length + ": ", 1, options.length) - 1;
    }

    /** Shows a numbered list of options and returns the text of the chosen one. */
    public static String readChoice(String title, String[] options) {
        return options[readChoiceIndex(title, options)];
    }

    /** Reads a date in yyyy-MM-dd format. */
    public static String readDate(String prompt) {
        while (true) {
            System.out.print(prompt);
            String text = scanner.nextLine().trim();
            try {
                return LocalDate.parse(text).toString();
            } catch (DateTimeParseException e) {
                System.out.println("  Please use the format yyyy-MM-dd (for example 2026-11-25).");
            }
        }
    }

    /** Asks a yes/no question. */
    public static boolean confirm(String prompt) {
        while (true) {
            System.out.print(prompt + " (y/n): ");
            String text = scanner.nextLine().trim().toLowerCase();
            if (text.equals("y") || text.equals("yes")) {
                return true;
            }
            if (text.equals("n") || text.equals("no")) {
                return false;
            }
            System.out.println("  Please type y or n.");
        }
    }

    /**
     * Reads a password. In a real terminal the typing is hidden.
     * Some development tools do not provide a real terminal; then the password is visible.
     */
    public static String readPassword(String prompt) {
        Console console = System.console();
        if (console != null) {
            char[] typed = console.readPassword(prompt);
            if (typed == null) {
                throw new NoSuchElementException("input closed");
            }
            String password = new String(typed);
            Arrays.fill(typed, ' ');
            return password;
        }
        System.out.print(prompt);
        return scanner.nextLine();
    }
}
