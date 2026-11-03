package poe.partone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

/**
 * StoredMessagesTest.java
 * Unit tests for Part 3, using the test data from the brief.
 */
public class StoredMessagesTest {

    // Test data from the brief (message number, recipient, text, flag)
    private static final String ID_1 = "1012345678";
    private static final String RECIPIENT_1 = "+27834557896";
    private static final String MESSAGE_1 = "Did you get the cake?";

    private static final String ID_2 = "2012345678";
    private static final String RECIPIENT_2 = "+27838884567";
    private static final String MESSAGE_2 = "Where are you? You are late! I have asked you to be on time.";

    private static final String ID_3 = "3012345678";
    private static final String RECIPIENT_3 = "+27834484567";
    private static final String MESSAGE_3 = "Yohoooo, I am at your gate.";

    private static final String ID_4 = "4012345678";
    private static final String RECIPIENT_4 = "0838884567";
    private static final String MESSAGE_4 = "It is dinner time!";

    private static final String ID_5 = "5012345678";
    private static final String RECIPIENT_5 = "+27838884567";
    private static final String MESSAGE_5 = "Ok, I am leaving without you.";

    private File storageFile;

    /** Fills the arrays with the five test messages, applying each message's flag. */
    @Before
    public void setUp() throws IOException {
        Message.resetAll();
        storageFile = File.createTempFile("quickchat3", ".json");
        storageFile.deleteOnExit();
        Message.setStorageFile(storageFile.getAbsolutePath());
        Message.setSender("Test Sender");

        new Message(ID_1, RECIPIENT_1, MESSAGE_1).SentMessage(1);      // Sent
        new Message(ID_2, RECIPIENT_2, MESSAGE_2).SentMessage(3);      // Stored
        new Message(ID_3, RECIPIENT_3, MESSAGE_3).SentMessage(2);      // Disregard
        new Message(ID_4, RECIPIENT_4, MESSAGE_4).SentMessage(1);      // Sent
        new Message(ID_5, RECIPIENT_5, MESSAGE_5).SentMessage(3);      // Stored
    }

    private static List<String> texts(List<Message> messages) {
        java.util.ArrayList<String> out = new java.util.ArrayList<>();
        for (Message m : messages) {
            out.add(m.getText());
        }
        return out;
    }

    // ---- The unit tests required by the brief ----

    @Test
    public void testSentMessagesArrayCorrectlyPopulated() {
        assertEquals(Arrays.asList("Did you get the cake?", "It is dinner time!"),
                texts(Message.getSentMessages()));
    }

    @Test
    public void testDisplayLongestMessage() {
        assertEquals("Where are you? You are late! I have asked you to be on time.",
                Message.longestStoredMessage());
    }

    @Test
    public void testSearchForMessageID() {
        String result = Message.searchByMessageID(ID_4);
        assertEquals("Recipient: 0838884567 | Message: It is dinner time!", result);
    }

    @Test
    public void testSearchAllMessagesForRecipient() {
        assertEquals(Arrays.asList(
                "Where are you? You are late! I have asked you to be on time.",
                "Ok, I am leaving without you."),
                Message.searchByRecipient("+27838884567"));
    }

    @Test
    public void testDeleteMessageUsingMessageHash() {
        String hash = "20:1:WHERETIME";
        assertEquals("Message: \"Where are you? You are late! I have asked you to be on time.\" successfully deleted.",
                Message.deleteByMessageHash(hash));
        assertEquals(Arrays.asList("Ok, I am leaving without you."), texts(Message.getStoredMessages()));
    }

    @Test
    public void testDisplayReportListsSentMessages() {
        String report = Message.printMessages();
        assertTrue(report.contains("Message Hash: 10:0:DIDCAKE"));
        assertTrue(report.contains("Recipient: +27834557896"));
        assertTrue(report.contains("Message: Did you get the cake?"));
        assertTrue(report.contains("Message Hash: 40:3:ITTIME"));
        assertTrue(report.contains("Recipient: 0838884567"));
        assertTrue(report.contains("Message: It is dinner time!"));
    }

    // ---- The other arrays ----

    @Test
    public void testDisregardedMessagesArray() {
        assertEquals(Arrays.asList("Yohoooo, I am at your gate."), texts(Message.getDisregardedMessages()));
    }

