package com.qq.recruitment.service;

import com.qq.recruitment.model.Application;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class ApplicationServiceTest {

    @Test
    public void testApplyFlow() {
        // Clean up
        File file = new File("src/main/resources/data/applications.json");
        if (file.exists()) {
            file.delete();
        }

        ApplicationService appService = new ApplicationService();
        String jobId = "job-123";
        String username = "student1";

        // Test Apply
        boolean success = appService.apply(jobId, username, "resume.pdf");
        assertTrue(success, "Application should succeed");

        // Test Duplicate Apply
        boolean duplicate = appService.apply(jobId, username, "resume.pdf");
        assertFalse(duplicate, "Duplicate application should be prevented");

        // Test Retrieve
        List<Application> myApps = appService.getApplicationsByApplicant(username);
        assertEquals(1, myApps.size());
        assertEquals(jobId, myApps.get(0).getJobId());
        assertEquals("PENDING", myApps.get(0).getStatus());
    }
}
