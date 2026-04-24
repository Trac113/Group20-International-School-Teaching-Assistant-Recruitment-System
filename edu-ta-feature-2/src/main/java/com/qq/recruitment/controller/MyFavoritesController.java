package com.qq.recruitment.controller;

import com.qq.recruitment.model.Job;
import com.qq.recruitment.model.User;
import com.qq.recruitment.service.FavoriteService;
import com.qq.recruitment.service.JobService;
import com.qq.recruitment.util.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.List;

public class MyFavoritesController {

    @FXML
    private TableView<Job> favoriteTable;
    @FXML
    private TableColumn<Job, String> jobIdColumn;
    @FXML
    private TableColumn<Job, String> titleColumn;
    @FXML
    private TableColumn<Job, String> categoryColumn;
    @FXML
    private TableColumn<Job, String> statusColumn;
    @FXML
    private TableColumn<Job, String> postedByColumn;

    private final FavoriteService favoriteService = new FavoriteService();

    @FXML
    public void initialize() {
        jobIdColumn.setCellValueFactory(data -> new SimpleStringProperty(JobService.toDisplayJobId(data.getValue().getId())));
        titleColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTitle()));
        categoryColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCategory()));
        statusColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));
        postedByColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPostedBy()));
        loadFavorites();
    }

    private void loadFavorites() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) {
            favoriteTable.setItems(FXCollections.observableArrayList());
            return;
        }
        List<Job> favorites = favoriteService.getFavoriteJobs(currentUser.getUsername());
        favoriteTable.setItems(FXCollections.observableArrayList(favorites));
    }
}
