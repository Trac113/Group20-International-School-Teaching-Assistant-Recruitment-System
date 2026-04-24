package com.qq.recruitment.service;

import com.qq.recruitment.dao.JsonFileDAO;
import com.qq.recruitment.model.Job;
import com.qq.recruitment.model.UserProfile;
import com.qq.recruitment.util.InputValidator;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JobService {
    private static final Pattern SIMPLE_JOB_ID_PATTERN = Pattern.compile("^job-\\d+$", Pattern.CASE_INSENSITIVE);
    private static final Pattern UUID_PATTERN = Pattern.compile("^[0-9a-fA-F\\-]{32,}$");
    private final JsonFileDAO jobDAO;
    private final ProfileService profileService;

    public JobService() {
        this.jobDAO = new JsonFileDAO();
        this.profileService = new ProfileService();
    }

    public void createJob(String title, String category, String description, String requirements, List<String> requiredSkills, String postedBy) {
        String normalizedTitle = normalizeRequiredField(title, "Title");
        String normalizedCategory = normalizeRequiredField(category, "Category");
        String normalizedDescription = normalizeRequiredField(description, "Description");
        String normalizedRequirements = normalizeRequiredField(requirements, "Requirements");
        String normalizedPostedBy = normalizeRequiredField(postedBy, "PostedBy");
        List<String> normalizedSkills = InputValidator.normalizeTagList(requiredSkills, "Required skill", 20, 40);

        if (existsDuplicateTitle(normalizedTitle)) {
            throw new IllegalArgumentException("Job title already exists.");
        }

        Job job = new Job(normalizedTitle, normalizedCategory, normalizedDescription, normalizedRequirements, normalizedSkills, normalizedPostedBy);
        job.setId(generateNextJobId());
        jobDAO.addJob(job);
    }

    public void createJob(String title, String category, String description, String requirements, List<String> requiredSkills, String postedBy, int maxApplicants) {
        String normalizedTitle = normalizeRequiredField(title, "Title");
        String normalizedCategory = normalizeRequiredField(category, "Category");
        String normalizedDescription = normalizeRequiredField(description, "Description");
        String normalizedRequirements = normalizeRequiredField(requirements, "Requirements");
        String normalizedPostedBy = normalizeRequiredField(postedBy, "PostedBy");
        List<String> normalizedSkills = InputValidator.normalizeTagList(requiredSkills, "Required skill", 20, 40);
        if (maxApplicants <= 0) {
            throw new IllegalArgumentException("Max applicants is required.");
        }
        if (maxApplicants > Job.MAX_ALLOWED_APPLICANTS) {
            throw new IllegalArgumentException("Max applicants must be between 1 and " + Job.MAX_ALLOWED_APPLICANTS + ".");
        }

        if (existsDuplicateTitle(normalizedTitle)) {
            throw new IllegalArgumentException("Job title already exists.");
        }

        Job job = new Job(normalizedTitle, normalizedCategory, normalizedDescription, normalizedRequirements, normalizedSkills, normalizedPostedBy, maxApplicants);
        job.setId(generateNextJobId());
        jobDAO.addJob(job);
    }

    public List<Job> getAllJobs() {
        List<Job> jobs = jobDAO.getAllJobs();
        boolean changed = false;
        for (Job job : jobs) {
            int before = job.getMaxApplicants();
            job.setMaxApplicants(before);
            if (job.getMaxApplicants() != before) {
                changed = true;
            }
        }
        if (changed) {
            jobDAO.saveJobs();
        }
        return jobs;
    }
    
    public List<Job> getOpenJobs() {
         return jobDAO.getAllJobs().stream()
                 .filter(job -> "OPEN".equals(job.getStatus()))
                 .collect(Collectors.toList());
    }

    public void updateJobStatus(String jobId, String status) {
        List<Job> allJobs = jobDAO.getAllJobs();
        for (Job job : allJobs) {
            if (job.getId().equals(jobId)) {
                job.setStatus(status);
                jobDAO.saveJobs();
                break;
            }
        }
    }

    public List<Job> searchOpenJobs(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getOpenJobs();
        }

        String normalized = keyword.trim().toLowerCase();
        if (normalized.length() < 2) {
            return getOpenJobs();
        }

        List<String> tokens = Arrays.stream(normalized.split("\\s+"))
                .filter(t -> !t.isBlank())
                .collect(Collectors.toList());

        return getOpenJobs().stream()
                .filter(job -> tokens.stream().allMatch(token -> matchesToken(job, token)))
                .sorted(Comparator.comparingInt((Job job) -> searchRelevance(job, normalized)).reversed())
                .collect(Collectors.toList());
    }

    public int getCurrentApplicantCount(String jobId) {
        JsonFileDAO freshDao = new JsonFileDAO();
        return (int) freshDao.getAllApplications().stream()
                .filter(app -> app.getJobId().equals(jobId))
                .filter(app -> "ACCEPTED".equals(app.getStatus()))
                .count();
    }

    public boolean isJobAtCapacity(String jobId) {
        JsonFileDAO freshDao = new JsonFileDAO();
        Job job = freshDao.getAllJobs().stream()
                .filter(j -> j.getId().equals(jobId))
                .findFirst()
                .orElse(null);
        if (job == null) {
            return false;
        }
        return getCurrentApplicantCount(jobId) >= job.getMaxApplicants();
    }

    public List<Job> getRecommendedOpenJobs(String username) {
        List<Job> openJobs = getOpenJobs();
        if (username == null || username.isBlank()) {
            return openJobs;
        }

        UserProfile profile = profileService.getProfile(username);
        if (profile == null || profile.getUsername() == null || profile.getUsername().isBlank()) {
            return openJobs;
        }

        return openJobs.stream()
                .sorted(Comparator
                        .comparingInt((Job job) -> recommendationScore(job, profile)).reversed()
                        .thenComparing(job -> normalize(job.getTitle())))
                .collect(Collectors.toList());
    }

    private int recommendationScore(Job job, UserProfile profile) {
        int score = 0;
        Set<String> candidateSkills = profile.getSkills() == null
                ? Set.of()
                : profile.getSkills().stream()
                .map(this::normalize)
                .filter(s -> !s.isBlank())
                .collect(Collectors.toSet());

        Set<String> requiredSkills = job.getRequiredSkills() == null
                ? Set.of()
                : job.getRequiredSkills().stream()
                .map(this::normalize)
                .filter(s -> !s.isBlank())
                .collect(Collectors.toSet());

        for (String skill : requiredSkills) {
            if (candidateSkills.contains(skill)) {
                score += 35;
            }
        }

        String major = normalize(profile.getMajor());
        String jobText = Stream.of(job.getTitle(), job.getCategory(), job.getRequirements(), job.getDescription())
                .map(this::normalize)
                .collect(Collectors.joining(" "));
        if (!major.isBlank() && jobText.contains(major)) {
            score += 15;
        }

        String bio = normalize(profile.getBio());
        if (!bio.isBlank()) {
            for (String skill : requiredSkills) {
                if (bio.contains(skill)) {
                    score += 5;
                }
            }
        }

        return score;
    }

    private boolean matchesToken(Job job, String token) {
        String title = normalize(job.getTitle());
        String category = normalize(job.getCategory());
        String postedBy = normalize(job.getPostedBy());
        String requirements = normalize(job.getRequirements());
        String skills = normalize(job.getRequiredSkills() == null ? "" : String.join(" ", job.getRequiredSkills()));
        return title.contains(token)
                || category.contains(token)
                || postedBy.contains(token)
                || requirements.contains(token)
                || skills.contains(token);
    }

    private int searchRelevance(Job job, String keyword) {
        String title = normalize(job.getTitle());
        String skills = normalize(job.getRequiredSkills() == null ? "" : String.join(" ", job.getRequiredSkills()));
        String requirements = normalize(job.getRequirements());
        String category = normalize(job.getCategory());
        String postedBy = normalize(job.getPostedBy());

        int score = 0;
        if (title.equals(keyword)) score += 100;
        if (title.startsWith(keyword)) score += 60;
        if (title.contains(keyword)) score += 40;
        if (skills.contains(keyword)) score += 30;
        if (requirements.contains(keyword)) score += 20;
        if (category.contains(keyword)) score += 15;
        if (postedBy.contains(keyword)) score += 10;
        return score;
    }

    private String normalize(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT).trim();
    }

    private String normalizeRequiredField(String value, String fieldName) {
        if ("Title".equals(fieldName)) {
            return InputValidator.normalizeRequiredText(value, fieldName, 120, false);
        }
        if ("Category".equals(fieldName)) {
            return InputValidator.normalizeRequiredText(value, fieldName, 80, false);
        }
        if ("PostedBy".equals(fieldName)) {
            return InputValidator.normalizeRequiredText(value, fieldName, 80, false);
        }
        return InputValidator.normalizeRequiredText(value, fieldName, 1000, true);
    }

    private boolean existsDuplicateTitle(String title) {
        String normalizedTitle = normalize(title);
        return jobDAO.getAllJobs().stream()
                .map(Job::getTitle)
                .map(this::normalize)
                .anyMatch(existing -> existing.equals(normalizedTitle));
    }

    public static String toDisplayJobId(String rawId) {
        if (rawId == null || rawId.isBlank()) {
            return "-";
        }
        String id = rawId.trim();
        if (SIMPLE_JOB_ID_PATTERN.matcher(id).matches()) {
            return id;
        }
        if (UUID_PATTERN.matcher(id).matches()) {
            String compact = id.replace("-", "").toLowerCase(Locale.ROOT);
            if (compact.length() >= 6) {
                return "legacy-" + compact.substring(0, 6);
            }
        }
        if (id.length() > 12) {
            return id.substring(0, 12) + "...";
        }
        return id;
    }

    private String generateNextJobId() {
        Pattern pattern = Pattern.compile("^job-(\\d+)$", Pattern.CASE_INSENSITIVE);
        int max = 0;
        for (Job existing : jobDAO.getAllJobs()) {
            String id = existing.getId();
            if (id == null) {
                continue;
            }
            Matcher matcher = pattern.matcher(id.trim());
            if (!matcher.matches()) {
                continue;
            }
            try {
                int value = Integer.parseInt(matcher.group(1));
                if (value > max) {
                    max = value;
                }
            } catch (NumberFormatException ignored) {
                // Ignore malformed legacy IDs and continue.
            }
        }
        return String.format("job-%03d", max + 1);
    }
}
