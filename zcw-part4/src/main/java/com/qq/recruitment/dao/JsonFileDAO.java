package com.qq.recruitment.dao;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qq.recruitment.model.User;
import com.qq.recruitment.model.Job;
import com.qq.recruitment.model.Application;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JsonFileDAO {
    private static final String USER_DATA_FILE = "src/main/resources/data/users.json";
    private static final String JOB_DATA_FILE = "src/main/resources/data/jobs.json";
    private static final String APP_DATA_FILE = "src/main/resources/data/applications.json";
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    private List<User> users = new ArrayList<>();
    private List<Job> jobs = new ArrayList<>();
    private List<Application> applications = new ArrayList<>();

    public JsonFileDAO() {
        loadUsers();
        loadJobs();
        loadApplications();
    }

    private void loadUsers() {
        loadFile(USER_DATA_FILE, new TypeReference<List<User>>() {}, list -> users = list);
    }

    private void loadJobs() {
        loadFile(JOB_DATA_FILE, new TypeReference<List<Job>>() {}, list -> jobs = list);
    }

    private void loadApplications() {
        loadFile(APP_DATA_FILE, new TypeReference<List<Application>>() {}, list -> applications = list);
    }

    private <T> void loadFile(String filePath, TypeReference<List<T>> typeRef, java.util.function.Consumer<List<T>> consumer) {
        File file = new File(filePath);
        if (file.exists()) {
            try {
                List<T> data = objectMapper.readValue(file, typeRef);
                consumer.accept(data);
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

    public void saveApplications() {
        saveData(APP_DATA_FILE, applications);
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

    public List<Application> getAllApplications() {
        return new ArrayList<>(applications);
    }

    public void addApplication(Application application) {
        applications.add(application);
        saveApplications();
    }
}
