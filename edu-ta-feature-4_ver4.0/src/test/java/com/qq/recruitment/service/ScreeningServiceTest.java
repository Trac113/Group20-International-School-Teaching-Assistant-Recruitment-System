package com.qq.recruitment.service;

import com.qq.recruitment.model.Application;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class ScreeningServiceTest {

    @Test
    public void testScreeningFlow() {
        // Clean up
        File file = new File("src/main/resources/data/applications.json");
        if (file.exists()) {
            file.delete();
        }

        ApplicationService appService = new ApplicationService();
        String jobId = "job-123";
        String username = "student1";

        // Create initial application
        appService.apply(jobId, username, "resume.pdf");
        
        List<Application> apps = appService.getApplicationsByApplicant(username);
        assertEquals(1, apps.size());
        Application app = apps.get(0);
        assertEquals("PENDING", app.getStatus());

        // Test Accept
        boolean updated = appService.updateApplicationStatus(app.getId(), "ACCEPTED");
        assertTrue(updated, "Status update should succeed");

        // Verify Status Persisted
        ApplicationService newService = new ApplicationService(); // Simulate new session
        List<Application> updatedApps = newService.getApplicationsByApplicant(username);
        assertEquals("ACCEPTED", updatedApps.get(0).getStatus());
    }
}
