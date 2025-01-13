package com.book.handler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

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
            // Create directories if they don't exist
            file.getParentFile().mkdirs();
            
            if (!file.exists()) {
                try (FileWriter fw = new FileWriter(file)) {
                    fw.write("name,email,password\n");
                }
                System.out.println("Created new users.csv file at: " + file.getAbsolutePath());
            }
        } catch (IOException e) {
            System.err.println("Error initializing users.csv: " + e.getMessage());
        }
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Enable CORS
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

        if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        String method = exchange.getRequestMethod();

        try {
            switch (method) {
                case "POST":
                    handlePost(exchange);
                    break;
                default:
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

    private void handleRegister(HttpExchange exchange, JSONObject json) throws IOException {
        String name = (String) json.get("name");
        String email = (String) json.get("email");
        String password = (String) json.get("password");

        if (userExists(email)) {
            sendResponse(exchange, 400, "Email already exists");
            return;
        }

        File file = new File(CSV_FILE);
        try (FileWriter fw = new FileWriter(file, true)) {
            fw.write(String.format("%s,%s,%s\n", name, email, password));
            System.out.println("Registered new user: " + email);
            sendResponse(exchange, 201, "User registered successfully");
        } catch (IOException e) {
            System.err.println("Error writing to users.csv: " + e.getMessage());
            throw e;
        }
    }

    private void handleLogin(HttpExchange exchange, JSONObject json) throws IOException {
        String email = (String) json.get("email");
        String password = (String) json.get("password");

        if (validateUser(email, password)) {
            sendResponse(exchange, 200, "Login successful");
        } else {
            sendResponse(exchange, 401, "Invalid credentials");
        }
    }

    private boolean userExists(String email) throws IOException {
        File file = new File(CSV_FILE);
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");
                if (values.length > 1 && values[1].equals(email)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean validateUser(String email, String password) throws IOException {
        File file = new File(CSV_FILE);
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");
                if (values.length > 2 && values[1].equals(email) && values[2].equals(password)) {
                    return true;
                }
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        JSONObject jsonResponse = new JSONObject();
        jsonResponse.put("message", response);
        
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        byte[] responseBytes = jsonResponse.toString().getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }
}