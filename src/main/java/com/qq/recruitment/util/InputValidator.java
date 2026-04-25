package com.qq.recruitment.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public final class InputValidator {
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[A-Za-z0-9_.-]{1,32}$");
    private static final Set<String> ALLOWED_ROLES = Set.of("APPLICANT", "TEACHER", "ADMIN");

    private InputValidator() {
    }

    public static String normalizeUsername(String raw) {
        if (raw == null) {
            throw new IllegalArgumentException("Username is required.");
        }
        String value = raw.trim();
        if (value.isBlank()) {
            throw new IllegalArgumentException("Username is required.");
        }
        if (containsWhitespace(raw)) {
            throw new IllegalArgumentException("Username cannot contain spaces.");
        }
        if (!isAsciiOnly(raw)) {
            throw new IllegalArgumentException("Username must be ASCII only.");
        }
        if (containsIllegalChars(raw, false)) {
            throw new IllegalArgumentException("Username contains illegal characters.");
        }
        if (!USERNAME_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("Username can only contain letters, digits, dot, underscore and hyphen.");
        }
        return value;
    }

    public static String normalizePassword(String raw) {
        if (raw == null) {
            throw new IllegalArgumentException("Password is required.");
        }
        String value = raw.trim();
        if (value.isBlank()) {
            throw new IllegalArgumentException("Password is required.");
        }
        if (containsWhitespace(raw)) {
            throw new IllegalArgumentException("Password cannot contain spaces.");
        }
        if (!isAsciiOnly(raw)) {
            throw new IllegalArgumentException("Password must be ASCII only.");
        }
        if (containsIllegalChars(raw, false)) {
            throw new IllegalArgumentException("Password contains illegal characters.");
        }
        if (value.length() > 128) {
            throw new IllegalArgumentException("Password is too long.");
        }
        return value;
    }

    public static String normalizeRole(String raw) {
        if (raw == null) {
            throw new IllegalArgumentException("Role is required.");
        }
        String role = raw.trim().toUpperCase();
        if (!ALLOWED_ROLES.contains(role)) {
            throw new IllegalArgumentException("Invalid role.");
        }
        return role;
    }

    public static String normalizeRequiredText(String raw, String fieldName, int maxLength, boolean allowNewLine) {
        String value = normalizeOptionalText(raw, fieldName, maxLength, allowNewLine);
        if (value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required.");
        }
        return value;
    }

    public static String normalizeOptionalText(String raw, String fieldName, int maxLength, boolean allowNewLine) {
        String value = raw == null ? "" : raw.trim();
        if (value.length() > maxLength) {
            throw new IllegalArgumentException(fieldName + " is too long.");
        }
        if (containsIllegalChars(value, allowNewLine)) {
            throw new IllegalArgumentException(fieldName + " contains illegal characters.");
        }
        return value;
    }

    public static List<String> normalizeTagList(List<String> rawList, String fieldName, int maxItems, int maxItemLength) {
        List<String> result = new ArrayList<>();
        if (rawList == null) {
            return result;
        }
        for (String raw : rawList) {
            String value = normalizeOptionalText(raw, fieldName, maxItemLength, false);
            if (!value.isBlank()) {
                result.add(value);
            }
        }
        if (result.size() > maxItems) {
            throw new IllegalArgumentException(fieldName + " exceeds allowed item count.");
        }
        return result;
    }

    private static boolean containsWhitespace(String value) {
        return value != null && value.chars().anyMatch(Character::isWhitespace);
    }

    private static boolean isAsciiOnly(String value) {
        return value != null && value.chars().allMatch(ch -> ch <= 127);
    }

    private static boolean containsIllegalChars(String value, boolean allowNewLine) {
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            if (ch == 127) {
                return true;
            }
            if (ch < 32) {
                if (allowNewLine && (ch == '\n' || ch == '\r' || ch == '\t')) {
                    continue;
                }
                return true;
            }
        }
        return false;
    }
}
