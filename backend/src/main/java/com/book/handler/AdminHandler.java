package com.book.handler;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class AdminHandler implements HttpHandler {
    private static final String ADMIN_CSV = "admins.csv";
    private Map<String, String> activeSessionTokens = new HashMap<>();

    private void setCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "http://localhost:5173");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization, x-user-email");
        exchange.getResponseHeaders().set("Access-Control-Allow-Credentials", "true");
        exchange.getResponseHeaders().set("Access-Control-Max-Age", "3600");
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Set CORS headers first
        setCorsHeaders(exchange);
        
        // Handle preflight
        if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        String path = exchange.getRequestURI().getPath();
        System.out.println("Handling request for path: " + path);
        
        try {
            if (path.endsWith("/login")) {
                if ("POST".equals(exchange.getRequestMethod())) {
                    handleAdminLogin(exchange);
                } else {
                    sendResponse(exchange, 405, "Method not allowed");
                }
            } else if (path.endsWith("/profile")) {
                handleAdminProfile(exchange);
            } else {
                sendResponse(exchange, 404, "Not found");
            }
        } catch (Exception e) {
            System.err.println("Error handling request: " + e.getMessage());
            e.printStackTrace();
            sendResponse(exchange, 500, "Internal server error");
        }
    }

    private String generateSessionToken() {
        return java.util.UUID.randomUUID().toString();
    }

    @SuppressWarnings("unchecked")
    private void handleAdminLogin(HttpExchange exchange) throws IOException {
        System.out.println("Starting admin login handler...");
        
        try {
            // Read the request body
            InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder requestBody = new StringBuilder();
            String line;
            
            while ((line = br.readLine()) != null) {
                requestBody.append(line);
            }

            System.out.println("Received request body: " + requestBody);

            // Parse JSON request
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(requestBody.toString());
            
            String email = (String) json.get("email");
            String password = (String) json.get("password");

            System.out.println("Attempting admin login with email: " + email);

            // Validate admin credentials
            if (validateAdmin(email, password)) {
                System.out.println("Admin login successful");
                String token = generateSessionToken();
                activeSessionTokens.put(email, token);
                
                // Prepare response
                JSONObject response = new JSONObject();
                response.put("message", "Admin login successful");
                response.put("token", token);
                response.put("role", "admin");
                response.put("email", email);
                
                System.out.println("About to send response with token: " + token);
                System.out.println("Response headers: " + exchange.getResponseHeaders());
                
                // Send success response
                sendJsonResponse(exchange, 200, response);
                System.out.println("Response sent successfully");
            } else {
                System.out.println("Admin login failed - invalid credentials");
                sendResponse(exchange, 401, "Invalid admin credentials");
            }
        } catch (ParseException e) {
            System.err.println("Error parsing request JSON: " + e.getMessage());
            sendResponse(exchange, 400, "Invalid request format");
        } catch (Exception e) {
            System.err.println("Unexpected error in admin login: " + e.getMessage());
            e.printStackTrace();
            sendResponse(exchange, 500, "Internal server error");
        }
    }

    private void sendJsonResponse(HttpExchange exchange, int statusCode, JSONObject jsonResponse) throws IOException {
        // Set content type header
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        
        // Convert response to bytes
        byte[] responseBytes = jsonResponse.toString().getBytes(StandardCharsets.UTF_8);
        
        // Log response for debugging
        System.out.println("Sending JSON response: " + jsonResponse.toString());
        System.out.println("Response status code: " + statusCode);
        
        // Send headers
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        
        // Write response body
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
            os.flush();
        }
    }

    @SuppressWarnings("unchecked")
    private void sendResponse(HttpExchange exchange, int statusCode, String message) throws IOException {
        JSONObject response = new JSONObject();
        response.put("message", message);
        sendJsonResponse(exchange, statusCode, response);
    }

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
            
            while ((line = reader.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue;
                }
                
                String[] values = line.split(",");
                if (values.length >= 3) {
                    String csvEmail = values[1].trim();
                    String csvPassword = values[2].trim();
                    
                    if (csvEmail.equals(email) && csvPassword.equals(password)) {
                        System.out.println("Found matching admin credentials");
                        return true;
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading admin CSV: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("No matching admin credentials found");
        return false;
    }

    public boolean validateSessionToken(String email, String token) {
        String storedToken = activeSessionTokens.get(email);
        return storedToken != null && storedToken.equals(token);
    }

    private void handleAdminProfile(HttpExchange exchange) throws IOException {
        if (!"GET".equals(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, "Method not allowed");
            return;
        }
    
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        String userEmail = exchange.getRequestHeaders().getFirst("x-user-email");
        
        System.out.println("Admin profile request received");
        System.out.println("Auth header: " + authHeader);
        System.out.println("User email: " + userEmail);
        
        if (authHeader == null || !authHeader.startsWith("Bearer ") || userEmail == null) {
            System.out.println("Missing or invalid authorization headers");
            sendResponse(exchange, 401, "Unauthorized");
            return;
        }
    
        String token = authHeader.substring(7);
        System.out.println("Profile request - Token validation: " + token + " for email: " + userEmail);
        
        if (!validateSessionToken(userEmail, token)) {
            System.out.println("Invalid session token");
            sendResponse(exchange, 401, "Invalid session");
            return;
        }
    
        try (BufferedReader reader = new BufferedReader(new FileReader(ADMIN_CSV))) {
            String line;
            boolean firstLine = true;
            
            while ((line = reader.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue;
                }
                
                String[] values = line.split(",");
                if (values.length >= 6 && values[1].trim().equals(userEmail)) {
                    JSONObject profile = new JSONObject();
                    profile.put("fullName", values[0].trim());
                    profile.put("email", values[1].trim());
                    profile.put("phoneNumber", values[3].trim());
                    profile.put("address", values[4].trim());
                    profile.put("role", values[5].trim());
                    
                    System.out.println("Found and sending admin profile");
                    sendJsonResponse(exchange, 200, profile);
                    return;
                }
            }
            
            System.out.println("Admin profile not found");
            sendResponse(exchange, 404, "Admin profile not found");
        } catch (IOException e) {
            System.err.println("Error reading admin CSV: " + e.getMessage());
            sendResponse(exchange, 500, "Internal server error");
        }
    }
}