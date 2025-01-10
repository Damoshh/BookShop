package com.book.handler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class AdminHandler implements HttpHandler {
    private static final String ADMIN_CSV = "backend/admins.csv";
    private Map<String, String> activeSessionTokens = new HashMap<>();

    public AdminHandler() {
        try {
            File file = new File(ADMIN_CSV);
            file.getParentFile().mkdirs();
            
            if (!file.exists()) {
                FileWriter fw = new FileWriter(file);
                fw.write("email,password\n");
                fw.write("admin@readify.com,admin123\n");
                fw.close();
                System.out.println("Created new admins.csv file at: " + file.getAbsolutePath());
            }
        } catch (IOException e) {
            System.err.println("Error initializing admins.csv: " + e.getMessage());
            e.printStackTrace();
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

        if ("POST".equals(exchange.getRequestMethod())) {
            handleAdminLogin(exchange);
        } else {
            sendResponse(exchange, 405, "Method not allowed");
        }
    }

    private String generateSessionToken() {
        return java.util.UUID.randomUUID().toString();
    }

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
        } catch (Exception e) {
            System.out.println("Error in admin login: " + e.getMessage());
            e.printStackTrace();
            sendResponse(exchange, 400, "Invalid request");
        }
    }

    private boolean validateAdmin(String email, String password) {
        File file = new File(ADMIN_CSV);
        System.out.println("Checking admin credentials in: " + file.getAbsolutePath());
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            boolean firstLine = true;
            
            while ((line = reader.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue;
                }
                
                String[] values = line.split(",");
                if (values.length >= 2 && 
                    values[0].trim().equals(email) && 
                    values[1].trim().equals(password)) {
                    return true;
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading admin CSV: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
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

    public boolean validateSessionToken(String email, String token) {
        String storedToken = activeSessionTokens.get(email);
        return storedToken != null && storedToken.equals(token);
    }
}