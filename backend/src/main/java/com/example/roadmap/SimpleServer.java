package com.example.roadmap;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.file.*;
import java.time.Instant;
import java.util.*;

public class SimpleServer {
    private static Path dataFilePath;
    private static Map<String, Object> roadmapsData;

    public static void main(String[] args) throws Exception {
        // Determine the data file path - it should be in frontend/DATOS
        String basePath = System.getenv("ROADMAP_BASE") != null ? 
            System.getenv("ROADMAP_BASE") : 
            new File(".").getAbsolutePath();
        
        // Try to find frontend/DATOS folder
        File frontendDir = new File(basePath).getParentFile();
        if (frontendDir != null && new File(frontendDir, "frontend").exists()) {
            dataFilePath = Paths.get(frontendDir.getAbsolutePath(), "frontend", "DATOS", "roadmap_angular_mvp.json");
        } else {
            // Fallback: use relative path from current directory
            dataFilePath = Paths.get(basePath, "../frontend/DATOS/roadmap_angular_mvp.json");
        }
        
        System.out.println("[SimpleServer] Data file path: " + dataFilePath.toAbsolutePath());
        
        // Load data from file
        loadData();
        
        HttpServer server = HttpServer.create(new InetSocketAddress(8081), 0);

        // POST /api/roadmaps - Create
        server.createContext("/api/roadmaps", exchange -> {
            if ("POST".equals(exchange.getRequestMethod())) {
                try {
                    String body = new String(exchange.getRequestBody().readAllBytes());
                    System.out.println("[SimpleServer] POST body: " + body);
                    
                    String title = extractField(body, "title");
                    String description = extractField(body, "description");
                    String producto = extractField(body, "producto");
                    String organizacion = extractField(body, "organizacion");
                    
                    if (title == null || title.isEmpty()) {
                        sendError(exchange, 400, "Title is required");
                        return;
                    }
                    
                    String id = UUID.randomUUID().toString();
                    Map<String, Object> roadmap = new LinkedHashMap<>();
                    roadmap.put("id", id);
                    roadmap.put("title", title);
                    roadmap.put("description", description != null ? description : "");
                    roadmap.put("producto", producto != null ? producto : "");
                    roadmap.put("organizacion", organizacion != null ? organizacion : "");
                    roadmap.put("createdAt", Instant.now().toString());
                    roadmap.put("horizonte_base", new LinkedHashMap<>() {{
                        put("inicio", "2026-T1");
                        put("fin", "2030-T4");
                    }});
                    roadmap.put("ejes_estrategicos", new ArrayList<>());
                    roadmap.put("iniciativas", new ArrayList<>());
                    
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> roadmaps = (List<Map<String, Object>>) roadmapsData.get("roadmaps");
                    roadmaps.add(roadmap);
                    saveData();
                    
                    exchange.getResponseHeaders().set("Content-Type", "application/json");
                    exchange.sendResponseHeaders(201, 0);
                    exchange.getResponseBody().write(toJson(roadmap).getBytes());
                    exchange.close();
                } catch (Exception e) {
                    e.printStackTrace();
                    sendError(exchange, 500, "Internal error");
                }
            }
            // GET /api/roadmaps - List all
            else if ("GET".equals(exchange.getRequestMethod())) {
                try {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> roadmaps = (List<Map<String, Object>>) roadmapsData.get("roadmaps");
                    StringBuilder json = new StringBuilder("[");
                    for (int i = 0; i < roadmaps.size(); i++) {
                        json.append(toJson(roadmaps.get(i)));
                        if (i < roadmaps.size() - 1) json.append(",");
                    }
                    json.append("]");
                    
                    exchange.getResponseHeaders().set("Content-Type", "application/json");
                    exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
                    exchange.sendResponseHeaders(200, 0);
                    exchange.getResponseBody().write(json.toString().getBytes());
                    exchange.close();
                } catch (Exception e) {
                    e.printStackTrace();
                    sendError(exchange, 500, "Internal error");
                }
            } else {
                sendError(exchange, 405, "Method not allowed");
            }
        });

        // GET/PUT /api/roadmaps/{id}
        server.createContext("/api/roadmaps/", exchange -> {
            String path = exchange.getRequestURI().getPath();
            if (path.startsWith("/api/roadmaps/")) {
                String id = path.substring("/api/roadmaps/".length());
                
                if ("GET".equals(exchange.getRequestMethod())) {
                    try {
                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> roadmaps = (List<Map<String, Object>>) roadmapsData.get("roadmaps");
                        Map<String, Object> roadmap = findRoadmapById(roadmaps, id);
                        
                        if (roadmap != null) {
                            exchange.getResponseHeaders().set("Content-Type", "application/json");
                            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
                            exchange.sendResponseHeaders(200, 0);
                            exchange.getResponseBody().write(toJson(roadmap).getBytes());
                        } else {
                            sendError(exchange, 404, "Not found");
                        }
                        exchange.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                        sendError(exchange, 500, "Internal error");
                    }
                } else if ("PUT".equals(exchange.getRequestMethod())) {
                    try {
                        String body = new String(exchange.getRequestBody().readAllBytes());
                        System.out.println("[SimpleServer] PUT body for " + id + ": " + body);
                        
                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> roadmaps = (List<Map<String, Object>>) roadmapsData.get("roadmaps");
                        Map<String, Object> roadmap = findRoadmapById(roadmaps, id);
                        
                        if (roadmap == null) {
                            sendError(exchange, 404, "Not found");
                            return;
                        }
                        
                        String title = extractField(body, "title");
                        if (title == null || title.isEmpty()) {
                            sendError(exchange, 400, "Title is required");
                            return;
                        }
                        
                        roadmap.put("title", title);
                        roadmap.put("description", extractField(body, "description") != null ? 
                            extractField(body, "description") : "");
                        
                        // Update ejes_estrategicos - parse as proper list
                        String ejesJson = extractJsonArray(body, "ejes_estrategicos");
                        if (!ejesJson.equals("[]")) {
                            @SuppressWarnings("unchecked")
                            List<Map<String, Object>> ejes = parseJsonArray(ejesJson);
                            roadmap.put("ejes_estrategicos", ejes);
                        }
                        
                        // Update iniciativas - parse as proper list
                        String initsJson = extractJsonArray(body, "iniciativas");
                        if (!initsJson.equals("[]")) {
                            @SuppressWarnings("unchecked")
                            List<Map<String, Object>> inits = parseJsonArray(initsJson);
                            roadmap.put("iniciativas", inits);
                        }
                        
                        saveData();
                        
                        exchange.getResponseHeaders().set("Content-Type", "application/json");
                        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
                        exchange.sendResponseHeaders(200, 0);
                        exchange.getResponseBody().write(toJson(roadmap).getBytes());
                        exchange.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                        sendError(exchange, 500, "Internal error");
                    }
                } else {
                    sendError(exchange, 405, "Method not allowed");
                }
            }
        });

        // Database connection endpoints
        server.createContext("/api/db/connect", exchange -> {
            if ("POST".equals(exchange.getRequestMethod())) {
                try {
                    String body = new String(exchange.getRequestBody().readAllBytes());
                    System.out.println("[SimpleServer] DB Connect request: " + body);
                    
                    String host = extractField(body, "host");
                    String port = extractField(body, "port");
                    String database = extractField(body, "database");
                    String username = extractField(body, "username");
                    String password = extractField(body, "password");
                    
                    if (host == null || host.isEmpty()) {
                        sendError(exchange, 400, "Host is required");
                        return;
                    }
                    
                    // Set connection parameters
                    MySQLConnection.setConnectionParams(
                        host != null ? host : "localhost",
                        port != null ? port : "3306",
                        database != null ? database : "roadmap_mvp",
                        username != null ? username : "root",
                        password != null ? password : ""
                    );
                    
                    // Try to connect
                    if (MySQLConnection.connect() != null) {
                        String response = "{\"status\":\"connected\",\"message\":\"Successfully connected to database\"}";
                        exchange.getResponseHeaders().set("Content-Type", "application/json");
                        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
                        exchange.sendResponseHeaders(200, 0);
                        exchange.getResponseBody().write(response.getBytes());
                    } else {
                        sendError(exchange, 500, "Failed to connect to database. Check credentials and server status.");
                    }
                    exchange.close();
                } catch (Exception e) {
                    e.printStackTrace();
                    sendError(exchange, 500, "Connection error: " + e.getMessage());
                }
            } else {
                sendError(exchange, 405, "Method not allowed");
            }
        });

        server.createContext("/api/db/status", exchange -> {
            if ("GET".equals(exchange.getRequestMethod())) {
                try {
                    boolean connected = MySQLConnection.isConnected();
                    String status = connected ? "connected" : "disconnected";
                    String response = "{\"status\":\"" + status + "\",\"connectionString\":\"" + 
                        (connected ? MySQLConnection.getConnectionInfo() : "not connected") + "\"}";
                    
                    exchange.getResponseHeaders().set("Content-Type", "application/json");
                    exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
                    exchange.sendResponseHeaders(200, 0);
                    exchange.getResponseBody().write(response.getBytes());
                    exchange.close();
                } catch (Exception e) {
                    sendError(exchange, 500, "Error checking status");
                }
            } else {
                sendError(exchange, 405, "Method not allowed");
            }
        });

        server.createContext("/api/db/disconnect", exchange -> {
            if ("POST".equals(exchange.getRequestMethod())) {
                try {
                    MySQLConnection.disconnect();
                    String response = "{\"status\":\"disconnected\",\"message\":\"Disconnected from database\"}";
                    
                    exchange.getResponseHeaders().set("Content-Type", "application/json");
                    exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
                    exchange.sendResponseHeaders(200, 0);
                    exchange.getResponseBody().write(response.getBytes());
                    exchange.close();
                } catch (Exception e) {
                    sendError(exchange, 500, "Disconnection error");
                }
            } else {
                sendError(exchange, 405, "Method not allowed");
            }
        });

        // CORS preflight
        server.createContext("/", exchange -> {
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
                exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, OPTIONS");
                exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
                exchange.sendResponseHeaders(204, -1);
                exchange.close();
            }
        });

