package com.qq.recruitment.service;

import com.qq.recruitment.dao.JsonFileDAO;
import com.qq.recruitment.model.Application;
import com.qq.recruitment.model.Job;
import com.qq.recruitment.model.UserProfile;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class AIServiceTest {

    @Test
    public void testAIAnalysisTriggerWithJsonResponse() {
        resetData();

        String jobId = createJob("AI Engineer", "Python, TensorFlow, NLP");
        createProfile("student_ai", Arrays.asList("Python", "Java"), "I love AI.");
        String applicationId = createApplication(jobId, "student_ai");

        AIService aiService = new StubAIService("{\"score\": 91, \"analysis\": \"Excellent fit for the role.\"}");
        aiService.analyzeApplication(applicationId);

        Application app = getApplicationById(applicationId);
        assertNotNull(app);
        assertEquals(91, app.getMatchScore());
        assertEquals("Excellent fit for the role.", app.getAiAnalysis());
    }

    @Test
    public void testAIAnalysisWithMarkdownWrappedJson() {
        resetData();

        String jobId = createJob("Data Science TA", "Python, Statistics");
        createProfile("student_markdown", Arrays.asList("Python", "Data Analysis"), "Ready to support labs.");
        String applicationId = createApplication(jobId, "student_markdown");

        String response = "```json\n{\"score\": 84, \"analysis\": \"Strong baseline and relevant skills.\"}\n```";
        AIService aiService = new StubAIService(response);
        aiService.analyzeApplication(applicationId);

        Application app = getApplicationById(applicationId);
        assertNotNull(app);
        assertEquals(84, app.getMatchScore());
        assertEquals("Strong baseline and relevant skills.", app.getAiAnalysis());
    }

    @Test
    public void testAIAnalysisWithoutProfileFallsBackGracefully() {
        resetData();

        String jobId = createJob("Java TA", "Java, OOP");
        String applicationId = createApplication(jobId, "student_no_profile");

        AIService aiService = new StubAIService("{\"score\": 77, \"analysis\": \"Can support entry-level tasks.\"}");
        aiService.analyzeApplication(applicationId);

        Application app = getApplicationById(applicationId);
        assertNotNull(app);
        assertEquals(77, app.getMatchScore());
        assertEquals("Can support entry-level tasks.", app.getAiAnalysis());
    }

    @Test
    public void testAIInvalidResponseUsesFallback() {
        resetData();

        String jobId = createJob("Web TA", "HTML, CSS, JavaScript");
        createProfile("student_invalid", Arrays.asList("HTML", "CSS"), "Frontend helper.");
        String applicationId = createApplication(jobId, "student_invalid");

        AIService aiService = new StubAIService("This is not JSON at all");
        aiService.analyzeApplication(applicationId);

        Application app = getApplicationById(applicationId);
        assertNotNull(app);
        assertTrue(app.getMatchScore() >= 60 && app.getMatchScore() <= 94);
        assertNotNull(app.getAiAnalysis());
        assertFalse(app.getAiAnalysis().isBlank());
    }

    @Test
    public void testAIAnalysisShouldNotOverwriteWithdrawStatus() {
        resetData();

        String jobId = createJob("Concurrent TA", "Java, Communication");
        createProfile("student_race", Arrays.asList("Java"), "Testing race case.");
        String applicationId = createApplication(jobId, "student_race");

        AIService aiService = new InterleavingStubAIService(applicationId, "{\"score\": 88, \"analysis\": \"Good match.\"}");
        aiService.analyzeApplication(applicationId);

        Application app = getApplicationById(applicationId);
        assertNotNull(app);
        assertEquals("WITHDRAWN", app.getStatus(), "AI persistence should not revert status updates from other operations");
        assertEquals(88, app.getMatchScore());
        assertEquals("Good match.", app.getAiAnalysis());
    }

    private void resetData() {
        File appFile = new File("src/main/resources/data/applications.json");
        if (appFile.exists()) appFile.delete();

        File jobFile = new File("src/main/resources/data/jobs.json");
        if (jobFile.exists()) jobFile.delete();

        File profileFile = new File("src/main/resources/data/profiles.json");
        if (profileFile.exists()) profileFile.delete();
    }

    private String createJob(String title, String requirements) {
        JobService jobService = new JobService();
        jobService.createJob(title, "Teaching", "Test description", requirements, null, "Prof. X");
        Job job = jobService.getAllJobs().get(0);
        return job.getId();
    }

    private void createProfile(String username, java.util.List<String> skills, String bio) {
        ProfileService profileService = new ProfileService();
        UserProfile profile = new UserProfile(username, "Computer Science", "2024001", skills, bio);
        profileService.updateProfile(profile);
    }

    private String createApplication(String jobId, String username) {
        JsonFileDAO dao = new JsonFileDAO();
        Application application = new Application(jobId, username, "resume.pdf");
        dao.addApplication(application);
        return application.getId();
    }

    private Application getApplicationById(String appId) {
        JsonFileDAO dao = new JsonFileDAO();
        return dao.getAllApplications().stream()
                .filter(a -> a.getId().equals(appId))
                .findFirst()
                .orElse(null);
    }

    static class StubAIService extends AIService {
        private final String response;

        StubAIService(String response) {
            this.response = response;
        }

        @Override
        protected String requestCompletion(String prompt) {
            return response;
        }
    }

    static class InterleavingStubAIService extends AIService {
        private final String appId;
        private final String response;

        InterleavingStubAIService(String appId, String response) {
            this.appId = appId;
            this.response = response;
        }

        @Override
        protected String requestCompletion(String prompt) {
            JsonFileDAO dao = new JsonFileDAO();
            dao.getAllApplications().stream()
                    .filter(a -> appId.equals(a.getId()))
                    .findFirst()
                    .ifPresent(a -> a.setStatus("WITHDRAWN"));
            dao.saveApplications();
            return response;
        }
    }
}
