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
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
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
    @FXML
    private TableColumn<Application, Void> actionColumn;

    private final ApplicationService applicationService = new ApplicationService();
    private final JobService jobService = new JobService();

    @FXML
    public void initialize() {
        jobIdColumn.setCellValueFactory(data -> new SimpleStringProperty(
                JobService.toDisplayJobId(data.getValue().getJobId())
        ));
        
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
        actionColumn.setCellFactory(param -> new TableCell<>() {
            private final Button withdrawBtn = new Button("Withdraw");
            private final Button updateResumeBtn = new Button("Update Resume");
            private final HBox pane = new HBox(6, withdrawBtn, updateResumeBtn);

            {
                withdrawBtn.setOnAction(event -> {
                    Application app = getTableView().getItems().get(getIndex());
                    handleWithdraw(app);
                });
                updateResumeBtn.setOnAction(event -> {
                    Application app = getTableView().getItems().get(getIndex());
                    handleUpdateResume(app);
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
                        setGraphic(new Label("-"));
                    }
                }
            }
        });

        loadMyApplications();
    }

    private void loadMyApplications() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) return;

        List<Application> myApps = applicationService.getApplicationsByApplicant(currentUser.getUsername());
        ObservableList<Application> apps = FXCollections.observableArrayList(myApps);
        appTable.setItems(apps);
    }

    private void handleWithdraw(Application app) {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Withdraw this application for job: " + JobService.toDisplayJobId(app.getJobId()) + "?",
                ButtonType.YES, ButtonType.NO);
        confirm.showAndWait();
        if (confirm.getResult() != ButtonType.YES) return;

        boolean ok = applicationService.withdrawApplication(app.getId(), currentUser.getUsername());
        if (ok) {
            loadMyApplications();
            showAlert(Alert.AlertType.INFORMATION, "Success", "Application withdrawn.");
        } else {
            showAlert(Alert.AlertType.WARNING, "Warning", "Only pending applications can be withdrawn.");
        }
    }

    private void handleUpdateResume(Application app) {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) return;

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select New Resume");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Document Files", "*.pdf", "*.docx", "*.doc")
        );
        Stage stage = (Stage) appTable.getScene().getWindow();
        File selected = fileChooser.showOpenDialog(stage);
        if (selected == null) return;

        boolean ok = applicationService.updateResume(app.getId(), currentUser.getUsername(), selected);
        if (ok) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "Resume updated. Application re-analyzed.");
            loadMyApplications();
        } else {
            showAlert(Alert.AlertType.WARNING, "Warning", "Resume can only be updated for pending applications.");
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
