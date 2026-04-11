package com.qq.recruitment.controller;

import com.qq.recruitment.model.Application;
import com.qq.recruitment.model.Job;
import com.qq.recruitment.model.User;
import com.qq.recruitment.service.ApplicationService;
import com.qq.recruitment.service.JobService;
import com.qq.recruitment.util.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.List;

public class MyApplicationsController {

    @FXML
    private TableView<Application> appTable;
    @FXML
    private TableColumn<Application, String> jobIdColumn;
    @FXML
    private TableColumn<Application, String> jobTitleColumn;
    @FXML
    private TableColumn<Application, String> statusColumn;
    @FXML
    private TableColumn<Application, String> appliedAtColumn;

    private final ApplicationService applicationService = new ApplicationService();
    private final JobService jobService = new JobService();

    @FXML
    public void initialize() {
        jobIdColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getJobId()));
        
        jobTitleColumn.setCellValueFactory(data -> {
            String jId = data.getValue().getJobId();
            String title = jobService.getAllJobs().stream()
                    .filter(j -> j.getId().equals(jId))
                    .map(Job::getTitle)
                    .findFirst()
                    .orElse("Unknown Job");
            return new SimpleStringProperty(title);
        });

        statusColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));
        appliedAtColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getAppliedAt()));

        loadMyApplications();
    }

    private void loadMyApplications() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) return;

        List<Application> myApps = applicationService.getApplicationsByApplicant(currentUser.getUsername());
        ObservableList<Application> apps = FXCollections.observableArrayList(myApps);
        appTable.setItems(apps);
    }
}
