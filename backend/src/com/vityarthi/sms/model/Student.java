package com.vityarthi.sms.model;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Domain entity model representing a Student profile and academic performance.
 */
public class Student {
    private String studentId;
    private String name;
    private String email;
    private final Map<String, Double> subjectMarks;
    private double gpa;

    public Student(String studentId, String name, String email) {
        this.studentId = Objects.requireNonNull(studentId, "studentId cannot be null").trim();
        this.name = Objects.requireNonNull(name, "name cannot be null").trim();
        this.email = Objects.requireNonNull(email, "email cannot be null").trim();
        this.subjectMarks = new LinkedHashMap<>();
        this.gpa = 0.0;
    }

    public Student(String studentId, String name, String email, Map<String, Double> marks, double gpa) {
        this.studentId = Objects.requireNonNull(studentId, "studentId cannot be null").trim();
        this.name = Objects.requireNonNull(name, "name cannot be null").trim();
        this.email = Objects.requireNonNull(email, "email cannot be null").trim();
        this.subjectMarks = new LinkedHashMap<>();
        if (marks != null) {
            this.subjectMarks.putAll(marks);
        }
        this.gpa = Math.max(0.0, Math.min(4.0, gpa));
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = Objects.requireNonNull(studentId).trim();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = Objects.requireNonNull(name).trim();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = Objects.requireNonNull(email).trim();
    }

    public Map<String, Double> getSubjectMarks() {
        return Collections.unmodifiableMap(subjectMarks);
    }

    public void addOrUpdateMark(String subject, double score) {
        if (subject == null || subject.trim().isEmpty()) {
            throw new IllegalArgumentException("Subject name cannot be empty");
        }
        if (score < 0.0 || score > 100.0) {
            throw new IllegalArgumentException("Score must be between 0 and 100");
        }
        subjectMarks.put(subject.trim(), score);
    }

    public void removeMark(String subject) {
        if (subject != null) {
            subjectMarks.remove(subject.trim());
        }
    }

    public void clearMarks() {
        subjectMarks.clear();
    }

    public double getGpa() {
        return gpa;
    }

    public void setGpa(double gpa) {
        this.gpa = Math.max(0.0, Math.min(4.0, gpa));
    }

    public Grade getOverallGrade() {
        return Grade.fromGpa(this.gpa);
    }

    public double getAveragePercentage() {
        if (subjectMarks.isEmpty()) return 0.0;
        double sum = 0.0;
        for (double score : subjectMarks.values()) {
            sum += score;
        }
        return sum / subjectMarks.size();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Student student = (Student) o;
        return Objects.equals(studentId.toLowerCase(), student.studentId.toLowerCase());
    }

    @Override
    public int hashCode() {
        return Objects.hash(studentId.toLowerCase());
    }

    @Override
    public String toString() {
        return "Student{" +
                "studentId='" + studentId + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", subjects=" + subjectMarks.keySet() +
                ", GPA=" + String.format("%.2f", gpa) +
                ", Grade=" + getOverallGrade() +
                '}';
    }
}
