package com.qq.recruitment.service;

import com.qq.recruitment.dao.JsonFileDAO;
import com.qq.recruitment.model.UserProfile;
import com.qq.recruitment.util.InputValidator;

import java.util.Optional;

public class ProfileService {
    public UserProfile getProfile(String username) {
        JsonFileDAO dao = new JsonFileDAO();
        Optional<UserProfile> profileOpt = dao.findProfileByUsername(username);
        return profileOpt.orElse(new UserProfile()); // Return empty profile if not found
    }

    public void updateProfile(UserProfile profile) {
        if (profile == null) {
            throw new IllegalArgumentException("Profile is required.");
        }
        String username = InputValidator.normalizeUsername(profile.getUsername());
        String major = InputValidator.normalizeOptionalText(profile.getMajor(), "Major", 100, false);
        String studentId = InputValidator.normalizeOptionalText(profile.getStudentId(), "Student ID", 40, false);
        String bio = InputValidator.normalizeOptionalText(profile.getBio(), "Bio", 1000, true);

        profile.setUsername(username);
        profile.setMajor(major);
        profile.setStudentId(studentId);
        profile.setBio(bio);
        profile.setSkills(InputValidator.normalizeTagList(profile.getSkills(), "Skill", 30, 40));
        if (profile.getWorkload() < 0) {
            profile.setWorkload(0);
        }

        JsonFileDAO dao = new JsonFileDAO();
        dao.saveOrUpdateProfile(profile);
    }

    public boolean updateWorkload(String username, int workload) {
        final String normalizedUsername;
        try {
            normalizedUsername = InputValidator.normalizeUsername(username);
        } catch (IllegalArgumentException ex) {
            return false;
        }

        if (workload < 0) {
            return false;
        }
        JsonFileDAO dao = new JsonFileDAO();
        Optional<UserProfile> profileOpt = dao.findProfileByUsername(normalizedUsername);
        UserProfile profile = profileOpt.orElse(new UserProfile());
        profile.setUsername(normalizedUsername);
        profile.setMaxWorkload(workload);
        dao.saveOrUpdateProfile(profile);
        return true;
    }
}
