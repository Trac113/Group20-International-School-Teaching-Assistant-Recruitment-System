package com.qq.recruitment.service;

import com.qq.recruitment.model.Job;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FavoriteServiceTest {

    @Test
    public void testToggleAndReadFavorites() {
        File jobFile = new File("src/main/resources/data/jobs.json");
        if (jobFile.exists()) jobFile.delete();
        File favFile = new File("src/main/resources/data/favorites.json");
        if (favFile.exists()) favFile.delete();

        JobService jobService = new JobService();
        jobService.createJob("Java TA", "Teaching", "desc", "req", null, "Prof. A");
        Job job = jobService.getAllJobs().get(0);

        FavoriteService favoriteService = new FavoriteService();
        String username = "student_fav";

        assertFalse(favoriteService.isFavorite(username, job.getId()));
        favoriteService.toggleFavorite(username, job.getId());
        assertTrue(favoriteService.isFavorite(username, job.getId()));

        List<Job> favoriteJobs = favoriteService.getFavoriteJobs(username);
        assertEquals(1, favoriteJobs.size());
        assertEquals("Java TA", favoriteJobs.get(0).getTitle());

        favoriteService.toggleFavorite(username, job.getId());
        assertFalse(favoriteService.isFavorite(username, job.getId()));
    }
}
