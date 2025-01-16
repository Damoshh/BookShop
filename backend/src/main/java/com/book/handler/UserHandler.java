package com.book.handler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class UserHandler implements HttpHandler {
    private static final String CSV_FILE = "backend/users.csv";
    
    public UserHandler() {
        try {
            File file = new File(CSV_FILE);
            file.getParentFile().mkdirs();
            
            if (!file.exists()) {
                try (FileWriter fw = new FileWriter(file)) {
                    // Updated CSV header to include additional fields
                    fw.write("name,email,password,phone,address,role\n");
                }
                System.out.println("Created new users.csv file at: " + file.getAbsolutePath());
            }
        } catch (IOException e) {
            System.err.println("Error initializing users.csv: " + e.getMessage());
        }
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type,Authorization");
    
        if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }
    
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();
    
        try {
            if (path.equals("/api/users/profile")) {
                if (method.equals("GET")) {
                    handleGetProfile(exchange);
                } else if (method.equals("PUT")) {
                    handleUpdateProfile(exchange);
                }
            } else if (method.equals("POST")) {
                handlePost(exchange);
            } else {
                sendResponse(exchange, 405, "Method not allowed");
            }
        } catch (IOException e) {
            sendResponse(exchange, 500, "Internal Server Error");
        }
    }
    

    private void handlePost(HttpExchange exchange) throws IOException {
        InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
        BufferedReader br = new BufferedReader(isr);
        StringBuilder requestBody = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            requestBody.append(line);
        }

        try {
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(requestBody.toString());
            String action = (String) json.get("action");

            System.out.println("Handling action: " + action);

            if ("register".equals(action)) {
                handleRegister(exchange, json);
            } else if ("login".equals(action)) {
                handleLogin(exchange, json);
            }
        } catch (IOException | ParseException e) {
            System.err.println("Error processing request: " + e.getMessage());
            sendResponse(exchange, 400, "Invalid request");
        }
    }

    @SuppressWarnings("unchecked")
    private void handleGetProfile(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        String email = query.split("=")[1];

        File file = new File(CSV_FILE);
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            reader.readLine(); // Skip header
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");
                if (values[1].equals(email)) {
                    JSONObject userData = new JSONObject();
                    userData.put("name", values[0]);
                    userData.put("email", values[1]);
                    userData.put("phone", values.length > 3 ? values[3] : "");
                    userData.put("address", values.length > 4 ? values[4] : "");
                    
                    sendJsonResponse(exchange, 200, userData);
                    return;
                }
            }
            sendResponse(exchange, 404, "User not found");
        }
    }

    private void handleUpdateProfile(HttpExchange exchange) throws IOException {
        InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
        BufferedReader br = new BufferedReader(isr);
        StringBuilder requestBody = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            requestBody.append(line);
        }

        try {
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(requestBody.toString());
            
            String email = (String) json.get("email");
            List<String> lines = new ArrayList<>();
            boolean userFound = false;

            File file = new File(CSV_FILE);
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                lines.add(reader.readLine()); // Add header
                while ((line = reader.readLine()) != null) {
                    String[] values = line.split(",");
                    if (values[1].equals(email)) {
                        // Update user data but keep password
                        lines.add(String.format("%s,%s,%s,%s,%s,%s",
                            json.get("name"),
                            email,
                            values[2], // keep existing password
                            json.get("phone"),
                            json.get("address"),
                            values.length > 5 ? values[5] : "user" // keep role or set default
                        ));
                        userFound = true;
                    } else {
                        lines.add(line);
                    }
                }
            }

            if (!userFound) {
                sendResponse(exchange, 404, "User not found");
                return;
            }

            try (FileWriter writer = new FileWriter(file)) {
                for (String updatedLine : lines) {
                    writer.write(updatedLine + "\n");
                }
            }

            sendResponse(exchange, 200, "Profile updated successfully");
        } catch (ParseException e) {
            sendResponse(exchange, 400, "Invalid request data");
        }
    }

    private void handleRegister(HttpExchange exchange, JSONObject json) throws IOException {
        String name = (String) json.get("name");
        String email = (String) json.get("email");
        String password = (String) json.get("password");

        if (userExists(email)) {
            sendResponse(exchange, 400, "Email already exists");
            return;
        }

        // Save with empty phone and address fields
        File file = new File(CSV_FILE);
        try (FileWriter fw = new FileWriter(file, true)) {
            fw.write(String.format("%s,%s,%s,,,user\n", name, email, password));
            System.out.println("Registered new user: " + email);
            sendResponse(exchange, 201, "User registered successfully");
        }
    }

    @SuppressWarnings("unchecked")
    private void handleLogin(HttpExchange exchange, JSONObject json) throws IOException {
        String email = (String) json.get("email");
        String password = (String) json.get("password");

        JSONObject response = validateUserAndGetDetails(email, password);
        if (response != null) {
            sendJsonResponse(exchange, 200, response);
        } else {
            sendResponse(exchange, 401, "Invalid credentials");
        }
    }

    private boolean userExists(String email) throws IOException {
        File file = new File(CSV_FILE);
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            reader.readLine(); // Skip header
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");
                if (values[1].equals(email)) {
                    return true;
                }
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private JSONObject validateUserAndGetDetails(String email, String password) throws IOException {
        File file = new File(CSV_FILE);
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            reader.readLine(); // Skip header
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");
                if (values[1].equals(email) && values[2].equals(password)) {
                    JSONObject userData = new JSONObject();
                    userData.put("name", values[0]);
                    userData.put("email", values[1]);
                    userData.put("role", values.length > 5 ? values[5] : "user");
                    return userData;
                }
            }
        }
        return null;
    }

    private void sendJsonResponse(HttpExchange exchange, int statusCode, JSONObject jsonResponse) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        byte[] responseBytes = jsonResponse.toString().getBytes(StandardCharsets.UTF_8);
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