package com.qq.recruitment.controller;

import com.qq.recruitment.model.Job;
import com.qq.recruitment.model.User;
import com.qq.recruitment.service.JobService;
import com.qq.recruitment.util.SessionManager;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class MyJobsController {

    @FXML
    private TableView<Job> jobTable;
    @FXML
    private TableColumn<Job, String> jobIdColumn;
    @FXML
    private TableColumn<Job, String> titleColumn;
    @FXML
    private TableColumn<Job, String> categoryColumn;
    @FXML
    private TableColumn<Job, String> statusColumn;
    @FXML
    private TableColumn<Job, Void> actionColumn;

    private final JobService jobService = new JobService();

    @FXML
    public void initialize() {
        jobIdColumn.setCellValueFactory(data -> new SimpleStringProperty(JobService.toDisplayJobId(data.getValue().getId())));
        titleColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTitle()));
        categoryColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCategory()));
        statusColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));

        actionColumn.setCellFactory(param -> new TableCell<>() {
            private final Button toggleBtn = new Button();
            private final Button editBtn = new Button("Edit");

            {
                toggleBtn.setOnAction(event -> {
                    Job job = getTableView().getItems().get(getIndex());
                    String newStatus = "OPEN".equals(job.getStatus()) ? "CLOSED" : "OPEN";
                    jobService.updateJobStatus(job.getId(), newStatus);
                    loadMyJobs();
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
                        toggleBtn.setText("Close");
                        toggleBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
                    } else {
                        toggleBtn.setText("Open");
                        toggleBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
                    }
                    HBox pane = new HBox(6, editBtn, toggleBtn);
                    setGraphic(pane);
                }
            }
        });

        loadMyJobs();
    }

    private void handleEditJob(Job job) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Job Details");
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
            loadMyJobs();
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to update job details.");
        }
    }

    private void loadMyJobs() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) return;

        Task<List<Job>> task = new Task<>() {
            @Override
            protected List<Job> call() {
                return jobService.getAllJobs().stream()
                        .filter(job -> currentUser.getUsername().equals(job.getPostedBy()))
                        .collect(Collectors.toList());
            }
        };
        task.setOnSucceeded(event -> Platform.runLater(
                () -> jobTable.setItems(FXCollections.observableArrayList(task.getValue()))
        ));
        task.setOnFailed(event -> Platform.runLater(
                () -> jobTable.setItems(FXCollections.observableArrayList())
        ));
        Thread thread = new Thread(task, "my-jobs-loader");
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
