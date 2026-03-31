package com.qq.recruitment.model;

import java.util.UUID;

public class Job {
    private String id;
    private String title;
    private String description;
    private String requirements;
    private String status; // "OPEN", "CLOSED"
    private String postedBy; // Username of the teacher/admin

    public Job() {
        this.id = UUID.randomUUID().toString();
        this.status = "OPEN";
    }

    public Job(String title, String description, String requirements, String postedBy) {
        this();
        this.title = title;
        this.description = description;
        this.requirements = requirements;
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
