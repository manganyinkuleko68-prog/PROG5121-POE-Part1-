package poe.partone;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Message.java
 *
 * Represents one QuickChat message and keeps the running arrays for the
 * current program run: sent, disregarded and stored messages, plus every
 * message hash and message ID. Stored messages are saved to, and read back
 * from, a JSON file.
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
    public static final String NOT_FOUND = "No matching message was found.";

    public static final int MAX_MESSAGE_LENGTH = 250;

    // ---- The arrays (all filled while the program runs, nothing hard-coded) ----
    private static final List<Message> sentMessages = new ArrayList<>();
    private static final List<Message> disregardedMessages = new ArrayList<>();
    private static final List<Message> storedMessages = new ArrayList<>();
    private static final List<String> messageHashes = new ArrayList<>();
    private static final List<String> messageIDs = new ArrayList<>();

    private static int messagesCreated = 0;
    private static String storageFile = "storedMessages.json";
    private static String sender = "Me";

    // ---- Fields of a single message ----
    private final String messageID;
    private final int messageNumber;
    private final String recipient;
    private final String text;
    private final String loadedHash; // only set for messages read back from the JSON file

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
        this.loadedHash = null;
        messageIDs.add(messageID);
        messageHashes.add(createMessageHash());
    }

    /** Used only when reading a stored message back from the JSON file; keeps its saved hash. */
    private Message(String messageID, String hash, String recipient, String text) {
        this.messageID = messageID;
        this.recipient = recipient;
        this.text = text;
        this.messageNumber = -1;
        this.loadedHash = hash;
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
     * A message read back from the JSON file keeps the hash it was saved with.
     *
     * @return the message hash
     */
    public String createMessageHash() {
        if (loadedHash != null) {
            return loadedHash;
        }
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
        saveStoredMessages();
    }

    /** Writes the whole stored-messages array to the JSON file. */
    private static void saveStoredMessages() {
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

        try (FileWriter writer = new FileWriter(storageFile, StandardCharsets.UTF_8)) {
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
    // Reading the stored messages back from the JSON file
    // ------------------------------------------------------------------

    /**
     * Reads the JSON file into the stored-messages array. Any messages
     * already in the stored array are replaced. If the file does not exist
     * yet, the stored array is simply empty.
     *
     * @return how many stored messages were read
     */
    public static int loadStoredMessages() {
        storedMessages.clear();
        if (!Files.exists(Paths.get(storageFile))) {
            return 0;
        }
        try {
            String json = new String(Files.readAllBytes(Paths.get(storageFile)), StandardCharsets.UTF_8);
            JsonReader reader = new JsonReader(json);
            reader.expect('[');
            reader.skipWhitespace();
            while (reader.peek() != ']') {
                reader.expect('{');
                String id = "";
                String hash = "";
                String recipient = "";
                String text = "";
                reader.skipWhitespace();
                while (reader.peek() != '}') {
                    String key = reader.readString();
                    reader.expect(':');
                    String value = reader.readString();
                    switch (key) {
                        case "messageID":   id = value; break;
                        case "messageHash": hash = value; break;
                        case "recipient":   recipient = value; break;
                        case "message":     text = value; break;
                        default: break; // ignore keys we do not know
                    }
                    reader.skipWhitespace();
                    if (reader.peek() == ',') {
                        reader.expect(',');
                        reader.skipWhitespace();
                    }
                }
                reader.expect('}');
                Message loaded = new Message(id, hash, recipient, text);
                storedMessages.add(loaded);
                if (!messageIDs.contains(id)) {
                    messageIDs.add(id);
                }
                if (!messageHashes.contains(hash)) {
                    messageHashes.add(hash);
                }
                reader.skipWhitespace();
                if (reader.peek() == ',') {
                    reader.expect(',');
                    reader.skipWhitespace();
                }
            }
        } catch (IOException | RuntimeException e) {
            System.out.println("Could not read " + storageFile + ": " + e.getMessage());
        }
        return storedMessages.size();
    }

    /** A very small reader for the JSON layout this program writes (an array of flat string objects). */
    private static class JsonReader {
        private final String s;
        private int i = 0;

        JsonReader(String s) {
            this.s = s;
        }

        void skipWhitespace() {
            while (i < s.length() && Character.isWhitespace(s.charAt(i))) {
                i++;
            }
        }

        char peek() {
            skipWhitespace();
            if (i >= s.length()) {
                throw new IllegalStateException("unexpected end of file");
            }
            return s.charAt(i);
        }

        void expect(char c) {
            if (peek() != c) {
                throw new IllegalStateException("expected '" + c + "' but found '" + s.charAt(i) + "'");
            }
            i++;
        }

        String readString() {
            expect('"');
            StringBuilder out = new StringBuilder();
            while (true) {
                if (i >= s.length()) {
                    throw new IllegalStateException("unterminated string");
                }
                char c = s.charAt(i++);
                if (c == '"') {
                    return out.toString();
                }
                if (c != '\\') {
                    out.append(c);
                    continue;
                }
                char e = s.charAt(i++);
                switch (e) {
                    case 'n': out.append('\n'); break;
                    case 'r': out.append('\r'); break;
                    case 't': out.append('\t'); break;
                    case 'b': out.append('\b'); break;
                    case 'f': out.append('\f'); break;
                    case 'u':
                        out.append((char) Integer.parseInt(s.substring(i, i + 4), 16));
                        i += 4;
                        break;
                    default: out.append(e); // covers \" \\ and \/
                }
            }
        }
    }

    // ------------------------------------------------------------------
    // Part 3: working with the stored messages
    // ------------------------------------------------------------------

    /**
     * @return the sender and recipient of every stored message, one per line
     */
    public static String displaySendersAndRecipients() {
        if (storedMessages.isEmpty()) {
            return "There are no stored messages.";
        }
        StringBuilder out = new StringBuilder();
        for (Message m : storedMessages) {
            if (out.length() > 0) {
                out.append("\n");
            }
            out.append("Sender: ").append(sender).append(" | Recipient: ").append(m.recipient);
        }
        return out.toString();
    }

    /**
     * Finds the longest stored message.
     *
     * @return the text of the longest stored message, or a notice if none are stored
     */
    public static String longestStoredMessage() {
        Message longest = null;
        for (Message m : storedMessages) {
            if (longest == null || m.text.length() > longest.text.length()) {
                longest = m;
            }
        }
        return longest == null ? "There are no stored messages." : longest.text;
    }

    /**
     * Searches the sent and stored messages for a message ID.
     *
     * @param id the message ID to look for
     * @return the recipient and message, or a not-found notice
     */
    public static String searchByMessageID(String id) {
        for (Message m : allSentAndStored()) {
            if (m.messageID.equals(id)) {
                return "Recipient: " + m.recipient + " | Message: " + m.text;
            }
        }
        return NOT_FOUND;
    }

    /**
     * Searches the sent and stored messages for every message to one recipient.
     *
     * @param recipient the recipient cell number
     * @return the matching message texts, in the order they were found
     */
    public static List<String> searchByRecipient(String recipient) {
        List<String> found = new ArrayList<>();
        for (Message m : allSentAndStored()) {
            if (m.recipient.equals(recipient)) {
                found.add(m.text);
            }
        }
        return found;
    }

    /**
     * Deletes a stored message using its message hash, then rewrites the JSON file.
     *
     * @param hash the message hash
     * @return a confirmation with the deleted message, or a not-found notice
     */
    public static String deleteByMessageHash(String hash) {
        for (int i = 0; i < storedMessages.size(); i++) {
            Message m = storedMessages.get(i);
            if (m.createMessageHash().equals(hash)) {
                storedMessages.remove(i);
                messageHashes.remove(hash);
                messageIDs.remove(m.messageID);
                saveStoredMessages();
                return "Message: \"" + m.text + "\" successfully deleted.";
            }
        }
        return NOT_FOUND;
    }

    /**
     * @return the full details of every stored message
     */
    public static String storedMessagesReport() {
        if (storedMessages.isEmpty()) {
            return "There are no stored messages.";
        }
        StringBuilder report = new StringBuilder();
        for (Message m : storedMessages) {
            if (report.length() > 0) {
                report.append("\n\n");
            }
            report.append(m.getDetails());
        }
        return report.toString();
    }

    private static List<Message> allSentAndStored() {
        List<Message> all = new ArrayList<>(sentMessages);
        all.addAll(storedMessages);
        return all;
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

    public String getRecipient() {
        return recipient;
    }

    public String getText() {
        return text;
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

    public static List<String> getMessageHashes() {
        return new ArrayList<>(messageHashes);
    }

    public static List<String> getMessageIDs() {
        return new ArrayList<>(messageIDs);
    }

    /** Sets the name shown as the sender of stored messages. */
    public static void setSender(String name) {
        sender = name;
    }

    /** Changes where stored messages are written (the tests use a temp file). */
    public static void setStorageFile(String path) {
        storageFile = path;
    }

    /** Clears all arrays and counters; the unit tests call this before every test. */
    public static void resetAll() {
        sentMessages.clear();
        disregardedMessages.clear();
        storedMessages.clear();
        messageHashes.clear();
        messageIDs.clear();
        messagesCreated = 0;
    }
}