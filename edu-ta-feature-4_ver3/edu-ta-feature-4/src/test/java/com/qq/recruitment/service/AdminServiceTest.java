package com.qq.recruitment.service;

import com.qq.recruitment.model.Application;
import com.qq.recruitment.model.Job;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

public class AdminServiceTest {

    @Test
    public void testStats() {
        // Clean up
        File appFile = new File("src/main/resources/data/applications.json");
        if (appFile.exists()) appFile.delete();
        File jobFile = new File("src/main/resources/data/jobs.json");
        if (jobFile.exists()) jobFile.delete();
        File profileFile = new File("src/main/resources/data/profiles.json");
        if (profileFile.exists()) profileFile.delete();
        File userFile = new File("src/main/resources/data/users.json");
        if (userFile.exists()) userFile.delete();

        UserService userService = new UserService();
        userService.register("student1", "password123", "Student 1", "APPLICANT");
        userService.register("student2", "password123", "Student 2", "APPLICANT");
        userService.register("student3", "password123", "Student 3", "APPLICANT");

        // Setup Data
        JobService jobService = new JobService();
        jobService.createJob("Java TA", "Teaching", "Desc", "Req", null, "Prof. A");
        jobService.createJob("Python TA", "Teaching", "Desc", "Req", null, "Prof. B");
        String javaJobId = jobService.getAllJobs().get(0).getId();
        String pythonJobId = jobService.getAllJobs().get(1).getId();

        ApplicationService appService = new ApplicationService();
        appService.apply(javaJobId, "student1", "resume.pdf");
        appService.apply(javaJobId, "student2", "resume.pdf");
        appService.apply(pythonJobId, "student3", "resume.pdf");

        // Create profile for student1
        ProfileService profileService = new ProfileService();
        com.qq.recruitment.model.UserProfile profile = new com.qq.recruitment.model.UserProfile("student1", "CS", "123", null, "bio");
        profileService.updateProfile(profile);

        // Update status
        String app1Id = appService.getApplicationsByApplicant("student1").get(0).getId();
        appService.updateApplicationStatus(app1Id, "ACCEPTED");

        // Test Admin Service
        AdminService adminService = new AdminService();
        
        // 1. Status Stats
        Map<String, Integer> statusStats = adminService.getApplicationStatusStats();
        assertEquals(1, statusStats.get("ACCEPTED"));
        assertEquals(2, statusStats.get("PENDING"));

        // 2. Job Stats
        Map<String, Integer> jobStats = adminService.getJobApplicationCounts();
        String javaKey = javaJobId + " | Java TA";
        String pythonKey = pythonJobId + " | Python TA";
        assertEquals(2, jobStats.get(javaKey));
        assertEquals(1, jobStats.get(pythonKey));

        // 3. Workload Stats
        Map<String, Integer> workloadStats = adminService.getTAWorkloadStats();
        // Since "student1" was ACCEPTED for Java TA, their workload should be 1
        assertEquals(1, workloadStats.get("student1"));
    }

    @Test
    public void testStatsShouldIgnoreDeletedApplicants() {
        File appFile = new File("src/main/resources/data/applications.json");
        if (appFile.exists()) appFile.delete();
        File jobFile = new File("src/main/resources/data/jobs.json");
        if (jobFile.exists()) jobFile.delete();
        File profileFile = new File("src/main/resources/data/profiles.json");
        if (profileFile.exists()) profileFile.delete();
        File userFile = new File("src/main/resources/data/users.json");
        if (userFile.exists()) userFile.delete();

        UserService userService = new UserService();
        userService.register("active_student", "password123", "Active", "APPLICANT");
        userService.register("deleted_student", "password123", "Deleted", "APPLICANT");

        JobService jobService = new JobService();
        jobService.createJob("Java TA", "Teaching", "Desc", "Req", null, "Prof. A");
        String jobId = jobService.getAllJobs().get(0).getId();

        ApplicationService appService = new ApplicationService();
        appService.apply(jobId, "active_student", "resume.pdf");
        appService.apply(jobId, "deleted_student", "resume.pdf");

        String activeAppId = appService.getApplicationsByApplicant("active_student").get(0).getId();
        String deletedAppId = appService.getApplicationsByApplicant("deleted_student").get(0).getId();
        appService.updateApplicationStatus(activeAppId, "ACCEPTED");
        appService.updateApplicationStatus(deletedAppId, "ACCEPTED");

        ProfileService profileService = new ProfileService();
        com.qq.recruitment.model.UserProfile activeProfile = new com.qq.recruitment.model.UserProfile("active_student", "CS", "001", null, "bio");
        activeProfile.setWorkload(1);
        profileService.updateProfile(activeProfile);
        com.qq.recruitment.model.UserProfile deletedProfile = new com.qq.recruitment.model.UserProfile("deleted_student", "CS", "002", null, "bio");
        deletedProfile.setWorkload(1);
        profileService.updateProfile(deletedProfile);

        assertTrue(userService.deleteUser("deleted_student"));

        AdminService adminService = new AdminService();
        Map<String, Integer> statusStats = adminService.getApplicationStatusStats();
        assertEquals(1, statusStats.get("ACCEPTED"));
        Map<String, Integer> workloadStats = adminService.getTAWorkloadStats();
        assertEquals(1, workloadStats.get("active_student"));
        assertFalse(workloadStats.containsKey("deleted_student"));
    }
}
