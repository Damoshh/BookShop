/* package com.book.handler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class OrderHandler implements HttpHandler {
    private static final String ORDERS_CSV = "backend/orders.csv";
    private final AdminHandler adminHandler;

    public OrderHandler(AdminHandler adminHandler) {
        this.adminHandler = adminHandler;
        try {
            File file = new File(ORDERS_CSV);
            file.getParentFile().mkdirs();
            
            if (!file.exists()) {
                try (FileWriter fw = new FileWriter(file)) {
                    fw.write("id,customerEmail,total,status,date\n");
                }
            }
        } catch (IOException e) {
            System.err.println("Error initializing orders.csv: " + e.getMessage());
        }
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type,Authorization");

        if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        switch (exchange.getRequestMethod()) {
            case "GET":
                handleGetOrders(exchange);
                break;
            case "POST":
                handleCreateOrder(exchange);
                break;
            default:
                sendResponse(exchange, 405, "Method not allowed");
        }
    }

    @SuppressWarnings("unchecked")
    private void handleGetOrders(HttpExchange exchange) throws IOException {
        if (!adminHandler.validateAuthToken(exchange.getRequestHeaders().getFirst("Authorization"))) {
            sendResponse(exchange, 401, "Unauthorized");
            return;
        }
    
        JSONArray orders = new JSONArray();
        try (BufferedReader reader = new BufferedReader(new FileReader(ORDERS_CSV))) {
            String line;
            boolean firstLine = true;
            
            while ((line = reader.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue;
                }
                
                String[] values = line.split(",");
                if (values.length >= 5) {
                    JSONObject order = new JSONObject();
                    order.put("_id", values[0]);
                    order.put("customerEmail", values[1]);
                    order.put("total", Double.valueOf(values[2]));
                    order.put("status", values[3]);
                    order.put("date", values[4]);
                    orders.add(order);
                }
            }
        }
        
        sendJsonResponse(exchange, 200, orders);
    }

private void handleCreateOrder(HttpExchange exchange) throws IOException {
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))) {
        StringBuilder body = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            body.append(line);
        }

        JSONObject newOrder = (JSONObject) new JSONParser().parse(body.toString());
        String orderLine = String.format("%s,%s,%.2f,%s,%s\n",
                newOrder.get("_id"),
                newOrder.get("customerEmail"),
                newOrder.get("total"),
                newOrder.get("status"),
                newOrder.get("date"));

        try (FileWriter fw = new FileWriter(ORDERS_CSV, true)) {
            fw.write(orderLine);
        }

        sendResponse(exchange, 201, "Order created successfully");
    } catch (Exception e) {
        sendResponse(exchange, 500, "Error creating order: " + e.getMessage());
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
}*/