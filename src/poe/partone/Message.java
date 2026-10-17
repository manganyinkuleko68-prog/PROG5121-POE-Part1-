package poe.partone;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Message.java
 *
 * Represents one QuickChat message and keeps the running lists/totals for
 * the current program run (sent, disregarded and stored messages).
 *
 * @author Student
 */
public class Message {

    // ---- Messages required by the brief ----
    public static final String RECIPIENT_OK = "Cell phone number successfully captured.";
    public static final String RECIPIENT_BAD = "Cell phone number is incorrectly formatted or does not contain an international code. Please correct the number and try again.";
    public static final String LENGTH_OK = "Message ready to send.";
    public static final String SENT_OK = "Message successfully sent.";
    public static final String DISREGARDED = "Press 0 to delete the message.";
    public static final String STORED_OK = "Message successfully stored.";
    public static final String INVALID_CHOICE = "Invalid option; please choose 1, 2 or 3.";

    public static final int MAX_MESSAGE_LENGTH = 250;

    // ---- Running state for this program run ----
    private static final List<Message> sentMessages = new ArrayList<>();
    private static final List<Message> disregardedMessages = new ArrayList<>();
    private static final List<Message> storedMessages = new ArrayList<>();
    private static int messagesCreated = 0;
    private static String storageFile = "storedMessages.json";

    // ---- Fields of a single message ----
    private final String messageID;
    private final int messageNumber;
    private final String recipient;
    private final String text;

    /** Creates a message with an auto-generated ten-digit ID. */
    public Message(String recipient, String text) {
        this(generateMessageID(), recipient, text);
    }

    /** Creates a message with a supplied ID (used by the unit tests so the hash is predictable). */
    public Message(String messageID, String recipient, String text) {
        this.messageID = messageID;
        this.recipient = recipient;
        this.text = text;
        this.messageNumber = messagesCreated++;
    }

    // ------------------------------------------------------------------
    // Validation
    // ------------------------------------------------------------------

    /**
     * Generates a random ten-digit message ID (leading zeros allowed).
     *
     * @return a ten character string made only of digits
     */
    private static String generateMessageID() {
        long number = ThreadLocalRandom.current().nextLong(10_000_000_000L);
        return String.format("%010d", number);
    }

    /**
     * Checks that the message ID is not more than ten characters long.
     *
     * @return true if the ID is valid, false otherwise
     */
    public boolean checkMessageID() {
        return messageID != null && !messageID.isEmpty() && messageID.length() <= 10;
    }

    /**
     * Validates a recipient cell number by reusing the Part 1 check in Login.
     *
     * @param recipient the cell number to validate
     * @return the success or failure message
     */
    public static String validateRecipient(String recipient) {
        if (new Login().checkCellPhoneNumber(recipient)) {
            return RECIPIENT_OK;
        }
        return RECIPIENT_BAD;
    }

    /**
     * Validates the length of a message body.
     *
     * @param text the message text
     * @return "Message ready to send." or how many characters it is over by
     */
    public static String validateMessageLength(String text) {
        int over = text.length() - MAX_MESSAGE_LENGTH;
        if (over > 0) {
            return "Message exceeds " + MAX_MESSAGE_LENGTH + " characters by " + over
                    + "; please reduce the size.";
        }
        return LENGTH_OK;
    }

    /**
     * Checks this message's recipient number.
     *
     * @return the success or failure message
     */
    public String checkRecipientCell() {
        return validateRecipient(recipient);
    }

    /**
     * Checks this message's length.
     *
     * @return the success or failure message
     */
    public String checkMessageLength() {
        return validateMessageLength(text);
    }

    // ------------------------------------------------------------------
    // Hash and ID
    // ------------------------------------------------------------------

    /**
     * Builds the message hash: first two digits of the ID, the message
     * number, then the first and last word of the message, in capitals.
     * Punctuation is removed from the words, e.g. 00:0:HITONIGHT.
     *
     * @return the message hash
     */
    public String createMessageHash() {
        String[] words = text.trim().split("\\s+");
        String firstWord = cleanWord(words[0]);
        String lastWord = cleanWord(words[words.length - 1]);
        String idStart = messageID.length() >= 2 ? messageID.substring(0, 2) : messageID;
        return (idStart + ":" + messageNumber + ":" + firstWord + lastWord).toUpperCase();
    }

