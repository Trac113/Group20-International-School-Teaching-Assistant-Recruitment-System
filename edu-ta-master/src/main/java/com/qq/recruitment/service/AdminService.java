package com.qq.recruitment.service;

import com.qq.recruitment.dao.JsonFileDAO;
import com.qq.recruitment.model.Application;
import com.qq.recruitment.model.Job;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminService {
    private final JsonFileDAO dao;

    public AdminService() {
        this.dao = new JsonFileDAO();
    }

    public Map<String, Integer> getApplicationStatusStats() {
        List<Application> apps = dao.getAllApplications();
        Map<String, Integer> stats = new HashMap<>();
        
        for (Application app : apps) {
            String status = app.getStatus();
            stats.put(status, stats.getOrDefault(status, 0) + 1);
        }
        return stats;
    }

    public Map<String, Integer> getJobApplicationCounts() {
        List<Application> apps = dao.getAllApplications();
        List<Job> jobs = dao.getAllJobs();
        Map<String, Integer> counts = new HashMap<>();

        // Initialize with all jobs to show 0 if no applications
        for (Job job : jobs) {
            counts.put(job.getTitle(), 0);
        }

        for (Application app : apps) {
            // Find job title for this application
            String jobTitle = jobs.stream()
                    .filter(j -> j.getId().equals(app.getJobId()))
                    .map(Job::getTitle)
                    .findFirst()
                    .orElse("Unknown Job");
            
            // Handle case where multiple jobs might have same title or just to be safe with map keys
            if (counts.containsKey(jobTitle)) {
                 counts.put(jobTitle, counts.get(jobTitle) + 1);
            } else {
                 counts.put(jobTitle, 1);
            }
        }
        return counts;
    }

    public Map<String, Integer> getTAWorkloadStats() {
        List<com.qq.recruitment.model.UserProfile> profiles = dao.getAllProfiles();
        Map<String, Integer> workloadStats = new HashMap<>();

        for (com.qq.recruitment.model.UserProfile profile : profiles) {
            if (profile.getWorkload() > 0) {
                workloadStats.put(profile.getUsername(), profile.getWorkload());
            }
        }
        return workloadStats;
    }
}
