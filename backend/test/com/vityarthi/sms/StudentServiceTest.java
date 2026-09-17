package com.vityarthi.sms;

import com.vityarthi.sms.exception.InvalidDataException;
import com.vityarthi.sms.exception.StudentNotFoundException;
import com.vityarthi.sms.model.Grade;
import com.vityarthi.sms.model.Student;
import com.vityarthi.sms.repository.FileDataRepository;
import com.vityarthi.sms.service.StudentService;
import com.vityarthi.sms.service.StudentServiceImpl;
import com.vityarthi.sms.util.InputValidator;

import java.io.File;
import java.util.List;

/**
 * Basic standalone unit test suite validating core business logic,
 * GPA calculations, validation constraints, and ranking algorithms.
 */
public class StudentServiceTest {

    private static int testsPassed = 0;
    private static int testsFailed = 0;

    public static void main(String[] args) {
        System.out.println("\n--- RUNNING SSIPMS TEST SUITE ---");

        testStudentRegistrationAndLookup();
        testInvalidDataValidations();
        testGpaCalculationMath();
        testAcademicRankingOrder();
        testGradeEnumMapping();

        System.out.println("\n--------------------------------");
        System.out.printf("Test Results: %d Passed, %d Failed\n", testsPassed, testsFailed);
        System.out.println("--------------------------------\n");

        if (testsFailed > 0) {
            System.exit(1);
        }
    }

    private static void testStudentRegistrationAndLookup() {
        try {
            String testDb = "data/test_students_" + System.currentTimeMillis() + ".csv";
            StudentService service = new StudentServiceImpl(new FileDataRepository(testDb));

            Student student = service.registerStudent("TEST001", "Alice Smith", "alice.smith@university.edu");
            assertEquals("TEST001", student.getStudentId(), "Student ID match");
            assertEquals("Alice Smith", student.getName(), "Name match");

            Student retrieved = service.getStudentById("TEST001");
            assertEquals(student, retrieved, "Lookup match");

            new File(testDb).deleteOnExit();
            pass("testStudentRegistrationAndLookup");
        } catch (Exception e) {
            fail("testStudentRegistrationAndLookup", e);
        }
    }

    private static void testInvalidDataValidations() {
        try {
            boolean caughtInvalidEmail = false;
            try {
                InputValidator.validateEmail("invalid-email-address");
            } catch (InvalidDataException e) {
                caughtInvalidEmail = true;
            }
            assertTrue(caughtInvalidEmail, "Should reject invalid email");

            boolean caughtInvalidScore = false;
            try {
                InputValidator.validateScore(105.0);
            } catch (InvalidDataException e) {
                caughtInvalidScore = true;
            }
            assertTrue(caughtInvalidScore, "Should reject score > 100");

            boolean caughtNegativeScore = false;
            try {
                InputValidator.validateScore(-5.0);
            } catch (InvalidDataException e) {
                caughtNegativeScore = true;
            }
            assertTrue(caughtNegativeScore, "Should reject score < 0");

            pass("testInvalidDataValidations");
        } catch (Exception e) {
            fail("testInvalidDataValidations", e);
        }
    }

    private static void testGpaCalculationMath() {
        try {
            String testDb = "data/test_gpa_" + System.currentTimeMillis() + ".csv";
            StudentService service = new StudentServiceImpl(new FileDataRepository(testDb));

            Student student = service.registerStudent("STU200", "Bob Jones", "bob@college.edu");
            service.recordSubjectScore("STU200", "Data Structures", 95.0);
            service.recordSubjectScore("STU200", "Algorithms", 85.0);

            Student updated = service.getStudentById("STU200");
            assertEquals(3.50, updated.getGpa(), "Calculated GPA match");
            assertEquals(Grade.A, updated.getOverallGrade(), "Grade category match");

            new File(testDb).deleteOnExit();
            pass("testGpaCalculationMath");
        } catch (Exception e) {
            fail("testGpaCalculationMath", e);
        }
    }

    private static void testAcademicRankingOrder() {
        try {
            String testDb = "data/test_rank_" + System.currentTimeMillis() + ".csv";
            StudentService service = new StudentServiceImpl(new FileDataRepository(testDb));

            service.registerStudent("STU1", "Average Student", "avg@college.edu");
            service.registerStudent("STU2", "Top Performer", "top@college.edu");

            service.recordSubjectScore("STU1", "Math", 75.0);
            service.recordSubjectScore("STU2", "Math", 95.0);

            List<Student> ranked = service.getRankedStudents();
            assertEquals("STU2", ranked.get(0).getStudentId(), "Top ranker must be STU2");
            assertEquals("STU1", ranked.get(1).getStudentId(), "Second ranker must be STU1");

            new File(testDb).deleteOnExit();
            pass("testAcademicRankingOrder");
        } catch (Exception e) {
            fail("testAcademicRankingOrder", e);
        }
    }

    private static void testGradeEnumMapping() {
        try {
            assertEquals(Grade.A, Grade.fromPercentage(95.0), "95% is Grade A");
            assertEquals(Grade.B, Grade.fromPercentage(82.5), "82.5% is Grade B");
            assertEquals(Grade.C, Grade.fromPercentage(74.0), "74% is Grade C");
            assertEquals(Grade.D, Grade.fromPercentage(61.0), "61% is Grade D");
            assertEquals(Grade.F, Grade.fromPercentage(45.0), "45% is Grade F");

            pass("testGradeEnumMapping");
        } catch (Exception e) {
            fail("testGradeEnumMapping", e);
        }
    }

    private static void pass(String testName) {
        System.out.printf("  [PASS] %s\n", testName);
        testsPassed++;
    }

    private static void fail(String testName, Throwable t) {
        System.out.printf("  [FAIL] %s - %s\n", testName, t.getMessage());
        testsFailed++;
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (expected == null && actual == null) return;
        if (expected != null && expected.equals(actual)) return;
        throw new AssertionError(String.format("%s. Expected: %s, Actual: %s", message, expected, actual));
    }

    private static void assertEquals(double expected, double actual, String message) {
        if (Math.abs(expected - actual) > 0.01) {
            throw new AssertionError(String.format("%s. Expected: %.2f, Actual: %.2f", message, expected, actual));
        }
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError("Assertion failed: " + message);
        }
    }
}
