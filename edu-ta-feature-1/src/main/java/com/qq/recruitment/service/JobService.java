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

    public void createJob(String title, String description, String requirements, String postedBy) {
        Job job = new Job(title, description, requirements, postedBy);
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
}
