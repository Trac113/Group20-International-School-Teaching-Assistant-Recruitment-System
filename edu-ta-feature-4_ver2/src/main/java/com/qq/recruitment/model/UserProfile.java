package com.qq.recruitment.model;

import java.util.List;
import java.util.ArrayList;

public class UserProfile {
    private String username;
    private String major;
    private String studentId;
    private List<String> skills;
    private String bio;
    private int workload; // Added for tracking the number of accepted jobs

    public UserProfile() {
        this.skills = new ArrayList<>();
        this.workload = 0;
    }

    public UserProfile(String username, String major, String studentId, List<String> skills, String bio) {
        this.username = username;
        this.major = major;
        this.studentId = studentId;
        this.skills = skills != null ? skills : new ArrayList<>();
        this.bio = bio;
        this.workload = 0;
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
}
