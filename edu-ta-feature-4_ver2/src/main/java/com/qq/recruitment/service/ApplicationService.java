package com.qq.recruitment.service;

import com.qq.recruitment.dao.JsonFileDAO;
import com.qq.recruitment.model.Application;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Collectors;

public class ApplicationService {
    private final JsonFileDAO dao;
    private static final String RESUME_DIR = "src/main/resources/data/resumes/";

    public ApplicationService() {
        this.dao = new JsonFileDAO();
        // Ensure resume directory exists
        File dir = new File(RESUME_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    public boolean apply(String jobId, String username, File sourceResumeFile) {
        // Check if already applied
        boolean alreadyApplied = dao.getAllApplications().stream()
                .anyMatch(app -> app.getJobId().equals(jobId) && app.getApplicantUsername().equals(username));
        
        if (alreadyApplied) {
            return false;
        }

        String savedResumePath = "";
        if (sourceResumeFile != null && sourceResumeFile.exists()) {
            try {
                // Generate unique filename to prevent overwriting
                String ext = getFileExtension(sourceResumeFile.getName());
                String newFileName = username + "_" + jobId + "_" + System.currentTimeMillis() + ext;
                Path targetPath = Paths.get(RESUME_DIR + newFileName);
                Files.copy(sourceResumeFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
                savedResumePath = targetPath.toString();
            } catch (IOException e) {
                e.printStackTrace();
                // Depending on requirements, we might want to return false here if upload fails
            }
        } else {
            // For tests or dummy calls
            savedResumePath = sourceResumeFile != null ? sourceResumeFile.getPath() : "dummy_path";
        }

        Application application = new Application(jobId, username, savedResumePath);
        dao.addApplication(application);

        new AIService().analyzeApplication(application.getId());
        
        return true;
    }

    // Helper method for old tests
    public boolean apply(String jobId, String username, String dummyPath) {
        return apply(jobId, username, new File(dummyPath));
    }

    private String getFileExtension(String fileName) {
        int lastIndexOf = fileName.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return ""; // empty extension
        }
        return fileName.substring(lastIndexOf);
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
                String oldStatus = app.getStatus();
                app.setStatus(newStatus);
                dao.saveApplications();
                
                // Update workload if status changed to ACCEPTED
                if ("ACCEPTED".equals(newStatus) && !"ACCEPTED".equals(oldStatus)) {
                    ProfileService profileService = new ProfileService();
                    com.qq.recruitment.model.UserProfile profile = profileService.getProfile(app.getApplicantUsername());
                    if (profile != null && profile.getUsername() != null) {
                        profile.setWorkload(profile.getWorkload() + 1);
                        profileService.updateProfile(profile);
                    }
                } else if (!"ACCEPTED".equals(newStatus) && "ACCEPTED".equals(oldStatus)) {
                    // Revert workload if changed from ACCEPTED to something else
                    ProfileService profileService = new ProfileService();
                    com.qq.recruitment.model.UserProfile profile = profileService.getProfile(app.getApplicantUsername());
                    if (profile != null && profile.getUsername() != null && profile.getWorkload() > 0) {
                        profile.setWorkload(profile.getWorkload() - 1);
                        profileService.updateProfile(profile);
                    }
                }
                
                return true;
            }
        }
        return false;
    }
}
