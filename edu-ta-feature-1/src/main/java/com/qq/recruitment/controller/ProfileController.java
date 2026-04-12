package com.qq.recruitment.controller;

import com.qq.recruitment.model.User;
import com.qq.recruitment.model.UserProfile;
import com.qq.recruitment.service.ProfileService;
import com.qq.recruitment.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ProfileController {

    @FXML
    private TextField majorField;
    @FXML
    private TextField studentIdField;
    @FXML
    private TextField skillsField;
    @FXML
    private TextArea bioArea;

    private final ProfileService profileService = new ProfileService();

    @FXML
    public void initialize() {
        loadProfile();
    }

    private void loadProfile() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
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

        String major = majorField.getText();
        String studentId = studentIdField.getText();
        String skillsStr = skillsField.getText();
        String bio = bioArea.getText();

        List<String> skills = Arrays.stream(skillsStr.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());

        UserProfile profile = new UserProfile(currentUser.getUsername(), major, studentId, skills, bio);
        profileService.updateProfile(profile);

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
