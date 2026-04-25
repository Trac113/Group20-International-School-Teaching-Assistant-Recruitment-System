package com.qq.recruitment.model;

import java.util.List;
import java.util.ArrayList;

public class UserProfile {
    public static final int DEFAULT_MAX_WORKLOAD = 3;
    public static final int MAX_ALLOWED_MAX_WORKLOAD = 5;

    private String username;
    private String major;
    private String studentId;
    private List<String> skills;
    private String bio;
    private int workload; // Current accepted workload
    private int maxWorkload; // Per-applicant configurable cap

    public UserProfile() {
        this.skills = new ArrayList<>();
        this.workload = 0;
        this.maxWorkload = DEFAULT_MAX_WORKLOAD;
    }

    public UserProfile(String username, String major, String studentId, List<String> skills, String bio) {
        this.username = username;
        this.major = major;
        this.studentId = studentId;
        this.skills = skills != null ? skills : new ArrayList<>();
        this.bio = bio;
        this.workload = 0;
        this.maxWorkload = DEFAULT_MAX_WORKLOAD;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public List<String> getSkills() {
        return skills;
    }

    public void setSkills(List<String> skills) {
        this.skills = skills;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public int getWorkload() {
        return workload;
    }

    public void setWorkload(int workload) {
        this.workload = workload;
    }

    public int getMaxWorkload() {
        return maxWorkload;
    }

    public void setMaxWorkload(int maxWorkload) {
        if (maxWorkload < 0) {
            this.maxWorkload = 0;
            return;
        }
        this.maxWorkload = Math.min(maxWorkload, MAX_ALLOWED_MAX_WORKLOAD);
    }
}
