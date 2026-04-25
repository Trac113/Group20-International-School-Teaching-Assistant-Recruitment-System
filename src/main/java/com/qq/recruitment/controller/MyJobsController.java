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
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;

import java.util.List;
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

            {
                toggleBtn.setOnAction(event -> {
                    Job job = getTableView().getItems().get(getIndex());
                    String newStatus = "OPEN".equals(job.getStatus()) ? "CLOSED" : "OPEN";
                    jobService.updateJobStatus(job.getId(), newStatus);
                    loadMyJobs(); // refresh
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
                    setGraphic(toggleBtn);
                }
            }
        });

        loadMyJobs();
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
}
