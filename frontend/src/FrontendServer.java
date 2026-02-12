package com.example.roadmap;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FrontendServer {
    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        // Serve static files
        server.createContext("/", exchange -> {
            try {
                String path = exchange.getRequestURI().getPath();
                if (path.equals("/")) path = "/index.html";

                String filePath = "."+ path;
                File file = new File(filePath);

                if (file.exists() && file.isFile()) {
                    byte[] content = Files.readAllBytes(file.toPath());
                    String contentType = getContentType(filePath);
                    exchange.getResponseHeaders().set("Content-Type", contentType);
                    exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
                    exchange.sendResponseHeaders(200, content.length);
                    exchange.getResponseBody().write(content);
                } else {
                    // Fallback to index.html for SPA routing
                    byte[] content = Files.readAllBytes(Paths.get("./index.html"));
                    exchange.getResponseHeaders().set("Content-Type", "text/html");
                    exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
                    exchange.sendResponseHeaders(200, content.length);
                    exchange.getResponseBody().write(content);
                }
                exchange.close();
            } catch (Exception e) {
                try {
                    exchange.sendResponseHeaders(500, 0);
                    exchange.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        server.setExecutor(null);
        server.start();
        System.out.println("Frontend running on http://localhost:8080");
        System.out.println("Backend API: http://localhost:8081");
    }

    static String getContentType(String filename) {
        if (filename.endsWith(".html")) return "text/html";
        if (filename.endsWith(".js")) return "application/javascript";
        if (filename.endsWith(".css")) return "text/css";
        if (filename.endsWith(".json")) return "application/json";
        if (filename.endsWith(".png")) return "image/png";
        if (filename.endsWith(".jpg")) return "image/jpeg";
        if (filename.endsWith(".svg")) return "image/svg+xml";
        return "text/plain";
    }
}
