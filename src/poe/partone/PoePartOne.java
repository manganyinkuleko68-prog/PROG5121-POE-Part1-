package poe.partone;

import java.util.Scanner;

/**
 * PoePartOne.java
 *
 * Console entry point. Part 1: register and log in. Part 2: once logged in,
 * the QuickChat menu lets the user send, disregard or store messages.
 * Part 3: the Stored Messages menu works with the stored messages array.
 *
 * @author Student
 */
public class PoePartOne {

    /** The registered user's name; shown as the sender of stored messages. */
    private static String senderName = "Me";

    /**
     * Runs registration, login and then the QuickChat menu.
     *
     * @param args command line arguments (not used)
     */
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Login login = new Login();

        if (registerUser(scanner, login) && loginUser(scanner, login)) {
            runQuickChat(scanner);
        }
        scanner.close();
    }

    // ------------------------------------------------------------------
    // Part 1: registration and login
    // ------------------------------------------------------------------

    private static boolean registerUser(Scanner scanner, Login login) {
        System.out.println("=== Registration ===");
        String firstName = promptFor(scanner, "Please enter your first name:");
        String lastName = promptFor(scanner, "Please enter your last name:");
        String username = promptFor(scanner, "Please enter the username:");
        String password = promptFor(scanner, "Enter password:");
        String cellNumber = promptFor(scanner, "Enter cell phone number:");
        senderName = firstName.trim() + " " + lastName.trim();

        System.out.println(login.registerUser(username, password, cellNumber, firstName, lastName));
        if (!login.isRegistered()) {
            System.out.println("Registration failed. Please restart the application and try again.");
        }
        return login.isRegistered();
    }

    private static boolean loginUser(Scanner scanner, Login login) {
        System.out.println("\n=== Login ===");
        String username = promptFor(scanner, "Enter username:");
        String password = promptFor(scanner, "Enter password:");

        boolean loggedIn = login.loginUser(username, password);
        System.out.println(login.returnLoginStatus(loggedIn));
        return loggedIn;
    }

    // ------------------------------------------------------------------
    // Part 2: QuickChat
    // ------------------------------------------------------------------

    private static void runQuickChat(Scanner scanner) {
        System.out.println("\nWelcome to QuickChat.");
        Message.setSender(senderName);
        int loaded = Message.loadStoredMessages();
        if (loaded > 0) {
            System.out.println(loaded + " stored message(s) loaded from the JSON file.");
        }

        int limit = promptForInt(scanner, "How many messages do you wish to enter?", 1, Integer.MAX_VALUE);
        int entered = 0;
        boolean running = true;

        while (running) {
            System.out.println("\n1) Send Messages");
            System.out.println("2) Show recently sent messages");
            System.out.println("3) Quit");
            System.out.println("4) Stored Messages");
            int choice = promptForInt(scanner, "Choose an option:", 1, 4);

            switch (choice) {
                case 1:
                    if (entered >= limit) {
                        System.out.println("You have already entered all " + limit + " messages.");
                    } else {
                        entered = sendMessages(scanner, entered, limit);
                    }
                    break;
                case 2:
                    System.out.println(Message.printMessages());
                    break;
                case 4:
                    storedMessagesMenu(scanner);
                    break;
                default:
                    running = false;
                    break;
            }
        }
        System.out.println("Goodbye.");
    }

    // ------------------------------------------------------------------
    // Part 3: Stored Messages
    // ------------------------------------------------------------------

    private static void storedMessagesMenu(Scanner scanner) {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- Stored Messages ---");
            System.out.println("1) Display the sender and recipient of all stored messages");
            System.out.println("2) Display the longest stored message");
            System.out.println("3) Search for a message ID");
            System.out.println("4) Search for all messages to a recipient");
            System.out.println("5) Delete a message using the message hash");
            System.out.println("6) Display a report of all stored messages");
            System.out.println("7) Back to the main menu");
            int choice = promptForInt(scanner, "Choose an option:", 1, 7);

            switch (choice) {
                case 1:
                    System.out.println(Message.displaySendersAndRecipients());
                    break;
                case 2:
                    System.out.println(Message.longestStoredMessage());
                    break;
                case 3:
                    String id = promptFor(scanner, "Enter the message ID:");
                    System.out.println(Message.searchByMessageID(id.trim()));
                    break;
                case 4:
                    String recipient = promptFor(scanner, "Enter the recipient cell number:");
                    java.util.List<String> found = Message.searchByRecipient(recipient.trim());
                    if (found.isEmpty()) {
                        System.out.println(Message.NOT_FOUND);
                    } else {
                        for (String text : found) {
                            System.out.println("\"" + text + "\"");
                        }
                    }
                    break;
                case 5:
                    String hash = promptFor(scanner, "Enter the message hash:");
                    System.out.println(Message.deleteByMessageHash(hash.trim().toUpperCase()));
                    break;
                case 6:
                    System.out.println(Message.storedMessagesReport());
                    break;
                default:
                    back = true;
                    break;
            }
        }
    }

    /** Collects messages until the limit is reached; returns how many have been entered so far. */
    private static int sendMessages(Scanner scanner, int entered, int limit) {
        while (entered < limit) {
            System.out.println("\n--- Message " + (entered + 1) + " of " + limit + " ---");

            String recipient = promptFor(scanner, "Enter the recipient cell number (e.g. +27718693002):");
            String recipientCheck = Message.validateRecipient(recipient);
            boolean recipientValid = recipientCheck.equals(Message.RECIPIENT_OK);
            System.out.println(recipientCheck);

            String text = promptFor(scanner, "Enter your message (max 250 characters):");
            while (!Message.validateMessageLength(text).equals(Message.LENGTH_OK)) {
                System.out.println(Message.validateMessageLength(text));
                text = promptFor(scanner, "Enter your message (max 250 characters):");
            }
            System.out.println(Message.LENGTH_OK);

            Message message = new Message(recipient, text);
            System.out.println(message.getMessageIDMessage());

            System.out.println("\n1) Send Message");
            System.out.println("2) Disregard Message");
            System.out.println("3) Store Message to send later");
            int option = promptForInt(scanner, "Choose an option:", 1, 3);
            while (option == 1 && !recipientValid) {
                System.out.println("This message cannot be sent until the recipient number is correct. Please disregard or store it.");
                option = promptForInt(scanner, "Choose an option:", 1, 3);
            }

            System.out.println(message.SentMessage(option));
            if (option == 1) {
                System.out.println("\n" + message.getDetails());
            } else if (option == 2) {
                String answer = promptFor(scanner, "Press 0 to delete the message:");
                if (answer.trim().equals("0")) {
                    System.out.println("Message deleted.");
                }
            }
            entered++;
        }

        System.out.println("\nAll " + limit + " messages have been entered.");
        System.out.println("Total messages sent: " + Message.returnTotalMessages());
        return entered;
    }

    // ------------------------------------------------------------------
    // Input helpers
    // ------------------------------------------------------------------

    private static String promptFor(Scanner scanner, String prompt) {
        System.out.println(prompt);
        return scanner.nextLine();
    }

    /** Keeps asking until the user types a whole number between min and max (inclusive). */
    private static int promptForInt(Scanner scanner, String prompt, int min, int max) {
        while (true) {
            String input = promptFor(scanner, prompt).trim();
            try {
                int value = Integer.parseInt(input);
                if (value >= min && value <= max) {
                    return value;
                }
            } catch (NumberFormatException e) {
                // fall through to the error message below
            }
            System.out.println("Please enter a whole number between " + min + " and "
                    + (max == Integer.MAX_VALUE ? "a larger number" : String.valueOf(max)) + ".");
        }
    }
}