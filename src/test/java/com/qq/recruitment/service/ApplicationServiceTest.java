package com.qq.recruitment.service;

import com.qq.recruitment.dao.JsonFileDAO;
import com.qq.recruitment.model.Application;
import com.qq.recruitment.model.UserProfile;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
    public void testWithdrawAndUpdateResumeFlow() throws IOException {
        File appFile = new File("src/main/resources/data/applications.json");
        if (appFile.exists()) {
            appFile.delete();
        }

        ApplicationService appService = new ApplicationService();
        String jobId = "job-456";
        String username = "student2";

        // Create a local dummy resume file
        File resume = new File("test_resume_for_update.pdf");
        try (FileWriter writer = new FileWriter(resume)) {
            writer.write("dummy");
        }

        boolean success = appService.apply(jobId, username, resume);
        assertTrue(success);
        assertTrue(appService.hasApplied(jobId, username));

        Application app = appService.getApplicationsByApplicant(username).get(0);
        boolean updated = appService.updateResume(app.getId(), username, resume);
        assertTrue(updated, "Pending application should allow resume update");

        boolean withdrawn = appService.withdrawApplication(app.getId(), username);
        assertTrue(withdrawn, "Pending application should allow withdraw");

        Application updatedApp = appService.getApplicationsByApplicant(username).get(0);
        assertEquals("WITHDRAWN", updatedApp.getStatus());
        assertFalse(appService.hasApplied(jobId, username), "Withdrawn application should not be treated as active");

        boolean reapplied = appService.apply(jobId, username, resume);
        assertTrue(reapplied, "Applicant should be able to apply again after withdraw");

        // Withdrawn app should no longer allow resume update
        boolean updateAfterWithdraw = appService.updateResume(app.getId(), username, resume);
        assertFalse(updateAfterWithdraw);

        if (resume.exists()) {
            resume.delete();
        }
    }

    @Test
    public void testAcceptBlockedWhenWorkloadReachesLimit() {
        File appFile = new File("src/main/resources/data/applications.json");
        if (appFile.exists()) appFile.delete();
        File profileFile = new File("src/main/resources/data/profiles.json");
        if (profileFile.exists()) profileFile.delete();

        String username = "student_at_capacity";
        ProfileService profileService = new ProfileService();
        UserProfile profile = new UserProfile(username, "CS", "2024009", null, "bio");
        profile.setWorkload(3);
        profileService.updateProfile(profile);

        JsonFileDAO dao = new JsonFileDAO();
        Application application = new Application("job-limit", username, "resume.pdf");
        dao.addApplication(application);

        ApplicationService appService = new ApplicationService();
        boolean updated = appService.updateApplicationStatus(application.getId(), "ACCEPTED");
        assertFalse(updated, "Accept should be blocked when workload is at max");

        Application latest = appService.getApplicationsByApplicant(username).get(0);
        assertEquals("PENDING", latest.getStatus(), "Status should remain pending when blocked");
    }

    @Test
    public void testAcceptShouldRespectAdminUpdatedWorkload() {
        File appFile = new File("src/main/resources/data/applications.json");
        if (appFile.exists()) appFile.delete();
        File profileFile = new File("src/main/resources/data/profiles.json");
        if (profileFile.exists()) profileFile.delete();

        String username = "student_admin_override";
        JsonFileDAO dao = new JsonFileDAO();

        Application accepted = new Application("job-a", username, "resume.pdf");
        accepted.setStatus("ACCEPTED");
        dao.addApplication(accepted);

        Application pending = new Application("job-b", username, "resume.pdf");
        pending.setStatus("PENDING");
        dao.addApplication(pending);

        ProfileService profileService = new ProfileService();
        UserProfile profile = new UserProfile(username, "CS", "2024011", null, "bio");
        profile.setWorkload(1);
        profile.setMaxWorkload(1);
        profileService.updateProfile(profile);

        ApplicationService appService = new ApplicationService();
        boolean updated = appService.updateApplicationStatus(pending.getId(), "ACCEPTED");
        assertFalse(updated, "Accept should be blocked after admin sets workload to max");

        Application latestPending = appService.getApplicationsByApplicant(username).stream()
                .filter(a -> pending.getId().equals(a.getId()))
                .findFirst()
                .orElseThrow();
        assertEquals("PENDING", latestPending.getStatus());
    }
}
