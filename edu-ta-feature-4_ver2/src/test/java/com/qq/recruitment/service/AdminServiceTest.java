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
        assertEquals(2, jobStats.get("Java TA"));
        assertEquals(1, jobStats.get("Python TA"));
    }
}
