package com.qq.recruitment.controller;

import com.qq.recruitment.model.Job;
import com.qq.recruitment.service.JobService;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class JobController {

    @FXML
    private TextField titleField;

    @FXML
    private TextArea descriptionArea;

    @FXML
    private TextArea requirementsArea;
    
    // In a real app, this would be injected or retrieved from a SessionManager
    private String currentUser = "admin"; 

    private final JobService jobService = new JobService();

    @FXML
    public void handlePostJob() {
        String title = titleField.getText();
        String description = descriptionArea.getText();
        String requirements = requirementsArea.getText();

        if (title.isEmpty() || description.isEmpty() || requirements.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "All fields are required.");
            return;
        }

        jobService.createJob(title, description, requirements, currentUser);
        showAlert(Alert.AlertType.INFORMATION, "Success", "Job posted successfully!");
        
        // Clear fields
        titleField.clear();
        descriptionArea.clear();
        requirementsArea.clear();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
