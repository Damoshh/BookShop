package com.book.handler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class BookHandler implements HttpHandler {
    private static final String BOOKS_CSV = "backend/books.csv";

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Set CORS headers
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "*");
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");

        // Handle preflight
        if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        try {
            if ("GET".equals(exchange.getRequestMethod())) {
                handleGetBooks(exchange);
            } else {
                sendErrorResponse(exchange, 405, "Method not allowed");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendErrorResponse(exchange, 500, "Internal server error: " + e.getMessage());
        } finally {
            exchange.close();
        }
    }

    private void handleGetBooks(HttpExchange exchange) throws IOException {
        JSONArray books = readBooksFromCsv();
        sendJsonResponse(exchange, 200, books);
    }

    @SuppressWarnings("unchecked")
    private JSONArray readBooksFromCsv() {
        JSONArray books = new JSONArray();
        File file = new File(BOOKS_CSV);
        
        // Create default books if file doesn't exist
        if (!file.exists()) {
            createDefaultBooks();
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            boolean isHeader = true;
            
            while ((line = reader.readLine()) != null) {
                if (isHeader) {
                    isHeader = false;
                    continue;
                }
                
                String[] values = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)"); // Handle commas in quotes
                if (values.length >= 7) {
                    JSONObject book = new JSONObject();
                    book.put("_id", values[0].trim());
                    book.put("title", values[1].trim());
                    book.put("name", values[1].trim());
                    book.put("author", values[2].trim());
                    book.put("price", Double.parseDouble(values[3].trim()));
                    book.put("category", values[4].trim());
                    book.put("description", values[5].trim().replace(";", ","));
                    book.put("coverImg", values[6].trim());
                    book.put("image", values[6].trim());
                    books.add(book);
                }
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }

        // Add sample book if no books were found
        if (books.isEmpty()) {
            books.add(createSampleBook());
        }

        return books;
    }

    private void createDefaultBooks() {
        try {
            File file = new File(BOOKS_CSV);
            file.getParentFile().mkdirs();
            
            try (FileWriter writer = new FileWriter(file)) {
                // Write header
                writer.write("_id,title,author,price,category,description,coverImg\n");
                
                // Write sample books
                writer.write("1,\"The Great Adventure\",\"John Smith\",29.99,Fiction,\"An exciting journey through unknown lands\",/books/adventure.jpg\n");
                writer.write("2,\"Mystery Manor\",\"Jane Doe\",24.99,Mystery,\"A thrilling mystery in an old mansion\",/books/mystery.jpg\n");
                writer.write("3,\"Science Today\",\"Dr. Brown\",39.99,Science,\"Modern scientific discoveries explained\",/books/science.jpg\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private JSONObject createSampleBook() {
        JSONObject book = new JSONObject();
        book.put("_id", "1");
        book.put("title", "Sample Book");
        book.put("name", "Sample Book");
        book.put("author", "John Doe");
        book.put("price", 29.99);
        book.put("category", "Fiction");
        book.put("description", "A fascinating sample book for your reading pleasure");
        book.put("coverImg", "/books/sample.jpg");
        book.put("image", "/books/sample.jpg");
        return book;
    }

    private void sendJsonResponse(HttpExchange exchange, int statusCode, Object response) throws IOException {
        byte[] responseBytes = response.toString().getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
            os.flush();
        }
    }

    @SuppressWarnings("unchecked")
    private void sendErrorResponse(HttpExchange exchange, int statusCode, String message) throws IOException {
        JSONObject error = new JSONObject();
        error.put("error", message);
        sendJsonResponse(exchange, statusCode, error);
    }
}