    @Test
    public void testStoredMessagesArray() {
        assertEquals(Arrays.asList(MESSAGE_2, MESSAGE_5), texts(Message.getStoredMessages()));
    }

    @Test
    public void testMessageHashArrayContainsAllHashes() {
        assertEquals(Arrays.asList("10:0:DIDCAKE", "20:1:WHERETIME", "30:2:YOHOOOOGATE",
                "40:3:ITTIME", "50:4:OKYOU"), Message.getMessageHashes());
    }

    @Test
    public void testMessageIDArrayContainsAllIDs() {
        assertEquals(Arrays.asList(ID_1, ID_2, ID_3, ID_4, ID_5), Message.getMessageIDs());
    }

    // ---- Stored messages menu options ----

    @Test
    public void testDisplaySenderAndRecipientOfStoredMessages() {
        assertEquals("Sender: Test Sender | Recipient: +27838884567\n"
                + "Sender: Test Sender | Recipient: +27838884567",
                Message.displaySendersAndRecipients());
    }

    @Test
    public void testStoredMessagesReport() {
        String report = Message.storedMessagesReport();
        assertTrue(report.contains("Message ID: " + ID_2));
        assertTrue(report.contains("Message Hash: 20:1:WHERETIME"));
        assertTrue(report.contains("Message: " + MESSAGE_5));
        assertTrue(!report.contains(MESSAGE_1));
    }

    @Test
    public void testSearchForUnknownMessageID() {
        assertEquals("No matching message was found.", Message.searchByMessageID("9999999999"));
    }

    @Test
    public void testSearchForUnknownRecipient() {
        assertTrue(Message.searchByRecipient("+27000000000").isEmpty());
    }

    @Test
    public void testDeleteWithUnknownHash() {
        assertEquals("No matching message was found.", Message.deleteByMessageHash("00:0:NOTHERE"));
        assertEquals(2, Message.getStoredMessages().size());
    }

    // ---- Reading the JSON file back into the stored array ----

    @Test
    public void testStoredMessagesAreReadBackFromJson() {
        Message.resetAll(); // empty every array, the file on disk is kept
        assertEquals(2, Message.loadStoredMessages());
        assertEquals(Arrays.asList(MESSAGE_2, MESSAGE_5), texts(Message.getStoredMessages()));
        assertEquals("20:1:WHERETIME", Message.getStoredMessages().get(0).createMessageHash());
        assertEquals(Arrays.asList("20:1:WHERETIME", "50:4:OKYOU"), Message.getMessageHashes());
        assertEquals(Arrays.asList(ID_2, ID_5), Message.getMessageIDs());
    }

    @Test
    public void testLoadedMessagesCanBeSearchedAndDeleted() {
        Message.resetAll();
        Message.loadStoredMessages();
        assertEquals("Recipient: +27838884567 | Message: Ok, I am leaving without you.",
                Message.searchByMessageID(ID_5));
        assertEquals("Message: \"Ok, I am leaving without you.\" successfully deleted.",
                Message.deleteByMessageHash("50:4:OKYOU"));
        assertEquals(1, Message.loadStoredMessages());
    }

    @Test
    public void testDeleteUpdatesTheJsonFile() {
        Message.deleteByMessageHash("20:1:WHERETIME");
        Message.resetAll();
        assertEquals(1, Message.loadStoredMessages());
        assertEquals(Arrays.asList(MESSAGE_5), texts(Message.getStoredMessages()));
    }

    @Test
    public void testLoadWhenFileDoesNotExist() {
        Message.resetAll();
        Message.setStorageFile(storageFile.getAbsolutePath() + ".missing");
        assertEquals(0, Message.loadStoredMessages());
        assertTrue(Message.getStoredMessages().isEmpty());
    }

    @Test
    public void testSpecialCharactersSurviveTheJsonRoundTrip() {
        String tricky = "She said \"hi\" \\ then left\tquickly";
        new Message("6012345678", "+27831112222", tricky).SentMessage(3);
        Message.resetAll();
        Message.loadStoredMessages();
        assertEquals(3, Message.getStoredMessages().size());
        assertEquals(tricky, Message.getStoredMessages().get(2).getText());
    }
}