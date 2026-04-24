package com.qq.recruitment.controller;

import com.qq.recruitment.service.AdminService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.concurrent.Task;

import java.util.Comparator;
import java.util.Map;

public class AdminController {

    @FXML
    private TableView<StatRow> statusTable;
    @FXML
    private TableColumn<StatRow, String> statusNameColumn;
    @FXML
    private TableColumn<StatRow, Integer> statusCountColumn;
    @FXML
    private TableView<StatRow> jobTable;
    @FXML
    private TableColumn<StatRow, String> jobNameColumn;
    @FXML
    private TableColumn<StatRow, Integer> jobCountColumn;
    @FXML
    private TableView<WorkloadRow> workloadTable;
    @FXML
    private TableColumn<WorkloadRow, String> workloadNameColumn;
    @FXML
    private TableColumn<WorkloadRow, Integer> workloadCountColumn;
    @FXML
    private TableColumn<WorkloadRow, String> workloadJobsColumn;
    @FXML
    private TableView<StatRow> hiredTable;
    @FXML
    private TableColumn<StatRow, String> hiredNameColumn;
    @FXML
    private TableColumn<StatRow, Integer> hiredCountColumn;

    private final AdminService adminService = new AdminService();

    @FXML
    public void initialize() {
        bindColumns();
        loadAllStatsAsync();
    }

    private void bindColumns() {
        statusNameColumn.setCellValueFactory(data -> data.getValue().nameProperty());
        statusCountColumn.setCellValueFactory(data -> data.getValue().valueProperty().asObject());

        jobNameColumn.setCellValueFactory(data -> data.getValue().nameProperty());
        jobCountColumn.setCellValueFactory(data -> data.getValue().valueProperty().asObject());

        workloadNameColumn.setCellValueFactory(data -> data.getValue().nameProperty());
        workloadCountColumn.setCellValueFactory(data -> data.getValue().valueProperty().asObject());
        workloadJobsColumn.setCellValueFactory(data -> data.getValue().jobsProperty());

        hiredNameColumn.setCellValueFactory(data -> data.getValue().nameProperty());
        hiredCountColumn.setCellValueFactory(data -> data.getValue().valueProperty().asObject());
    }

    private void loadAllStatsAsync() {
        Task<DashboardData> task = new Task<>() {
            @Override
            protected DashboardData call() {
                return new DashboardData(
                        adminService.getApplicationStatusStats(),
                        adminService.getJobApplicationCounts(),
                        adminService.getTAWorkloadStats(),
                        adminService.getApplicantAcceptedJobs(),
                        adminService.getJobAcceptedCounts()
                );
            }
        };

        task.setOnSucceeded(event -> {
            DashboardData result = task.getValue();
            Platform.runLater(() -> {
                statusTable.setItems(toRows(result.statusStats));
                jobTable.setItems(toRows(result.jobStats));
                workloadTable.setItems(toWorkloadRows(result.workloadStats, result.workloadJobs));
                hiredTable.setItems(toRows(result.hiredStats));
            });
        });
        task.setOnFailed(event -> Platform.runLater(() -> {
            statusTable.setItems(FXCollections.observableArrayList());
            jobTable.setItems(FXCollections.observableArrayList());
            workloadTable.setItems(FXCollections.observableArrayList());
            hiredTable.setItems(FXCollections.observableArrayList());
        }));

        Thread thread = new Thread(task, "admin-dashboard-loader");
        thread.setDaemon(true);
        thread.start();
    }

    private ObservableList<StatRow> toRows(Map<String, Integer> stats) {
        ObservableList<StatRow> rows = FXCollections.observableArrayList();
        for (Map.Entry<String, Integer> entry : stats.entrySet()) {
            rows.add(new StatRow(entry.getKey(), entry.getValue()));
        }
        rows.sort(Comparator.comparingInt(StatRow::getValue).reversed().thenComparing(StatRow::getName));
        return rows;
    }

    private ObservableList<WorkloadRow> toWorkloadRows(Map<String, Integer> workloadStats, Map<String, String> workloadJobs) {
        ObservableList<WorkloadRow> rows = FXCollections.observableArrayList();
        for (Map.Entry<String, Integer> entry : workloadStats.entrySet()) {
            String username = entry.getKey();
            rows.add(new WorkloadRow(username, entry.getValue(), workloadJobs.getOrDefault(username, "-")));
        }
        rows.sort(Comparator.comparingInt(WorkloadRow::getValue).reversed().thenComparing(WorkloadRow::getName));
        return rows;
    }

    private static class DashboardData {
        private final Map<String, Integer> statusStats;
        private final Map<String, Integer> jobStats;
        private final Map<String, Integer> workloadStats;
        private final Map<String, String> workloadJobs;
        private final Map<String, Integer> hiredStats;

        private DashboardData(Map<String, Integer> statusStats,
                              Map<String, Integer> jobStats,
                              Map<String, Integer> workloadStats,
                              Map<String, String> workloadJobs,
                              Map<String, Integer> hiredStats) {
            this.statusStats = statusStats == null ? Map.of() : statusStats;
            this.jobStats = jobStats == null ? Map.of() : jobStats;
            this.workloadStats = workloadStats == null ? Map.of() : workloadStats;
            this.workloadJobs = workloadJobs == null ? Map.of() : workloadJobs;
            this.hiredStats = hiredStats == null ? Map.of() : hiredStats;
        }
    }

    public static class StatRow {
        private final javafx.beans.property.SimpleStringProperty name;
        private final javafx.beans.property.SimpleIntegerProperty value;

        public StatRow(String name, int value) {
            this.name = new javafx.beans.property.SimpleStringProperty(name);
            this.value = new javafx.beans.property.SimpleIntegerProperty(value);
        }

        public String getName() {
            return name.get();
        }

        public int getValue() {
            return value.get();
        }

        public javafx.beans.property.SimpleStringProperty nameProperty() {
            return name;
        }

        public javafx.beans.property.SimpleIntegerProperty valueProperty() {
            return value;
        }
    }

    public static class WorkloadRow {
        private final javafx.beans.property.SimpleStringProperty name;
        private final javafx.beans.property.SimpleIntegerProperty value;
        private final javafx.beans.property.SimpleStringProperty jobs;

        public WorkloadRow(String name, int value, String jobs) {
            this.name = new javafx.beans.property.SimpleStringProperty(name);
            this.value = new javafx.beans.property.SimpleIntegerProperty(value);
            this.jobs = new javafx.beans.property.SimpleStringProperty(jobs);
        }

        public String getName() {
            return name.get();
        }

        public int getValue() {
            return value.get();
        }

        public javafx.beans.property.SimpleStringProperty nameProperty() {
            return name;
        }

        public javafx.beans.property.SimpleIntegerProperty valueProperty() {
            return value;
        }

        public javafx.beans.property.SimpleStringProperty jobsProperty() {
            return jobs;
        }
    }
}
