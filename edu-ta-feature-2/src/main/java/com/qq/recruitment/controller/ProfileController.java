package com.qq.recruitment.controller;

import com.qq.recruitment.model.User;
import com.qq.recruitment.model.UserProfile;
import com.qq.recruitment.service.ProfileService;
import com.qq.recruitment.service.UserService;
import com.qq.recruitment.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for the applicant profile editing screen.
 * Loads existing profile data and handles save with validation for major, student ID, skills, and bio.
 */
public class ProfileController {

    @FXML
    private TextField fullNameField;
    @FXML
    private TextField majorField;
    @FXML
    private TextField studentIdField;
    @FXML
    private TextField skillsField;
    @FXML
    private TextArea bioArea;

    private final ProfileService profileService = new ProfileService();
    private final UserService userService = new UserService();

    @FXML
    public void initialize() {
        loadProfile();
    }

    private void loadProfile() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            fullNameField.setText(currentUser.getFullName() != null ? currentUser.getFullName() : "");
            UserProfile profile = profileService.getProfile(currentUser.getUsername());
            if (profile != null) {
                majorField.setText(profile.getMajor() != null ? profile.getMajor() : "");
                studentIdField.setText(profile.getStudentId() != null ? profile.getStudentId() : "");
                if (profile.getSkills() != null) {
                    skillsField.setText(String.join(", ", profile.getSkills()));
                }
                bioArea.setText(profile.getBio() != null ? profile.getBio() : "");
            }
        }
    }

    @FXML
    private void handleSave() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Not logged in.");
            return;
        }

        String fullName = fullNameField.getText() == null ? "" : fullNameField.getText().trim();
        String major = majorField.getText() == null ? "" : majorField.getText().trim();
        String studentId = studentIdField.getText() == null ? "" : studentIdField.getText().trim();
        String skillsStr = skillsField.getText() == null ? "" : skillsField.getText();
        String bio = bioArea.getText() == null ? "" : bioArea.getText().trim();

        if (fullName.isBlank()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Full name cannot be blank.");
            return;
        }
        if (major.isBlank() || studentId.isBlank()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Major and Student ID cannot be blank.");
            return;
        }

        // Update full name via UserService
        userService.updateFullName(currentUser.getUsername(), fullName);

        List<String> skills = Arrays.stream(skillsStr.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());

        UserProfile existingProfile = profileService.getProfile(currentUser.getUsername());
        int currentWorkload = (existingProfile != null) ? existingProfile.getWorkload() : 0;

        UserProfile profile = new UserProfile(currentUser.getUsername(), major, studentId, skills, bio);
        profile.setWorkload(currentWorkload);
        try {
            profileService.updateProfile(profile);
        } catch (IllegalArgumentException ex) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", ex.getMessage());
            return;
        }

        // Refresh session user
        currentUser.setFullName(fullName);
        showAlert(Alert.AlertType.INFORMATION, "Success", "Profile updated successfully!");
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
