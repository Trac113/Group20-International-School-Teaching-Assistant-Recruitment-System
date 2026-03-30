package com.qq.recruitment.service;

import com.qq.recruitment.dao.JsonFileDAO;
import com.qq.recruitment.model.Application;
import com.qq.recruitment.model.Job;
import com.qq.recruitment.model.User;

import java.util.Optional;

public class AIService {
    private final JsonFileDAO dao;
    // Mock API Key - in real app, load from config
    private static final String API_KEY = "sk-xxxxxxxxxxxxxxxx"; 

    public AIService() {
        this.dao = new JsonFileDAO();
    }

    /**
     * Analyzes an application against the job requirements using AI.
     * In a real implementation, this would call the Qwen API.
     * Here we simulate the AI response logic.
     */
    public void analyzeApplication(String applicationId) {
        Optional<Application> appOpt = dao.getAllApplications().stream()
                .filter(a -> a.getId().equals(applicationId))
                .findFirst();

        if (appOpt.isEmpty()) return;
        Application app = appOpt.get();

        Optional<Job> jobOpt = dao.getAllJobs().stream()
                .filter(j -> j.getId().equals(app.getJobId()))
                .findFirst();

        if (jobOpt.isEmpty()) return;
        Job job = jobOpt.get();
        
        // Mock AI Logic: Simple keyword matching simulation
        // In real Qwen API integration:
        // 1. Construct prompt: "Job Requirements: " + job.getRequirements() + "\nCandidate Profile: " + ...
        // 2. Call API
        // 3. Parse JSON response for score and analysis

        int score = calculateMockScore(job.getRequirements(), "Candidate Resume Content Placeholder");
        String analysis = generateMockAnalysis(score);

        // Update Application
        app.setMatchScore(score);
        app.setAiAnalysis(analysis);
        dao.saveApplications();
    }

    private int calculateMockScore(String requirements, String resumeContent) {
        // Simple simulation: Random score for demo purposes
        // Or strictly based on length/complexity of requirements
        return 60 + (int)(Math.random() * 35); // Random score between 60 and 95
    }

    private String generateMockAnalysis(int score) {
        if (score >= 85) {
            return "Strong Match: Candidate skills align well with requirements.";
        } else if (score >= 70) {
            return "Good Match: Meets most core requirements, some gaps.";
        } else {
            return "Potential Match: Needs further review on specific skills.";
        }
    }
}
