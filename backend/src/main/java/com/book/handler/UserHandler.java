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
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class UserHandler implements HttpHandler {
    private static final String CSV_FILE = "users.csv";
    
    public UserHandler() {
        try {
            File file = new File(CSV_FILE);
            if (!file.exists()) {
                // Create new file with new format
                try (FileWriter fw = new FileWriter(file)) {
                    fw.write("userId,name,email,password,phone,street,city,state,zipcode,country,role\n");
                }
                System.out.println("Created new users.csv file at: " + file.getAbsolutePath());
            } else {
                // Check existing file format
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    String header = reader.readLine();
                    if (header != null && header.contains("address") && !header.contains("street")) {
                        // Old format detected, read all data
                        List<String> lines = new ArrayList<>();
                        lines.add("userId,name,email,password,phone,street,city,state,zipcode,country,role");
                        
                        String line;
                        while ((line = reader.readLine()) != null) {
                            String[] values = line.split(",");
                            if (values.length >= 7) {
                                String address = values[5].trim();
                                String street, city, state, zipcode, country;
                                
                                // Parse existing address
                                if (address.contains("Selayang")) {
                                    street = "Jalan Selayang";
                                    city = "Selayang";
                                    state = "Selangor";
                                    zipcode = "68100";
                                } else if (address.contains("Rawang")) {
                                    street = "Jalan Rawang";
                                    city = "Rawang";
                                    state = "Selangor";
                                    zipcode = "48000";
                                } else {
                                    street = address;
                                    city = "";
                                    state = "";
                                    zipcode = "";
                                }
                                country = "Malaysia";
                                
                                // Format in new structure
                                lines.add(String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s",
                                    values[0], // userId
                                    values[1], // name
                                    values[2], // email
                                    values[3], // password
                                    values[4], // phone
                                    street,
                                    city,
                                    state,
                                    zipcode,
                                    country,
                                    values[6]  // role
                                ));
                            }
                        }
                        
                        // Write back in new format
                        try (FileWriter writer = new FileWriter(file)) {
                            for (String updatedLine : lines) {
                                writer.write(updatedLine + "\n");
                            }
                        }
                        System.out.println("Migrated users.csv to new format");
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error initializing users.csv: " + e.getMessage());
        }
    }

    @Override
    @SuppressWarnings({"ConvertToStringSwitch", "UseSpecificCatch", "CallToPrintStackTrace"})
    public void handle(HttpExchange exchange) throws IOException {
        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

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

    @SuppressWarnings("unchecked")
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
                    userData.put("phone", values[4]);
                    userData.put("street", values.length > 5 ? values[5] : "");
                    userData.put("city", values.length > 6 ? values[6] : "");
                    userData.put("state", values.length > 7 ? values[7] : "");
                    userData.put("zipcode", values.length > 8 ? values[8] : "");
                    userData.put("country", values.length > 9 ? values[9] : "");
                    
                    sendJsonResponse(exchange, 200, userData);
                    return;
                }
            }
            sendResponse(exchange, 404, "User not found");
        }
    }

    @SuppressWarnings({"resource", "UseSpecificCatch", "CallToPrintStackTrace"})
    private void handleUpdateProfile(HttpExchange exchange) throws IOException {
        String requestBody = new BufferedReader(new InputStreamReader(exchange.getRequestBody()))
            .lines().reduce("", String::concat);
            
        try {
            JSONObject json = (JSONObject) new JSONParser().parse(requestBody);
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
                        // Update user data with new format
                        lines.add(String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s",
                            values[0], // userId
                            json.get("name"),
                            email,
                            values[3], // password
                            json.get("phone"),
                            json.get("street"),
                            json.get("city"),
                            json.get("state"),
                            json.get("zipcode"),
                            json.get("country"),
                            values[values.length > 10 ? 10 : values.length - 1] // role
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
            
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 400, "Invalid request data");
        }
    }

    @SuppressWarnings("unchecked")
private void handleRegister(HttpExchange exchange, JSONObject json) throws IOException {
    // Extract all required fields from JSON
    String name = (String) json.get("name");
    String email = (String) json.get("email");
    String password = (String) json.get("password");
    String phone = (String) json.get("phone");
    String street = (String) json.get("street");
    String city = (String) json.get("city");
    String state = (String) json.get("state");
    String zipcode = (String) json.get("zipcode");
    String country = (String) json.get("country");

    // Validate required fields
    if (name == null || email == null || password == null) {
        sendResponse(exchange, 400, "Missing required fields");
        return;
    }

    // Check if user already exists
    if (userExists(email)) {
        sendResponse(exchange, 400, "Email already exists");
        return;
    }

    String userId = generateUserId();
    List<String> existingUsers = new ArrayList<>();
    
    // Read existing users
    try (BufferedReader reader = new BufferedReader(new FileReader(CSV_FILE))) {
        String line;
        while ((line = reader.readLine()) != null) {
            existingUsers.add(line);
        }
    }
    
    // Write all users including the new one
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(CSV_FILE))) {
        // Write header if file is empty
        if (existingUsers.isEmpty()) {
            writer.write("userId,name,email,password,phone,street,city,state,zipcode,country,role\n");
        } else {
            // Write existing users
            for (String line : existingUsers) {
                writer.write(line);
                writer.newLine();
            }
        }
        
        // Write new user
        writer.write(String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s",
            userId,
            name,
            email,
            password,
            phone != null ? phone : "",
            street != null ? street : "",
            city != null ? city : "",
            state != null ? state : "",
            zipcode != null ? zipcode : "",
            country != null ? country : "Malaysia",
            "user"
        ));
        writer.newLine();
    }

    JSONObject response = new JSONObject();
    response.put("userId", userId);
    response.put("name", name);
    response.put("email", email);
    response.put("token", generateToken());
    response.put("role", "user");

    sendJsonResponse(exchange, 201, response);
}

    // Existing helper methods remain the same
    private String generateUserId() {
        return "user_" + java.util.UUID.randomUUID().toString().substring(0, 8);
    }

    private String generateToken() {
        return java.util.UUID.randomUUID().toString();
    }

    private boolean userExists(String email) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(CSV_FILE))) {
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

    @SuppressWarnings({"resource", "ConvertToStringSwitch", "UseSpecificCatch", "CallToPrintStackTrace"})
    private void handlePost(HttpExchange exchange) throws IOException {
        try {
            String requestBody = new BufferedReader(new InputStreamReader(exchange.getRequestBody()))
                .lines().reduce("", String::concat);

            JSONObject json = (JSONObject) new JSONParser().parse(requestBody);
            String action = (String) json.get("action");

            if ("register".equals(action)) {
                handleRegister(exchange, json);
            } else if ("login".equals(action)) {
                handleLogin(exchange, json);
            } else {
                sendResponse(exchange, 400, "Invalid action");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, "Server error: " + e.getMessage());
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

    @SuppressWarnings("unchecked")
    private JSONObject validateUserAndGetDetails(String email, String password) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(CSV_FILE))) {
            String line;
            reader.readLine(); // Skip header
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");
                if (values.length > 3 && values[2].equals(email) && values[3].equals(password)) {
                    JSONObject userData = new JSONObject();
                    userData.put("userId", values[0]);
                    userData.put("name", values[1]);
                    userData.put("email", values[2]);
                    userData.put("role", values[values.length - 1]);
                    userData.put("token", generateToken());
                    return userData;
                }
            }
        }
        return null;
    }
}