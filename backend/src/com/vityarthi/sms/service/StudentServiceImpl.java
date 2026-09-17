package com.vityarthi.sms.service;

import com.vityarthi.sms.exception.InvalidDataException;
import com.vityarthi.sms.exception.StudentNotFoundException;
import com.vityarthi.sms.model.Grade;
import com.vityarthi.sms.model.Student;
import com.vityarthi.sms.repository.FileDataRepository;
import com.vityarthi.sms.util.InputValidator;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Production-ready implementation of StudentService managing thread-safe in-memory cache,
 * strict validation, GPA recalculations, class analytics, and repository persistence.
 */
public class StudentServiceImpl implements StudentService {

    private final Map<String, Student> studentCache;
    private final FileDataRepository repository;

    public StudentServiceImpl() {
        this(new FileDataRepository());
    }

    public StudentServiceImpl(FileDataRepository repository) {
        this.repository = Objects.requireNonNull(repository, "repository cannot be null");
        this.studentCache = new ConcurrentHashMap<>();
        loadFromRepository();
    }

    private void loadFromRepository() {
        List<Student> loaded = repository.loadAll();
        for (Student s : loaded) {
            studentCache.put(s.getStudentId().toUpperCase(), s);
        }
    }

    @Override
    public Student registerStudent(String studentId, String name, String email) throws InvalidDataException {
        InputValidator.validateStudentId(studentId);
        InputValidator.validateName(name);
        InputValidator.validateEmail(email);

        String normalizedId = studentId.trim().toUpperCase();
        if (studentCache.containsKey(normalizedId)) {
            throw new InvalidDataException("A student with ID '" + studentId + "' is already registered.");
        }

        Student student = new Student(studentId.trim(), name.trim(), email.trim());
        studentCache.put(normalizedId, student);
        persistChanges();
        return student;
    }

    @Override
    public Student getStudentById(String studentId) throws StudentNotFoundException {
        if (studentId == null) {
            throw new StudentNotFoundException("Student ID cannot be null.");
        }
        Student student = studentCache.get(studentId.trim().toUpperCase());
        if (student == null) {
            throw new StudentNotFoundException("Student with ID '" + studentId + "' was not found.");
        }
        return student;
    }

    @Override
    public Optional<Student> findStudentById(String studentId) {
        if (studentId == null) return Optional.empty();
        return Optional.ofNullable(studentCache.get(studentId.trim().toUpperCase()));
    }

    @Override
    public List<Student> getAllStudents() {
        return new ArrayList<>(studentCache.values());
    }

    @Override
    public Student updateStudentProfile(String studentId, String newName, String newEmail)
            throws StudentNotFoundException, InvalidDataException {
        Student student = getStudentById(studentId);

        if (newName != null && !newName.trim().isEmpty()) {
            InputValidator.validateName(newName);
            student.setName(newName.trim());
        }

        if (newEmail != null && !newEmail.trim().isEmpty()) {
            InputValidator.validateEmail(newEmail);
            student.setEmail(newEmail.trim());
        }

        persistChanges();
        return student;
    }

    @Override
    public Student recordSubjectScore(String studentId, String subject, double score)
            throws StudentNotFoundException, InvalidDataException {
        InputValidator.validateSubject(subject);
        InputValidator.validateScore(score);

        Student student = getStudentById(studentId);
        student.addOrUpdateMark(subject.trim(), score);
        
        double newGpa = calculateGpa(student);
        student.setGpa(newGpa);

        persistChanges();
        return student;
    }

    @Override
    public Student removeSubjectScore(String studentId, String subject) throws StudentNotFoundException {
        Student student = getStudentById(studentId);
        student.removeMark(subject);

        double newGpa = calculateGpa(student);
        student.setGpa(newGpa);

        persistChanges();
        return student;
    }

    @Override
    public double calculateGpa(Student student) {
        if (student == null || student.getSubjectMarks().isEmpty()) {
            return 0.0;
        }

        Map<String, Double> marks = student.getSubjectMarks();
        double totalGradePoints = 0.0;

        for (double score : marks.values()) {
            Grade grade = Grade.fromPercentage(score);
            totalGradePoints += grade.getGradePoint();
        }

        double calculatedGpa = totalGradePoints / marks.size();
        return Math.round(calculatedGpa * 100.0) / 100.0; // 2 decimal precision
    }

    @Override
    public void recalculateAllGpas() {
        for (Student s : studentCache.values()) {
            s.setGpa(calculateGpa(s));
        }
        persistChanges();
    }

    @Override
    public boolean deleteStudent(String studentId) throws StudentNotFoundException {
        if (studentId == null) {
            throw new StudentNotFoundException("Student ID cannot be null.");
        }
        String normalizedId = studentId.trim().toUpperCase();
        if (!studentCache.containsKey(normalizedId)) {
            throw new StudentNotFoundException("Student with ID '" + studentId + "' was not found.");
        }
        studentCache.remove(normalizedId);
        persistChanges();
        return true;
    }

    @Override
    public List<Student> getRankedStudents() {
        return studentCache.values().stream()
                .sorted(Comparator.comparingDouble(Student::getGpa).reversed()
                        .thenComparing(Comparator.comparingDouble(Student::getAveragePercentage).reversed())
                        .thenComparing(Student::getName))
                .collect(Collectors.toList());
    }

    @Override
    public List<Student> getTopStudents(int limit) {
        if (limit <= 0) return Collections.emptyList();
        return getRankedStudents().stream().limit(limit).collect(Collectors.toList());
    }

    @Override
    public double getClassAverageGpa() {
        if (studentCache.isEmpty()) return 0.0;
        double sum = studentCache.values().stream().mapToDouble(Student::getGpa).sum();
        return Math.round((sum / studentCache.size()) * 100.0) / 100.0;
    }

    @Override
    public double getClassAveragePercentage() {
        if (studentCache.isEmpty()) return 0.0;
        double sum = studentCache.values().stream().mapToDouble(Student::getAveragePercentage).sum();
        return Math.round((sum / studentCache.size()) * 100.0) / 100.0;
    }

    @Override
    public void persistChanges() {
        repository.saveAll(studentCache.values());
    }
}
