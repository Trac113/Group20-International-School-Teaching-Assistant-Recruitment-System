package com.qq.recruitment.controller;

import com.qq.recruitment.model.Job;
import com.qq.recruitment.model.User;
import com.qq.recruitment.service.ApplicationService;
import com.qq.recruitment.service.FavoriteService;
import com.qq.recruitment.service.JobService;
import com.qq.recruitment.util.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class ApplicantController {

    @FXML
    private TableView<Job> jobTable;
    @FXML
    private TableColumn<Job, String> jobIdColumn;
    @FXML
    private TableColumn<Job, String> titleColumn;
    @FXML
    private TableColumn<Job, String> categoryColumn;
    @FXML
    private TableColumn<Job, String> skillsColumn;
    @FXML
    private TableColumn<Job, String> descriptionColumn;
    @FXML
    private TableColumn<Job, Void> favoriteColumn;
    @FXML
    private TableColumn<Job, Void> actionColumn;

    private final JobService jobService = new JobService();
    private final ApplicationService applicationService = new ApplicationService();
    private final FavoriteService favoriteService = new FavoriteService();

    @FXML
    public void initialize() {
        jobIdColumn.setCellValueFactory(data -> new SimpleStringProperty(JobService.toDisplayJobId(data.getValue().getId())));
        titleColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTitle()));
        categoryColumn.setCellValueFactory(data -> {
            Job job = data.getValue();
            int current = jobService.getCurrentApplicantCount(job.getId());
            int max = job.getMaxApplicants();
            return new SimpleStringProperty(job.getCategory() + " (" + current + "/" + max + ")");
        });
        skillsColumn.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getRequiredSkills() != null ? String.join(", ", data.getValue().getRequiredSkills()) : ""
        ));
        descriptionColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDescription()));

        favoriteColumn.setCellFactory(param -> new TableCell<>() {
            private final Button favBtn = new Button();

            {
                favBtn.setOnAction(event -> {
                    Job job = getTableView().getItems().get(getIndex());
                    User currentUser = SessionManager.getInstance().getCurrentUser();
                    if (currentUser == null) return;
                    favoriteService.toggleFavorite(currentUser.getUsername(), job.getId());
                    getTableView().refresh();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Job job = getTableView().getItems().get(getIndex());
                    User currentUser = SessionManager.getInstance().getCurrentUser();
                    boolean favorite = currentUser != null && favoriteService.isFavorite(currentUser.getUsername(), job.getId());
                    favBtn.setText(favorite ? "Unfav" : "Fav");
                    setGraphic(favBtn);
                }
            }
        });

        actionColumn.setCellFactory(param -> new TableCell<>() {
            private final Button applyBtn = new Button("Apply");
            private final Label appliedLabel = new Label("Applied");
            private final Label fullLabel = new Label("Full");

            {
                applyBtn.setOnAction(event -> {
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
                    Job job = getTableView().getItems().get(getIndex());
                    User currentUser = SessionManager.getInstance().getCurrentUser();
                    if (currentUser != null && applicationService.hasApplied(job.getId(), currentUser.getUsername())) {
                        setGraphic(appliedLabel);
                    } else if (jobService.isJobAtCapacity(job.getId())) {
                        setGraphic(fullLabel);
                    } else {
                        setGraphic(applyBtn);
                    }
                }
            }
        });

        loadJobs();
    }

    private void loadJobs() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        ObservableList<Job> jobs = FXCollections.observableArrayList(
                currentUser == null
                        ? jobService.getOpenJobs()
                        : jobService.getRecommendedOpenJobs(currentUser.getUsername())
        );
        jobTable.setItems(jobs);
    }

    private void handleApply(Job job) {
        ButtonType yesButton = new ButtonType("Yes", ButtonBar.ButtonData.YES);
        ButtonType noButton = new ButtonType("No", ButtonBar.ButtonData.NO);
        Alert alert = new Alert(
                Alert.AlertType.CONFIRMATION,
                "Apply for " + job.getTitle() + "?\nYou will need to select your resume (PDF/DOCX).",
                yesButton,
                noButton
        );
        alert.showAndWait();

        if (alert.getResult() == yesButton) {
            User currentUser = SessionManager.getInstance().getCurrentUser();
            if (currentUser == null) return;

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Resume");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Document Files", "*.pdf", "*.docx", "*.doc")
            );

            Stage stage = (Stage) jobTable.getScene().getWindow();
            File selectedFile = fileChooser.showOpenDialog(stage);

            if (selectedFile != null) {
                boolean success = applicationService.apply(job.getId(), currentUser.getUsername(), selectedFile);
                if (success) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Application submitted successfully!");
                    jobTable.refresh();
                } else {
                    if (jobService.isJobAtCapacity(job.getId())) {
                        showAlert(Alert.AlertType.WARNING, "Warning", "This job has reached its application capacity.");
                    } else {
                        showAlert(Alert.AlertType.WARNING, "Warning", "You have already applied for this job.");
                    }
                }
            } else {
                showAlert(Alert.AlertType.INFORMATION, "Cancelled", "Application cancelled. Resume is required.");
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
