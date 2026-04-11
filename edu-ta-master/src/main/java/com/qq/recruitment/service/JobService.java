package com.qq.recruitment.service;

import com.qq.recruitment.dao.JsonFileDAO;
import com.qq.recruitment.model.Job;

import java.util.List;
import java.util.stream.Collectors;

public class JobService {
    private final JsonFileDAO jobDAO;

    public JobService() {
        this.jobDAO = new JsonFileDAO();
    }

    public void createJob(String title, String category, String description, String requirements, List<String> requiredSkills, String postedBy) {
        Job job = new Job(title, category, description, requirements, requiredSkills, postedBy);
        jobDAO.addJob(job);
    }

    public List<Job> getAllJobs() {
        return jobDAO.getAllJobs();
    }
    
    public List<Job> getOpenJobs() {
         return jobDAO.getAllJobs().stream()
                 .filter(job -> "OPEN".equals(job.getStatus()))
                 .collect(Collectors.toList());
    }

    public void updateJobStatus(String jobId, String status) {
        List<Job> allJobs = jobDAO.getAllJobs();
        for (Job job : allJobs) {
            if (job.getId().equals(jobId)) {
                job.setStatus(status);
                jobDAO.saveJobs();
                break;
            }
        }
    }

    public List<Job> searchOpenJobs(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getOpenJobs();
        }
        String lowerKeyword = keyword.toLowerCase();
        return getOpenJobs().stream()
                .filter(job -> (job.getTitle() != null && job.getTitle().toLowerCase().contains(lowerKeyword)) ||
                               (job.getPostedBy() != null && job.getPostedBy().toLowerCase().contains(lowerKeyword)))
                .collect(Collectors.toList());
    }
}
