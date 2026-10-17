package poe.partone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.junit.Before;
import org.junit.Test;

/**
 * MessageTest.java
 * Unit tests for Part 2, using the test data from the brief.
 */
public class MessageTest {

    // Test data from the brief
    private static final String RECIPIENT_1 = "+27718693002";
    private static final String MESSAGE_1 = "Hi Mike, can you join us for dinner tonight?";
    private static final String RECIPIENT_2 = "08575975889";
    private static final String MESSAGE_2 = "Hi Keegan, did you receive the payment?";

    // Fixed IDs so the hash is predictable (real IDs are random)
    private static final String ID_1 = "0012345678";
    private static final String ID_2 = "1234567890";

    private File storageFile;

    @Before
    public void setUp() throws IOException {
        Message.resetAll();
        storageFile = File.createTempFile("quickchat", ".json");
        storageFile.deleteOnExit();
        Message.setStorageFile(storageFile.getAbsolutePath());
    }

    // ---- Message length ----

    @Test
    public void testMessageLength_Success() {
        Message message = new Message(ID_1, RECIPIENT_1, MESSAGE_1);
        assertEquals("Message ready to send.", message.checkMessageLength());
    }

    @Test
    public void testMessageLength_ExactlyLimit_Success() {
        Message message = new Message(ID_1, RECIPIENT_1, "a".repeat(250));
        assertEquals("Message ready to send.", message.checkMessageLength());
    }

    @Test
    public void testMessageLength_Failure() {
        Message message = new Message(ID_1, RECIPIENT_1, "a".repeat(260));
        assertEquals("Message exceeds 250 characters by 10; please reduce the size.",
                message.checkMessageLength());
    }

    // ---- Recipient number ----

    @Test
    public void testRecipient_Success() {
        Message message = new Message(ID_1, RECIPIENT_1, MESSAGE_1);
        assertEquals("Cell phone number successfully captured.", message.checkRecipientCell());
    }

    @Test
    public void testRecipient_Failure() {
        Message message = new Message(ID_2, RECIPIENT_2, MESSAGE_2);
        assertEquals("Cell phone number is incorrectly formatted or does not contain an international code. Please correct the number and try again.",
                message.checkRecipientCell());
    }

    // ---- Message hash ----

    @Test
    public void testMessageHash_Message1() {
        Message message = new Message(ID_1, RECIPIENT_1, MESSAGE_1);
        assertEquals("00:0:HITONIGHT", message.createMessageHash());
    }

    @Test
    public void testMessageHash_AllMessagesInLoop() {
        String[] ids = {ID_1, ID_2};
        String[] recipients = {RECIPIENT_1, RECIPIENT_2};
        String[] texts = {MESSAGE_1, MESSAGE_2};
        String[] expectedHashes = {"00:0:HITONIGHT", "12:1:HIPAYMENT"};

        for (int i = 0; i < texts.length; i++) {
            Message message = new Message(ids[i], recipients[i], texts[i]);
            assertEquals(expectedHashes[i], message.createMessageHash());
        }
    }

    // ---- Message ID ----

    @Test
    public void testMessageID_IsCreated() {
        Message message = new Message(RECIPIENT_1, MESSAGE_1);
        String id = message.getMessageID();

        assertEquals("Message ID generated: " + id, message.getMessageIDMessage());
        assertTrue(message.checkMessageID());
        assertTrue(id.matches("\\d{10}"));
    }

    @Test
    public void testMessageID_TooLong_Fails() {
        Message message = new Message("12345678901", RECIPIENT_1, MESSAGE_1);
        assertFalse(message.checkMessageID());
    }

    // ---- Send / disregard / store ----

    @Test
    public void testSentMessage_Send() {
        Message message = new Message(ID_1, RECIPIENT_1, MESSAGE_1);
        assertEquals("Message successfully sent.", message.SentMessage(1));
    }

    @Test
    public void testSentMessage_Disregard() {
        Message message = new Message(ID_2, RECIPIENT_2, MESSAGE_2);
        assertEquals("Press 0 to delete the message.", message.SentMessage(2));
    }

    @Test
    public void testSentMessage_Store() {
        Message message = new Message(ID_1, RECIPIENT_1, MESSAGE_1);
        assertEquals("Message successfully stored.", message.SentMessage(3));
    }

    // ---- Totals and reports ----

    @Test
    public void testTotalMessages_OnlySentMessagesCount() {
        Message first = new Message(ID_1, RECIPIENT_1, MESSAGE_1);
        Message second = new Message(ID_2, RECIPIENT_2, MESSAGE_2);
        first.SentMessage(1);
        second.SentMessage(2);

        assertEquals(1, Message.returnTotalMessages());
    }

    @Test
    public void testPrintMessages_ShowsDetailsInOrder() {
        new Message(ID_1, RECIPIENT_1, MESSAGE_1).SentMessage(1);

        String expected = "Message ID: 0012345678\n"
                + "Message Hash: 00:0:HITONIGHT\n"
                + "Recipient: +27718693002\n"
                + "Message: Hi Mike, can you join us for dinner tonight?";
        assertEquals(expected, Message.printMessages());
    }

    // ---- JSON storage ----

    @Test
    public void testStoreMessage_WritesJson() throws IOException {
        new Message(ID_1, RECIPIENT_1, MESSAGE_1).SentMessage(3);

        String json = new String(Files.readAllBytes(storageFile.toPath()), StandardCharsets.UTF_8);
        assertTrue(json.contains("\"messageID\": \"0012345678\""));
        assertTrue(json.contains("\"messageHash\": \"00:0:HITONIGHT\""));
        assertTrue(json.contains("\"recipient\": \"+27718693002\""));
        assertTrue(json.contains("\"message\": \"Hi Mike, can you join us for dinner tonight?\""));
    }
}