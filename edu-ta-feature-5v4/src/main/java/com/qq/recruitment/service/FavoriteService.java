package com.qq.recruitment.service;

import com.qq.recruitment.dao.JsonFileDAO;
import com.qq.recruitment.model.Favorite;
import com.qq.recruitment.model.Job;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class FavoriteService {
    private final JsonFileDAO dao;

    public FavoriteService() {
        this.dao = new JsonFileDAO();
    }

    public boolean isFavorite(String username, String jobId) {
        return dao.findFavoriteByUsername(username)
                .map(f -> f.getJobIds() != null && f.getJobIds().contains(jobId))
                .orElse(false);
    }

    public void toggleFavorite(String username, String jobId) {
        Favorite favorite = dao.findFavoriteByUsername(username)
                .orElse(new Favorite(username, new ArrayList<>()));
        List<String> jobIds = favorite.getJobIds() == null ? new ArrayList<>() : favorite.getJobIds();
        if (jobIds.contains(jobId)) {
            jobIds.remove(jobId);
        } else {
            jobIds.add(jobId);
        }
        favorite.setUsername(username);
        favorite.setJobIds(jobIds);
        dao.saveOrUpdateFavorite(favorite);
    }

    public List<Job> getFavoriteJobs(String username) {
        Set<String> favIds = dao.findFavoriteByUsername(username)
                .map(f -> f.getJobIds() == null ? Set.<String>of() : f.getJobIds().stream().collect(Collectors.toSet()))
                .orElse(Set.of());
        return dao.getAllJobs().stream()
                .filter(j -> favIds.contains(j.getId()))
                .collect(Collectors.toList());
    }
}
