package com.vityarthi.sms.model;

/**
 * Enumeration representing academic letter grades and GPA point equivalents.
 */
public enum Grade {
    A(4.0, 90.0, 100.0, "Outstanding"),
    B(3.0, 80.0, 89.99, "Good"),
    C(2.0, 70.0, 79.99, "Satisfactory"),
    D(1.0, 60.0, 69.99, "Pass"),
    F(0.0, 0.0, 59.99, "Fail");

    private final double gradePoint;
    private final double minPercentage;
    private final double maxPercentage;
    private final String description;

    Grade(double gradePoint, double minPercentage, double maxPercentage, String description) {
        this.gradePoint = gradePoint;
        this.minPercentage = minPercentage;
        this.maxPercentage = maxPercentage;
        this.description = description;
    }

    public double getGradePoint() {
        return gradePoint;
    }

    public double getMinPercentage() {
        return minPercentage;
    }

    public double getMaxPercentage() {
        return maxPercentage;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Determines the appropriate letter grade for a given percentage score (0-100).
     */
    public static Grade fromPercentage(double percentage) {
        if (percentage >= 90.0) return A;
        if (percentage >= 80.0) return B;
        if (percentage >= 70.0) return C;
        if (percentage >= 60.0) return D;
        return F;
    }

    /**
     * Determines the letter grade from a 4.0 scale GPA.
     */
    public static Grade fromGpa(double gpa) {
        if (gpa >= 3.5) return A;
        if (gpa >= 2.5) return B;
        if (gpa >= 1.5) return C;
        if (gpa >= 1.0) return D;
        return F;
    }
}
