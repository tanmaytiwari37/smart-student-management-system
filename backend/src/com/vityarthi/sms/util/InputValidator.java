package com.vityarthi.sms.util;

import com.vityarthi.sms.exception.InvalidDataException;

import java.util.regex.Pattern;

/**
 * Utility helper enforcing strict validation rules for student identifiers,
 * emails, names, subjects, and scoring ranges.
 */
public final class InputValidator {

    // RegEx patterns
    private static final Pattern STUDENT_ID_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]{3,20}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$");
    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-Z\\s.'-]{2,60}$");

    private InputValidator() {
        // Prevent instantiation
    }

    public static void validateStudentId(String studentId) throws InvalidDataException {
        if (studentId == null || studentId.trim().isEmpty()) {
            throw new InvalidDataException("Student ID cannot be empty or null.");
        }
        if (!STUDENT_ID_PATTERN.matcher(studentId.trim()).matches()) {
            throw new InvalidDataException("Invalid Student ID format. Must be 3-20 alphanumeric characters (or underscores/hyphens).");
        }
    }

    public static void validateName(String name) throws InvalidDataException {
        if (name == null || name.trim().isEmpty()) {
            throw new InvalidDataException("Student name cannot be empty or null.");
        }
        if (!NAME_PATTERN.matcher(name.trim()).matches()) {
            throw new InvalidDataException("Invalid student name. Must contain 2 to 60 alphabetic characters.");
        }
    }

    public static void validateEmail(String email) throws InvalidDataException {
        if (email == null || email.trim().isEmpty()) {
            throw new InvalidDataException("Student email cannot be empty or null.");
        }
        if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new InvalidDataException("Invalid email format (e.g., student@university.edu).");
        }
    }

    public static void validateScore(double score) throws InvalidDataException {
        if (Double.isNaN(score) || Double.isInfinite(score) || score < 0.0 || score > 100.0) {
            throw new InvalidDataException("Invalid score: " + score + ". Marks must be between 0.0 and 100.0.");
        }
    }

    public static void validateSubject(String subject) throws InvalidDataException {
        if (subject == null || subject.trim().isEmpty()) {
            throw new InvalidDataException("Subject name cannot be empty or null.");
        }
        if (subject.trim().length() < 2 || subject.trim().length() > 50) {
            throw new InvalidDataException("Subject name must be between 2 and 50 characters.");
        }
    }
}
