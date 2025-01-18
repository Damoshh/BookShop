package com.book.handler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class AdminHandler implements HttpHandler {
    private static final String ADMIN_CSV = "admins.csv";
    private Map<String, String> activeSessionTokens = new HashMap<>();

    private boolean validateEmail(String email) {
        return email != null && email.toLowerCase().endsWith("@readify.com");
    }

    private boolean validateAdmin(String email, String password) {
        if (!validateEmail(email)) {
            System.out.println("Email validation failed for: " + email);
            return false;
        }
    
        File file = new File(ADMIN_CSV);
        System.out.println("\n=== Admin Login Debug Info ===");
        System.out.println("1. CSV File Path: " + file.getAbsolutePath());
        System.out.println("2. File exists: " + file.exists());
        System.out.println("3. Attempting login with:");
        System.out.println("   - Email: " + email);
        System.out.println("   - Password: " + password);
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            boolean firstLine = true;
            int lineNumber = 0;
            
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                System.out.println("\n4. Reading line " + lineNumber + ": " + line);
                
                if (firstLine) {
                    System.out.println("   - This is the header line, skipping");
                    firstLine = false;
                    continue;
                }
                
                String[] values = line.split(",");
                System.out.println("5. Split line into " + values.length + " values:");
                for (int i = 0; i < values.length; i++) {
                    System.out.println("   - Column " + i + ": [" + values[i].trim() + "]");
                }
                
                if (values.length >= 3) {
                    String csvEmail = values[1].trim();
                    String csvPassword = values[2].trim();
                    
                    System.out.println("\n6. Comparing credentials:");
                    System.out.println("   - CSV Email: [" + csvEmail + "] vs Input Email: [" + email + "]");
                    System.out.println("   - CSV Password: [" + csvPassword + "] vs Input Password: [" + password + "]");
                    
                    if (csvEmail.equals(email) && csvPassword.equals(password)) {
                        System.out.println("7. ✅ Credentials match!");
                        return true;
                    } else {
                        System.out.println("7. ❌ Credentials don't match");
                    }
                } else {
                    System.out.println("\n⚠️ Warning: Line has fewer than 3 columns");
                }
            }
        } catch (IOException e) {
            System.err.println("\n🛑 Error reading admin CSV:");
            System.err.println("- Message: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("\n8. ❌ No matching credentials found");
        return false;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Existing CORS headers are fine from what we see in Main.java
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type,Authorization");

        if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        if ("POST".equals(exchange.getRequestMethod())) {
            handleAdminLogin(exchange);
        } else {
            sendResponse(exchange, 405, "Method not allowed");
        }
    }

    private String generateSessionToken() {
        return java.util.UUID.randomUUID().toString();
    }

    @SuppressWarnings("unchecked")
    private void handleAdminLogin(HttpExchange exchange) throws IOException {
        try {
            InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder requestBody = new StringBuilder();
            String line;
            
            while ((line = br.readLine()) != null) {
                requestBody.append(line);
            }
    
            System.out.println("Received request body: " + requestBody.toString());
    
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(requestBody.toString());
            
            String email = (String) json.get("email");
            String password = (String) json.get("password");
    
            System.out.println("Attempting admin login with email: " + email);
    
            if (validateAdmin(email, password)) {
                System.out.println("Admin login successful");
                String sessionToken = generateSessionToken();
                activeSessionTokens.put(email, sessionToken);
                
                JSONObject response = new JSONObject();
                response.put("message", "Admin login successful");
                response.put("sessionToken", sessionToken);
                response.put("role", "admin");
                response.put("email", email);
                sendJsonResponse(exchange, 200, response);
            } else {
                System.out.println("Admin login failed - invalid credentials");
                sendResponse(exchange, 401, "Invalid admin credentials");
            }
        } catch (IOException | ParseException e) {
            System.out.println("Error in admin login: " + e.getMessage());
            sendResponse(exchange, 400, "Invalid request");
        }
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

    public boolean validateSessionToken(String email, String token) {
        String storedToken = activeSessionTokens.get(email);
        return storedToken != null && storedToken.equals(token);
    }
}