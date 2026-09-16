 /*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package poe.partone;

import java.util.regex.Pattern;

/**
 *
 * @author Student
 */
public class Login {

    // Stored account details (single-user store).
    private String storedUsername;
    private String storedPassword;
    private String storedCellNumber;
    private String storedFirstName;
    private String storedLastName;
    private boolean isRegistered = false;

    // Regex patterns
    private static final Pattern UPPERCASE = Pattern.compile(".*[A-Z].*");
    private static final Pattern DIGIT = Pattern.compile(".*[0-9].*");
    private static final Pattern SPECIAL_CHAR = Pattern.compile(".*[^a-zA-Z0-9].*");
    private static final Pattern CELL_NUMBER = Pattern.compile("^\\+27\\d{9}$");

    // Checks that the username contains an underscore and is no more
    // than five characters long.
    public boolean checkUserName(String username) {
        return username.contains("_") && username.length() <= 5;
    }

    // Checks that the password is at least eight characters long and
    // contains a capital letter, a number, and a special character.
    public boolean checkPasswordComplexity(String password) {
        return password.length() >= 8
                && UPPERCASE.matcher(password).matches()
                && DIGIT.matcher(password).matches()
                && SPECIAL_CHAR.matcher(password).matches();
    }

    // Checks that the cell number contains the South African
    // international code followed by the number.
    public boolean checkCellPhoneNumber(String cellNumber) {
        return CELL_NUMBER.matcher(cellNumber).matches();
    }

    // Registers a user, validating username, password and cell number.
    public String registerUser(String username, String password, String cellNumber,
                                String firstName, String lastName) {

        if (!checkUserName(username)) {
            return "Username is not correctly formatted; please ensure that your username contains an underscore and is no more than five characters in length.";
        }

        if (!checkPasswordComplexity(password)) {
            return "Password is not correctly formatted; please ensure that the password contains at least eight characters, a capital letter, a number, and a special character.";
        }

        if (!checkCellPhoneNumber(cellNumber)) {
            return "Cell number is incorrectly formatted or does not contain an international code; please correct the number and try again.";
        }

        this.storedUsername = username;
        this.storedPassword = password;
        this.storedCellNumber = cellNumber;
        this.storedFirstName = firstName;
        this.storedLastName = lastName;
        this.isRegistered = true;

        return "Username successfully captured.\nPassword successfully captured.\nCell number successfully captured.";
    }

    // Verifies that the entered login details match the stored details.
    public boolean loginUser(String username, String password) {
        if (!isRegistered) {
            return false;
        }
        return storedUsername.equals(username) && storedPassword.equals(password);
    }

    // Returns the appropriate login status message.
    public String returnLoginStatus(boolean loginSuccessful) {
        if (loginSuccessful) {
            return "Welcome " + storedFirstName + ", " + storedLastName + " it is great to see you again.";
        }
        return "Username or password incorrect, please try again.";
    }

    public boolean isRegistered() {
        return isRegistered;
    }
}