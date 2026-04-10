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

    private final JobService jobService = new JobService();

    @FXML
    public void handlePostJob() {
        String title = titleField.getText();
        String category = categoryField.getText();
        String description = descriptionArea.getText();
        String requirements = requirementsArea.getText();
        String skillsStr = skillsField.getText();

        if (title.isEmpty() || description.isEmpty() || requirements.isEmpty() || category.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Title, Category, Description and Requirements are required.");
            return;
        }
        
        List<String> requiredSkills = Arrays.stream(skillsStr.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());

        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) return;

        jobService.createJob(title, category, description, requirements, requiredSkills, currentUser.getUsername());
        showAlert(Alert.AlertType.INFORMATION, "Success", "Job posted successfully!");
        
        // Clear fields
        titleField.clear();
        categoryField.clear();
        descriptionArea.clear();
        requirementsArea.clear();
        skillsField.clear();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
