package com.qq.recruitment.service;

import com.qq.recruitment.dao.JsonFileDAO;
import com.qq.recruitment.model.Application;
import com.qq.recruitment.model.Job;
import com.qq.recruitment.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class AdminService {
    private final JsonFileDAO dao;

    public AdminService() {
        this.dao = new JsonFileDAO();
    }

    public Map<String, Integer> getApplicationStatusStats() {
        List<Application> apps = dao.getAllApplications();
        Set<String> activeUsernames = getActiveUsernames();
        Map<String, Integer> stats = new HashMap<>();
        
        for (Application app : apps) {
            if (!activeUsernames.contains(app.getApplicantUsername())) {
                continue;
            }
            String status = app.getStatus();
            stats.put(status, stats.getOrDefault(status, 0) + 1);
        }
        return stats;
    }

    public Map<String, Integer> getJobApplicationCounts() {
        List<Application> apps = dao.getAllApplications();
        List<Job> jobs = dao.getAllJobs();
        Set<String> activeUsernames = getActiveUsernames();
        Map<String, Integer> counts = new HashMap<>();

        // Initialize with all jobs to show 0 if no applications
        for (Job job : jobs) {
            counts.put(jobDisplayKey(job), 0);
        }

        for (Application app : apps) {
            if (!activeUsernames.contains(app.getApplicantUsername())) {
                continue;
            }
            // Find job title for this application
            String jobKey = jobs.stream()
                    .filter(j -> j.getId().equals(app.getJobId()))
                    .map(this::jobDisplayKey)
                    .findFirst()
                    .orElse("Unknown Job");
            counts.put(jobKey, counts.getOrDefault(jobKey, 0) + 1);
        }
        return counts;
    }

    public Map<String, Integer> getJobAcceptedCounts() {
        List<Application> apps = dao.getAllApplications();
        List<Job> jobs = dao.getAllJobs();
        Set<String> activeUsernames = getActiveUsernames();
        Map<String, Integer> counts = new HashMap<>();

        for (Job job : jobs) {
            counts.put(jobDisplayKey(job), 0);
        }

        for (Application app : apps) {
            if (!activeUsernames.contains(app.getApplicantUsername())) {
                continue;
            }
            if (!"ACCEPTED".equals(app.getStatus())) {
                continue;
            }
            String jobKey = jobs.stream()
                    .filter(j -> j.getId().equals(app.getJobId()))
                    .map(this::jobDisplayKey)
                    .findFirst()
                    .orElse("Unknown Job");
            counts.put(jobKey, counts.getOrDefault(jobKey, 0) + 1);
        }

        return counts;
    }

    public Map<String, Integer> getTAWorkloadStats() {
        List<Application> apps = dao.getAllApplications();
        Set<String> activeUsernames = getActiveUsernames();
        Map<String, Integer> workloadStats = new HashMap<>();

        for (Application app : apps) {
            if (!activeUsernames.contains(app.getApplicantUsername())) {
                continue;
            }
            if (!"ACCEPTED".equals(app.getStatus())) {
                continue;
            }
            String username = app.getApplicantUsername();
            workloadStats.put(username, workloadStats.getOrDefault(username, 0) + 1);
        }
        return workloadStats;
    }

    public Map<String, String> getApplicantAcceptedJobs() {
        List<Application> apps = dao.getAllApplications();
        List<Job> jobs = dao.getAllJobs();
        Set<String> activeUsernames = getActiveUsernames();
        Map<String, String> jobsById = jobs.stream().collect(Collectors.toMap(
                Job::getId,
                this::jobDisplayKey,
                (a, b) -> a
        ));
        Map<String, LinkedHashSet<String>> acceptedJobs = new HashMap<>();

        for (Application app : apps) {
            if (!activeUsernames.contains(app.getApplicantUsername())) {
                continue;
            }
            if (!"ACCEPTED".equals(app.getStatus())) {
                continue;
            }
            String display = jobsById.getOrDefault(app.getJobId(), JobService.toDisplayJobId(app.getJobId()) + " | Unknown Job");
            acceptedJobs.computeIfAbsent(app.getApplicantUsername(), k -> new LinkedHashSet<>()).add(display);
        }

        Map<String, String> result = new HashMap<>();
        for (Map.Entry<String, LinkedHashSet<String>> entry : acceptedJobs.entrySet()) {
            result.put(entry.getKey(), String.join(", ", new ArrayList<>(entry.getValue())));
        }
        return result;
    }

    private Set<String> getActiveUsernames() {
        return dao.getAllUsers().stream()
                .map(User::getUsername)
                .filter(u -> u != null && !u.isBlank())
                .collect(Collectors.toCollection(HashSet::new));
    }

    private String jobDisplayKey(Job job) {
        return JobService.toDisplayJobId(job.getId()) + " | " + job.getTitle();
    }
}
