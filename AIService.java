package com.qq.recruitment.controller;

import com.qq.recruitment.model.Job;
import com.qq.recruitment.service.ApplicationService;
import com.qq.recruitment.service.JobService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

public class ApplicantController {

    @FXML
    private TableView<Job> jobTable;
    @FXML
    private TableColumn<Job, String> titleColumn;
    @FXML
    private TableColumn<Job, String> descriptionColumn;
    @FXML
    private TableColumn<Job, String> requirementsColumn;
    @FXML
    private TableColumn<Job, Void> actionColumn;

    private final JobService jobService = new JobService();
    private final ApplicationService applicationService = new ApplicationService();
    
    // In a real app, this would be injected from session
    private String currentUser = "student1"; 

    @FXML
    public void initialize() {
        // Setup columns
        titleColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTitle()));
        descriptionColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDescription()));
        requirementsColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRequirements()));

        // Add "Apply" button to each row
        actionColumn.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button("Apply");

            {
                btn.setOnAction(event -> {
                    Job job = getTableView().getItems().get(getIndex());
                    handleApply(job);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(btn);
                }
            }
        });

        loadJobs();
    }

    private void loadJobs() {
        ObservableList<Job> jobs = FXCollections.observableArrayList(jobService.getOpenJobs());
        jobTable.setItems(jobs);
    }

    private void handleApply(Job job) {
        // Basic interaction: Confirm and Apply
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Apply for " + job.getTitle() + "?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait();

        if (alert.getResult() == ButtonType.YES) {
            // For Sprint 1/2, resume path is dummy or file picker can be added later
            String dummyResumePath = "path/to/resume.pdf"; 
            
            boolean success = applicationService.apply(job.getId(), currentUser, dummyResumePath);
            
            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Application submitted!");
            } else {
                showAlert(Alert.AlertType.WARNING, "Warning", "You have already applied for this job.");
            }
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
