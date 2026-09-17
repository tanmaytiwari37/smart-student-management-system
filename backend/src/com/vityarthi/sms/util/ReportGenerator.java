package com.vityarthi.sms.util;

import com.vityarthi.sms.model.Grade;
import com.vityarthi.sms.model.Student;
import com.vityarthi.sms.service.StudentService;

import java.util.*;

/**
 * Utility for rendering ANSI-formatted tabular student transcripts,
 * class-wide performance analytics, and merit rankings.
 */
public final class ReportGenerator {

    private ReportGenerator() {
        // Prevent instantiation
    }

    public static String generateStudentTranscript(Student student) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n===============================================================\n");
        sb.append("                 OFFICIAL ACADEMIC TRANSCRIPT                 \n");
        sb.append("===============================================================\n");
        sb.append(String.format("Student ID : %-20s  Name  : %s\n", student.getStudentId(), student.getName()));
        sb.append(String.format("Email      : %-20s  Status: ACTIVE\n", student.getEmail()));
        sb.append("---------------------------------------------------------------\n");
        sb.append(String.format("%-25s | %-10s | %-8s | %-12s\n", "Subject", "Score", "Grade", "Points"));
        sb.append("---------------------------------------------------------------\n");

        if (student.getSubjectMarks().isEmpty()) {
            sb.append("  [No subject marks recorded yet for this student]\n");
        } else {
            for (Map.Entry<String, Double> entry : student.getSubjectMarks().entrySet()) {
                Grade g = Grade.fromPercentage(entry.getValue());
                sb.append(String.format("%-25s | %-10.2f | %-8s | %-12.2f\n",
                        entry.getKey(), entry.getValue(), g.name(), g.getGradePoint()));
            }
        }

        sb.append("---------------------------------------------------------------\n");
        sb.append(String.format("Average Score : %-8.2f%%   Cumulative GPA : %.2f / 4.00\n",
                student.getAveragePercentage(), student.getGpa()));
        sb.append(String.format("Overall Grade : %-8s      Standing       : %s\n",
                student.getOverallGrade().name(), student.getOverallGrade().getDescription()));
        sb.append("===============================================================\n");
        return sb.toString();
    }

    public static String generateClassSummaryReport(StudentService service) {
        List<Student> students = service.getAllStudents();
        StringBuilder sb = new StringBuilder();
        sb.append("\n=========================================================================================\n");
        sb.append("                       SSIPMS - COMPREHENSIVE CLASS ROSTER                               \n");
        sb.append("=========================================================================================\n");
        sb.append(String.format("%-10s | %-22s | %-24s | %-8s | %-6s | %-8s\n",
                "ID", "Name", "Email", "Avg(%)", "GPA", "Grade"));
        sb.append("-----------------------------------------------------------------------------------------\n");

        if (students.isEmpty()) {
            sb.append("  No student records currently present in the system.\n");
        } else {
            for (Student s : students) {
                sb.append(String.format("%-10s | %-22s | %-24s | %-8.2f | %-6.2f | %-8s\n",
                        s.getStudentId(),
                        truncate(s.getName(), 22),
                        truncate(s.getEmail(), 24),
                        s.getAveragePercentage(),
                        s.getGpa(),
                        s.getOverallGrade().name()
                ));
            }
        }

        sb.append("-----------------------------------------------------------------------------------------\n");
        sb.append(String.format("Total Students: %-5d | Class Avg GPA: %-5.2f | Class Avg Score: %.2f%%\n",
                students.size(), service.getClassAverageGpa(), service.getClassAveragePercentage()));
        sb.append("=========================================================================================\n");
        return sb.toString();
    }

    public static String generateMeritRankReport(StudentService service, int topN) {
        List<Student> ranked = service.getRankedStudents();
        StringBuilder sb = new StringBuilder();
        sb.append("\n===================================================================================\n");
        sb.append(String.format("                    ACADEMIC MERIT RANKING (TOP %d)                               \n", Math.min(topN, ranked.size())));
        sb.append("===================================================================================\n");
        sb.append(String.format("%-6s | %-12s | %-24s | %-8s | %-8s | %-6s\n",
                "Rank", "Student ID", "Full Name", "GPA", "Avg(%)", "Grade"));
        sb.append("-----------------------------------------------------------------------------------\n");

        if (ranked.isEmpty()) {
            sb.append("  No ranking data available.\n");
        } else {
            int rank = 1;
            for (Student s : ranked) {
                if (rank > topN) break;
                sb.append(String.format("#%-5d | %-12s | %-24s | %-8.2f | %-8.2f | %-6s\n",
                        rank++,
                        s.getStudentId(),
                        truncate(s.getName(), 24),
                        s.getGpa(),
                        s.getAveragePercentage(),
                        s.getOverallGrade().name()
                ));
            }
        }

        sb.append("===================================================================================\n");
        return sb.toString();
    }

    public static String generateGradeDistribution(StudentService service) {
        List<Student> students = service.getAllStudents();
        Map<Grade, Integer> distribution = new EnumMap<>(Grade.class);
        for (Grade g : Grade.values()) {
            distribution.put(g, 0);
        }

        for (Student s : students) {
            Grade g = s.getOverallGrade();
            distribution.put(g, distribution.get(g) + 1);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("\n=======================================================\n");
        sb.append("              GRADE DISTRIBUTION ANALYSIS              \n");
        sb.append("=======================================================\n");
        for (Grade g : Grade.values()) {
            int count = distribution.get(g);
            int percentage = students.isEmpty() ? 0 : (count * 100) / students.size();
            String bar = "█".repeat(Math.max(0, percentage / 5));
            sb.append(String.format("Grade %s (%-12s) : [%-20s] %2d (%d%%)\n",
                    g.name(), g.getDescription(), bar, count, percentage));
        }
        sb.append("=======================================================\n");
        return sb.toString();
    }

    private static String truncate(String val, int maxLen) {
        if (val == null) return "";
        if (val.length() <= maxLen) return val;
        return val.substring(0, maxLen - 3) + "...";
    }
}
