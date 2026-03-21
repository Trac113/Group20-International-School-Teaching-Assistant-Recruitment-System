package com.qq.recruitment.controller;

import com.qq.recruitment.model.Application;
import com.qq.recruitment.service.ApplicationService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

public class ScreeningController {

    @FXML
    private TableView<Application> applicationTable;
    @FXML
    private TableColumn<Application, String> jobIdColumn;
    @FXML
    private TableColumn<Application, String> applicantColumn;
    @FXML
    private TableColumn<Application, String> statusColumn;
    @FXML
    private TableColumn<Application, Void> actionColumn;

    private final ApplicationService applicationService = new ApplicationService();

    @FXML
    public void initialize() {
        // Setup columns
        jobIdColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getJobId()));
        applicantColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getApplicantUsername()));
        statusColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));

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
         // Re-fetch data from service
         // For now, let's just assume we are managing a specific job context
    }

    private void handleUpdateStatus(Application app, String newStatus) {
        boolean success = applicationService.updateApplicationStatus(app.getId(), newStatus);
        if (success) {
            app.setStatus(newStatus);
            applicationTable.refresh();
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
