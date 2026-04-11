package com.qq.recruitment.service;

import com.qq.recruitment.dao.JsonFileDAO;
import com.qq.recruitment.model.Application;
import com.qq.recruitment.model.UserProfile;
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

    @Test
    public void testAcceptCreatesProfileAndUpdatesWorkloadWhenProfileMissing() {
        File appFile = new File("src/main/resources/data/applications.json");
        if (appFile.exists()) appFile.delete();
        File profileFile = new File("src/main/resources/data/profiles.json");
        if (profileFile.exists()) profileFile.delete();

        String username = "student_without_profile";
        String jobId = "job-abc";

        JsonFileDAO dao = new JsonFileDAO();
        Application application = new Application(jobId, username, "resume.pdf");
        dao.addApplication(application);

        ApplicationService appService = new ApplicationService();
        boolean updated = appService.updateApplicationStatus(application.getId(), "ACCEPTED");
        assertTrue(updated, "Status update should succeed");

        UserProfile profile = new ProfileService().getProfile(username);
        assertNotNull(profile, "Profile should be created");
        assertEquals(username, profile.getUsername(), "Created profile should bind applicant username");
        assertEquals(1, profile.getWorkload(), "Workload should be incremented to 1");
    }
}
