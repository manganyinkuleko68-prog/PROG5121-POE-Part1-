package poe.partone;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class LoginTest {

    private Login login;

    @Before
    public void setUp() {
        login = new Login();
    }

    @Test
    public void testUsername_CorrectlyFormatted_ReturnsTrue() {
        assertTrue(login.checkUserName("kyl_1"));
    }

    @Test
    public void testUsername_IncorrectlyFormatted_ReturnsFalse() {
        assertFalse(login.checkUserName("kylian123"));
    }

    @Test
    public void testPassword_MeetsComplexity_ReturnsTrue() {
        assertTrue(login.checkPasswordComplexity("Ch&&sec@ke99!"));
    }

    @Test
    public void testPassword_DoesNotMeetComplexity_ReturnsFalse() {
        assertFalse(login.checkPasswordComplexity("password"));
    }

    @Test
    public void testCellPhone_CorrectlyFormatted_ReturnsTrue() {
        assertTrue(login.checkCellPhoneNumber("+27838968976"));
    }

    @Test
    public void testCellPhone_IncorrectlyFormatted_ReturnsFalse() {
        assertFalse(login.checkCellPhoneNumber("08966553"));
    }

    @Test
    public void testLogin_Successful_ReturnsTrue() {
        login.registerUser("kyl_1", "Ch&&sec@ke99!", "+27838968976", "Kyle", "Adams");
        assertTrue(login.loginUser("kyl_1", "Ch&&sec@ke99!"));
    }

    @Test
    public void testLogin_Failed_ReturnsFalse() {
        login.registerUser("kyl_1", "Ch&&sec@ke99!", "+27838968976", "Kyle", "Adams");
        assertFalse(login.loginUser("kyl_1", "wrongPassword1!"));
    }

    @Test
    public void testRegisterUser_PasswordMessage_Failure() {
        String result = login.registerUser("kyl_1", "password", "+27838968976", "Kyle", "Adams");
        assertEquals(
            "Password is not correctly formatted; please ensure that the password contains at least eight characters, a capital letter, a number, and a special character.",
            result
        );
    }

    @Test
    public void testRegisterUser_UsernameMessage_Failure() {
        String result = login.registerUser("kylian123", "Ch&&sec@ke99!", "+27838968976", "Kyle", "Adams");
        assertEquals(
            "Username is not correctly formatted; please ensure that your username contains an underscore and is no more than five characters in length.",
            result
        );
    }

    @Test
    public void testRegisterUser_CellMessage_Failure() {
        String result = login.registerUser("kyl_1", "Ch&&sec@ke99!", "08966553", "Kyle", "Adams");
        assertEquals(
            "Cell number is incorrectly formatted or does not contain an international code; please correct the number and try again.",
            result
        );
    }

    @Test
    public void testReturnLoginStatus_Success() {
        login.registerUser("kyl_1", "Ch&&sec@ke99!", "+27838968976", "Kyle", "Adams");
        boolean success = login.loginUser("kyl_1", "Ch&&sec@ke99!");
        assertEquals("Welcome Kyle, Adams it is great to see you again.", login.returnLoginStatus(success));
    }

    @Test
    public void testReturnLoginStatus_Failure() {
        login.registerUser("kyl_1", "Ch&&sec@ke99!", "+27838968976", "Kyle", "Adams");
        boolean success = login.loginUser("kyl_1", "wrongPassword1!");
        assertEquals("Username or password incorrect, please try again.", login.returnLoginStatus(success));
    }
}