        server.setExecutor(null);
        server.start();
        System.out.println("Backend running on http://localhost:8081");
        System.out.println("Data persisted to: " + dataFilePath.toAbsolutePath());
        System.out.println("API endpoints:");
        System.out.println("  POST   /api/roadmaps        (create)");
        System.out.println("  GET    /api/roadmaps        (list)");
        System.out.println("  GET    /api/roadmaps/{id}   (get)");
        System.out.println("  PUT    /api/roadmaps/{id}   (update)");
        System.out.println("  POST   /api/db/connect      (connect to MySQL)");
        System.out.println("  GET    /api/db/status       (check DB connection)");
        System.out.println("  POST   /api/db/disconnect   (disconnect from MySQL)");
    }

    static void loadData() {
        try {
            if (Files.exists(dataFilePath)) {
                String content = Files.readString(dataFilePath);
                // Use a simple JSON parser that doesn't lose data
                try {
                    roadmapsData = parseJsonMap(content);
                } catch (Exception e) {
                    System.err.println("Failed to parse JSON, using empty roadmaps: " + e.getMessage());
                    roadmapsData = new LinkedHashMap<>();
                    roadmapsData.put("roadmaps", new ArrayList<>());
                }
                System.out.println("[SimpleServer] Loaded " + 
                    ((List<?>) roadmapsData.get("roadmaps")).size() + " roadmaps");
            } else {
                roadmapsData = new LinkedHashMap<>();
                roadmapsData.put("roadmaps", new ArrayList<>());
                saveData();
                System.out.println("[SimpleServer] Created new data file: " + dataFilePath);
            }
        } catch (Exception e) {
            System.err.println("Error loading data: " + e.getMessage());
            roadmapsData = new LinkedHashMap<>();
            roadmapsData.put("roadmaps", new ArrayList<>());
        }
    }

    static void saveData() {
        try {
            Files.createDirectories(dataFilePath.getParent());
            String json = toJson(roadmapsData);
            Files.writeString(dataFilePath, json);
            System.out.println("[SimpleServer] Saved data to: " + dataFilePath);
        } catch (Exception e) {
            System.err.println("Error saving data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    static Map<String, Object> findRoadmapById(List<Map<String, Object>> roadmaps, String id) {
        for (Map<String, Object> r : roadmaps) {
            if (r.get("id").equals(id)) return r;
        }
        return null;
    }

    static String extractField(String json, String field) {
        String pattern = "\"" + field + "\":";
        int start = json.indexOf(pattern);
        if (start == -1) return null;
        start += pattern.length();
        
        while (start < json.length() && (json.charAt(start) == ' ' || json.charAt(start) == '"')) {
            start++;
        }
        
        int end = start;
        boolean escaped = false;
        while (end < json.length()) {
            char c = json.charAt(end);
            if (escaped) {
                escaped = false;
                end++;
            } else if (c == '\\') {
                escaped = true;
                end++;
            } else if (c == '"' || c == ',' || c == '}') {
                break;
            } else {
                end++;
            }
        }
        
        return end > start ? json.substring(start, end).trim() : null;
    }

    static String extractJsonArray(String json, String field) {
        String pattern = "\"" + field + "\":";
        int start = json.indexOf(pattern);
        if (start == -1) return "[]";
        start += pattern.length();
        
        while (start < json.length() && (json.charAt(start) == ' ' || json.charAt(start) == '\n')) {
            start++;
        }
        
        if (start >= json.length() || json.charAt(start) != '[') return "[]";
        
        int depth = 0, end = start;
        while (end < json.length()) {
            char c = json.charAt(end);
            if (c == '[') depth++;
            else if (c == ']') {
                depth--;
                if (depth == 0) return json.substring(start, end + 1);
            }
            end++;
        }
        return "[]";
    }

    static void sendError(HttpExchange exchange, int code, String msg) throws IOException {
        String json = "{\"error\":\"" + msg + "\"}";
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(code, 0);
        exchange.getResponseBody().write(json.getBytes());
        exchange.close();
    }

    // Simple JSON parser for deserializing
    static Map<String, Object> parseJson(String json) {
        Map<String, Object> result = new LinkedHashMap<>();
        // Very basic: just extract the "roadmaps" array
        int idx = json.indexOf("\"roadmaps\":");
        if (idx != -1) {
            int start = json.indexOf("[", idx);
            int depth = 0, end = start;
            while (end < json.length()) {
                char c = json.charAt(end);
                if (c == '[') depth++;
                else if (c == ']') {
                    depth--;
                    if (depth == 0) {
                        result.put("roadmaps", parseJsonArray(json.substring(start, end + 1)));
                        return result;
                    }
                }
                end++;
            }
        }
        result.put("roadmaps", new ArrayList<>());
        return result;
    }

    // Better JSON map parser that preserves structure
    static Map<String, Object> parseJsonMap(String json) {
        json = json.trim();
        if (!json.startsWith("{")) return new LinkedHashMap<>();
        
        Map<String, Object> result = new LinkedHashMap<>();
        int depth = 0, i = 1;
        boolean inString = false, escaped = false;
        int keyStart = -1, valueStart = -1;
        String currentKey = null;
        
        while (i < json.length() - 1) {
            char c = json.charAt(i);
            
            if (escaped) {
                escaped = false;
                i++;
                continue;
            }
            
            if (c == '\\' && inString) {
                escaped = true;
                i++;
                continue;
            }
            
            if (c == '"' && !escaped) {
                if (!inString) {
                    keyStart = i + 1;
                    inString = true;
                } else {
                    String potential = json.substring(keyStart, i);
                    if (json.length() > i + 1 && json.charAt(i + 1) == ':') {
                        currentKey = potential;
                    }
                    inString = false;
                }
                i++;
                continue;
            }
            
            if (!inString) {
                if (c == ':' && currentKey != null) {
                    valueStart = i + 1;
                    while (valueStart < json.length() && Character.isWhitespace(json.charAt(valueStart))) {
                        valueStart++;
                    }
                    
                    // Extract value
                    int valueEnd = valueStart;
                    if (json.charAt(valueStart) == '{' || json.charAt(valueStart) == '[') {
                        int d = 1;
                        valueEnd = valueStart + 1;
                        while (valueEnd < json.length() && d > 0) {
                            char vc = json.charAt(valueEnd);
                            if (vc == '{' || vc == '[') d++;
                            else if (vc == '}' || vc == ']') d--;
                            valueEnd++;
                        }
                    } else if (json.charAt(valueStart) == '"') {
                        valueEnd = valueStart + 1;
                        boolean vesc = false;
                        while (valueEnd < json.length()) {
                            if (vesc) vesc = false;
                            else if (json.charAt(valueEnd) == '\\') vesc = true;
                            else if (json.charAt(valueEnd) == '"') break;
                            valueEnd++;
                        }
                        valueEnd++;
                    } else {
                        while (valueEnd < json.length() && json.charAt(valueEnd) != ',' && json.charAt(valueEnd) != '}') {
                            valueEnd++;
                        }
                    }
                    
                    String valueStr = json.substring(valueStart, valueEnd).trim();
                    result.put(currentKey, parseValue(valueStr));
                    currentKey = null;
                }
            }
            
            i++;
        }
        
        return result;
    }
    
    static Object parseValue(String str) {
        str = str.trim();
        if (str.equals("null")) return null;
        if (str.equals("true")) return true;
        if (str.equals("false")) return false;
        if (str.startsWith("\"") && str.endsWith("\"")) {
            return str.substring(1, str.length() - 1)
                .replace("\\\"", "\"")
                .replace("\\\\", "\\");
        }
        if (str.startsWith("[")) return parseJsonArray(str);
        if (str.startsWith("{")) return parseJsonMap(str);
        try {
            if (str.contains(".")) return Double.parseDouble(str);
            return Long.parseLong(str);
        } catch (NumberFormatException e) {
            return str;
        }
    }

    @SuppressWarnings("unchecked")
    static List<Map<String, Object>> parseJsonArray(String json) {
        List<Map<String, Object>> list = new ArrayList<>();
        // Find all {...} objects within the array
        int depth = 0, objStart = -1;
        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '{') {
                if (depth == 0) objStart = i;
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0 && objStart != -1) {
                    String obj = json.substring(objStart, i + 1);
                    list.add(parseObject(obj));
                    objStart = -1;
                }
            }
        }
        return list;
    }

    static Map<String, Object> parseObject(String json) {
        Map<String, Object> obj = new LinkedHashMap<>();
        // Remove outer braces
        json = json.trim();
        if (json.startsWith("{")) json = json.substring(1);
        if (json.endsWith("}")) json = json.substring(0, json.length() - 1);
        
        int depth = 0, fieldStart = 0;
        boolean inString = false, escaped = false;
        
        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            
            if (escaped) {
                escaped = false;
                continue;
            }
            
            if (c == '\\' && inString) {
                escaped = true;
                continue;
            }
            
            if (c == '"' && !escaped) {
                inString = !inString;
                continue;
            }
            
            if (!inString) {
                if (c == '{' || c == '[') depth++;
                else if (c == '}' || c == ']') depth--;
                else if (c == ',' && depth == 0) {
                    String field = json.substring(fieldStart, i).trim();
                    parseField(obj, field);
                    fieldStart = i + 1;
                }
            }
        }
        
        if (fieldStart < json.length()) {
            String field = json.substring(fieldStart).trim();
            parseField(obj, field);
        }
        
        return obj;
    }
    
    static void parseField(Map<String, Object> obj, String field) {
        int colonIdx = field.indexOf(":");
        if (colonIdx <= 0) return;
        
        String key = field.substring(0, colonIdx).trim();
        String value = field.substring(colonIdx + 1).trim();
        
        // Remove quotes from key
        if (key.startsWith("\"") && key.endsWith("\"")) {
            key = key.substring(1, key.length() - 1);
        }
        
        // Parse value based on type
        Object parsedValue;
        if (value.startsWith("\"") && value.endsWith("\"")) {
            // String value - remove quotes
            parsedValue = value.substring(1, value.length() - 1)
                .replace("\\\"", "\"")
                .replace("\\\\", "\\");
        } else if (value.equals("true") || value.equals("false")) {
            parsedValue = Boolean.parseBoolean(value);
        } else if (value.equals("null")) {
            parsedValue = null;
        } else if (value.startsWith("[") || value.startsWith("{")) {
            // Keep as string for now - will be handled separately
            parsedValue = value;
        } else {
            // Try to parse as number
            try {
                if (value.contains(".")) {
                    parsedValue = Double.parseDouble(value);
                } else {
                    parsedValue = Long.parseLong(value);
                }
            } catch (NumberFormatException e) {
                parsedValue = value;
            }
        }
        
        obj.put(key, parsedValue);
    }

    // Convert object to JSON string with proper serialization
    static String toJson(Object obj) {
        if (obj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) obj;
            StringBuilder sb = new StringBuilder("{");
            boolean first = true;
            for (Map.Entry<String, Object> e : map.entrySet()) {
                if (!first) sb.append(",");
                sb.append("\"").append(e.getKey()).append("\":");
                sb.append(toJson(e.getValue()));
                first = false;
            }
            return sb.append("}").toString();
        } else if (obj instanceof List) {
            @SuppressWarnings("unchecked")
            List<Object> list = (List<Object>) obj;
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < list.size(); i++) {
                if (i > 0) sb.append(",");
                sb.append(toJson(list.get(i)));
            }
            return sb.append("]").toString();
        } else if (obj instanceof String) {
            String s = (String) obj;
            // Check if it's already valid JSON (starts with [ or {)
            String trimmed = s.trim();
            if ((trimmed.startsWith("[") && trimmed.endsWith("]")) ||
                (trimmed.startsWith("{") && trimmed.endsWith("}"))) {
                return trimmed;
            }
            // Otherwise, escape and quote it as a JSON string
            return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
        } else {
            return String.valueOf(obj);
        }
    }
}
