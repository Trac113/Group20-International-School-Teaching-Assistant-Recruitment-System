package com.qq.recruitment.dao;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qq.recruitment.model.User;
import com.qq.recruitment.model.Job;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JsonFileDAO {
    private static final String USER_DATA_FILE = "src/main/resources/data/users.json";
    private static final String JOB_DATA_FILE = "src/main/resources/data/jobs.json";
    private final ObjectMapper objectMapper = new ObjectMapper();
    private List<User> users = new ArrayList<>();
    private List<Job> jobs = new ArrayList<>();

    public JsonFileDAO() {
        loadUsers();
        loadJobs();
    }

    private void loadUsers() {
        File file = new File(USER_DATA_FILE);
        if (file.exists()) {
            try {
                users = objectMapper.readValue(file, new TypeReference<List<User>>() {});
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadJobs() {
        File file = new File(JOB_DATA_FILE);
        if (file.exists()) {
            try {
                jobs = objectMapper.readValue(file, new TypeReference<List<Job>>() {});
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void saveUsers() {
        saveData(USER_DATA_FILE, users);
    }

    public void saveJobs() {
        saveData(JOB_DATA_FILE, jobs);
    }

    private void saveData(String filePath, Object data) {
        try {
            File file = new File(filePath);
            if (file.getParentFile() != null) {
                file.getParentFile().mkdirs();
            }
            objectMapper.writeValue(file, data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Optional<User> findUserByUsername(String username) {
        return users.stream()
                .filter(u -> u.getUsername().equals(username))
                .findFirst();
    }

    public void addUser(User user) {
        users.add(user);
        saveUsers();
    }

    public List<Job> getAllJobs() {
        return new ArrayList<>(jobs);
    }

    public void addJob(Job job) {
        jobs.add(job);
        saveJobs();
    }
}
