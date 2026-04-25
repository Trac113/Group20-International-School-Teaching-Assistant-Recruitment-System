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
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.GridPane;
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
            private final Button fullNameBtn = new Button("Edit Name");
            private final Button workloadBtn = new Button("Set Workload");
            private final HBox pane = new HBox(6, passwordBtn, fullNameBtn, workloadBtn);

            {
                fullNameBtn.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    TextInputDialog dialog = new TextInputDialog(user.getFullName());
                    dialog.setTitle("Edit Full Name");
                    dialog.setHeaderText("Edit full name for user: " + user.getUsername());
                    dialog.setContentText("Full Name:");
                    Optional<String> result = dialog.showAndWait();
                    if (result.isEmpty()) {
                        return;
                    }
                    String newName = result.get().trim();
                    if (newName.isBlank()) {
                        showAlert(Alert.AlertType.WARNING, "Warning", "Full name cannot be empty.");
                        return;
                    }
                    if (userService.updateFullName(user.getUsername(), newName)) {
                        showAlert(Alert.AlertType.INFORMATION, "Success", "Full name updated.");
                        loadUsersAsync();
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Error", "Failed to update full name.");
                    }
                });

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
            private final Button editBtn = new Button("Edit");

            {
                toggleBtn.setOnAction(event -> {
                    Job job = getTableView().getItems().get(getIndex());
                    String newStatus = "OPEN".equals(job.getStatus()) ? "CLOSED" : "OPEN";
                    jobService.updateJobStatus(job.getId(), newStatus);
                    job.setStatus(newStatus);
                    jobTable.refresh();
                    loadJobsAsync();
                });

                editBtn.setOnAction(event -> {
                    Job job = getTableView().getItems().get(getIndex());
                    handleEditJob(job);
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
                    HBox pane = new HBox(6, editBtn, toggleBtn);
                    setGraphic(pane);
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

    private void handleEditJob(Job job) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Job Detail");
        dialog.setHeaderText("Edit job: " + job.getTitle());
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextArea descArea = new TextArea(job.getDescription());
        descArea.setPrefRowCount(5);
        TextArea reqArea = new TextArea(job.getRequirements());
        reqArea.setPrefRowCount(5);

        grid.add(new Label("Description:"), 0, 0);
        grid.add(descArea, 1, 0);
        grid.add(new Label("Requirements:"), 0, 1);
        grid.add(reqArea, 1, 1);

        dialog.getDialogPane().setContent(grid);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }

        String newDesc = descArea.getText().trim();
        String newReq = reqArea.getText().trim();

        if (newDesc.isBlank() || newReq.isBlank()) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Description and requirements cannot be empty.");
            return;
        }

        if (jobService.updateJobDetails(job.getId(), newDesc, newReq)) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "Job details updated.");
            loadJobsAsync();
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to update job details.");
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
