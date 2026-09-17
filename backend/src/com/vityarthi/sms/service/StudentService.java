package com.vityarthi.sms.service;

import com.vityarthi.sms.exception.InvalidDataException;
import com.vityarthi.sms.exception.StudentNotFoundException;
import com.vityarthi.sms.model.Student;

import java.util.List;
import java.util.Optional;

/**
 * Service contract defining core CRUD operations, academic evaluations,
 * sorting, ranking, and statistical calculations.
 */
public interface StudentService {

    /**
     * Registers a new student into the system after strict validation.
     */
    Student registerStudent(String studentId, String name, String email) throws InvalidDataException;

    /**
     * Retrieves a student by unique student identifier.
     */
    Student getStudentById(String studentId) throws StudentNotFoundException;

    /**
     * Finds an optional student by unique student identifier without throwing.
     */
    Optional<Student> findStudentById(String studentId);

    /**
     * Returns an unmodifiable list of all registered students.
     */
    List<Student> getAllStudents();

    /**
     * Updates profile attributes of an existing student.
     */
    Student updateStudentProfile(String studentId, String newName, String newEmail) 
            throws StudentNotFoundException, InvalidDataException;

    /**
     * Records or updates a subject score for a student and recalculates GPA.
     */
    Student recordSubjectScore(String studentId, String subject, double score) 
            throws StudentNotFoundException, InvalidDataException;

    /**
     * Removes a subject score from a student's profile and updates GPA.
     */
    Student removeSubjectScore(String studentId, String subject) 
            throws StudentNotFoundException;

    /**
     * Calculates the GPA on a 4.0 scale based on 0-100 mark averages.
     */
    double calculateGpa(Student student);

    /**
     * Recalculates and persists GPA across all registered students.
     */
    void recalculateAllGpas();

    /**
     * Permanently deletes a student record from the system.
     */
    boolean deleteStudent(String studentId) throws StudentNotFoundException;

    /**
     * Returns students ranked in descending order of GPA / Performance.
     */
    List<Student> getRankedStudents();

    /**
     * Returns top N students by academic rank.
     */
    List<Student> getTopStudents(int limit);

    /**
     * Calculates the overall class average GPA.
     */
    double getClassAverageGpa();

    /**
     * Calculates overall class average percentage.
     */
    double getClassAveragePercentage();

    /**
     * Persists in-memory records to file store.
     */
    void persistChanges();
}
