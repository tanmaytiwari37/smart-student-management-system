package com.vityarthi.sms.repository;

import com.vityarthi.sms.model.Student;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Repository implementation for persisting and loading student records from central CSV storage.
 * Resolves path reliably whether executed from the project root or backend directory.
 */
public class FileDataRepository {
    private final Path filePath;

    public FileDataRepository() {
        this.filePath = resolveDataFilePath();
        ensureFileExists();
    }

    public FileDataRepository(String customPath) {
        this.filePath = Paths.get(customPath);
        ensureFileExists();
    }

    private static Path resolveDataFilePath() {
        Path direct = Paths.get("data/students.csv");
        if (Files.exists(direct) || Files.exists(Paths.get("data"))) {
            return direct.toAbsolutePath().normalize();
        }
        Path parentData = Paths.get("../data/students.csv");
        if (Files.exists(parentData) || Files.exists(Paths.get("../data"))) {
            return parentData.toAbsolutePath().normalize();
        }
        return direct.toAbsolutePath().normalize();
    }

    /**
     * Ensures parent directories and the target CSV file exist.
     */
    private void ensureFileExists() {
        try {
            if (filePath.getParent() != null) {
                Files.createDirectories(filePath.getParent());
            }
            if (!Files.exists(filePath)) {
                Files.createFile(filePath);
                writeHeader();
            } else if (Files.size(filePath) == 0) {
                writeHeader();
            }
        } catch (IOException e) {
            System.err.println("Warning: Unable to initialize data file at " + filePath + ": " + e.getMessage());
        }
    }

    private void writeHeader() {
        try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8)) {
            writer.write("studentId,name,email,subjectMarks,gpa");
            writer.newLine();
        } catch (IOException e) {
            System.err.println("Error writing CSV header: " + e.getMessage());
        }
    }

    /**
     * Reads all students from the persistent CSV file.
     */
    public List<Student> loadAll() {
        ensureFileExists();
        List<Student> students = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
            String line = reader.readLine(); // Header
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                Student s = parseCsvLine(line);
                if (s != null) {
                    students.add(s);
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading student records from " + filePath + ": " + e.getMessage());
        }
        return students;
    }

    /**
     * Saves the entire collection of students to the CSV file.
     */
    public synchronized void saveAll(Collection<Student> students) {
        ensureFileExists();
        try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8)) {
            writer.write("studentId,name,email,subjectMarks,gpa");
            writer.newLine();
            for (Student student : students) {
                writer.write(formatCsvLine(student));
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving student records to " + filePath + ": " + e.getMessage());
        }
    }

    private Student parseCsvLine(String line) {
        try {
            String[] parts = line.split(",", -1);
            if (parts.length < 5) return null;

            String studentId = parts[0].trim();
            String name = parts[1].trim();
            String email = parts[2].trim();
            String marksRaw = parts[3].trim();
            double gpa = Double.parseDouble(parts[4].trim());

            Map<String, Double> marksMap = new LinkedHashMap<>();
            if (!marksRaw.isEmpty()) {
                String[] subjectPairs = marksRaw.split(";");
                for (String pair : subjectPairs) {
                    String[] kv = pair.split(":");
                    if (kv.length == 2) {
                        marksMap.put(kv[0].trim(), Double.parseDouble(kv[1].trim()));
                    }
                }
            }

            return new Student(studentId, name, email, marksMap, gpa);
        } catch (Exception e) {
            System.err.println("Skipping malformed CSV record: " + line);
            return null;
        }
    }

    private String formatCsvLine(Student s) {
        StringBuilder marksSb = new StringBuilder();
        Map<String, Double> marks = s.getSubjectMarks();
        int count = 0;
        for (Map.Entry<String, Double> entry : marks.entrySet()) {
            if (count > 0) marksSb.append(";");
            marksSb.append(entry.getKey()).append(":").append(entry.getValue());
            count++;
        }

        return String.format("%s,%s,%s,%s,%.2f",
                s.getStudentId(),
                escapeCsv(s.getName()),
                escapeCsv(s.getEmail()),
                marksSb.toString(),
                s.getGpa()
        );
    }

    private String escapeCsv(String val) {
        if (val == null) return "";
        return val.replace(",", " ");
    }
}
