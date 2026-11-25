package com.atlas;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.HexFormat;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Handles login and passwords.
 *
 * Passwords are never stored as plain text. For every user we store a random "salt" and a
 * "hash" made with PBKDF2 (a password-hashing method built into Java). To check a login we hash
 * the typed password with the stored salt and compare the result with the stored hash.
 *
 * Users are saved in data/users.txt. On the very first run a default account is created and
 * its password must be changed at the first login.
 */
public class AuthManager {

    private static final String FILE_NAME = "users.txt";
    private static final String FILE_HEADER = "# username|salt|passwordHash|mustChangePassword";
    private static final String DEFAULT_USER = "admin";
    private static final String DEFAULT_PASSWORD = "Atlas@2026";
    private static final int MAX_ATTEMPTS = 3;
    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final int ITERATIONS = 65536;
    private static final int KEY_BITS = 256;

    /** Small holder for one user's saved login details. */
    private static class User {
        private final String username;
        private String salt;
        private String hash;
        private boolean mustChange;

        private User(String username, String salt, String hash, boolean mustChange) {
            this.username = username;
            this.salt = salt;
            this.hash = hash;
            this.mustChange = mustChange;
        }
    }

    private final ArrayList<User> users = new ArrayList<>();

    public AuthManager() {
        load();
        if (users.isEmpty()) {
            addUser(DEFAULT_USER, DEFAULT_PASSWORD, true);
            System.out.println("First run: the default account '" + DEFAULT_USER + "' was created (see README for the password).");
        }
    }

    // ====================== file handling ======================

    private void load() {
        for (String line : FileHelper.readLines(FILE_NAME)) {
            String[] parts = line.split("\\|", -1);
            if (parts.length == 4) {
                users.add(new User(parts[0], parts[1], parts[2], Boolean.parseBoolean(parts[3])));
            } else {
                System.out.println("Warning: invalid line skipped in " + FILE_NAME);
            }
        }
    }

    private void save() {
        ArrayList<String> lines = new ArrayList<>();
        for (User user : users) {
            lines.add(user.username + "|" + user.salt + "|" + user.hash + "|" + user.mustChange);
        }
        if (!FileHelper.writeLines(FILE_NAME, FILE_HEADER, lines)) {
            System.out.println("WARNING: the user file could not be saved.");
        }
    }

    // ====================== core operations (no user input) ======================

    private User findUser(String username) {
        for (User user : users) {
            if (user.username.equalsIgnoreCase(username)) {
                return user;
            }
        }
        return null;
    }

    /** Creates a user with a freshly generated salt. Returns false if the name exists or the password is weak. */
    public boolean addUser(String username, String password, boolean mustChange) {
        if (findUser(username) != null || !isStrongPassword(password, username)) {
            return false;
        }
        String salt = newSalt();
        users.add(new User(username, salt, hash(password, salt), mustChange));
        save();
        return true;
    }

    /** True if the username exists and the password is correct. */
    public boolean authenticate(String username, String password) {
        User user = findUser(username);
        if (user == null) {
            hash(password, "00");   // do the same amount of work so timing does not reveal which names exist
            return false;
        }
        String attempt = hash(password, user.salt);
        return MessageDigest.isEqual(attempt.getBytes(), user.hash.getBytes());
    }

    /** Sets a new password. Returns false if the user does not exist or the password is too weak. */
    public boolean setPassword(String username, String newPassword) {
        User user = findUser(username);
        if (user == null || !isStrongPassword(newPassword, username)) {
            return false;
        }
        user.salt = newSalt();
        user.hash = hash(newPassword, user.salt);
        user.mustChange = false;
        save();
        return true;
    }

    public boolean mustChangePassword(String username) {
        User user = findUser(username);
        return user != null && user.mustChange;
    }

    /** Password rules: at least 8 characters, with a letter and a digit, and not equal to the username. */
    public static boolean isStrongPassword(String password, String username) {
        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            return false;
        }
        boolean hasLetter = false;
        boolean hasDigit = false;
        for (int i = 0; i < password.length(); i++) {
            char c = password.charAt(i);
            if (Character.isLetter(c)) {
                hasLetter = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            }
        }
        return hasLetter && hasDigit && !password.equalsIgnoreCase(username);
    }

    // ====================== hashing helpers ======================

    private static String newSalt() {
        byte[] bytes = new byte[16];
        new SecureRandom().nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }

    private static String hash(String password, String saltHex) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), HexFormat.of().parseHex(saltHex), ITERATIONS, KEY_BITS);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            return HexFormat.of().formatHex(factory.generateSecret(spec).getEncoded());
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("Password hashing is not available on this system", e);
        }
    }

    // ====================== screens ======================

    /**
     * Asks for username and password (at most 3 tries).
     * Returns the username on success, or null if all tries failed.
     */
    public String login() {
        System.out.println("LOGIN");
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            String username = InputHelper.readText("Username: ", 30);
            String password = InputHelper.readPassword("Password: ");
            if (authenticate(username, password)) {
                String official = findUser(username).username;
                AuditLog.setUser(official);
                AuditLog.log("LOGIN", "success");
                if (mustChangePassword(official)) {
                    System.out.println("\nYou must choose a new password before continuing.");
                    forcePasswordChange(official);
                }
                System.out.println("\nWelcome, " + official + ".");
                return official;
            }
            AuditLog.log("LOGIN", "failed for user '" + username + "'");
            System.out.println("Invalid username or password. Attempts left: " + (MAX_ATTEMPTS - attempt));
        }
        AuditLog.log("LOGIN", "locked out after " + MAX_ATTEMPTS + " failed attempts");
        return null;
    }

    /** Keeps asking until the user sets a valid new password. */
    private void forcePasswordChange(String username) {
        while (!askAndSetNewPassword(username)) {
            // askAndSetNewPassword already explained the problem
        }
    }

    /** Menu option "Change Password": checks the current password first. */
    public void changePasswordScreen(String username) {
        System.out.println("\n--- Change password ---");
        String current = InputHelper.readPassword("Current password: ");
        if (!authenticate(username, current)) {
            System.out.println("Current password is incorrect.");
            AuditLog.log("PASSWORD", "change refused (wrong current password)");
            return;
        }
        if (askAndSetNewPassword(username)) {
            AuditLog.log("PASSWORD", "changed");
            System.out.println("Password changed.");
        }
    }

    private boolean askAndSetNewPassword(String username) {
        System.out.println("Rules: at least " + MIN_PASSWORD_LENGTH + " characters, with a letter and a digit.");
        String first = InputHelper.readPassword("New password: ");
        String second = InputHelper.readPassword("Repeat new password: ");
        if (!first.equals(second)) {
            System.out.println("The two passwords do not match.");
            return false;
        }
        if (!isStrongPassword(first, username)) {
            System.out.println("That password does not follow the rules.");
            return false;
        }
        if (authenticate(username, first)) {
            System.out.println("The new password must be different from the current one.");
            return false;
        }
        return setPassword(username, first);
    }
}
