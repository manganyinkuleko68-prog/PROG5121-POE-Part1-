package poe.partone;

import java.util.Scanner;

/**
 * PoePartOne.java
 *
 * Entry point for Part 1 of the PoE. Runs a console-based registration and
 * login flow: it prompts the user for their details, registers them via the
 * {@link Login} class (validating username, password, and cell number
 * formatting), and then prompts for login credentials to verify against the
 * stored registration.
 *
 * @author Student
 */
public class PoePartOne {

    /**
     * Prints a prompt and reads a single line of input from the console.
     *
     * @param scanner the Scanner reading from System.in
     * @param prompt the message to display before reading input
     * @return the line entered by the user
     */
    private static String promptFor(Scanner scanner, String prompt) {
        System.out.println(prompt);
        return scanner.nextLine();
    }

    /**
     * Runs the registration and login console flow.
     *
     * Collects the user's first name, last name, username, password, and cell
     * number, then attempts registration via {@link Login#registerUser}. If
     * registration fails, the program prints the failure reason and exits. If
     * registration succeeds, the program prompts for login credentials and
     * prints the resulting login status.
     *
     * @param args the command line arguments (not used)
     */
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Login login = new Login();

        String firstName = promptFor(scanner, "Please enter your first name:");
        String lastName = promptFor(scanner, "Please enter your last name:");
        String username = promptFor(scanner, "Please enter the username:");
        String password = promptFor(scanner, "Enter password");
        String cellNumber = promptFor(scanner, "Enter callphone");

        String registrationResult = login.registerUser(username, password, cellNumber, firstName, lastName);
        System.out.println(registrationResult);

        if (!login.isRegistered()) {
            System.out.println("Registration failed. Please restart the application and try again.");
            scanner.close();
            return;
        }

        System.out.println("\nNow please log in.");
        String loginUsername = promptFor(scanner, "Enter username:");
        String loginPassword = promptFor(scanner, "Enter password:");

        boolean loginSuccessful = login.loginUser(loginUsername, loginPassword);
        System.out.println(login.returnLoginStatus(loginSuccessful));

        scanner.close();
    }
}
