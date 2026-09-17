package com.vityarthi.sms;

import com.vityarthi.sms.exception.InvalidDataException;
import com.vityarthi.sms.exception.StudentNotFoundException;
import com.vityarthi.sms.model.Student;
import com.vityarthi.sms.service.StudentService;
import com.vityarthi.sms.service.StudentServiceImpl;
import com.vityarthi.sms.util.ReportGenerator;

import java.util.Scanner;

/**
 * Main Application CLI Entry Point providing an interactive console terminal interface
 * for the Smart Student Information & Performance Management System (SSIPMS).
 */
public class Main {

    private static final Scanner scanner = new Scanner(System.in);
    private static StudentService studentService;

    public static void main(String[] args) {
        studentService = new StudentServiceImpl();
        printBanner();

        boolean running = true;
        while (running) {
            printMenu();
            System.out.print("Enter your choice (1-9): ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    handleRegisterStudent();
                    break;
                case "2":
                    handleViewAllStudents();
                    break;
                case "3":
                    handleSearchStudent();
                    break;
                case "4":
                    handleRecordMarks();
                    break;
                case "5":
                    handleUpdateStudent();
                    break;
                case "6":
                    handleDeleteStudent();
                    break;
                case "7":
                    handleViewRankings();
                    break;
                case "8":
                    handleViewAnalytics();
                    break;
                case "9":
                    System.out.println("\n[✓] Persisting records and exiting SSIPMS. Have a great day!");
                    studentService.persistChanges();
                    running = false;
                    break;
                default:
                    System.out.println("\n[!] Invalid selection. Please choose an option between 1 and 9.");
            }
        }
    }

    private static void printBanner() {
        System.out.println("\n*****************************************************************");
        System.out.println("*  Smart Student Information & Performance Management System   *");
        System.out.println("*                     SSIPMS Core v1.0.0                        *");
        System.out.println("*****************************************************************");
    }

    private static void printMenu() {
        System.out.println("\n--- MAIN MENU ---");
        System.out.println("1. Register New Student");
        System.out.println("2. View All Registered Students");
        System.out.println("3. Search & View Student Academic Transcript");
        System.out.println("4. Record / Update Subject Marks");
        System.out.println("5. Edit Student Profile (Name / Email)");
        System.out.println("6. Delete Student Record");
        System.out.println("7. View Merit Rankings (Leaderboard)");
        System.out.println("8. View Class Analytics & Grade Distribution");
        System.out.println("9. Save & Exit");
        System.out.println("-----------------");
    }

    private static void handleRegisterStudent() {
        System.out.println("\n--- [1] Register New Student ---");
        try {
            System.out.print("Enter Student ID (e.g. STU101): ");
            String id = scanner.nextLine().trim();

            System.out.print("Enter Full Name: ");
            String name = scanner.nextLine().trim();

            System.out.print("Enter Email Address: ");
            String email = scanner.nextLine().trim();

            Student registered = studentService.registerStudent(id, name, email);
            System.out.println("\n[✓] Success: Student registered successfully -> " + registered.getName() + " (" + registered.getStudentId() + ")");
        } catch (InvalidDataException e) {
            System.out.println("\n[✕] Validation Error: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("\n[✕] Unexpected Error: " + e.getMessage());
        }
    }

    private static void handleViewAllStudents() {
        System.out.println(ReportGenerator.generateClassSummaryReport(studentService));
    }

    private static void handleSearchStudent() {
        System.out.println("\n--- [3] Search Student Transcript ---");
        System.out.print("Enter Student ID: ");
        String id = scanner.nextLine().trim();

        try {
            Student student = studentService.getStudentById(id);
            System.out.println(ReportGenerator.generateStudentTranscript(student));
        } catch (StudentNotFoundException e) {
            System.out.println("\n[✕] " + e.getMessage());
        }
    }

    private static void handleRecordMarks() {
        System.out.println("\n--- [4] Record / Update Subject Marks ---");
        System.out.print("Enter Student ID: ");
        String id = scanner.nextLine().trim();

        try {
            Student student = studentService.getStudentById(id);
            System.out.println("Updating marks for: " + student.getName() + " (" + student.getStudentId() + ")");
            
            System.out.print("Enter Subject Name (e.g. Mathematics, Physics): ");
            String subject = scanner.nextLine().trim();

            System.out.print("Enter Score (0.0 to 100.0): ");
            String scoreInput = scanner.nextLine().trim();
            double score = Double.parseDouble(scoreInput);

            studentService.recordSubjectScore(id, subject, score);
            System.out.println("\n[✓] Score updated! Updated GPA for " + student.getName() + " is now " + String.format("%.2f", student.getGpa()));
        } catch (NumberFormatException e) {
            System.out.println("\n[✕] Input Error: Marks must be a valid numerical value.");
        } catch (StudentNotFoundException | InvalidDataException e) {
            System.out.println("\n[✕] " + e.getMessage());
        }
    }

    private static void handleUpdateStudent() {
        System.out.println("\n--- [5] Edit Student Profile ---");
        System.out.print("Enter Student ID: ");
        String id = scanner.nextLine().trim();

        try {
            Student student = studentService.getStudentById(id);
            System.out.println("Current Profile: " + student.getName() + " | " + student.getEmail());

            System.out.print("Enter New Name (leave blank to keep current): ");
            String newName = scanner.nextLine().trim();

            System.out.print("Enter New Email (leave blank to keep current): ");
            String newEmail = scanner.nextLine().trim();

            studentService.updateStudentProfile(id, newName.isEmpty() ? null : newName, newEmail.isEmpty() ? null : newEmail);
            System.out.println("\n[✓] Student profile updated successfully.");
        } catch (StudentNotFoundException | InvalidDataException e) {
            System.out.println("\n[✕] " + e.getMessage());
        }
    }

    private static void handleDeleteStudent() {
        System.out.println("\n--- [6] Delete Student Record ---");
        System.out.print("Enter Student ID to delete: ");
        String id = scanner.nextLine().trim();

        try {
            Student student = studentService.getStudentById(id);
            System.out.print("Are you sure you want to permanently delete " + student.getName() + " (" + student.getStudentId() + ")? (y/n): ");
            String confirm = scanner.nextLine().trim().toLowerCase();

            if ("y".equals(confirm) || "yes".equals(confirm)) {
                studentService.deleteStudent(id);
                System.out.println("\n[✓] Student record permanently deleted.");
            } else {
                System.out.println("\n[-] Deletion cancelled.");
            }
        } catch (StudentNotFoundException e) {
            System.out.println("\n[✕] " + e.getMessage());
        }
    }

    private static void handleViewRankings() {
        System.out.print("\nEnter number of top rankers to view (e.g. 5, 10, or 0 for all): ");
        String input = scanner.nextLine().trim();
        int limit = 10;
        try {
            if (!input.isEmpty()) {
                int parsed = Integer.parseInt(input);
                if (parsed > 0) limit = parsed;
                else if (parsed == 0) limit = Integer.MAX_VALUE;
            }
        } catch (NumberFormatException ignored) {}

        System.out.println(ReportGenerator.generateMeritRankReport(studentService, limit));
    }

    private static void handleViewAnalytics() {
        System.out.println(ReportGenerator.generateClassSummaryReport(studentService));
        System.out.println(ReportGenerator.generateGradeDistribution(studentService));
    }
}
