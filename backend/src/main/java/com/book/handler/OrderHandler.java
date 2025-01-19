package com.book.handler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class OrderHandler implements HttpHandler {
    private static final String ORDERS_CSV = "orders.csv";
    private static final String CART_ITEMS_CSV = "cartitems.csv";

    public OrderHandler() {
        initializeOrdersFile();
    }

    private void initializeOrdersFile() {
        try {
            String currentDir = System.getProperty("user.dir");
            File file = new File(currentDir, ORDERS_CSV);
            if (!file.exists()) {
                try (FileWriter fw = new FileWriter(file)) {
                    // Initialize with headers
                    fw.write("orderId,userId,totalAmount,deliveryFee,status,paymentMethod,deliveryAddress,orderDate,items\n");
                }
                System.out.println("Created new orders.csv file at: " + file.getAbsolutePath());
            }
        } catch (IOException e) {
            System.err.println("Error initializing orders.csv: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    @SuppressWarnings({"UseSpecificCatch", "CallToPrintStackTrace"})
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        // Handle CORS preflight
        if ("OPTIONS".equals(method)) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        try {
            // Test endpoint for file writing verification
            if (path.endsWith("/test")) {
                handleTestWrite(exchange);
                return;
            }

            switch (method) {
                case "POST" -> {
                    if (path.endsWith("/create")) {
                        handleCreateOrder(exchange);
                    }
                }
                case "GET" -> {
                    if (path.contains("/user/")) {
                        handleGetUserOrders(exchange);
                    } else {
                        handleGetAllOrders(exchange);
                    }
                }
                default -> sendResponse(exchange, 405, "Method not allowed");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, "Server error: " + e.getMessage());
        }
    }

    private void handleTestWrite(HttpExchange exchange) throws IOException {
        try {
            String currentDir = System.getProperty("user.dir");
            File testFile = new File(currentDir, ORDERS_CSV);
            try (FileWriter fw = new FileWriter(testFile, true)) {
                fw.write("test_order,test_user,100.00,5.00,Pending,COD,\"Test Address\",2024-01-19,\"[]\"\n");
            }
            sendResponse(exchange, 200, "Test order written successfully to " + testFile.getAbsolutePath());
        } catch (IOException e) {
            sendResponse(exchange, 500, "Error writing test order: " + e.getMessage());
        }
    }

    @SuppressWarnings({ "unchecked", "resource", "CallToPrintStackTrace", "UseSpecificCatch" })
    private void handleCreateOrder(HttpExchange exchange) throws IOException {
        try {
            System.out.println("Starting order creation process...");
            
            // Get the current working directory
            String currentDir = System.getProperty("user.dir");
            File ordersFile = new File(currentDir, ORDERS_CSV);
            
            System.out.println("Orders file path: " + ordersFile.getAbsolutePath());
            
            // Read request body
            InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder requestBody = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                requestBody.append(line);
            }
            
            System.out.println("Received order data: " + requestBody.toString());
            
            JSONObject orderData = (JSONObject) new JSONParser().parse(requestBody.toString());
            
            // Generate unique order ID with timestamp
            String timestamp = LocalDateTime.now().toString().replace(":", "").replace(".", "");
            String orderId = "ORD_" + timestamp.substring(0, 14);
            
            // Format delivery address to prevent CSV issues
            String deliveryAddress = orderData.get("deliveryAddress").toString()
                .replace("\"", "'")  // Replace double quotes with single quotes
                .replace(",", ";");  // Replace commas with semicolons
            
            // Format items array to prevent CSV issues
            JSONArray items = (JSONArray) orderData.get("items");
            String itemsStr = items.toString()
                .replace("\"", "'")
                .replace(",", ";");
                
            // Calculate total amount and delivery fee from items
            double totalAmount = Double.parseDouble(orderData.get("totalAmount").toString());
            double deliveryFee = Double.parseDouble(orderData.get("deliveryFee").toString());
            
            // Create order entry with proper escaping and formatting
            String orderEntry = String.format("%s,%s,%.2f,%.2f,%s,%s,\"%s\",%s,\"%s\"\n",
                orderId,
                orderData.get("userId"),
                totalAmount,
                deliveryFee,
                "Pending",  // Initial status
                "COD",     // Payment method
                deliveryAddress,
                LocalDateTime.now().toString(),
                itemsStr
            );
            
            // Save to orders.csv with proper encoding
            try (FileWriter fw = new FileWriter(ordersFile, true);
                BufferedWriter bw = new BufferedWriter(fw)) {
                bw.write(orderEntry);
                bw.flush();
                System.out.println("Order successfully written to file: " + orderEntry);
            }

            // Clear user's cart after successful order
            clearUserCart(orderData.get("userId").toString());
            
            System.out.println("Order creation completed successfully");
            
            // Send success response
            JSONObject response = new JSONObject();
            response.put("orderId", orderId);
            response.put("message", "Order placed successfully");
            response.put("totalAmount", totalAmount);
            response.put("deliveryFee", deliveryFee);
            response.put("status", "Pending");
            response.put("orderDate", LocalDateTime.now().toString());
            
            sendJsonResponse(exchange, 201, response);
            
        } catch (Exception e) {
            System.err.println("Error in order creation: " + e.getMessage());
            e.printStackTrace();
            sendResponse(exchange, 500, "Error creating order: " + e.getMessage());
        }
    }

    private void clearUserCart(String userId) throws IOException {
        String currentDir = System.getProperty("user.dir");
        File inputFile = new File(currentDir, CART_ITEMS_CSV);
        File tempFile = new File(currentDir, "cartitems_temp.csv");
    
        try {
            // Create temp file if it doesn't exist
            if (!tempFile.exists()) {
                tempFile.createNewFile();
            }
    
            try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
                 BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
                
                // Copy header
                String header = reader.readLine();
                if (header != null) {
                    writer.write(header + "\n");
                }
    
                // Copy all lines except those matching userId
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] values = line.split(",");
                    if (values.length > 1 && !values[1].equals(userId)) {
                        writer.write(line + "\n");
                    }
                }
                
                // Ensure all data is written
                writer.flush();
            }
    
            // Delete the original file
            if (inputFile.exists() && !inputFile.delete()) {
                throw new IOException("Could not delete original cart file");
            }
    
            // Rename temp file to original
            if (!tempFile.renameTo(inputFile)) {
                throw new IOException("Could not rename temp file");
            }
    
        } catch (IOException e) {
            System.err.println("Error clearing cart: " + e.getMessage());
            // If something goes wrong, ensure temp file is cleaned up
            if (tempFile.exists()) {
                tempFile.delete();
            }
            throw e;
        }
    }

    @SuppressWarnings({"unchecked", "UnnecessaryTemporaryOnConversionFromString"})
    private void handleGetUserOrders(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String userId = path.substring(path.lastIndexOf("/") + 1);
        String currentDir = System.getProperty("user.dir");
        
        JSONArray orders = new JSONArray();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(new File(currentDir, ORDERS_CSV)))) {
            String line;
            boolean firstLine = true;
            while ((line = reader.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue;
                }
                
                String[] values = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1); // Split considering quoted values
                if (values.length >= 9 && values[1].equals(userId)) {
                    JSONObject order = new JSONObject();
                    order.put("orderId", values[0]);
                    order.put("totalAmount", Double.parseDouble(values[2]));
                    order.put("deliveryFee", Double.parseDouble(values[3]));
                    order.put("status", values[4]);
                    order.put("paymentMethod", values[5]);
                    order.put("deliveryAddress", values[6].replace("\"\"", "\""));
                    order.put("orderDate", values[7]);
                    order.put("items", values[8].replace("\"\"", "\""));
                    orders.add(order);
                }
            }
        }
        
        sendJsonResponse(exchange, 200, orders);
    }

    @SuppressWarnings({"unchecked", "UnnecessaryTemporaryOnConversionFromString"})
    private void handleGetAllOrders(HttpExchange exchange) throws IOException {
        String currentDir = System.getProperty("user.dir");
        JSONArray orders = new JSONArray();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(new File(currentDir, ORDERS_CSV)))) {
            String line;
            boolean firstLine = true;
            while ((line = reader.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue;
                }
                
                String[] values = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
                JSONObject order = new JSONObject();
                order.put("orderId", values[0]);
                order.put("userId", values[1]);
                order.put("totalAmount", Double.parseDouble(values[2]));
                order.put("deliveryFee", Double.parseDouble(values[3]));
                order.put("status", values[4]);
                order.put("paymentMethod", values[5]);
                order.put("deliveryAddress", values[6].replace("\"\"", "\""));
                order.put("orderDate", values[7]);
                order.put("items", values[8].replace("\"\"", "\""));
                orders.add(order);
            }
        }
        
        sendJsonResponse(exchange, 200, orders);
    }

    private void sendJsonResponse(HttpExchange exchange, int statusCode, Object response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        byte[] responseBytes = response.toString().getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }

    @SuppressWarnings("unchecked")
    private void sendResponse(HttpExchange exchange, int statusCode, String message) throws IOException {
        JSONObject response = new JSONObject();
        response.put("message", message);
        sendJsonResponse(exchange, statusCode, response);
    }
}