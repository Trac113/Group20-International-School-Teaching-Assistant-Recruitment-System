package com.qq.recruitment.controller;

import com.qq.recruitment.App;
import com.qq.recruitment.model.User;
import com.qq.recruitment.util.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableView;
import javafx.scene.layout.Region;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.io.IOException;

/**
 * Main application shell controller that builds the role-based sidebar navigation
 * and manages the central content area by dynamically loading FXML views.
 */
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
            addNavButton("My Account", "teacher_account");
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
        btn.getStyleClass().add("nav-btn");
        btn.setOnAction(e -> loadContent(fxmlName));
        sidebar.getChildren().add(btn);
    }

    private void loadContent(String fxml) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("/view/" + fxml + ".fxml"));
            Parent root = fxmlLoader.load();
            applyResponsiveBehavior(root);

            ScrollPane scroller = new ScrollPane(root);
            scroller.setFitToWidth(true);
            scroller.setFitToHeight(true);
            scroller.setPannable(true);
            scroller.getStyleClass().add("content-scroll");

            contentArea.getChildren().clear();
            contentArea.getChildren().add(scroller);
            VBox.setVgrow(scroller, Priority.ALWAYS);
        } catch (IOException e) {
            e.printStackTrace();
            contentArea.getChildren().clear();
            contentArea.getChildren().add(new Label("Error loading " + fxml));
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void applyResponsiveBehavior(Node node) {
        if (node instanceof Region region) {
            region.setMaxWidth(Double.MAX_VALUE);
            region.setMaxHeight(Double.MAX_VALUE);
        }
        if (node instanceof TableView tableView) {
            tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        }
        if (node instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                applyResponsiveBehavior(child);
            }
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
