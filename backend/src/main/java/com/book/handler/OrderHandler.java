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

    @SuppressWarnings("CallToPrintStackTrace")
    private void initializeOrdersFile() {
        try {
            String currentDir = System.getProperty("user.dir");
            File file = new File(currentDir, ORDERS_CSV);
            if (!file.exists()) {
                try (FileWriter fw = new FileWriter(file)) {
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

        if ("OPTIONS".equals(method)) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        try {
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

    @SuppressWarnings({ "unchecked", "CallToPrintStackTrace", "UseSpecificCatch" })
    private void handleCreateOrder(HttpExchange exchange) throws IOException {
        try {
            System.out.println("Starting order creation process...");
            
            String currentDir = System.getProperty("user.dir");
            File ordersFile = new File(currentDir, ORDERS_CSV);
            
            System.out.println("Orders file path: " + ordersFile.getAbsolutePath());
            
            InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder requestBody = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                requestBody.append(line);
            }
            
            System.out.println("Received order data: " + requestBody.toString());
            
            JSONObject orderData = (JSONObject) new JSONParser().parse(requestBody.toString());
            
            String timestamp = LocalDateTime.now().toString().replace(":", "").replace(".", "");
            String orderId = "ORD_" + timestamp.substring(0, 14);
            
            String deliveryAddress = orderData.get("deliveryAddress").toString()
                .replace("\"", "'")
                .replace(",", ";");
            
            JSONArray items = (JSONArray) orderData.get("items");
            String itemsStr = items.toString()
                .replace("\"", "'")
                .replace(",", ";");
                
            double totalAmount = Double.parseDouble(orderData.get("totalAmount").toString());
            double deliveryFee = Double.parseDouble(orderData.get("deliveryFee").toString());
            
            String orderEntry = String.format("%s,%s,%.2f,%.2f,%s,%s,\"%s\",%s,\"%s\"\n",
                orderId,
                orderData.get("userId"),
                totalAmount,
                deliveryFee,
                "Pending",
                "COD",
                deliveryAddress,
                LocalDateTime.now().toString(),
                itemsStr
            );
            
            try (FileWriter fw = new FileWriter(ordersFile, true);
                BufferedWriter bw = new BufferedWriter(fw)) {
                bw.write(orderEntry);
                bw.flush();
                System.out.println("Order successfully written to file: " + orderEntry);
            }

            clearUserCart(orderData.get("userId").toString());
            
            System.out.println("Order creation completed successfully");
            
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
            if (!tempFile.exists()) {
                tempFile.createNewFile();
            }
    
            try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
                 BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
                
                String header = reader.readLine();
                if (header != null) {
                    writer.write(header + "\n");
                }
    
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] values = line.split(",");
                    if (values.length > 1 && !values[1].equals(userId)) {
                        writer.write(line + "\n");
                    }
                }
                
                writer.flush();
            }
    
            if (inputFile.exists() && !inputFile.delete()) {
                throw new IOException("Could not delete original cart file");
            }
    
            if (!tempFile.renameTo(inputFile)) {
                throw new IOException("Could not rename temp file");
            }
    
        } catch (IOException e) {
            System.err.println("Error clearing cart: " + e.getMessage());
            if (tempFile.exists()) {
                tempFile.delete();
            }
            throw e;
        }
    }

    @SuppressWarnings({"unchecked"})
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
                
                String[] values = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
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

    @SuppressWarnings({"unchecked"})
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

    // Admin order status update handler
    public void handleAdminOrderUpdate(HttpExchange exchange) throws IOException {
        try {
            InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder requestBody = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                requestBody.append(line);
            }

            JSONObject updateData = (JSONObject) new JSONParser().parse(requestBody.toString());
            String orderId = (String) updateData.get("orderId");

            updateOrderStatusAdmin(orderId, exchange);
        } catch (Exception e) {
            System.err.println("Error in admin order update: " + e.getMessage());
            e.printStackTrace();
            sendResponse(exchange, 500, "Error updating order status: " + e.getMessage());
        }
    }

    // User order status update handler
    public void handleUserOrderUpdate(HttpExchange exchange) throws IOException {
        try {
            InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder requestBody = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                requestBody.append(line);
            }

            JSONObject updateData = (JSONObject) new JSONParser().parse(requestBody.toString());
            String orderId = (String) updateData.get("orderId");

            updateOrderStatusUser(orderId, exchange);
        } catch (Exception e) {
            System.err.println("Error in user order update: " + e.getMessage());
            e.printStackTrace();
            sendResponse(exchange, 500, "Error updating order status: " + e.getMessage());
        }
    }

    private void updateOrderStatusAdmin(String orderId, HttpExchange exchange) throws IOException {
        String currentDir = System.getProperty("user.dir");
        File inputFile = new File(currentDir, ORDERS_CSV);
        File tempFile = new File(currentDir, "orders_temp.csv");

        try {
            if (!tempFile.exists()) {
                tempFile.createNewFile();
            }

            boolean orderFound = false;
            try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
                 BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
                
                String header = reader.readLine();
                writer.write(header + "\n");

                String orderLine;
                while ((orderLine = reader.readLine()) != null) {
                    String[] values = orderLine.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
                    
                    if (values[0].equals(orderId) && values[4].equals("Pending")) {
                        values[4] = "Delivery";
                        writer.write(String.join(",", values) + "\n");
                        orderFound = true;
                    } else {
                        writer.write(orderLine + "\n");
                    }
                }
            }

            if (!orderFound) {
                sendResponse(exchange, 400, "Order not found or not in Pending status");
                return;
            }

            updateFile(inputFile, tempFile);
            sendResponse(exchange, 200, "Order status updated to Delivery successfully");
        } catch (IOException e) {
            if (tempFile.exists()) {
                tempFile.delete();
            }
            throw e;
        }
    }

    private void updateOrderStatusUser(String orderId, HttpExchange exchange) throws IOException {
        String currentDir = System.getProperty("user.dir");
        File inputFile = new File(currentDir, ORDERS_CSV);
        File tempFile = new File(currentDir, "orders_temp.csv");

        try {
            if (!tempFile.exists()) {
                tempFile.createNewFile();
            }

            boolean orderFound = false;
            try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
                 BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
                
                String header = reader.readLine();
                writer.write(header + "\n");

                String orderLine;
                while ((orderLine = reader.readLine()) != null) {
                    String[] values = orderLine.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
                    
                    if (values[0].equals(orderId) && values[4].equals("Delivery")) {
                        values[4] = "Delivered";
                        writer.write(String.join(",", values) + "\n");
                        orderFound = true;
                    } else {
                        writer.write(orderLine + "\n");
                    }
                }
            }

            if (!orderFound) {
                sendResponse(exchange, 400, "Order not found or not in Delivery status");
                return;
            }

            updateFile(inputFile, tempFile);
            sendResponse(exchange, 200, "Order status updated to Delivered successfully");
        } catch (IOException e) {
            if (tempFile.exists()) {
                tempFile.delete();
            }
            throw e;
        }
    }
  
        private void updateFile(File inputFile, File tempFile) throws IOException {
        if (!inputFile.delete()) {
            throw new IOException("Could not delete original orders file");
        }

        if (!tempFile.renameTo(inputFile)) {
            throw new IOException("Could not rename temp file");
        }
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