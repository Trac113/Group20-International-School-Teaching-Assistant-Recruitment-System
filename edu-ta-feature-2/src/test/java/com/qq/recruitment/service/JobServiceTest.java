package com.qq.recruitment.service;

import com.qq.recruitment.dao.JsonFileDAO;
import com.qq.recruitment.model.Application;
import com.qq.recruitment.model.Job;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class JobServiceTest {

    @Test
    public void testCreateAndRetrieveJobs() {
        // Clean up test data
        File file = new File("src/main/resources/data/jobs.json");
        if (file.exists()) {
            file.delete();
        }

        JobService jobService = new JobService();

        // Test Create Job
        jobService.createJob("Java TA", "Teaching", "Assist with Java course", "Knowledge of Java", null, "Prof. Smith");
        
        // Test Retrieve Jobs
        List<Job> jobs = jobService.getAllJobs();
        assertEquals(1, jobs.size(), "Should have 1 job");
        
        Job job = jobs.get(0);
        assertEquals("Java TA", job.getTitle());
        assertEquals("Prof. Smith", job.getPostedBy());
        assertEquals("OPEN", job.getStatus());
        assertNotNull(job.getId(), "Job ID should be generated");

        // Test Open Jobs Filter
        List<Job> openJobs = jobService.getOpenJobs();
        assertEquals(1, openJobs.size());
    }

    @Test
    public void testRecommendedJobsShouldRankByProfileSkills() {
        File jobFile = new File("src/main/resources/data/jobs.json");
        if (jobFile.exists()) {
            jobFile.delete();
        }
        File profileFile = new File("src/main/resources/data/profiles.json");
        if (profileFile.exists()) {
            profileFile.delete();
        }

        ProfileService profileService = new ProfileService();
        com.qq.recruitment.model.UserProfile profile = new com.qq.recruitment.model.UserProfile(
                "student_reco", "Computer Science", "2024010",
                Arrays.asList("Java", "Data Structure"), "Interested in Java tutoring");
        profileService.updateProfile(profile);

        JobService jobService = new JobService();
        jobService.createJob("Java TA", "Teaching", "Java lab support", "Need Java", Arrays.asList("Java"), "Prof. A");
        jobService.createJob("Python TA", "Teaching", "Python support", "Need Python", Arrays.asList("Python"), "Prof. B");

        List<Job> recommended = jobService.getRecommendedOpenJobs("student_reco");
        assertEquals(2, recommended.size());
        assertEquals("Java TA", recommended.get(0).getTitle(), "Java-related job should rank first for Java-skilled applicant");
    }

    @Test
    public void testSearchShouldRequireEffectiveKeyword() {
        File jobFile = new File("src/main/resources/data/jobs.json");
        if (jobFile.exists()) {
            jobFile.delete();
        }

        JobService jobService = new JobService();
        jobService.createJob("Java TA", "Teaching", "Java support", "Need Java", Arrays.asList("Java"), "Prof. Smith");
        jobService.createJob("Python TA", "Teaching", "Python support", "Need Python", Arrays.asList("Python"), "Prof. Wang");

        List<Job> oneChar = jobService.searchOpenJobs("a");
        assertEquals(2, oneChar.size(), "Single-char keyword should not over-filter results");

        List<Job> javaOnly = jobService.searchOpenJobs("java");
        assertEquals(1, javaOnly.size());
        assertEquals("Java TA", javaOnly.get(0).getTitle());
    }

    @Test
    public void testJobCapacityCount() {
        File jobFile = new File("src/main/resources/data/jobs.json");
        if (jobFile.exists()) {
            jobFile.delete();
        }
        File appFile = new File("src/main/resources/data/applications.json");
        if (appFile.exists()) {
            appFile.delete();
        }

        JobService jobService = new JobService();
        jobService.createJob("Capacity TA", "Teaching", "desc", "req", Arrays.asList("Java"), "Prof. L", 2);
        Job job = jobService.getAllJobs().get(0);

        JsonFileDAO dao = new JsonFileDAO();
        Application a1 = new Application(job.getId(), "u1", "resume.pdf");
        a1.setStatus("PENDING");
        dao.addApplication(a1);
        Application a2 = new Application(job.getId(), "u2", "resume.pdf");
        a2.setStatus("ACCEPTED");
        dao.addApplication(a2);

        assertEquals(1, jobService.getCurrentApplicantCount(job.getId()));
        assertFalse(jobService.isJobAtCapacity(job.getId()));
    }

    @Test
    public void testCreateJobShouldRejectDuplicateOrBlankTitle() {
        File jobFile = new File("src/main/resources/data/jobs.json");
        if (jobFile.exists()) {
            jobFile.delete();
        }

        JobService jobService = new JobService();
        jobService.createJob("Java TA", "Teaching", "desc", "req", Arrays.asList("Java"), "Prof. A");

        assertThrows(IllegalArgumentException.class, () ->
                jobService.createJob("  java ta  ", "Teaching", "desc", "req", Arrays.asList("Java"), "Prof. B"));

        assertThrows(IllegalArgumentException.class, () ->
                jobService.createJob("   ", "Teaching", "desc", "req", Arrays.asList("Java"), "Prof. C"));

        assertThrows(IllegalArgumentException.class, () ->
                jobService.createJob("Math TA", "Teaching", "desc", "req", Arrays.asList("Math"), "Prof. D", 0));
    }

    @Test
    public void testCreateJobShouldRejectIllegalControlCharacters() {
        File jobFile = new File("src/main/resources/data/jobs.json");
        if (jobFile.exists()) {
            jobFile.delete();
        }

        JobService jobService = new JobService();
        assertThrows(IllegalArgumentException.class, () ->
                jobService.createJob("Java\u0001TA", "Teaching", "desc", "req", Arrays.asList("Java"), "Prof. A"));
    }
}
