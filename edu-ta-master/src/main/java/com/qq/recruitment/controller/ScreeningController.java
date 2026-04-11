package com.qq.recruitment.controller;

import com.qq.recruitment.model.Application;
import com.qq.recruitment.model.UserProfile;
import com.qq.recruitment.service.ApplicationService;
import com.qq.recruitment.service.ProfileService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ScreeningController {

    @FXML
    private TableView<Application> applicationTable;
    @FXML
    private TableColumn<Application, String> jobIdColumn;
    @FXML
    private TableColumn<Application, String> applicantColumn;
    @FXML
    private TableColumn<Application, String> appliedAtColumn;
    @FXML
    private TableColumn<Application, String> statusColumn;
    @FXML
    private TableColumn<Application, Number> scoreColumn;
    @FXML
    private TableColumn<Application, Void> actionColumn;

    @FXML
    private Button batchAcceptBtn;
    @FXML
    private Button batchRejectBtn;
    
    @FXML
    private VBox detailDrawer;
    @FXML
    private Label detailNameLabel;
    @FXML
    private Label detailMajorLabel;
    @FXML
    private Label detailSkillsLabel;
    @FXML
    private Label detailWorkloadLabel;
    @FXML
    private Label detailResumeLabel;
    @FXML
    private Label detailAiAnalysisLabel;
    @FXML
    private Button openResumeBtn;

    private final ApplicationService applicationService = new ApplicationService();
    private final ProfileService profileService = new ProfileService();
    private String selectedResumePath;

    @FXML
    public void initialize() {
        // Setup columns
        jobIdColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getJobId()));
        applicantColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getApplicantUsername()));
        appliedAtColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getAppliedAt()));
        statusColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));
        scoreColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleIntegerProperty(data.getValue().getMatchScore()));

        // Enable multi-selection for batch operations
        applicationTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // Add selection listener to update detail drawer
        applicationTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                updateDetailDrawer(newSelection);
            }
        });

        // Add "Accept/Reject" buttons to each row
        actionColumn.setCellFactory(param -> new TableCell<>() {
            private final Button acceptBtn = new Button("Accept");
            private final Button rejectBtn = new Button("Reject");
            private final HBox pane = new HBox(5, acceptBtn, rejectBtn);

            {
                acceptBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
                rejectBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");

                acceptBtn.setOnAction(event -> {
                    Application app = getTableView().getItems().get(getIndex());
                    handleUpdateStatus(app, "ACCEPTED");
                });

                rejectBtn.setOnAction(event -> {
                    Application app = getTableView().getItems().get(getIndex());
                    handleUpdateStatus(app, "REJECTED");
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Application app = getTableView().getItems().get(getIndex());
                    if ("PENDING".equals(app.getStatus())) {
                        setGraphic(pane);
                    } else {
                        setGraphic(new Label(app.getStatus()));
                    }
                }
            }
        });

        loadApplications();
    }

    private void loadApplications() {
        // Load all applications for screening (in a real scenario, this would be filtered by the logged-in teacher's posted jobs)
        ObservableList<Application> apps = FXCollections.observableArrayList(applicationService.getAllApplications());
        applicationTable.setItems(apps);
    }
    
    // Helper to refresh table
    public void refreshTable() {
         loadApplications();
    }

    private void updateDetailDrawer(Application app) {
        UserProfile profile = profileService.getProfile(app.getApplicantUsername());
        if (profile != null && profile.getUsername() != null) {
            detailNameLabel.setText("Name: " + profile.getUsername());
            detailMajorLabel.setText("Major: " + (profile.getMajor() != null ? profile.getMajor() : "-"));
            detailSkillsLabel.setText("Skills: " + (profile.getSkills() != null && !profile.getSkills().isEmpty() ? String.join(", ", profile.getSkills()) : "-"));
            detailWorkloadLabel.setText("Current Workload: " + profile.getWorkload());
        } else {
            detailNameLabel.setText("Name: " + app.getApplicantUsername() + " (No Profile)");
            detailMajorLabel.setText("Major: -");
            detailSkillsLabel.setText("Skills: -");
            detailWorkloadLabel.setText("Current Workload: 0");
        }
        detailResumeLabel.setText("Resume: " + (app.getResumePath() != null ? app.getResumePath() : "-"));
        selectedResumePath = app.getResumePath();
        detailAiAnalysisLabel.setText(app.getAiAnalysis() != null && !app.getAiAnalysis().isEmpty() ? app.getAiAnalysis() : "No AI Analysis available.");
    }

    @FXML
    public void handleOpenResume() {
        if (selectedResumePath == null || selectedResumePath.isBlank() || "-".equals(selectedResumePath)) {
            showAlert(Alert.AlertType.WARNING, "Warning", "No resume path available for this application.");
            return;
        }

        File resumeFile = resolveResumeFile(selectedResumePath);
        if (!resumeFile.exists()) {
            showAlert(Alert.AlertType.ERROR, "File Not Found", "Resume file does not exist:\n" + resumeFile.getPath());
            return;
        }

        if (!Desktop.isDesktopSupported()) {
            showAlert(Alert.AlertType.ERROR, "Unsupported", "Desktop open is not supported in this environment.");
            return;
        }

        try {
            Desktop.getDesktop().open(resumeFile);
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Open Failed", "Failed to open resume file:\n" + e.getMessage());
        }
    }

    private File resolveResumeFile(String rawPath) {
        Path path = Paths.get(rawPath);
        if (path.isAbsolute()) {
            return path.toFile();
        }
        return Paths.get("").toAbsolutePath().resolve(path).normalize().toFile();
    }

    @FXML
    public void handleBatchAccept() {
        ObservableList<Application> selected = applicationTable.getSelectionModel().getSelectedItems();
        if (selected.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Warning", "No applications selected.");
            return;
        }
        boolean updated = false;
        for (Application app : selected) {
            if ("PENDING".equals(app.getStatus())) {
                applicationService.updateApplicationStatus(app.getId(), "ACCEPTED");
                app.setStatus("ACCEPTED");
                updated = true;
            }
        }
        if (updated) {
            applicationTable.refresh();
            // Re-select to trigger drawer update
            Application lastSelected = applicationTable.getSelectionModel().getSelectedItem();
            if (lastSelected != null) {
                updateDetailDrawer(lastSelected);
            }
            showAlert(Alert.AlertType.INFORMATION, "Success", "Batch accept completed.");
        }
    }

    @FXML
    public void handleBatchReject() {
        ObservableList<Application> selected = applicationTable.getSelectionModel().getSelectedItems();
        if (selected.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Warning", "No applications selected.");
            return;
        }
        boolean updated = false;
        for (Application app : selected) {
            if ("PENDING".equals(app.getStatus())) {
                applicationService.updateApplicationStatus(app.getId(), "REJECTED");
                app.setStatus("REJECTED");
                updated = true;
            }
        }
        if (updated) {
            applicationTable.refresh();
            showAlert(Alert.AlertType.INFORMATION, "Success", "Batch reject completed.");
        }
    }

    private void handleUpdateStatus(Application app, String newStatus) {
        boolean success = applicationService.updateApplicationStatus(app.getId(), newStatus);
        if (success) {
            app.setStatus(newStatus);
            applicationTable.refresh();
            // Update drawer if it's currently selected
            if (app.equals(applicationTable.getSelectionModel().getSelectedItem())) {
                updateDetailDrawer(app);
            }
            showAlert(Alert.AlertType.INFORMATION, "Success", "Application " + newStatus.toLowerCase() + ".");
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to update status.");
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