    private static String cleanWord(String word) {
        return word.replaceAll("[^A-Za-z0-9]", "");
    }

    /**
     * @return the "Message ID generated" confirmation text
     */
    public String getMessageIDMessage() {
        return "Message ID generated: " + messageID;
    }

    // ------------------------------------------------------------------
    // Send / disregard / store
    // ------------------------------------------------------------------

    /**
     * Applies the user's choice for this message.
     *
     * @param choice 1 = send, 2 = disregard, 3 = store to send later
     * @return the message for the chosen option
     */
    public String SentMessage(int choice) {
        switch (choice) {
            case 1:
                sentMessages.add(this);
                return SENT_OK;
            case 2:
                disregardedMessages.add(this);
                return DISREGARDED;
            case 3:
                storeMessage();
                return STORED_OK;
            default:
                return INVALID_CHOICE;
        }
    }

    /**
     * Stores this message and rewrites the JSON file with every stored
     * message so far.
     */
    public void storeMessage() {
        storedMessages.add(this);
        StringBuilder json = new StringBuilder("[\n");
        for (int i = 0; i < storedMessages.size(); i++) {
            Message m = storedMessages.get(i);
            json.append("  {\n")
                .append("    \"messageID\": \"").append(escapeJson(m.messageID)).append("\",\n")
                .append("    \"messageHash\": \"").append(escapeJson(m.createMessageHash())).append("\",\n")
                .append("    \"recipient\": \"").append(escapeJson(m.recipient)).append("\",\n")
                .append("    \"message\": \"").append(escapeJson(m.text)).append("\"\n")
                .append("  }");
            json.append(i < storedMessages.size() - 1 ? ",\n" : "\n");
        }
        json.append("]\n");

        try (FileWriter writer = new FileWriter(storageFile)) {
            writer.write(json.toString());
        } catch (IOException e) {
            System.out.println("Could not write " + storageFile + ": " + e.getMessage());
        }
    }

    /** Escapes the characters that are not allowed raw inside a JSON string. */
    private static String escapeJson(String value) {
        StringBuilder out = new StringBuilder();
        for (char c : value.toCharArray()) {
            switch (c) {
                case '"':  out.append("\\\""); break;
                case '\\': out.append("\\\\"); break;
                case '\n': out.append("\\n"); break;
                case '\r': out.append("\\r"); break;
                case '\t': out.append("\\t"); break;
                default:
                    if (c < 0x20) {
                        out.append(String.format("\\u%04x", (int) c));
                    } else {
                        out.append(c);
                    }
            }
        }
        return out.toString();
    }

    // ------------------------------------------------------------------
    // Reporting
    // ------------------------------------------------------------------

    /**
     * @return this message's details in the order: ID, hash, recipient, message
     */
    public String getDetails() {
        return "Message ID: " + messageID + "\n"
                + "Message Hash: " + createMessageHash() + "\n"
                + "Recipient: " + recipient + "\n"
                + "Message: " + text;
    }

    /**
     * @return the details of every message sent during this run
     */
    public static String printMessages() {
        if (sentMessages.isEmpty()) {
            return "No messages have been sent.";
        }
        StringBuilder report = new StringBuilder();
        for (Message m : sentMessages) {
            if (report.length() > 0) {
                report.append("\n\n");
            }
            report.append(m.getDetails());
        }
        return report.toString();
    }

    /**
     * @return how many messages have been sent during this run
     */
    public static int returnTotalMessages() {
        return sentMessages.size();
    }

    // ------------------------------------------------------------------
    // Accessors / housekeeping
    // ------------------------------------------------------------------

    public String getMessageID() {
        return messageID;
    }

    public static List<Message> getSentMessages() {
        return new ArrayList<>(sentMessages);
    }

    public static List<Message> getDisregardedMessages() {
        return new ArrayList<>(disregardedMessages);
    }

    public static List<Message> getStoredMessages() {
        return new ArrayList<>(storedMessages);
    }

    /** Changes where stored messages are written (the tests use a temp file). */
    public static void setStorageFile(String path) {
        storageFile = path;
    }

    /** Clears all lists and counters; the unit tests call this before every test. */
    public static void resetAll() {
        sentMessages.clear();
        disregardedMessages.clear();
        storedMessages.clear();
        messagesCreated = 0;
    }
}