package com.book.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.simple.JSONObject;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class AdminDashboardHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Set CORS headers
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");

        if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        // Check for admin session token
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        if (authHeader == null || !validateAdminSession(authHeader)) {
            sendResponse(exchange, 401, "Unauthorized");
            return;
        }

        if ("GET".equals(exchange.getRequestMethod())) {
            handleGetDashboardData(exchange);
        } else {
            sendResponse(exchange, 405, "Method not allowed");
        }
    }

    private boolean validateAdminSession(String authHeader) {
        // TODO: Implement proper session validation
        return authHeader.startsWith("Bearer ");
    }

    private void handleGetDashboardData(HttpExchange exchange) throws IOException {
        JSONObject dashboardData = new JSONObject();
        // Add dashboard data here
        dashboardData.put("totalBooks", 100);  // Example data
        dashboardData.put("totalUsers", 50);
        dashboardData.put("totalOrders", 25);

        sendJsonResponse(exchange, 200, dashboardData);
    }

    private void sendJsonResponse(HttpExchange exchange, int statusCode, JSONObject jsonResponse) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        byte[] responseBytes = jsonResponse.toString().getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String message) throws IOException {
        JSONObject response = new JSONObject();
        response.put("message", message);
        sendJsonResponse(exchange, statusCode, response);
    }
}