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
import java.util.UUID;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class BookHandler implements HttpHandler {
    private static final String BOOKS_CSV = "backend/books.csv";

    public BookHandler() {
        try {
            File file = new File(BOOKS_CSV);
            file.getParentFile().mkdirs();
            
            if (!file.exists()) {
                try (FileWriter fw = new FileWriter(file)) {
                    fw.write("_id,title,author,price,category,description,coverImg\n");
                    
                }
                System.out.println("Created new books.csv file at: " + file.getAbsolutePath());
            }
        } catch (IOException e) {
            System.err.println("Error initializing books.csv: " + e.getMessage());
        }
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type,Authorization");

        if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        try {
            switch (method) {
                case "GET":
                    handleGetBooks(exchange);
                    break;
                case "POST":
                    if (isAdminRequest(exchange)) {
                        handleAddBook(exchange);
                    } else {
                        sendResponse(exchange, 401, "Unauthorized");
                    }
                    break;
                case "DELETE":
                    if (isAdminRequest(exchange)) {
                        handleDeleteBook(exchange, path);
                    } else {
                        sendResponse(exchange, 401, "Unauthorized");
                    }
                    break;
                default:
                    sendResponse(exchange, 405, "Method not allowed");
            }
        } catch (IOException e) {
            sendResponse(exchange, 500, "Internal server error");
        }
    }

    private boolean isAdminRequest(HttpExchange exchange) {
        
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        
        return authHeader != null && authHeader.startsWith("Bearer ");
    }

    private void handleGetBooks(HttpExchange exchange) throws IOException {
        JSONArray books = readBooksFromCsv();
        sendJsonResponse(exchange, 200, books);
    }

    @SuppressWarnings("unchecked")
    private void handleAddBook(HttpExchange exchange) throws IOException {
        try {
            InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder requestBody = new StringBuilder();
            String line;
            
            while ((line = br.readLine()) != null) {
                requestBody.append(line);
            }

            JSONParser parser = new JSONParser();
            JSONObject bookData = (JSONObject) parser.parse(requestBody.toString());
            
            
            String newId = UUID.randomUUID().toString();
            
            try ( 
                    FileWriter fw = new FileWriter(BOOKS_CSV, true)) {
                fw.write(String.format("%s,%s,%s,%.2f,%s,%s,%s\n",
                        newId,
                        bookData.get("title"),
                        bookData.get("author"),
                        Double.parseDouble(bookData.get("price").toString()),
                        bookData.get("category"),
                        bookData.get("description").toString().replace(",", ";"),
                        bookData.get("coverImg")
                ));
            }

            JSONObject response = new JSONObject();
            response.put("message", "Book added successfully");
            response.put("id", newId);
            sendJsonResponse(exchange, 200, response);

        } catch (IOException | NumberFormatException | ParseException e) {
            sendResponse(exchange, 400, "Invalid book data");
        }
    }

    private void handleDeleteBook(HttpExchange exchange, String path) throws IOException {
        String bookId = path.substring(path.lastIndexOf('/') + 1);
        List<String> remainingBooks = new ArrayList<>();
        boolean bookFound = false;

        
        try (BufferedReader reader = new BufferedReader(new FileReader(BOOKS_CSV))) {
            String line;
            boolean isFirstLine = true;
            
            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    remainingBooks.add(line);
                    isFirstLine = false;
                    continue;
                }
                
                String[] values = line.split(",");
                if (!values[0].equals(bookId)) {
                    remainingBooks.add(line);
                } else {
                    bookFound = true;
                }
            }
        }

        if (!bookFound) {
            sendResponse(exchange, 404, "Book not found");
            return;
        }

        
        try (FileWriter writer = new FileWriter(BOOKS_CSV)) {
            for (String line : remainingBooks) {
                writer.write(line + "\n");
            }
        }

        sendResponse(exchange, 200, "Book deleted successfully");
    }

    @SuppressWarnings("unchecked")
    private JSONArray readBooksFromCsv() {
        JSONArray books = new JSONArray();
        try (BufferedReader reader = new BufferedReader(new FileReader(BOOKS_CSV))) {
            String line;
            boolean firstLine = true;
            
            while ((line = reader.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue;
                }
                
                String[] values = line.split(",");
                if (values.length >= 7) {
                    JSONObject book = new JSONObject();
                    book.put("_id", values[0]);
                    book.put("title", values[1]);
                    book.put("author", values[2]);
                    book.put("price", Double.valueOf(values[3]));
                    book.put("category", values[4]);
                    book.put("description", values[5].replace(";", ","));
                    book.put("coverImg", values[6]);
                    books.add(book);
                }
            }
        } catch (IOException e) {
        }
        return books;
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