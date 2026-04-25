package com.qq.recruitment.model;

import java.util.ArrayList;
import java.util.List;

public class Favorite {
    private String username;
    private List<String> jobIds;

    public Favorite() {
        this.jobIds = new ArrayList<>();
    }

    public Favorite(String username, List<String> jobIds) {
        this.username = username;
        this.jobIds = jobIds != null ? jobIds : new ArrayList<>();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<String> getJobIds() {
        return jobIds;
    }

    public void setJobIds(List<String> jobIds) {
        this.jobIds = jobIds;
    }
}
