package com.qq.recruitment.controller;

import com.qq.recruitment.service.AdminService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;

import java.util.Map;

public class AdminController {

    @FXML
    private PieChart statusPieChart;

    @FXML
    private BarChart<String, Number> jobBarChart;

    @FXML
    private BarChart<String, Number> workloadBarChart;

    private final AdminService adminService = new AdminService();

    @FXML
    public void initialize() {
        loadStatusStats();
        loadJobStats();
        loadWorkloadStats();
    }

    private void loadStatusStats() {
        Map<String, Integer> stats = adminService.getApplicationStatusStats();
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();

        for (Map.Entry<String, Integer> entry : stats.entrySet()) {
            pieChartData.add(new PieChart.Data(entry.getKey(), entry.getValue()));
        }

        statusPieChart.setData(pieChartData);
        statusPieChart.setTitle("Application Status");
    }

    private void loadJobStats() {
        Map<String, Integer> stats = adminService.getJobApplicationCounts();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Applications");

        for (Map.Entry<String, Integer> entry : stats.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }

        jobBarChart.getData().add(series);
        jobBarChart.setTitle("Applications per Job");
    }

    private void loadWorkloadStats() {
        Map<String, Integer> stats = adminService.getTAWorkloadStats();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Workload");

        for (Map.Entry<String, Integer> entry : stats.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }

        workloadBarChart.getData().add(series);
        workloadBarChart.setTitle("TA Workload Distribution");
    }
}
