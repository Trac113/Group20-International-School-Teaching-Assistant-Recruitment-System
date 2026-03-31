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
}
