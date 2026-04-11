package com.qq.recruitment.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Job {
    private String id;
    private String title;
    private String category; // e.g., "Teaching", "Event"
    private String description;
    private String requirements;
    private List<String> requiredSkills;
    private String status; // "OPEN", "CLOSED"
    private String postedBy; // Username of the teacher/admin

    public Job() {
        this.id = UUID.randomUUID().toString();
        this.status = "OPEN";
        this.requiredSkills = new ArrayList<>();
    }

    public Job(String title, String category, String description, String requirements, List<String> requiredSkills, String postedBy) {
        this();
        this.title = title;
        this.category = category;
        this.description = description;
        this.requirements = requirements;
        this.requiredSkills = requiredSkills != null ? requiredSkills : new ArrayList<>();
        this.postedBy = postedBy;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRequirements() {
        return requirements;
    }

    public void setRequirements(String requirements) {
        this.requirements = requirements;
    }

    public List<String> getRequiredSkills() {
        return requiredSkills;
    }

    public void setRequiredSkills(List<String> requiredSkills) {
        this.requiredSkills = requiredSkills;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPostedBy() {
        return postedBy;
    }

    public void setPostedBy(String postedBy) {
        this.postedBy = postedBy;
    }
}
