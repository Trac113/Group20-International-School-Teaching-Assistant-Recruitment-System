package com.qq.recruitment.controller;

import com.qq.recruitment.model.Application;
import com.qq.recruitment.model.Job;
import com.qq.recruitment.model.User;
import com.qq.recruitment.model.UserProfile;
import com.qq.recruitment.service.ApplicationService;
import com.qq.recruitment.service.JobService;
import com.qq.recruitment.service.ProfileService;
import com.qq.recruitment.service.UserService;
import com.qq.recruitment.util.SessionManager;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ScreeningController {

    @FXML
    private TableView<Application> applicationTable;
    @FXML
    private TableColumn<Application, String> jobIdColumn;
    @FXML
    private TableColumn<Application, String> jobTitleColumn;
    @FXML
    private TableColumn<Application, String> applicantColumn;
    @FXML
    private TableColumn<Application, String> workloadColumn;
    @FXML
    private TableColumn<Application, String> capacityColumn;
    @FXML
    private TableColumn<Application, String> appliedAtColumn;
    @FXML
    private TableColumn<Application, String> statusColumn;
    @FXML
    private TableColumn<Application, Number> scoreColumn;
    @FXML
    private TableColumn<Application, Void> actionColumn;
    @FXML
    private TableColumn<Application, Boolean> selectColumn;

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
    @FXML
    private Button saveResumeBtn;

    private final ApplicationService applicationService = new ApplicationService();
    private final ProfileService profileService = new ProfileService();
    private final JobService jobService = new JobService();
    private final UserService userService = new UserService();
    private String selectedResumePath;
    private final Map<String, BooleanProperty> checkedMap = new LinkedHashMap<>();
    private Map<String, Job> jobMap = new LinkedHashMap<>();

    @FXML
    public void initialize() {
        // Setup columns
        applicationTable.setEditable(true);
        jobIdColumn.setCellValueFactory(data -> new SimpleStringProperty(JobService.toDisplayJobId(data.getValue().getJobId())));
        jobTitleColumn.setCellValueFactory(data -> new SimpleStringProperty(resolveJobTitle(data.getValue().getJobId())));
        applicantColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getApplicantUsername()));
        workloadColumn.setCellValueFactory(data -> new SimpleStringProperty(resolveApplicantWorkload(data.getValue().getApplicantUsername())));
        capacityColumn.setCellValueFactory(data -> new SimpleStringProperty(resolveJobCapacity(data.getValue().getJobId())));
        appliedAtColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getAppliedAt()));
        statusColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));
        scoreColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleIntegerProperty(data.getValue().getMatchScore()));

        selectColumn.setEditable(true);
        selectColumn.setCellValueFactory(cellData -> getCheckedProperty(cellData.getValue().getId()));
        selectColumn.setCellFactory(CheckBoxTableCell.forTableColumn(selectColumn));

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
                        if (isDeletedApplicant(app)) {
                            acceptBtn.setDisable(true);
                            rejectBtn.setDisable(true);
                            acceptBtn.setText("Account Deleted");
                            rejectBtn.setText("Account Deleted");
                            acceptBtn.setTooltip(new Tooltip("Cannot review applications from deleted accounts."));
                            rejectBtn.setTooltip(new Tooltip("Cannot review applications from deleted accounts."));
                        } else if (!canCurrentReviewerHandle(app)) {
                            acceptBtn.setDisable(true);
                            rejectBtn.setDisable(true);
                            acceptBtn.setText("No Permission");
                            rejectBtn.setText("No Permission");
                            acceptBtn.setTooltip(new Tooltip("You can only screen your own posted jobs."));
                            rejectBtn.setTooltip(new Tooltip("You can only screen your own posted jobs."));
                        } else if (isAtCapacity(app)) {
                            acceptBtn.setDisable(true);
                            rejectBtn.setDisable(false);
                            acceptBtn.setText("At Capacity");
                            rejectBtn.setText("Reject");
                            acceptBtn.setTooltip(new Tooltip("Applicant workload reached limit: "
                                    + applicationService.getApplicantWorkload(app.getApplicantUsername())
                                    + "/" + applicationService.getApplicantMaxWorkload(app.getApplicantUsername())));
                            rejectBtn.setTooltip(null);
                        } else {
                            acceptBtn.setDisable(false);
                            rejectBtn.setDisable(false);
                            acceptBtn.setText("Accept");
                            rejectBtn.setText("Reject");
                            acceptBtn.setTooltip(null);
                            rejectBtn.setTooltip(null);
                        }
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
        Task<LoadResult> task = new Task<>() {
            @Override
            protected LoadResult call() {
                List<Job> jobs = jobService.getAllJobs();
                Map<String, Job> localJobMap = jobs.stream()
                        .filter(j -> j.getId() != null)
                        .collect(Collectors.toMap(Job::getId, j -> j, (a, b) -> a, LinkedHashMap::new));
                User currentUser = SessionManager.getInstance().getCurrentUser();
                List<Application> filtered = filterByRole(applicationService.getAllApplications(), currentUser, localJobMap);
                return new LoadResult(FXCollections.observableArrayList(filtered), localJobMap);
            }
        };

        task.setOnSucceeded(event -> Platform.runLater(() -> {
            LoadResult result = task.getValue();
            applicationTable.setItems(result.items());
            jobMap = result.jobMap();
            checkedMap.clear();
            applicationTable.refresh();
        }));
        task.setOnFailed(event -> Platform.runLater(() -> {
            applicationTable.setItems(FXCollections.observableArrayList());
            jobMap = new LinkedHashMap<>();
            checkedMap.clear();
        }));

        Thread thread = new Thread(task, "screening-loader");
        thread.setDaemon(true);
        thread.start();
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
            int current = applicationService.getApplicantWorkload(app.getApplicantUsername());
            int max = applicationService.getApplicantMaxWorkload(app.getApplicantUsername());
            detailWorkloadLabel.setText("Current Workload: " + current + "/" + max);
        } else {
            detailNameLabel.setText("Name: " + app.getApplicantUsername() + " (No Profile)");
            detailMajorLabel.setText("Major: -");
            detailSkillsLabel.setText("Skills: -");
            detailWorkloadLabel.setText("Current Workload: 0/" + applicationService.getApplicantMaxWorkload(app.getApplicantUsername()));
        }
        detailResumeLabel.setText("Resume: " + (app.getResumePath() != null ? app.getResumePath() : "-"));
        selectedResumePath = app.getResumePath();
        String capacityHint = isAtCapacity(app)
                ? "\n\n[Capacity Notice] Applicant reached workload limit and cannot be accepted now."
                : "";
        detailAiAnalysisLabel.setText((app.getAiAnalysis() != null && !app.getAiAnalysis().isEmpty() ? app.getAiAnalysis() : "No AI Analysis available.") + capacityHint);
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

    @FXML
    public void handleSaveResumeAs() {
        if (selectedResumePath == null || selectedResumePath.isBlank() || "-".equals(selectedResumePath)) {
            showAlert(Alert.AlertType.WARNING, "Warning", "No resume path available for this application.");
            return;
        }

        File resumeFile = resolveResumeFile(selectedResumePath);
        if (!resumeFile.exists()) {
            showAlert(Alert.AlertType.ERROR, "File Not Found", "Resume file does not exist:\n" + resumeFile.getPath());
            return;
        }

        javafx.stage.FileChooser chooser = new javafx.stage.FileChooser();
        chooser.setTitle("Save Resume As");
        chooser.setInitialFileName(resumeFile.getName());
        File target = chooser.showSaveDialog(applicationTable.getScene().getWindow());
        if (target == null) {
            return;
        }

        try {
            Files.copy(resumeFile.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
            showAlert(Alert.AlertType.INFORMATION, "Success", "Resume saved to:\n" + target.getAbsolutePath());
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Save Failed", "Failed to save resume:\n" + e.getMessage());
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
        List<Application> selected = getCheckedApplications();
        if (selected.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Warning", "No checked applications.");
            return;
        }
        int acceptedCount = 0;
        int blockedCount = 0;
        int unauthorizedCount = 0;
        for (Application app : selected) {
            if ("PENDING".equals(app.getStatus())) {
                if (isDeletedApplicant(app)) {
                    blockedCount++;
                    continue;
                }
                if (!canCurrentReviewerHandle(app)) {
                    unauthorizedCount++;
                    continue;
                }
                boolean ok = applicationService.updateApplicationStatus(app.getId(), "ACCEPTED");
                if (ok) {
                    app.setStatus("ACCEPTED");
                    acceptedCount++;
                } else {
                    blockedCount++;
                }
            }
        }
        if (acceptedCount > 0 || blockedCount > 0 || unauthorizedCount > 0) {
            loadApplications();
            applicationTable.refresh();
            showAlert(Alert.AlertType.INFORMATION, "Batch Result",
                    "Accepted: " + acceptedCount
                            + "\nBlocked (workload/deleted): " + blockedCount
                            + "\nNo permission: " + unauthorizedCount);
        }
    }

    @FXML
    public void handleBatchReject() {
        List<Application> selected = getCheckedApplications();
        if (selected.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Warning", "No checked applications.");
            return;
        }
        boolean updated = false;
        int unauthorizedCount = 0;
        int deletedCount = 0;
        for (Application app : selected) {
            if ("PENDING".equals(app.getStatus())) {
                if (isDeletedApplicant(app)) {
                    deletedCount++;
                    continue;
                }
                if (!canCurrentReviewerHandle(app)) {
                    unauthorizedCount++;
                    continue;
                }
                applicationService.updateApplicationStatus(app.getId(), "REJECTED");
                app.setStatus("REJECTED");
                updated = true;
            }
        }
        if (updated || unauthorizedCount > 0) {
            loadApplications();
            applicationTable.refresh();
            showAlert(Alert.AlertType.INFORMATION, "Batch Result",
                    "Batch reject completed.\nNo permission: " + unauthorizedCount + "\nDeleted account skipped: " + deletedCount);
        }
    }

    private void handleUpdateStatus(Application app, String newStatus) {
        if (isDeletedApplicant(app)) {
            showAlert(Alert.AlertType.WARNING, "Account Deleted", "Applications from deleted accounts cannot be reviewed.");
            return;
        }
        if (!canCurrentReviewerHandle(app)) {
            showAlert(Alert.AlertType.WARNING, "No Permission", "You can only operate applications for your own jobs.");
            return;
        }
        boolean success = applicationService.updateApplicationStatus(app.getId(), newStatus);
        if (success) {
            app.setStatus(newStatus);
            loadApplications();
            applicationTable.refresh();
            // Update drawer if it's currently selected
            if (app.equals(applicationTable.getSelectionModel().getSelectedItem())) {
                updateDetailDrawer(app);
            }
            showAlert(Alert.AlertType.INFORMATION, "Success", "Application " + newStatus.toLowerCase() + ".");
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to update status. Applicant may have reached workload limit.");
        }
    }

    private BooleanProperty getCheckedProperty(String applicationId) {
        return checkedMap.computeIfAbsent(applicationId, id -> new SimpleBooleanProperty(false));
    }

    private List<Application> getCheckedApplications() {
        return applicationTable.getItems().stream()
                .filter(app -> {
                    BooleanProperty property = checkedMap.get(app.getId());
                    return property != null && property.get();
                })
                .collect(Collectors.toList());
    }

    private boolean isAtCapacity(Application app) {
        return applicationService.getApplicantWorkload(app.getApplicantUsername())
                >= applicationService.getApplicantMaxWorkload(app.getApplicantUsername());
    }

    private String resolveJobTitle(String jobId) {
        Job job = jobMap.get(jobId);
        if (job != null && job.getTitle() != null && !job.getTitle().isBlank()) {
            return job.getTitle();
        }
        return "Unknown Job";
    }

    private String resolveApplicantWorkload(String username) {
        int current = applicationService.getApplicantWorkload(username);
        int max = applicationService.getApplicantMaxWorkload(username);
        return current + "/" + max;
    }

    private String resolveJobCapacity(String jobId) {
        Job job = jobMap.get(jobId);
        if (job == null) {
            return "-";
        }
        int accepted = jobService.getCurrentApplicantCount(jobId);
        return accepted + "/" + job.getMaxApplicants();
    }

    private List<Application> filterByRole(List<Application> applications, User currentUser, Map<String, Job> localJobMap) {
        if (currentUser == null) {
            return List.of();
        }

        if ("ADMIN".equals(currentUser.getRole())) {
            return applications;
        }

        if (!"TEACHER".equals(currentUser.getRole())) {
            return List.of();
        }

        String reviewer = currentUser.getUsername();
        return applications.stream()
                .filter(app -> {
                    Job job = localJobMap.get(app.getJobId());
                    return job != null && reviewer.equals(job.getPostedBy());
                })
                .collect(Collectors.toList());
    }

    private boolean canCurrentReviewerHandle(Application app) {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) {
            return false;
        }
        if ("ADMIN".equals(currentUser.getRole())) {
            return true;
        }
        if (!"TEACHER".equals(currentUser.getRole())) {
            return false;
        }
        Job job = jobMap.get(app.getJobId());
        return job != null && currentUser.getUsername().equals(job.getPostedBy());
    }

    private boolean isDeletedApplicant(Application app) {
        return !userService.userExists(app.getApplicantUsername());
    }

    private record LoadResult(ObservableList<Application> items, Map<String, Job> jobMap) {
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
