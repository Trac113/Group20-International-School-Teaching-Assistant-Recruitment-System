package com.qq.recruitment.service;

import com.qq.recruitment.model.Job;
import org.junit.jupiter.api.Test;
import java.io.File;
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
        jobService.createJob("Java TA", "Assist with Java course", "Knowledge of Java", "Prof. Smith");
        
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
}
