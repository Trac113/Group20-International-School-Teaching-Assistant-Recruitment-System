package com.qq.recruitment.controller;

import com.qq.recruitment.model.Job;
import com.qq.recruitment.service.JobService;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import com.qq.recruitment.util.SessionManager;
import com.qq.recruitment.model.User;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class JobController {
    private static final int UPPER_MAX_APPLICANTS = Job.MAX_ALLOWED_APPLICANTS;

    @FXML
    private TextField titleField;
    @FXML
    private TextField categoryField;
    @FXML
    private TextArea descriptionArea;
    @FXML
    private TextArea requirementsArea;
    @FXML
    private TextField skillsField;
    @FXML
    private TextField maxApplicantsField;

    private final JobService jobService = new JobService();

    @FXML
    public void handlePostJob() {
        String title = titleField.getText() == null ? "" : titleField.getText().trim();
        String category = categoryField.getText() == null ? "" : categoryField.getText().trim();
        String description = descriptionArea.getText() == null ? "" : descriptionArea.getText().trim();
        String requirements = requirementsArea.getText() == null ? "" : requirementsArea.getText().trim();
        String skillsStr = skillsField.getText();
        String maxApplicantsText = maxApplicantsField.getText();

        if (title.isBlank() || description.isBlank() || requirements.isBlank() || category.isBlank()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Title, Category, Description and Requirements are required.");
            return;
        }

        if (maxApplicantsText == null || maxApplicantsText.isBlank()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Max applicants is required.");
            return;
        }

        int maxApplicants;
        try {
            maxApplicants = Integer.parseInt(maxApplicantsText.trim());
            if (maxApplicants <= 0 || maxApplicants > UPPER_MAX_APPLICANTS) {
                showAlert(Alert.AlertType.ERROR, "Error", "Max applicants must be between 1 and " + UPPER_MAX_APPLICANTS + ".");
                return;
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Max applicants must be numeric.");
            return;
        }
        
        List<String> requiredSkills = Arrays.stream(skillsStr.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());

        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) {
            showAlert(Alert.AlertType.ERROR, "Session Expired", "Please login again before posting a job.");
            return;
        }
        if (!"TEACHER".equals(currentUser.getRole()) && !"ADMIN".equals(currentUser.getRole())) {
            showAlert(Alert.AlertType.ERROR, "Permission Denied", "Only TEACHER or ADMIN can post jobs.");
            return;
        }

        try {
            jobService.createJob(title, category, description, requirements, requiredSkills, currentUser.getUsername(), maxApplicants);
            showAlert(Alert.AlertType.INFORMATION, "Success", "Job posted successfully!");
        } catch (IllegalArgumentException e) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", e.getMessage());
            return;
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to post job: " + e.getMessage());
            return;
        }
        
        // Clear fields
        titleField.clear();
        categoryField.clear();
        descriptionArea.clear();
        requirementsArea.clear();
        skillsField.clear();
        maxApplicantsField.clear();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
