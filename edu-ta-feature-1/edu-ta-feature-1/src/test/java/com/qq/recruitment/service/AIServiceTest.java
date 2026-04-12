package com.qq.recruitment.service;

import com.qq.recruitment.model.Application;
import com.qq.recruitment.model.Job;
import org.junit.jupiter.api.Test;
import java.io.File;
import static org.junit.jupiter.api.Assertions.*;

public class AIServiceTest {

    @Test
    public void testAIAnalysisTrigger() {
        // Clean up
        File appFile = new File("src/main/resources/data/applications.json");
        if (appFile.exists()) appFile.delete();
        File jobFile = new File("src/main/resources/data/jobs.json");
        if (jobFile.exists()) jobFile.delete();

        // Setup
        JobService jobService = new JobService();
        jobService.createJob("AI Engineer", "Develop AI models", "Python, TensorFlow, NLP", "Prof. X");
        String jobId = jobService.getAllJobs().get(0).getId();

        ApplicationService appService = new ApplicationService();
        
        // Apply (should trigger AI analysis)
        appService.apply(jobId, "student_ai", "resume_ai.pdf");

        // Reload data from DAO to get the updated application
        // In the real app, we might need a small delay if async, but here it's sync
        ApplicationService newAppService = new ApplicationService();
        Application app = newAppService.getApplicationsByApplicant("student_ai").get(0);
        
        // Check if score is updated (mock logic generates > 60)
        assertTrue(app.getMatchScore() >= 60, "AI Score should be calculated. Actual: " + app.getMatchScore());
        assertNotNull(app.getAiAnalysis(), "AI Analysis should be present");
        assertFalse(app.getAiAnalysis().isEmpty(), "AI Analysis should not be empty");
        
        System.out.println("AI Score: " + app.getMatchScore());
        System.out.println("AI Analysis: " + app.getAiAnalysis());
    }
}
