package com.vityarthi.sms;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.vityarthi.sms.exception.InvalidDataException;
import com.vityarthi.sms.exception.StudentNotFoundException;
import com.vityarthi.sms.model.Grade;
import com.vityarthi.sms.model.Student;
import com.vityarthi.sms.service.StudentService;
import com.vityarthi.sms.service.StudentServiceImpl;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.Executors;

/**
 * Built-in Lightweight HTTP Server providing REST APIs and static file delivery
 * on Port 8080 without external server dependencies.
 */
public class WebServer {

    private static final int PORT = 8080;
    private static StudentService studentService;

    public static void main(String[] args) throws IOException {
        studentService = new StudentServiceImpl();

        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.setExecutor(Executors.newVirtualThreadPerTaskExecutor() != null ? 
                Executors.newVirtualThreadPerTaskExecutor() : Executors.newCachedThreadPool());

        // API Endpoints
        server.createContext("/api/students/marks", new MarksHandler());
        server.createContext("/api/students", new StudentsHandler());
        server.createContext("/api/analytics", new AnalyticsHandler());

        // Static File Endpoint (Frontend SPA)
        server.createContext("/", new StaticFileHandler());

        server.start();
        System.out.println("=================================================================");
        System.out.println("🚀 SSIPMS Full-Stack Web Server started successfully!");
        System.out.println("👉 Dashboard UI : http://localhost:" + PORT + "/");
        System.out.println("👉 API Endpoint : http://localhost:" + PORT + "/api/students");
        System.out.println("=================================================================");
    }

    // --- HANDLERS ---

