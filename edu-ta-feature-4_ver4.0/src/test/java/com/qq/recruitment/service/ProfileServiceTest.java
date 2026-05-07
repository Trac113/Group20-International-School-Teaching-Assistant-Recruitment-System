package com.qq.recruitment.service;

import com.qq.recruitment.model.UserProfile;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ProfileServiceTest {

    @Test
    public void testUpdateProfileShouldRejectIllegalCharacters() {
        File profileFile = new File("src/main/resources/data/profiles.json");
        if (profileFile.exists()) {
            profileFile.delete();
        }

        ProfileService profileService = new ProfileService();
        UserProfile profile = new UserProfile("student_1", "Computer\u0001Science", "2024001", List.of("Java"), "Bio");

        assertThrows(IllegalArgumentException.class, () -> profileService.updateProfile(profile));
    }

    @Test
    public void testUpdateProfileShouldAllowValidData() {
        File profileFile = new File("src/main/resources/data/profiles.json");
        if (profileFile.exists()) {
            profileFile.delete();
        }

        ProfileService profileService = new ProfileService();
        UserProfile profile = new UserProfile("student_1", "Computer Science", "2024001", List.of("Java", "OOP"), "Bio text");

        assertDoesNotThrow(() -> profileService.updateProfile(profile));
    }
}
