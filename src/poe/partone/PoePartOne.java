/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package poe.partone;

import java.util.Scanner;

/**
 *
 * @author Student
 */
public class PoePartOne {

    /**
     * @param args the command line arguments
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
        
        
        
        
    
