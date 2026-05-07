package com.qq.recruitment.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qq.recruitment.dao.JsonFileDAO;
import com.qq.recruitment.model.Application;
import com.qq.recruitment.model.Job;
import com.qq.recruitment.model.UserProfile;
import com.volcengine.ark.runtime.model.completion.chat.ChatCompletionRequest;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessage;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessageRole;
import com.volcengine.ark.runtime.service.ArkService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AIService {
    private static final String API_KEY = "12b115aa-12ab-4e12-a134-9ce904570a20";
    private static final String MODEL_EP = "doubao-seed-2-0-pro-260215";
    private static final int MAX_RETRIES = 2;

    private final JsonFileDAO dao;
    private final ProfileService profileService;
    private final ObjectMapper mapper;

    public AIService() {
        this.dao = new JsonFileDAO();
        this.profileService = new ProfileService();
        this.mapper = new ObjectMapper();
    }

    /**
     * Analyzes an application against the job requirements using Volcengine Ark API.
     */
    public void analyzeApplication(String applicationId) {
        Optional<Application> appOpt = dao.getAllApplications().stream()
                .filter(a -> a.getId().equals(applicationId))
                .findFirst();

        if (appOpt.isEmpty()) {
            return;
        }
        Application app = appOpt.get();

        Optional<Job> jobOpt = dao.getAllJobs().stream()
                .filter(j -> j.getId().equals(app.getJobId()))
                .findFirst();

        if (jobOpt.isEmpty()) {
            return;
        }
        Job job = jobOpt.get();

        UserProfile profile = profileService.getProfile(app.getApplicantUsername());

        String candidateSkills = "Not provided";
        String candidateBio = "Not provided";
        String candidateMajor = "Not provided";
        if (profile != null) {
            if (profile.getSkills() != null && !profile.getSkills().isEmpty()) {
                candidateSkills = String.join(", ", profile.getSkills());
            }
            if (profile.getBio() != null && !profile.getBio().isBlank()) {
                candidateBio = profile.getBio();
            }
            if (profile.getMajor() != null && !profile.getMajor().isBlank()) {
                candidateMajor = profile.getMajor();
            }
        }

        String prompt = String.format(
                "You are an AI assistant helping a Teaching Assistant Recruitment system.\n" +
                        "Job Title: %s\n" +
                        "Job Description: %s\n" +
                        "Job Requirements: %s\n" +
                        "Candidate Major: %s\n" +
                        "Candidate Skills: %s\n" +
                        "Candidate Bio: %s\n\n" +
                        "Based on the information above, please evaluate the candidate's match for the job.\n" +
                        "You must return ONLY a JSON object with two fields:\n" +
                        "1. \"score\": an integer between 0 and 100 representing the match score.\n" +
                        "2. \"analysis\": a short text (max 2 sentences) analyzing the candidate's strengths and missing skills.",
                job.getTitle(), job.getDescription(), job.getRequirements(),
                candidateMajor, candidateSkills, candidateBio
        );

        try {
            String responseContent = requestCompletion(prompt);
            boolean parsed = parseAndUpdateApplication(app, responseContent);
            if (!parsed) {
                applyFallback(app, job.getRequirements(), candidateSkills, "Response parse failed");
            }
        } catch (Exception e) {
            applyFallback(app, job.getRequirements(), candidateSkills, "API call failed: " + e.getMessage());
        }

        persistAiResultSafely(app.getId(), app.getMatchScore(), app.getAiAnalysis());
    }

    protected String requestCompletion(String prompt) {
        Exception last = null;

        for (int attempt = 1; attempt <= MAX_RETRIES + 1; attempt++) {
            try {
                ArkService service = ArkService.builder().apiKey(API_KEY).build();

                List<ChatMessage> messages = new ArrayList<>();
                messages.add(ChatMessage.builder().role(ChatMessageRole.SYSTEM).content("You are a helpful HR assistant.").build());
                messages.add(ChatMessage.builder().role(ChatMessageRole.USER).content(prompt).build());

                ChatCompletionRequest request = ChatCompletionRequest.builder()
                        .model(MODEL_EP)
                        .messages(messages)
                        .build();

                Object content = service.createChatCompletion(request)
                        .getChoices().get(0)
                        .getMessage().getContent();

                if (content == null) {
                    throw new IllegalStateException("AI response content is null");
                }
                return String.valueOf(content);
            } catch (Exception e) {
                last = e;
                boolean retryable = isRetryable(e);
                System.err.println("[AIService] attempt " + attempt + " failed: " + e.getMessage());
                if (!retryable || attempt > MAX_RETRIES) {
                    break;
                }
                try {
                    Thread.sleep(400L * attempt);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        if (last instanceof RuntimeException) {
            throw (RuntimeException) last;
        }
        throw new RuntimeException(last == null ? "Unknown AI failure" : last.getMessage(), last);
    }

    private boolean isRetryable(Exception e) {
        String msg = e.getMessage();
        if (msg == null) {
            return false;
        }
        String lower = msg.toLowerCase();
        return lower.contains("timeout")
                || lower.contains("timed out")
                || lower.contains("connection reset")
                || lower.contains("connection refused")
                || lower.contains("temporarily unavailable")
                || lower.contains("503")
                || lower.contains("502")
                || lower.contains("500");
    }

    private boolean parseAndUpdateApplication(Application app, String responseContent) {
        if (responseContent == null || responseContent.isBlank()) {
            return false;
        }

        JsonNode rootNode = tryParseJson(responseContent);
        if (rootNode == null) {
            String jsonStr = extractJsonObject(responseContent);
            if (jsonStr == null) {
                return false;
            }
            rootNode = tryParseJson(jsonStr);
            if (rootNode == null) {
                return false;
            }
        }

        int parsedScore = rootNode.path("score").asInt(-1);
        String parsedAnalysis = rootNode.path("analysis").asText("").trim();

        if (parsedScore < 0) {
            return false;
        }

        app.setMatchScore(clampScore(parsedScore));
        if (parsedAnalysis.isEmpty()) {
            app.setAiAnalysis("AI response did not include a detailed analysis.");
        } else {
            app.setAiAnalysis(parsedAnalysis);
        }
        return true;
    }

    private JsonNode tryParseJson(String content) {
        try {
            return mapper.readTree(content);
        } catch (Exception e) {
            return null;
        }
    }

    private String extractJsonObject(String content) {
        Pattern pattern = Pattern.compile("\\{.*\\}", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            return matcher.group(0);
        }
        return null;
    }

    private int clampScore(int score) {
        if (score < 0) {
            return 0;
        }
        if (score > 100) {
            return 100;
        }
        return score;
    }

    private void applyFallback(Application app, String requirements, String candidateSkills, String reason) {
        System.err.println("[AIService] " + reason + ". Using fallback simulation.");
        int score = calculateMockScore(requirements, candidateSkills);
        String analysis = generateMockAnalysis(score);
        app.setMatchScore(score);
        app.setAiAnalysis(analysis);
    }

    private int calculateMockScore(String requirements, String resumeContent) {
        String basis = String.valueOf(requirements) + "|" + String.valueOf(resumeContent);
        int normalized = Math.abs(basis.hashCode());
        return 60 + (normalized % 35);
    }

    private String generateMockAnalysis(int score) {
        if (score >= 85) {
            return "Strong Match: Candidate skills align well with requirements.";
        } else if (score >= 70) {
            return "Good Match: Meets most core requirements, some gaps.";
        } else {
            return "Potential Match: Needs further review on specific skills.";
        }
    }

    private void persistAiResultSafely(String applicationId, int score, String analysis) {
        synchronized (JsonFileDAO.class) {
            JsonFileDAO freshDao = new JsonFileDAO();
            Optional<Application> target = freshDao.getAllApplications().stream()
                    .filter(a -> applicationId.equals(a.getId()))
                    .findFirst();
            if (target.isEmpty()) {
                return;
            }
            Application latest = target.get();
            latest.setMatchScore(clampScore(score));
            latest.setAiAnalysis(analysis == null ? "" : analysis);
            freshDao.saveApplications();
        }
    }
}
