package com.qq.recruitment.controller;

import com.qq.recruitment.model.Job;
import com.qq.recruitment.model.User;
import com.qq.recruitment.service.ApplicationService;
import com.qq.recruitment.service.JobService;
import com.qq.recruitment.service.ProfileService;
import com.qq.recruitment.service.UserService;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.HBox;

import java.util.List;
import java.util.Optional;

public class AdminManagementController {
    private static final int MAX_ADMIN_SETTABLE_WORKLOAD = 5;

    @FXML
    private TableView<User> userTable;
    @FXML
    private TableColumn<User, String> usernameColumn;
    @FXML
    private TableColumn<User, String> fullNameColumn;
    @FXML
    private TableColumn<User, String> roleColumn;
    @FXML
    private TableColumn<User, Void> userActionColumn;

    @FXML
    private TableView<Job> jobTable;
    @FXML
    private TableColumn<Job, String> jobIdColumn;
    @FXML
    private TableColumn<Job, String> jobTitleColumn;
    @FXML
    private TableColumn<Job, String> jobPostedByColumn;
    @FXML
    private TableColumn<Job, String> jobStatusColumn;
    @FXML
    private TableColumn<Job, Void> jobActionColumn;

    private final UserService userService = new UserService();
    private final JobService jobService = new JobService();
    private final ProfileService profileService = new ProfileService();
    private final ApplicationService applicationService = new ApplicationService();

    @FXML
    public void initialize() {
        bindUserColumns();
        bindJobColumns();
        loadUsersAsync();
        loadJobsAsync();
    }

    private void bindUserColumns() {
        usernameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getUsername()));
        fullNameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getFullName()));
        roleColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRole()));

        userActionColumn.setCellFactory(param -> new TableCell<>() {
            private final Button passwordBtn = new Button("Reset Password");
            private final Button deleteBtn = new Button("Delete");
            private final Button workloadBtn = new Button("Set Workload");
            private final HBox pane = new HBox(6, passwordBtn, workloadBtn, deleteBtn);

            {
                passwordBtn.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    TextInputDialog dialog = new TextInputDialog();
                    dialog.setTitle("Reset Password");
                    dialog.setHeaderText("Set new password for user: " + user.getUsername());
                    dialog.setContentText("New Password:");
                    Optional<String> result = dialog.showAndWait();
                    if (result.isEmpty()) {
                        return;
                    }

                    String newPassword = result.get().trim();
                    if (newPassword.isBlank()) {
                        showAlert(Alert.AlertType.WARNING, "Warning", "Password cannot be empty.");
                        return;
                    }

                    if (userService.updateUserPassword(user.getUsername(), newPassword)) {
                        showAlert(Alert.AlertType.INFORMATION, "Success", "Password updated.");
                        loadUsersAsync();
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Error", "Failed to update password.");
                    }
                });

                deleteBtn.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    if ("ADMIN".equals(user.getRole())) {
                        showAlert(Alert.AlertType.WARNING, "Warning", "ADMIN accounts cannot be deleted.");
                        return;
                    }
                    if (userService.deleteUser(user.getUsername())) {
                        showAlert(Alert.AlertType.INFORMATION, "Success", "User deleted.");
                        loadUsersAsync();
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete user.");
                    }
                });

                workloadBtn.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    if (!"APPLICANT".equals(user.getRole())) {
                        showAlert(Alert.AlertType.WARNING, "Warning", "Workload can only be set for APPLICANT users.");
                        return;
                    }

                    int current = applicationService.getApplicantMaxWorkload(user.getUsername());
                    TextInputDialog dialog = new TextInputDialog(String.valueOf(current));
                    dialog.setTitle("Set Max Workload");
                    dialog.setHeaderText("Set max workload for: " + user.getUsername());
                    dialog.setContentText("Max Workload (0-" + MAX_ADMIN_SETTABLE_WORKLOAD + "):");
                    Optional<String> result = dialog.showAndWait();
                    if (result.isEmpty()) {
                        return;
                    }
                    String value = result.get().trim();
                    int target;
                    try {
                        target = Integer.parseInt(value);
                    } catch (NumberFormatException ex) {
                        showAlert(Alert.AlertType.WARNING, "Warning", "Workload must be an integer.");
                        return;
                    }
                    if (target < 0) {
                        showAlert(Alert.AlertType.WARNING, "Warning", "Workload cannot be negative.");
                        return;
                    }
                    if (target > MAX_ADMIN_SETTABLE_WORKLOAD) {
                        showAlert(Alert.AlertType.WARNING, "Warning", "Max workload cannot exceed " + MAX_ADMIN_SETTABLE_WORKLOAD + ".");
                        return;
                    }
                    if (profileService.updateWorkload(user.getUsername(), target)) {
                        showAlert(Alert.AlertType.INFORMATION, "Success", "Max workload updated.");
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Error", "Failed to update workload.");
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(pane);
                }
            }
        });
    }

    private void bindJobColumns() {
        jobIdColumn.setCellValueFactory(data -> new SimpleStringProperty(JobService.toDisplayJobId(data.getValue().getId())));
        jobTitleColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTitle()));
        jobPostedByColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPostedBy()));
        jobStatusColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));

        jobActionColumn.setCellFactory(param -> new TableCell<>() {
            private final Button toggleBtn = new Button();

            {
                toggleBtn.setOnAction(event -> {
                    Job job = getTableView().getItems().get(getIndex());
                    String newStatus = "OPEN".equals(job.getStatus()) ? "CLOSED" : "OPEN";
                    jobService.updateJobStatus(job.getId(), newStatus);
                    loadJobsAsync();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Job job = getTableView().getItems().get(getIndex());
                    if ("OPEN".equals(job.getStatus())) {
                        toggleBtn.setText("Take Down");
                    } else {
                        toggleBtn.setText("Reopen");
                    }
                    setGraphic(toggleBtn);
                }
            }
        });
    }

    private void loadUsersAsync() {
        Task<List<User>> task = new Task<>() {
            @Override
            protected List<User> call() {
                return userService.getAllUsers();
            }
        };
        task.setOnSucceeded(event -> Platform.runLater(
                () -> userTable.setItems(FXCollections.observableArrayList(task.getValue()))
        ));
        task.setOnFailed(event -> Platform.runLater(
                () -> userTable.setItems(FXCollections.observableArrayList())
        ));
        Thread thread = new Thread(task, "admin-user-loader");
        thread.setDaemon(true);
        thread.start();
    }

    private void loadJobsAsync() {
        Task<List<Job>> task = new Task<>() {
            @Override
            protected List<Job> call() {
                return jobService.getAllJobs();
            }
        };
        task.setOnSucceeded(event -> Platform.runLater(
                () -> jobTable.setItems(FXCollections.observableArrayList(task.getValue()))
        ));
        task.setOnFailed(event -> Platform.runLater(
                () -> jobTable.setItems(FXCollections.observableArrayList())
        ));
        Thread thread = new Thread(task, "admin-job-loader");
        thread.setDaemon(true);
        thread.start();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
