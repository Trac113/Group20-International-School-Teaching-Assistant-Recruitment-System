package com.qq.recruitment.service;

import com.qq.recruitment.dao.JsonFileDAO;
import com.qq.recruitment.model.Application;

import java.util.List;
import java.util.stream.Collectors;

public class ApplicationService {
    private final JsonFileDAO dao;

    public ApplicationService() {
        this.dao = new JsonFileDAO();
    }

    public boolean apply(String jobId, String username, String resumePath) {
        // Check if already applied
        boolean alreadyApplied = dao.getAllApplications().stream()
                .anyMatch(app -> app.getJobId().equals(jobId) && app.getApplicantUsername().equals(username));
        
        if (alreadyApplied) {
            return false;
        }

        Application application = new Application(jobId, username, resumePath);
        dao.addApplication(application);
        return true;
    }

    public List<Application> getApplicationsByApplicant(String username) {
        return dao.getAllApplications().stream()
                .filter(app -> app.getApplicantUsername().equals(username))
                .collect(Collectors.toList());
    }

    public List<Application> getApplicationsByJob(String jobId) {
        return dao.getAllApplications().stream()
                .filter(app -> app.getJobId().equals(jobId))
                .collect(Collectors.toList());
    }

    public List<Application> getAllApplications() {
        return dao.getAllApplications();
    }

    public boolean updateApplicationStatus(String applicationId, String newStatus) {
        List<Application> allApps = dao.getAllApplications();
        for (Application app : allApps) {
            if (app.getId().equals(applicationId)) {
                app.setStatus(newStatus);
                dao.saveApplications();
                return true;
            }
        }
        return false;
    }
}