    static class StudentsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);

            String method = exchange.getRequestMethod().toUpperCase();
            if ("OPTIONS".equals(method)) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            try {
                if ("GET".equals(method)) {
                    List<Student> students = studentService.getRankedStudents();
                    StringBuilder json = new StringBuilder("[");
                    for (int i = 0; i < students.size(); i++) {
                        if (i > 0) json.append(",");
                        json.append(studentToJson(students.get(i), i + 1));
                    }
                    json.append("]");
                    sendJsonResponse(exchange, 200, json.toString());

                } else if ("POST".equals(method)) {
                    String body = readRequestBody(exchange);
                    Map<String, String> data = parseSimpleJson(body);

                    String id = data.get("studentId");
                    String name = data.get("name");
                    String email = data.get("email");

                    Student registered = studentService.registerStudent(id, name, email);
                    sendJsonResponse(exchange, 201, studentToJson(registered, 0));

                } else if ("DELETE".equals(method)) {
                    String query = exchange.getRequestURI().getQuery();
                    String studentId = null;
                    if (query != null && query.contains("id=")) {
                        studentId = query.split("id=")[1].split("&")[0];
                        studentId = URLDecoder.decode(studentId, StandardCharsets.UTF_8);
                    } else {
                        String body = readRequestBody(exchange);
                        Map<String, String> data = parseSimpleJson(body);
                        studentId = data.get("studentId");
                    }

                    if (studentId == null || studentId.trim().isEmpty()) {
                        sendErrorResponse(exchange, 400, "Missing required query parameter: id");
                        return;
                    }

                    studentService.deleteStudent(studentId);
                    sendJsonResponse(exchange, 200, "{\"success\":true,\"message\":\"Student deleted successfully\"}");

                } else {
                    sendErrorResponse(exchange, 405, "Method Not Allowed");
                }
            } catch (InvalidDataException e) {
                sendErrorResponse(exchange, 400, e.getMessage());
            } catch (StudentNotFoundException e) {
                sendErrorResponse(exchange, 404, e.getMessage());
            } catch (Exception e) {
                sendErrorResponse(exchange, 500, "Internal Server Error: " + e.getMessage());
            }
        }
    }

    static class MarksHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);

            String method = exchange.getRequestMethod().toUpperCase();
            if ("OPTIONS".equals(method)) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if (!"POST".equals(method)) {
                sendErrorResponse(exchange, 405, "Method Not Allowed");
                return;
            }

            try {
                String body = readRequestBody(exchange);
                Map<String, String> data = parseSimpleJson(body);

                String studentId = data.get("studentId");
                String subject = data.get("subject");
                String scoreStr = data.get("score");

                if (studentId == null || subject == null || scoreStr == null) {
                    sendErrorResponse(exchange, 400, "Missing required fields: studentId, subject, or score");
                    return;
                }

                double score = Double.parseDouble(scoreStr);
                Student updated = studentService.recordSubjectScore(studentId, subject, score);
                sendJsonResponse(exchange, 200, studentToJson(updated, 0));

            } catch (NumberFormatException e) {
                sendErrorResponse(exchange, 400, "Score must be a valid numerical value");
            } catch (InvalidDataException e) {
                sendErrorResponse(exchange, 400, e.getMessage());
            } catch (StudentNotFoundException e) {
                sendErrorResponse(exchange, 404, e.getMessage());
            } catch (Exception e) {
                sendErrorResponse(exchange, 500, "Internal Server Error: " + e.getMessage());
            }
        }
    }

    static class AnalyticsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);

            if ("OPTIONS".equals(exchange.getRequestMethod().toUpperCase())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            List<Student> all = studentService.getAllStudents();
            List<Student> ranked = studentService.getRankedStudents();
            String topRankerName = ranked.isEmpty() ? "None" : ranked.get(0).getName();
            double topGpa = ranked.isEmpty() ? 0.0 : ranked.get(0).getGpa();

            Map<Grade, Integer> dist = new EnumMap<>(Grade.class);
            for (Grade g : Grade.values()) dist.put(g, 0);
            for (Student s : all) {
                Grade g = s.getOverallGrade();
                dist.put(g, dist.get(g) + 1);
            }

            StringBuilder sb = new StringBuilder();
            sb.append("{");
            sb.append("\"totalStudents\":").append(all.size()).append(",");
            sb.append("\"classAverageGpa\":").append(studentService.getClassAverageGpa()).append(",");
            sb.append("\"classAveragePercentage\":").append(studentService.getClassAveragePercentage()).append(",");
            sb.append("\"topRanker\":\"").append(escapeJson(topRankerName)).append("\",");
            sb.append("\"topGpa\":").append(topGpa).append(",");
            sb.append("\"gradeDistribution\":{");
            sb.append("\"A\":").append(dist.get(Grade.A)).append(",");
            sb.append("\"B\":").append(dist.get(Grade.B)).append(",");
            sb.append("\"C\":").append(dist.get(Grade.C)).append(",");
            sb.append("\"D\":").append(dist.get(Grade.D)).append(",");
            sb.append("\"F\":").append(dist.get(Grade.F));
            sb.append("}}");

            sendJsonResponse(exchange, 200, sb.toString());
        }
    }

    static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (path.equals("/") || path.isEmpty()) {
                path = "/index.html";
            }

            Path file = resolveFrontendFile(path);
            if (!Files.exists(file) || Files.isDirectory(file)) {
                // Fallback to index.html for SPA routing
                file = resolveFrontendFile("/index.html");
            }

            if (!Files.exists(file)) {
                sendErrorResponse(exchange, 404, "Frontend file not found");
                return;
            }

            String contentType = getContentType(file.toString());
            byte[] bytes = Files.readAllBytes(file);

            Headers headers = exchange.getResponseHeaders();
            headers.set("Content-Type", contentType);
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }

        private Path resolveFrontendFile(String relativePath) {
            if (relativePath.startsWith("/")) relativePath = relativePath.substring(1);
            Path p1 = Paths.get("frontend", relativePath);
            if (Files.exists(p1)) return p1.toAbsolutePath().normalize();

            Path p2 = Paths.get("../frontend", relativePath);
            if (Files.exists(p2)) return p2.toAbsolutePath().normalize();

            return p1.toAbsolutePath().normalize();
        }

        private String getContentType(String path) {
            if (path.endsWith(".html")) return "text/html; charset=UTF-8";
            if (path.endsWith(".css")) return "text/css; charset=UTF-8";
            if (path.endsWith(".js")) return "application/javascript; charset=UTF-8";
            if (path.endsWith(".json")) return "application/json; charset=UTF-8";
            if (path.endsWith(".svg")) return "image/svg+xml";
            if (path.endsWith(".png")) return "image/png";
            return "application/octet-stream";
        }
    }

    // --- UTILITIES ---

    private static void addCorsHeaders(HttpExchange exchange) {
        Headers headers = exchange.getResponseHeaders();
        headers.set("Access-Control-Allow-Origin", "*");
        headers.set("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS");
        headers.set("Access-Control-Allow-Headers", "Content-Type, Authorization");
    }

    private static void sendJsonResponse(HttpExchange exchange, int statusCode, String json) throws IOException {
        addCorsHeaders(exchange);
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static void sendErrorResponse(HttpExchange exchange, int statusCode, String message) throws IOException {
        String json = "{\"error\":\"" + escapeJson(message) + "\"}";
        sendJsonResponse(exchange, statusCode, json);
    }

    private static String readRequestBody(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = is.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }
            return baos.toString(StandardCharsets.UTF_8);
        }
    }

    private static String studentToJson(Student s, int rank) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"studentId\":\"").append(escapeJson(s.getStudentId())).append("\",");
        sb.append("\"name\":\"").append(escapeJson(s.getName())).append("\",");
        sb.append("\"email\":\"").append(escapeJson(s.getEmail())).append("\",");
        sb.append("\"gpa\":").append(String.format(Locale.US, "%.2f", s.getGpa())).append(",");
        sb.append("\"grade\":\"").append(s.getOverallGrade().name()).append("\",");
        sb.append("\"averagePercentage\":").append(String.format(Locale.US, "%.2f", s.getAveragePercentage())).append(",");
        if (rank > 0) {
            sb.append("\"rank\":").append(rank).append(",");
        }
        sb.append("\"subjectMarks\":{");
        int count = 0;
        for (Map.Entry<String, Double> entry : s.getSubjectMarks().entrySet()) {
            if (count > 0) sb.append(",");
            sb.append("\"").append(escapeJson(entry.getKey())).append("\":").append(entry.getValue());
            count++;
        }
        sb.append("}}");
        return sb.toString();
    }

    private static Map<String, String> parseSimpleJson(String json) {
        Map<String, String> map = new LinkedHashMap<>();
        if (json == null) return map;
        String clean = json.trim();
        if (clean.startsWith("{")) clean = clean.substring(1);
        if (clean.endsWith("}")) clean = clean.substring(0, clean.length() - 1);

        String[] pairs = clean.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
        for (String pair : pairs) {
            String[] kv = pair.split(":(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
            if (kv.length == 2) {
                String key = kv[0].trim().replace("\"", "");
                String value = kv[1].trim().replace("\"", "");
                map.put(key, value);
            }
        }
        return map;
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }
}
