package poe.partone;

import java.util.regex.Pattern;

/**
 * Login.java
 *
 * Handles user registration and login validation for Part 1 of the PoE.
 *
 * Regex resources consulted while building the validation patterns: - Password
 * complexity pattern adapted from discussion on Stack Overflow:
 * https://stackoverflow.com/questions/19605150/regex-for-password-must-contain-at-least-eight-characters-at-least-one-number
 * - South African cell number / international code pattern adapted from:
 * https://stackoverflow.com/questions/29467750/regex-for-south-african-mobile-numbers
 *
 * @author Student
 */
public class Login {

    // Stored account details (single-user store, as this PoE only requires
    // one registered account at a time).
    private String storedUsername;
    private String storedPassword;
    private String storedCellNumber;
    private String storedFirstName;
    private String storedLastName;
    private boolean isRegistered = false;

    // Password: min 8 chars, at least one uppercase letter, one digit, one special character.
    private static final Pattern UPPERCASE = Pattern.compile(".*[A-Z].*");
    private static final Pattern DIGIT = Pattern.compile(".*[0-9].*");
    private static final Pattern SPECIAL_CHAR = Pattern.compile(".*[^a-zA-Z0-9].*");

    // South African cell number: '+27' international code followed by 9 digits.
    private static final Pattern CELL_NUMBER = Pattern.compile("^\\+27\\d{9}$");

    // Registration failure messages, extracted here so they aren't repeated
    // inline in registerUser().
    private static final String USERNAME_ERROR
            = "Username is not correctly formatted; please ensure that your username contains an underscore and is no more than five characters in length.";
    private static final String PASSWORD_ERROR
            = "Password is not correctly formatted; please ensure that the password contains at least eight characters, a capital letter, a number, and a special character.";
    private static final String CELLNUMBER_ERROR
            = "Cell number is incorrectly formatted or does not contain an international code; please correct the number and try again.";

    /**
     * Checks that the username contains an underscore and is no more than five
     * characters long.
     *
     * @param username the username string to validate
     * @return true if the username is correctly formatted, false otherwise
     */
    public boolean checkUserName(String username) {
        if (username == null) {
            return false;
        }
        return username.contains("_") && username.length() <= 5;
    }

    /**
     * Checks that the password is at least eight characters long and contains a
     * capital letter, a number, and a special character.
     *
     * @param password the password string to validate
     * @return true if the password meets all complexity requirements, false
     * otherwise
     */
    public boolean checkPasswordComplexity(String password) {
        if (password == null) {
            return false;
        }
        return password.length() >= 8
                && UPPERCASE.matcher(password).matches()
                && DIGIT.matcher(password).matches()
                && SPECIAL_CHAR.matcher(password).matches();
    }

    /**
     * Checks that the cell number contains the South African international code
     * (+27) followed by the number.
     *
     * @param cellNumber the cell number string to validate
     * @return true if the cell number is correctly formatted, false otherwise
     */
    public boolean checkCellPhoneNumber(String cellNumber) {
        if (cellNumber == null) {
            return false;
        }
        return CELL_NUMBER.matcher(cellNumber).matches();
    }

    /**
     * Registers a user, validating the username, password, and cell number in
     * turn. Stores the user's details if all checks pass.
     *
     * @param username the desired username
     * @param password the desired password
     * @param cellNumber the user's South African cell number
     * @param firstName the user's first name
     * @param lastName the user's last name
     * @return a message indicating which validation failed, or a success
     * message if registration succeeded
     */
    public String registerUser(String username, String password, String cellNumber,
            String firstName, String lastName) {

        if (!checkUserName(username)) {
            return USERNAME_ERROR;
        }

        if (!checkPasswordComplexity(password)) {
            return PASSWORD_ERROR;
        }

        if (!checkCellPhoneNumber(cellNumber)) {
            return CELLNUMBER_ERROR;
        }

        this.storedUsername = username;
        this.storedPassword = password;
        this.storedCellNumber = cellNumber;
        this.storedFirstName = firstName;
        this.storedLastName = lastName;
        this.isRegistered = true;

        return "Username successfully captured.\nPassword successfully captured.\nCell number successfully captured.";
    }

    /**
     * Verifies that the entered login details match the stored details from
     * registration.
     *
     * @param username the entered username
     * @param password the entered password
     * @return true if the credentials match the registered account, false
     * otherwise
     */
    public boolean loginUser(String username, String password) {
        if (!isRegistered) {
            return false;
        }
        return storedUsername.equals(username) && storedPassword.equals(password);
    }

    /**
     * Returns the appropriate login status message.
     *
     * @param loginSuccessful whether the login attempt succeeded
     * @return a welcome message if successful, or a failure message otherwise
     */
    public String returnLoginStatus(boolean loginSuccessful) {
        if (loginSuccessful) {
            return "Welcome " + storedFirstName + ", " + storedLastName + " it is great to see you again.";
        }
        return "Username or password incorrect, please try again.";
    }

    /**
     * Returns whether a user is currently registered.
     *
     * @return true if a user has been registered, false otherwise
     */
    public boolean isRegistered() {
        return isRegistered;
    }
}
