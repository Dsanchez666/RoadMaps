import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FrontendServer {
    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(3000), 0);

        // Proxy /api/* requests to backend on 8081
        server.createContext("/api/", exchange -> {
            try { proxyToBackend(exchange); } catch (Exception e) { handleError(exchange, e); }
        });

        // Serve static files
        server.createContext("/", exchange -> {
            try { serveStatic(exchange); } catch (Exception e) { handleError(exchange, e); }
        });

        server.setExecutor(null);
        server.start();
        System.out.println("✓ Frontend running on http://localhost:3000");
        System.out.println("  Backend API proxy: /api/* → http://localhost:8081");
    }

    static void proxyToBackend(HttpExchange exchange) throws Exception {
        try {
            String path = exchange.getRequestURI().getPath();
            String query = exchange.getRequestURI().getRawQuery();
            String backendUrl = "http://localhost:8081" + path + (query != null ? "?" + query : "");
            
            URL url = new URL(backendUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod(exchange.getRequestMethod());
            // allow sending request body when needed
            conn.setDoOutput(true);
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            
            // Copy request headers (skip Host and Connection)
            exchange.getRequestHeaders().forEach((k, v) -> {
                if (!k.equalsIgnoreCase("Host") && !k.equalsIgnoreCase("Connection")) {
                    conn.setRequestProperty(k, v.get(0));
                }
            });
            
            // Copy request body for POST/PUT
            if ("POST".equals(exchange.getRequestMethod()) || "PUT".equals(exchange.getRequestMethod())) {
                byte[] body = exchange.getRequestBody().readAllBytes();
                conn.setRequestProperty("Content-Length", String.valueOf(body.length));
                if (body.length > 0) {
                    conn.getOutputStream().write(body);
                    conn.getOutputStream().flush();
                }
            }
            
            // Get response
            int code = conn.getResponseCode();
            InputStream respStream = code >= 400 ? conn.getErrorStream() : conn.getInputStream();
            byte[] response = respStream != null ? respStream.readAllBytes() : new byte[0];
            
            // Send response back to client
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.sendResponseHeaders(code, response.length);
            if (response.length > 0) {
                exchange.getResponseBody().write(response);
            }
            exchange.close();
        } catch (Exception e) {
            e.printStackTrace();
            try {
                String error = "{\"error\":\"" + (e.getMessage() != null ? e.getMessage().replace("\"","\\\"") : "Internal error") + "\"}";
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(500, error.length());
                exchange.getResponseBody().write(error.getBytes());
                exchange.close();
            } catch (Exception ex) { ex.printStackTrace(); }
        }
    }

    static void serveStatic(HttpExchange exchange) throws Exception {
        try {
            String path = exchange.getRequestURI().getPath();
            if (path.equals("/")) path = "/index.html";

            String filePath = "." + path;
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
            } catch (Exception ex) { ex.printStackTrace(); }
        }
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

    static void handleError(HttpExchange exchange, Exception e) {
        try {
            String msg = e.getMessage() != null ? e.getMessage() : "Internal error";
            String error = "{\"error\":\"" + msg.replace("\"", "\\\"") + "\"}";
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(500, error.length());
            exchange.getResponseBody().write(error.getBytes());
            exchange.close();
        } catch (Exception ex) { ex.printStackTrace(); }
    }
}
