package com.qq.recruitment.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class Application {
    private String id;
    private String jobId;
    private String applicantUsername;
    private String status; // "PENDING", "ACCEPTED", "REJECTED"
    private String resumePath;
    private String appliedAt;
    private int matchScore; // 0-100 score from AI
    private String aiAnalysis; // Brief analysis from AI

    public Application() {
        this.id = UUID.randomUUID().toString();
        this.status = "PENDING";
        this.appliedAt = LocalDateTime.now().toString();
        this.matchScore = 0;
        this.aiAnalysis = "";
    }

    public Application(String jobId, String applicantUsername, String resumePath) {
        this();
        this.jobId = jobId;
        this.applicantUsername = applicantUsername;
        this.resumePath = resumePath;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getApplicantUsername() {
        return applicantUsername;
    }

    public void setApplicantUsername(String applicantUsername) {
        this.applicantUsername = applicantUsername;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getResumePath() {
        return resumePath;
    }

    public void setResumePath(String resumePath) {
        this.resumePath = resumePath;
    }

    public String getAppliedAt() {
        return appliedAt;
    }

    public void setAppliedAt(String appliedAt) {
        this.appliedAt = appliedAt;
    }

    public int getMatchScore() {
        return matchScore;
    }

    public void setMatchScore(int matchScore) {
        this.matchScore = matchScore;
    }

    public String getAiAnalysis() {
        return aiAnalysis;
    }

    public void setAiAnalysis(String aiAnalysis) {
        this.aiAnalysis = aiAnalysis;
    }
}
