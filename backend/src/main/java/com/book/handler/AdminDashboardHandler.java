package com.book.handler;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import org.json.simple.JSONObject;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class AdminDashboardHandler implements HttpHandler {
    private static final String BOOKS_CSV = "books.csv";
    private static final String USERS_CSV = "users.csv";
    private static final String ORDERS_CSV = "orders.csv";

    @Override
    @SuppressWarnings({"unchecked", "UseSpecificCatch"})
    public void handle(HttpExchange exchange) throws IOException {
        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        try {
            JSONObject stats = new JSONObject();
            
            // Get total books
            int totalBooks = countTotalBooks();
            stats.put("totalBooks", totalBooks);
            
            // Get total users (excluding admins)
            int totalUsers = countTotalUsers();
            stats.put("totalUsers", totalUsers);
            
            // Get active orders (Pending status)
            int activeOrders = countActiveOrders();
            stats.put("activeOrders", activeOrders);
            
            // Get total sales (Delivered orders)
            double totalSales = calculateTotalSales();
            stats.put("totalSales", totalSales);

            // Send response
            String response = stats.toString();
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, responseBytes.length);
            
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBytes);
            }

        } catch (Exception e) {
            String response = "{\"error\": \"" + e.getMessage() + "\"}";
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(500, responseBytes.length);
            
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBytes);
            }
        }
    }

    private int countTotalBooks() throws IOException {
        int count = -1; // Start at -1 to exclude header
        try (BufferedReader reader = new BufferedReader(new FileReader(BOOKS_CSV))) {
            while (reader.readLine() != null) count++;
        }
        return count;
    }

    private int countTotalUsers() throws IOException {
        int count = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(USERS_CSV))) {
            reader.readLine(); // Skip header
            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");
                if (values.length > 10 && values[10].equals("user")) {
                    count++;
                }
            }
        }
        return count;
    }

    private int countActiveOrders() throws IOException {
        int count = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(ORDERS_CSV))) {
            reader.readLine(); // Skip header
            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
                if (values.length > 4 && values[4].equals("Pending")) {
                    count++;
                }
            }
        }
        return count;
    }

    private double calculateTotalSales() throws IOException {
        double total = 0.0;
        try (BufferedReader reader = new BufferedReader(new FileReader(ORDERS_CSV))) {
            reader.readLine(); // Skip header
            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
                if (values.length > 4 && values[4].equals("Delivered")) {
                    total += Double.parseDouble(values[2]); // totalAmount
                }
            }
        }
        return total;
    }
}