# Project Statement & Problem Specification

## 1. Problem Statement
Academic institutions require lightweight, reliable, decoupled, and intuitive software systems to record student profiles, track continuous multi-subject evaluations, compute Grade Point Averages (GPA), and generate actionable institutional analytics without introducing bloated third-party dependencies, complex runtime servers, or proprietary database configurations.

## 2. Project Scope
The **Smart Student Information & Performance Management System (SSIPMS)** delivers:
- **Zero-Dependency Backend**: Core Java (JDK 17+) implementation utilizing built-in concurrency and lightweight `com.sun.net.httpserver.HttpServer`.
- **Full-Stack REST Architecture**: Integrated JSON REST endpoints coupled with a responsive single-page dashboard.
- **Academic Scorecard Engine**: Multi-course score recording with automated 4.0-scale GPA recalculation.
- **Visual Analytics**: Interactive grade distributions and institutional rosters rendered via Chart.js and Tailwind CSS.
- **Resilient File Persistence**: Thread-safe synchronization and recovery with central CSV storage (`data/students.csv`).

## 3. Target Audience
- University/College Academic Registrars & Department Coordinators.
- Course Instructors evaluating semester scorecards.
- Computer Science / Engineering students studying layered full-stack architectures built with Pure Core Java.

## 4. High-Level Features
1. **Full-Stack Web Interface**: Single-page dashboard supporting live filtering, student registration modals, score logging modals, and formatted academic transcript dialogs.
2. **Dual Execution Modes**: Interactive Command Line Interface (`Main.java`) and Web Server Mode (`WebServer.java`).
3. **Validation Subsystem**: Strict regular expression checks preventing corrupted entries.
4. **Evaluation & GPA Engine**: Maps numerical scores (0-100) to standard letter grades (A, B, C, D, F) and grade points (4.0 Scale).
5. **Analytics Engine**: Computes class-wide averages, median performance, and generates graphical grade distribution bar charts.
