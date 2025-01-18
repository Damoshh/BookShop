package com.book.handler;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class UserHandler implements HttpHandler {
    private static final String CSV_FILE = "users.csv";
    
    public UserHandler() {
        try {
            File file = new File(CSV_FILE);
            if (!file.exists()) {
                try (FileWriter fw = new FileWriter(file)) {
                    fw.write("userId,name,email,password,phone,address,role\n");
                }
                System.out.println("Created new users.csv file at: " + file.getAbsolutePath());
            }
        } catch (IOException e) {
            System.err.println("Error initializing users.csv: " + e.getMessage());
        }
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // CORS is handled in Main.java
        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();
        System.out.println("Handling request: " + method + " " + path); // Debug log

        try {
            if (path.endsWith("/profile")) {
                if (method.equals("GET")) {
                    handleGetProfile(exchange);
                } else if (method.equals("PUT")) {
                    handleUpdateProfile(exchange);
                } else {
                    sendResponse(exchange, 405, "Method not allowed");
                }
            } else if (method.equals("POST")) {
                handlePost(exchange);
            } else {
                sendResponse(exchange, 405, "Method not allowed");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, "Server error: " + e.getMessage());
        }
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        try {
            String requestBody = readRequestBody(exchange);
            System.out.println("Received request body: " + requestBody); // Debug log

            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(requestBody);
            String action = (String) json.get("action");
            System.out.println("Processing action: " + action); // Debug log

            if ("register".equals(action)) {
                handleRegister(exchange, json);
            } else if ("login".equals(action)) {
                handleLogin(exchange, json);
            } else {
                sendResponse(exchange, 400, "Invalid action");
            }
        } catch (ParseException e) {
            System.err.println("Error parsing JSON: " + e.getMessage());
            sendResponse(exchange, 400, "Invalid JSON format");
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, "Server error: " + e.getMessage());
        }
    }

    private void handleRegister(HttpExchange exchange, JSONObject json) throws IOException {
        String name = (String) json.get("name");
        String email = (String) json.get("email");
        String password = (String) json.get("password");

        if (name == null || email == null || password == null) {
            sendResponse(exchange, 400, "Missing required fields");
            return;
        }

        if (userExists(email)) {
            sendResponse(exchange, 400, "Email already exists");
            return;
        }

        String userId = generateUserId();
        File file = new File(CSV_FILE);
        try (FileWriter fw = new FileWriter(file, true)) {
            fw.write(String.format("%s,%s,%s,%s,,,user\n", 
                userId, name, email, password));

            JSONObject response = new JSONObject();
            response.put("userId", userId);
            response.put("name", name);
            response.put("email", email);
            response.put("token", generateToken());
            response.put("role", "user");

            sendJsonResponse(exchange, 201, response);
        }
    }

    private void handleLogin(HttpExchange exchange, JSONObject json) throws IOException {
        String email = (String) json.get("email");
        String password = (String) json.get("password");

        if (email == null || password == null) {
            sendResponse(exchange, 400, "Missing credentials");
            return;
        }

        JSONObject userData = validateUserAndGetDetails(email, password);
        if (userData != null) {
            sendJsonResponse(exchange, 200, userData);
        } else {
            sendResponse(exchange, 401, "Invalid credentials");
        }
    }

    private void handleGetProfile(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        if (query == null || !query.startsWith("email=")) {
            sendResponse(exchange, 400, "Email parameter is required");
            return;
        }

        String email = query.split("=")[1];
        File file = new File(CSV_FILE);
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            reader.readLine(); // Skip header
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");
                if (values[2].equals(email)) {
                    JSONObject userData = new JSONObject();
                    userData.put("userId", values[0]);
                    userData.put("name", values[1]);
                    userData.put("email", values[2]);
                    userData.put("phone", values.length > 4 ? values[4] : "");
                    userData.put("address", values.length > 5 ? values[5] : "");
                    
                    sendJsonResponse(exchange, 200, userData);
                    return;
                }
            }
            sendResponse(exchange, 404, "User not found");
        }
    }

    private void handleUpdateProfile(HttpExchange exchange) throws IOException {
        String requestBody = readRequestBody(exchange);
        try {
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(requestBody);
            
            String email = (String) json.get("email");
            if (email == null) {
                sendResponse(exchange, 400, "Email is required");
                return;
            }

            List<String> lines = new ArrayList<>();
            boolean userFound = false;
            File file = new File(CSV_FILE);

            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                lines.add(reader.readLine()); // Add header
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] values = line.split(",");
                    if (values[2].equals(email)) {
                        lines.add(String.format("%s,%s,%s,%s,%s,%s,%s",
                            values[0],
                            json.get("name"),
                            email,
                            values[3],
                            json.get("phone"),
                            json.get("address"),
                            values.length > 6 ? values[6] : "user"
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

    private String readRequestBody(HttpExchange exchange) throws IOException {
        try (InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
             BufferedReader br = new BufferedReader(isr)) {
            StringBuilder body = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                body.append(line);
            }
            return body.toString();
        }
    }

    private boolean userExists(String email) throws IOException {
        File file = new File(CSV_FILE);
        if (!file.exists()) return false;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            reader.readLine(); // Skip header
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");
                if (values.length > 2 && values[2].equals(email)) {
                    return true;
                }
            }
        }
        return false;
    }

    private JSONObject validateUserAndGetDetails(String email, String password) throws IOException {
        File file = new File(CSV_FILE);
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            reader.readLine(); // Skip header
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");
                if (values.length > 3 && values[2].equals(email) && values[3].equals(password)) {
                    JSONObject userData = new JSONObject();
                    userData.put("userId", values[0]);
                    userData.put("name", values[1]);
                    userData.put("email", values[2]);
                    userData.put("role", values.length > 6 ? values[6] : "user");
                    userData.put("token", generateToken());
                    return userData;
                }
            }
        }
        return null;
    }

    private String generateUserId() {
        return "user_" + java.util.UUID.randomUUID().toString().substring(0, 8);
    }

    private String generateToken() {
        return java.util.UUID.randomUUID().toString();
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