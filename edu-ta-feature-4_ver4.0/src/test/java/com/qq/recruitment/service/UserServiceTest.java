package com.qq.recruitment.service;

import com.qq.recruitment.model.User;
import org.junit.jupiter.api.Test;
import java.io.File;
import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTest {

    @Test
    public void testRegisterAndLogin() {
        // Clean up test data
        File file = new File("src/main/resources/data/users.json");
        if (file.exists()) {
            file.delete();
        }

        UserService userService = new UserService();
        
        // Test Register
        boolean registered = userService.register("testuser", "password123", "Test User", "APPLICANT");
        assertTrue(registered, "Registration should succeed");

        // Test Duplicate Register
        boolean duplicate = userService.register("testuser", "password123", "Test User", "APPLICANT");
        assertFalse(duplicate, "Duplicate registration should fail");

        // Test Login Success
        User user = userService.login("testuser", "password123");
        assertNotNull(user, "Login should succeed");
        assertEquals("Test User", user.getFullName());
        assertEquals("APPLICANT", user.getRole());

        // Test Login Failure
        User badUser = userService.login("testuser", "wrongpassword");
        assertNull(badUser, "Login with wrong password should fail");
        
        User nonExistent = userService.login("nonexistent", "password");
        assertNull(nonExistent, "Login with non-existent user should fail");
    }

    @Test
    public void testRegisterAndLoginShouldRejectBlankSpaces() {
        File file = new File("src/main/resources/data/users.json");
        if (file.exists()) {
            file.delete();
        }

        UserService userService = new UserService();

        boolean blankUsername = userService.register("   ", "password123", "Test User", "APPLICANT");
        assertFalse(blankUsername);

        boolean blankPassword = userService.register("testuser", "   ", "Test User", "APPLICANT");
        assertFalse(blankPassword);

        boolean hasSpaceUsername = userService.register(" testuser ", "password123", "Test User", "APPLICANT");
        assertFalse(hasSpaceUsername, "Username containing spaces should be rejected");

        boolean hasSpacePassword = userService.register("testuser", " password123 ", "Test User", "APPLICANT");
        assertFalse(hasSpacePassword, "Password containing spaces should be rejected");

        boolean ok = userService.register("testuser", "password123", "Test User", "APPLICANT");
        assertTrue(ok, "Valid credentials without spaces should be accepted");

        User user = userService.login("testuser", "password123");
        assertNotNull(user);

        User blankLogin = userService.login("   ", "   ");
        assertNull(blankLogin);
    }

    @Test
    public void testRegisterShouldRejectNonAsciiAndDeleteShouldOnlyBlockAdmin() {
        File file = new File("src/main/resources/data/users.json");
        if (file.exists()) {
            file.delete();
        }

        UserService userService = new UserService();

        boolean chineseUsername = userService.register("测试用户", "password123", "Test", "APPLICANT");
        assertFalse(chineseUsername, "Chinese username should be rejected");

        boolean chinesePassword = userService.register("testuser", "密码123", "Test", "APPLICANT");
        assertFalse(chinesePassword, "Chinese password should be rejected");

        assertTrue(userService.register("admin01", "admin123", "Admin", "ADMIN"));
        assertTrue(userService.register("teacher_a", "teacher123", "Teacher", "TEACHER"));
        assertTrue(userService.register("student_a", "student123", "Student", "APPLICANT"));

        assertFalse(userService.deleteUser("admin01"), "ADMIN should not be deletable");
        assertTrue(userService.deleteUser("teacher_a"), "TEACHER should be deletable");
        assertTrue(userService.deleteUser("student_a"), "APPLICANT should be deletable");
    }

    @Test
    public void testRegisterShouldRejectIllegalCharactersAndInvalidRole() {
        File file = new File("src/main/resources/data/users.json");
        if (file.exists()) {
            file.delete();
        }

        UserService userService = new UserService();

        assertFalse(userService.register("bad/name", "password123", "Bad", "APPLICANT"),
                "Username with illegal symbol should be rejected");
        assertFalse(userService.register("normal_user", "pass\u0001word", "Bad", "APPLICANT"),
                "Password with control chars should be rejected");
        assertFalse(userService.register("normal_user", "password123", "Bad", "HACKER"),
                "Unknown role should be rejected");
    }
}
