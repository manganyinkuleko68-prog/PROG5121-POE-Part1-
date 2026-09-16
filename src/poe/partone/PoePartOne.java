package poe.partone;

import java.util.Scanner;

/**
 * PoePartOne.java
 *
 * Entry point for Part 1 of the PoE. Runs a console-based registration
 * and login flow: it prompts the user for their details, registers them
 * via the {@link Login} class (validating username, password, and cell
 * number formatting), and then prompts for login credentials to verify
 * against the stored registration.
 *
 * @author Student
 */
public class PoePartOne {

    /**
     * Runs the registration and login console flow.
     *
     * Collects the user's first name, last name, username, password, and
     * cell number, then attempts registration via {@link Login#registerUser}.
     * If registration fails, the program prints the failure reason and
     * exits. If registration succeeds, the program prompts for login
     * credentials and prints the resulting login status.
     *
     * @param args the command line arguments (not used)
     */
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Login login = new Login();

        System.out.println("Please enter your first name:");
        String firstName = scanner.nextLine();

        System.out.println("Please enter your last name:");
        String lastName = scanner.nextLine();

        System.out.println("Please enter the username:");
        String username = scanner.nextLine();

        System.out.println("Enter password");
        String password = scanner.nextLine();

        System.out.println("Enter callphone");
        String cellNumber = scanner.nextLine();

        String registrationResult = login.registerUser(username, password, cellNumber, firstName, lastName);
        System.out.println(registrationResult);

        if (!login.isRegistered()) {
            System.out.println("Registration failed. Please restart the application and try again.");
            scanner.close();
            return;
        }

        System.out.println("\nNow please log in.");
        System.out.println("Enter username:");
        String loginUsername = scanner.nextLine();

        System.out.println("Enter password:");
        String loginPassword = scanner.nextLine();

        boolean loginSuccessful = login.loginUser(loginUsername, loginPassword);
        System.out.println(login.returnLoginStatus(loginSuccessful));

        scanner.close();
    }
}