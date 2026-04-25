package com.qq.recruitment.controller;

import com.qq.recruitment.App;
import com.qq.recruitment.model.User;
import com.qq.recruitment.util.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class MainController {

    @FXML
    private Label userLabel;

    @FXML
    private VBox sidebar;

    @FXML
    private VBox contentArea;
    @FXML
    private Button logoutButton;

    @FXML
    public void initialize() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            userLabel.setText("Welcome, " + currentUser.getUsername());
            buildSidebar(currentUser.getRole());
        }
    }

    private void buildSidebar(String role) {
        sidebar.getChildren().clear();

        if ("APPLICANT".equals(role)) {
            addNavButton("My Profile", "profile");
            addNavButton("Available Jobs", "job_list");
            addNavButton("My Favorites", "my_favorites");
            addNavButton("My Applications", "my_applications");
        } else if ("TEACHER".equals(role)) {
            addNavButton("Post a Job", "job_post");
            addNavButton("Manage My Jobs", "my_jobs");
            addNavButton("Screening", "screening_list");
        } else if ("ADMIN".equals(role)) {
            addNavButton("Dashboard", "admin_dashboard");
            addNavButton("Manage Accounts", "admin_management");
        }
    }

    private void addNavButton(String text, String fxmlName) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setStyle("-fx-background-color: transparent; -fx-border-color: #dddddd; -fx-border-width: 0 0 1 0; -fx-padding: 10; -fx-alignment: center-left;");
        btn.setOnAction(e -> loadContent(fxmlName));
        sidebar.getChildren().add(btn);
    }

    private void loadContent(String fxml) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("/view/" + fxml + ".fxml"));
            Parent root = fxmlLoader.load();
            contentArea.getChildren().clear();
            contentArea.getChildren().add(root);
            // Make the loaded content grow to fill the area
            // javafx.scene.layout.VBox.setVgrow(root, javafx.scene.layout.Priority.ALWAYS);
        } catch (IOException e) {
            e.printStackTrace();
            contentArea.getChildren().clear();
            contentArea.getChildren().add(new Label("Error loading " + fxml));
        }
    }

    @FXML
    private void handleLogout() {
        SessionManager.getInstance().logout();
        try {
            App.setRoot("login");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
