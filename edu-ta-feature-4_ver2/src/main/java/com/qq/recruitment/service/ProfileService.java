package com.qq.recruitment.service;

import com.qq.recruitment.dao.JsonFileDAO;
import com.qq.recruitment.model.UserProfile;

import java.util.Optional;

public class ProfileService {
    private final JsonFileDAO dao;

    public ProfileService() {
        this.dao = new JsonFileDAO();
    }

    public UserProfile getProfile(String username) {
        Optional<UserProfile> profileOpt = dao.findProfileByUsername(username);
        return profileOpt.orElse(new UserProfile()); // Return empty profile if not found
    }

    public void updateProfile(UserProfile profile) {
        dao.saveOrUpdateProfile(profile);
    }
}
