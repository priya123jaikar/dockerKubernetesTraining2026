package com.assignment1;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

public class App {
    private static final Gson GSON = new Gson();

    private static final String DB_HOST = getenv("DB_HOST", "db");
    private static final String DB_PORT = getenv("DB_PORT", "3306");
    private static final String DB_NAME = getenv("DB_NAME", "appdb");
    private static final String DB_USER = getenv("DB_USER", "appuser");
    private static final String DB_PASSWORD = getenv("DB_PASSWORD", "apppass");

    private static final String JDBC_URL = "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME
            + "?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC";

    public static void main(String[] args) throws Exception {
        Class.forName("com.mysql.cj.jdbc.Driver");
        connectWithRetry(20, 3000);
        ensureProductsTable();

        int port = Integer.parseInt(getenv("PORT", "5000"));
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/health", new HealthHandler());
        server.createContext("/api/products", new ProductsHandler());
        server.setExecutor(Executors.newFixedThreadPool(10));
        server.start();

        System.out.println("Java backend running on port " + port);
    }

    private static String getenv(String name, String defaultValue) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? defaultValue : value;
    }

    private static void connectWithRetry(int maxAttempts, long delayMs) throws InterruptedException {
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try (Connection conn = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD);
                 Statement stmt = conn.createStatement()) {
                stmt.execute("SELECT 1");
                System.out.println("Connected to MySQL.");
                return;
            } catch (SQLException ex) {
                System.err.println("DB connection failed (attempt " + attempt + "/" + maxAttempts + "): " + ex.getMessage());
                if (attempt == maxAttempts) {
                    throw new RuntimeException("Unable to connect to database after retries.", ex);
                }
                Thread.sleep(delayMs);
            }
        }
    }

    private static void ensureProductsTable() {
        String createTableSql = "CREATE TABLE IF NOT EXISTS products ("
                + "id INT AUTO_INCREMENT PRIMARY KEY,"
                + "name VARCHAR(100) NOT NULL,"
                + "quantity INT NOT NULL,"
                + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
                + ")";

        try (Connection conn = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement()) {
            stmt.execute(createTableSql);
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to create products table.", ex);
        }
    }

    private static class HealthHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (handleOptions(exchange)) {
                return;
            }
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJson(exchange, 405, Map.of("message", "Method not allowed"));
                return;
            }

            try (Connection conn = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD);
                 Statement stmt = conn.createStatement()) {
                stmt.execute("SELECT 1");
                sendJson(exchange, 200, Map.of("status", "ok", "service", "backend", "database", "connected"));
            } catch (SQLException ex) {
                sendJson(exchange, 500, Map.of("status", "error", "service", "backend", "database", "disconnected"));
            }
        }
    }

    private static class ProductsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (handleOptions(exchange)) {
                return;
            }

            String method = exchange.getRequestMethod();
            if ("GET".equalsIgnoreCase(method)) {
                listProducts(exchange);
                return;
            }
            if ("POST".equalsIgnoreCase(method)) {
                createProduct(exchange);
                return;
            }
            sendJson(exchange, 405, Map.of("message", "Method not allowed"));
        }

        private void listProducts(HttpExchange exchange) throws IOException {
            String sql = "SELECT id, name, quantity, created_at FROM products ORDER BY id";
            List<Map<String, Object>> products = new ArrayList<>();

            try (Connection conn = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD);
                 PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    Map<String, Object> product = new HashMap<>();
                    product.put("id", rs.getInt("id"));
                    product.put("name", rs.getString("name"));
                    product.put("quantity", rs.getInt("quantity"));
                    product.put("created_at", rs.getTimestamp("created_at")
                            .toInstant().atOffset(ZoneOffset.UTC)
                            .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
                    products.add(product);
                }
                sendJson(exchange, 200, products);
            } catch (SQLException ex) {
                sendJson(exchange, 500, Map.of("message", "Failed to fetch products", "error", ex.getMessage()));
            }
        }

        private void createProduct(HttpExchange exchange) throws IOException {
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);

            JsonObject json;
            try {
                json = GSON.fromJson(body, JsonObject.class);
            } catch (JsonSyntaxException ex) {
                sendJson(exchange, 400, Map.of("message", "Invalid JSON"));
                return;
            }

            if (json == null || !json.has("name") || !json.has("quantity")) {
                sendJson(exchange, 400, Map.of("message", "name and quantity are required"));
                return;
            }

            String name = json.get("name").getAsString().trim();
            int quantity;
            try {
                quantity = json.get("quantity").getAsInt();
            } catch (Exception ex) {
                sendJson(exchange, 400, Map.of("message", "quantity must be a number"));
                return;
            }

            if (name.isEmpty()) {
                sendJson(exchange, 400, Map.of("message", "name cannot be empty"));
                return;
            }

            String insertSql = "INSERT INTO products (name, quantity) VALUES (?, ?)";
            String selectSql = "SELECT id, name, quantity, created_at FROM products WHERE id = ?";

            try (Connection conn = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD);
                 PreparedStatement insertPs = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {

                insertPs.setString(1, name);
                insertPs.setInt(2, quantity);
                insertPs.executeUpdate();

                int newId;
                try (ResultSet keys = insertPs.getGeneratedKeys()) {
                    if (!keys.next()) {
                        sendJson(exchange, 500, Map.of("message", "Failed to retrieve inserted id"));
                        return;
                    }
                    newId = keys.getInt(1);
                }

                try (PreparedStatement selectPs = conn.prepareStatement(selectSql)) {
                    selectPs.setInt(1, newId);
                    try (ResultSet rs = selectPs.executeQuery()) {
                        if (rs.next()) {
                            Map<String, Object> product = new HashMap<>();
                            product.put("id", rs.getInt("id"));
                            product.put("name", rs.getString("name"));
                            product.put("quantity", rs.getInt("quantity"));
                            product.put("created_at", rs.getTimestamp("created_at")
                                    .toInstant().atOffset(ZoneOffset.UTC)
                                    .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
                            sendJson(exchange, 201, product);
                            return;
                        }
                    }
                }

                sendJson(exchange, 500, Map.of("message", "Failed to fetch inserted product"));
            } catch (SQLException ex) {
                sendJson(exchange, 500, Map.of("message", "Failed to add product", "error", ex.getMessage()));
            }
        }
    }

    private static boolean handleOptions(HttpExchange exchange) throws IOException {
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            addCorsHeaders(exchange);
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
            return true;
        }
        return false;
    }

    private static void sendJson(HttpExchange exchange, int statusCode, Object data) throws IOException {
        byte[] response = GSON.toJson(data).getBytes(StandardCharsets.UTF_8);
        addCorsHeaders(exchange);
        exchange.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(statusCode, response.length);
        exchange.getResponseBody().write(response);
        exchange.close();
    }

    private static void addCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
    }
}
