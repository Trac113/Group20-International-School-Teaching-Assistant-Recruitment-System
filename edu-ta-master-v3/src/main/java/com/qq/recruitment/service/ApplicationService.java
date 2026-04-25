package com.qq.recruitment.service;

import com.qq.recruitment.dao.JsonFileDAO;
import com.qq.recruitment.model.Application;
import com.qq.recruitment.model.UserProfile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ApplicationService {
    private static final String RESUME_DIR = "src/main/resources/data/resumes/";
    private static final int MAX_WORKLOAD = 3;

    public ApplicationService() {
        // Ensure resume directory exists
        File dir = new File(RESUME_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    public boolean apply(String jobId, String username, File sourceResumeFile) {
        // Check if already applied
        JsonFileDAO freshDao = new JsonFileDAO();
        boolean alreadyApplied = freshDao.getAllApplications().stream()
                .anyMatch(app -> app.getJobId().equals(jobId)
                        && app.getApplicantUsername().equals(username)
                        && !"WITHDRAWN".equals(app.getStatus())
                        && !"REJECTED".equals(app.getStatus()));

        if (alreadyApplied) {
            return false;
        }

        if (isApplicantWorkloadFull(username)) {
            return false;
        }

        if (new JobService().isJobAtCapacity(jobId)) {
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
        freshDao.addApplication(application);

        runAiAnalysisAsync(application.getId());
        
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
        return new JsonFileDAO().getAllApplications().stream()
                .filter(app -> app.getApplicantUsername().equals(username))
                .collect(Collectors.toList());
    }

    public List<Application> getApplicationsByJob(String jobId) {
        return new JsonFileDAO().getAllApplications().stream()
                .filter(app -> app.getJobId().equals(jobId))
                .collect(Collectors.toList());
    }

    public List<Application> getAllApplications() {
        return new JsonFileDAO().getAllApplications();
    }

    public boolean updateApplicationStatus(String applicationId, String newStatus) {
        synchronized (JsonFileDAO.class) {
            JsonFileDAO freshDao = new JsonFileDAO();
            List<Application> allApps = freshDao.getAllApplications();
            for (Application app : allApps) {
                if (app.getId().equals(applicationId)) {
                    String oldStatus = app.getStatus();

                    if ("ACCEPTED".equals(newStatus) && !"ACCEPTED".equals(oldStatus)) {
                        int currentWorkload = getApplicantWorkload(app.getApplicantUsername());
                    int maxWorkload = getApplicantMaxWorkload(app.getApplicantUsername());
                    if (currentWorkload >= maxWorkload) {
                        return false;
                    }
                }

                    app.setStatus(newStatus);
                    freshDao.saveApplications();

                    // Update workload if status changed to ACCEPTED
                    if ("ACCEPTED".equals(newStatus) && !"ACCEPTED".equals(oldStatus)) {
                        ProfileService profileService = new ProfileService();
                        UserProfile profile = ensureProfile(profileService, app.getApplicantUsername());
                        profile.setWorkload(profile.getWorkload() + 1);
                        profileService.updateProfile(profile);
                    } else if (!"ACCEPTED".equals(newStatus) && "ACCEPTED".equals(oldStatus)) {
                        // Revert workload if changed from ACCEPTED to something else
                        ProfileService profileService = new ProfileService();
                        UserProfile profile = profileService.getProfile(app.getApplicantUsername());
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

    public boolean hasApplied(String jobId, String username) {
        return new JsonFileDAO().getAllApplications().stream()
                .anyMatch(app -> app.getJobId().equals(jobId)
                        && app.getApplicantUsername().equals(username)
                        && !"WITHDRAWN".equals(app.getStatus())
                        && !"REJECTED".equals(app.getStatus()));
    }

    public boolean withdrawApplication(String applicationId, String username) {
        synchronized (JsonFileDAO.class) {
            JsonFileDAO freshDao = new JsonFileDAO();
            Optional<Application> target = freshDao.getAllApplications().stream()
                    .filter(app -> app.getId().equals(applicationId) && app.getApplicantUsername().equals(username))
                    .findFirst();
            if (target.isEmpty()) {
                return false;
            }

            Application app = target.get();
            if (!"PENDING".equals(app.getStatus())) {
                return false;
            }

            app.setStatus("WITHDRAWN");
            freshDao.saveApplications();
            return true;
        }
    }

    public boolean updateResume(String applicationId, String username, File sourceResumeFile) {
        if (sourceResumeFile == null || !sourceResumeFile.exists()) {
            return false;
        }

        synchronized (JsonFileDAO.class) {
            JsonFileDAO freshDao = new JsonFileDAO();
            Optional<Application> target = freshDao.getAllApplications().stream()
                    .filter(app -> app.getId().equals(applicationId) && app.getApplicantUsername().equals(username))
                    .findFirst();
            if (target.isEmpty()) {
                return false;
            }

            Application app = target.get();
            if (!"PENDING".equals(app.getStatus())) {
                return false;
            }

            try {
                String ext = getFileExtension(sourceResumeFile.getName());
                String newFileName = username + "_" + app.getJobId() + "_" + System.currentTimeMillis() + ext;
                Path targetPath = Paths.get(RESUME_DIR + newFileName);
                Files.copy(sourceResumeFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
                app.setResumePath(targetPath.toString());
                freshDao.saveApplications();

                // Refresh AI score/analysis after resume update.
                runAiAnalysisAsync(app.getId());
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    public int getApplicantWorkload(String username) {
        UserProfile profile = new ProfileService().getProfile(username);
        if (profile == null || profile.getUsername() == null || profile.getUsername().isBlank()) {
            return 0;
        }
        return profile.getWorkload();
    }

    public int getMaxWorkload() {
        return MAX_WORKLOAD;
    }

    public int getApplicantMaxWorkload(String username) {
        UserProfile profile = new ProfileService().getProfile(username);
        if (profile == null || profile.getUsername() == null || profile.getUsername().isBlank()) {
            return MAX_WORKLOAD;
        }
        int max = profile.getMaxWorkload();
        return max >= 0 ? max : MAX_WORKLOAD;
    }

    public boolean isApplicantWorkloadFull(String username) {
        return getApplicantWorkload(username) >= getApplicantMaxWorkload(username);
    }

    private UserProfile ensureProfile(ProfileService profileService, String username) {
        UserProfile profile = profileService.getProfile(username);
        if (profile == null || profile.getUsername() == null || profile.getUsername().isBlank()) {
            profile = new UserProfile();
            profile.setUsername(username);
        }
        return profile;
    }

    private void runAiAnalysisAsync(String applicationId) {
        Thread thread = new Thread(() -> {
            try {
                new AIService().analyzeApplication(applicationId);
            } catch (Exception ignored) {
                // Ignore background AI failures to avoid blocking primary application flow.
            }
        }, "ai-analyze-" + applicationId);
        thread.setDaemon(true);
        thread.start();
    }
}
